/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.domain;

import jade.util.leap.*;

import jade.domain.FIPAAgentManagement.*;
import jade.core.Agent;
import jade.core.AID;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.lang.Codec.*;
import jade.content.onto.Ontology;
import jade.content.onto.BasicOntology;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.content.abs.*;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Done;

import java.util.Date;


/**
 * This class provides a set of static methods to communicate with
 * a DF Service that complies with FIPA specifications.
 * It includes methods to register, deregister, modify and search with a DF. 
 * Each of this method has version with all the needed parameters, or with a 
 * subset of them where, those parameters that can be omitted have been 
 * defaulted to the default DF of the platform, the AID of the sending agent,
 * the default Search Constraints.
 * Notice that all these methods blocks every activity of the agent until 
 * the action (i.e. register/deregister/modify/search) has been successfully 
 * executed or a jade.domain.FIPAException exception has been thrown 
 * (e.g. because a FAILURE message has been received by the DF). 
 * In some cases, instead, it is more convenient to execute this task in a 
 * non-blocking way. The method getNonBlockingBehaviour() returns a 
 * non-blocking behaviour of type RequestFIPAServiceBehaviour that can be 
 * added to the queue of the agent behaviours, as usual, by using 
 * <code>Agent.addBehaviour()</code>. 
 * @author Fabio Bellifemine (CSELT S.p.A.)
 * @author Elisabetta Cortese (TiLab S.p.A.)
  @version $Date$ $Revision$ 
 * 
 **/
public class DFService extends FIPAServiceCommunicator {

  private static Codec c = new SLCodec();
  private static Ontology o = FIPAManagementOntology.getInstance();
  private static ContentManager cm = new ContentManager();
  static {
	  cm.registerLanguage(c, "FIPA-SL0");
	  cm.registerLanguage(c, "FIPA-SL"); // The subscription message uses full SL
		cm.registerOntology(o);
  }
  
  /**
   * check that the <code>DFAgentDescription</code> contains the mandatory
   * slots, i.e. the agent name and, for each servicedescription, the
   * service name and the service type
   * @throw a MissingParameter exception is it is not valid
   */
  static void checkIsValid(DFAgentDescription dfd) throws MissingParameter {
    // FIXME: use FIPAManagementOntology constants instead of Strings  
  	if (dfd.getName()==null) 
      throw new MissingParameter(FIPAManagementOntology.DFAGENTDESCRIPTION, "name");
    Iterator i = dfd.getAllServices();
    ServiceDescription sd;
    while (i.hasNext()) {
      sd = (ServiceDescription)i.next();
      if (sd.getName() == null)
	throw new MissingParameter(FIPAManagementOntology.SERVICEDESCRIPTION, "name");
      if (sd.getType() == null)
	throw new MissingParameter(FIPAManagementOntology.SERVICEDESCRIPTION, "type");
    }
  }

  
  /**
     Register a DFDescription with a <b>DF</b> agent. The lease duration request
     is not exact; the returned lease is allowed to have a shorter (but not longer)
     duration thatn what was requested setting the corresponding lease-time field
     in the <code>DFAgentDescription</code>.
     @param a is the Agent performing the registration (it is needed in order
     to send/receive messages
     @param dfName The AID of the <b>DF</b> agent to register with.
     @param dfd A <code>DFAgentDescriptor</code> object containing all
     data necessary to the registration. If the Agent name is empty, than
     it is set according to the <code>a</code> parameter.
     @return the effective lease time, in milliseconds, assigned to the
     <code>DFAgentDescription</code>. 0 if the request has not been satisfied.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition or when
     the method locally discovers that the DFDescription is not valid.
   */
  public static Date register(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, dfName);

    if (dfd.getName() == null)
      dfd.setName(a.getAID());
    checkIsValid(dfd);

    // Build a DF action object for the request
    Register r = new Register();
    r.setDescription(dfd);

    Action act = new Action();
    act.setActor(dfName);
    act.setAction(r);
    synchronized (cm) {
			try{    
				cm.fillContent(request, act);
			}
			catch(Exception e){
				throw new FIPAException("Error encoding REQUEST content. "+e);
			}
   
    }

    
//    synchronized(c) { //must be synchronized because this is a static method
//      request.setContent(encode(act,c,o));
//    }
    // Send message and collect reply
    ACLMessage reply = doFipaRequestClient(a,request);
    // get the effective lease time assigne the current request
    
    Date retLeaseTime = null;
    Done doneRegister = null;
    try{
        
        synchronized (cm) {
            doneRegister = (Done) cm.extractContent(reply);
        }
    }catch(Exception e) {
        throw new FIPAException("Error decoding REQUEST content. "+e);
    }
    if(doneRegister!= null) {
        Action replyAction = (Action) doneRegister.getAction();
        Register replyRegister = (Register) replyAction.getAction();
        //Register replyRegister = (Register) doneRegister.getAction();
        DFAgentDescription replyDFA = (DFAgentDescription)replyRegister.getDescription();
        retLeaseTime = replyDFA.getLeaseTime();
    }
        
    
    return retLeaseTime;
  }


  /**
   * registers a <code>DFAgentDescription</code> with the default DF
   * @see #register(Agent,AID,DFAgentDescription)
   **/
  public static Date register(Agent a, DFAgentDescription dfd) throws FIPAException {
    return register(a,a.getDefaultDF(),dfd);
  }

  /**
     Deregister a DFAgentDescription from a <b>DF</b> agent. 
     @param dfName The AID of the <b>DF</b> agent to deregister from.
     @param dfd A <code>DFAgentDescription</code> object containing all
     data necessary to the deregistration.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public static void deregister(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {

    ACLMessage request = createRequestMessage(a, dfName);

    if (dfd.getName() == null)
      dfd.setName(a.getAID());
    // Build a DF action object for the request
    Deregister d = new Deregister();
    d.setDescription(dfd);

    Action act = new Action();
    act.setActor(dfName);
    act.setAction(d);
    synchronized (cm) {
			try{    
				cm.fillContent(request, act);
			}
			catch(Exception e){
				throw new FIPAException("Error encoding REQUEST content. "+e);
			}
    }
		

//    synchronized(c) { //must be synchronized because this is a static method
//      request.setContent(encode(act,c,o));
//    }

    // Send message and collect reply
    doFipaRequestClient(a,request);
  }

  /**
   * The default DF of the platform is used.
@see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
  **/
  public static void deregister(Agent a, DFAgentDescription dfd) throws FIPAException {
    deregister(a,a.getDefaultDF(),dfd);
  }

  /**
   * A default Agent Description is used which contains only the AID
   * of this agent.
@see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
  **/
  public static void deregister(Agent a, AID dfName) throws FIPAException {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(a.getAID());
    deregister(a,dfName,dfd);
  }

  /**
   * The default DF of the platform is used.
   * A default Agent Description is used which contains only the AID
   * of this agent.
@see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
  **/
  public static void deregister(Agent a) throws FIPAException {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(a.getAID());
    deregister(a,dfd);
  }


  /**
     Modifies data contained within a <b>DF</b>
     agent. 
     @param a is the Agent performing the request of modification 
     @param dfName The AID of the <b>DF</b> agent holding the data
     to be changed.
     @param dfd A <code>DFAgentDescriptor</code> object containing all
     new data values; 
     @return the effective lease time, in milliseconds, assigned to the
     <code>DFAgentDescription</code>. 0 if the request has not been satisfied.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public static Date modify(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, dfName);

    if (dfd.getName() == null)
      dfd.setName(a.getAID());
    checkIsValid(dfd);
    // Build a DF action object for the request
    Modify m = new Modify();
    m.setDescription(dfd);

    Action act = new Action();
    act.setActor(dfName);
    act.setAction(m);
    synchronized (cm) {
			try{    
				cm.fillContent(request, act);
			}
			catch(Exception e){
				throw new FIPAException("Error encoding REQUEST content. "+e);
			}
    }
//    synchronized(c) { //must be synchronized because this is a static method
//      // Write the action in the :content slot of the request
//      request.setContent(encode(act,c,o));
//    }

    // Send message and collect reply
    ACLMessage reply = doFipaRequestClient(a,request);
    // get the effective lease time assigne the current request
    Date retLeaseTime = null;
    Done doneModify = null;
    try{
        synchronized(cm) {
            doneModify = (Done) cm.extractContent(reply);
        }
    }catch(Exception e) {
        throw new FIPAException("Error dencoding INFORM content. "+e);
    }
    if(doneModify!=null) {
        Action replyAction = (Action) doneModify.getAction();
        Modify replyModify = (Modify) replyAction.getAction();
        DFAgentDescription replyDFA = (DFAgentDescription)replyModify.getDescription();
        retLeaseTime = replyDFA.getLeaseTime();
    }
    return retLeaseTime;
    
  }

  /**
   * The default DF of the platform is used.
@see #modify(Agent a, AID dfName, DFAgentDescription dfd)
  **/
  public static Date modify(Agent a, DFAgentDescription dfd) throws FIPAException {
    return modify(a,a.getDefaultDF(),dfd);
  }

  /**
     Searches for data contained within a <b>DF</b> agent. 
     @param a is the Agent performing the request of search 
     @param dfName The AID of the <b>DF</b> agent to start search from.
     @param dfd A <code>DFAgentDescription</code> object containing
     data to search for; this parameter is used as a template to match
     data against.
     @param constraints of the search 
     @return An array of <code>DFAgentDescription</code> 
     containing all found
     items matching the given
     descriptor, subject to given search constraints for search depth
     and result size.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public static DFAgentDescription[] search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    ACLMessage request = createRequestMessage(a, dfName);

    // Build a DF action object for the request
    Search s = new Search();
    s.setDescription(dfd);
    s.setConstraints(constraints);

    Action act = new Action();
    act.setActor(dfName);
    act.setAction(s);

    synchronized (cm) {
			try{
				cm.fillContent(request, act);
    	}
			catch(Exception e){
				throw new FIPAException("Error encoding REQUEST content. "+e);
			}
    }
	
	  // Send message and collect reply
	  ACLMessage inform = doFipaRequestClient(a,request);

	  // Parse the content and returns the items found as an array
		Result r = null;	
    synchronized (cm) {
			try{
	    	r = (Result) cm.extractContent( inform );
    	}
			catch(Exception e){
				throw new FIPAException("Error decoding INFORM content. "+e);
			}
    }
		
    return toArray(r.getItems());
  }

  private static DFAgentDescription[] toArray(List l) throws FIPAException {
		try {
    	DFAgentDescription[] items = new DFAgentDescription[l.size()];
			for(int i = 0; i < l.size(); i++){
				items[i] = (DFAgentDescription)l.get(i);
			}
			return items;
		}
		catch (ClassCastException cce) {
			throw new FIPAException("Found items are not DFAgentDescriptions. "+cce);
		}
  }


  /**
   * The default DF is used.
@see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static DFAgentDescription[] search(Agent a, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    return search(a,a.getDefaultDF(),dfd,constraints);
  }

  /**
   * The default DF is used.
   * The default SearchConstraints are used. According to FIPA they are
   * defaulted to null value for all slots.
@see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static DFAgentDescription[] search(Agent a, DFAgentDescription dfd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return search(a,a.getDefaultDF(),dfd,constraints);
  }

  /**
   * The default SearchConstraints are used. According to FIPA they are
   * defaulted to null value for all slots.
@see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static DFAgentDescription[] search(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return search(a,dfName,dfd,constraints);
  }


  // SUBSCRIPTION related methods

  /** 
     Utility method that allows easily creating the message that has to 
     be sent to the DF to subscribe to receive notifications when a new 
     DF agent description matching the indicated template is registererd
     with the DF. This method can be fruitfully used in combination with 
     the <code>SubscriptionInitiator</code> protocol.
     @param a The agent that is subscribing to the DF
     @param dfName The AID of the <b>DF</b> agent to subscribe to.
     @param template A <code>DFAgentDescription</code> object that is used 
     as a template to identify DF description that will be notified
     @param constraints The constraints to limit the number of results to be
     notified.
     @return the subscription message.
     @see jade.proto.SubscriptionInitiator
   */
  public static ACLMessage getSubscriptionMessage(Agent a, AID dfName, DFAgentDescription template, SearchConstraints constraints) throws FIPAException {
    ACLMessage subscribe = new ACLMessage(ACLMessage.SUBSCRIBE);
    subscribe.setSender(a.getAID());
    subscribe.addReceiver(dfName);
    subscribe.setProtocol("fipa-subscribe");
    subscribe.setLanguage(c.getName());
    subscribe.setOntology(o.getName());
 
 		AbsVariable x = new AbsVariable("x", FIPAManagementVocabulary.DFAGENTDESCRIPTION);

    // Build a DF action object for the request
    Search s = new Search();
    s.setDescription(template);
    s.setConstraints(constraints);

		Action actSearch = new Action();
		actSearch.setActor(dfName);
		actSearch.setAction(s);
	
		AbsPredicate results = new AbsPredicate(BasicOntology.RESULT);
		results.set(BasicOntology.RESULT_VALUE, x);
		
		synchronized (cm) {
			try {
				results.set(BasicOntology.RESULT_ACTION, o.fromObject(actSearch));
	
				AbsIRE iota = new AbsIRE(SLVocabulary.IOTA);
				iota.setVariable(x);
				iota.setProposition(results);
	
				cm.fillContent(subscribe, iota);
			}
			catch (Exception e) {
				throw new FIPAException("Error creating subscription message. "+e);
			}
		}
    return subscribe;
  }
  
  /** 
  	 Searches the DF and remains blocked until a result is found or the
  	 specified timeout has expired.
     @param a The agent that is performing the search
     @param dfName The AID of the <b>DF</b> agent where to search into.
     @param template A <code>DFAgentDescription</code> object that is used 
     as a template to identify the DF descriptions to search for.  
     @param constraints The constraints to limit the number of results to be
     sent back.
     @param timeout The maximum amount of time that we want to remain blocked 
     waiting for results.
     @return The DF agent descriptions matching the specified template or 
     <code>null</code> if the timeout expires.
   */
  public static DFAgentDescription[] searchUntilFound(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints, long timeout) throws FIPAException {
    
    ACLMessage subscribe = getSubscriptionMessage(a, dfName, dfd, constraints);

		// set conv-id and reply-with field
		String replyWith ="rw"+a.getName()+(new Date()).getTime();
		String convId = "conv"+a.getName()+(new Date()).getTime();
    subscribe.setReplyWith( replyWith );
    subscribe.setConversationId( convId );

		a.send(subscribe);
		DFAgentDescription[] result = waitForResults(a, timeout, replyWith, convId);

		// SEND the CANCEL message
		ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
		cancel.addReceiver(dfName);
		cancel.setLanguage(c.getName());
		cancel.setOntology(o.getName());
		cancel.setConversationId(convId);
		Action act = new Action(dfName, OntoACLMessage.wrap(subscribe));
		synchronized (cm) {
			try {
				cm.fillContent(cancel, act);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
  	}
		a.send(cancel);
		
		return result;
  }
  
  private static DFAgentDescription[] waitForResults(Agent a, long timeout, String replyWith, String convId) throws FIPAException {
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId(convId),MessageTemplate.MatchInReplyTo(replyWith));
		long sendTime = System.currentTimeMillis();
		ACLMessage reply = a.blockingReceive(mt, timeout);
		
		if(reply != null) {
			if (reply.getPerformative() == ACLMessage.AGREE){
				// We received an AGREE --> Go back waiting for the INFORM unless the time is over.
				long agreeTime = System.currentTimeMillis();
				timeout -= (agreeTime - sendTime);
				if (timeout <= 0) {
					return null;
				}
				reply = a.blockingReceive(mt, timeout);
			}
			if(reply != null) {
				if (reply.getPerformative() == ACLMessage.INFORM){
					// We received the INFORM --> Parse it and return the result
					List items = null;
					try{
						synchronized (cm) {
							AbsPredicate absEquals = (AbsPredicate) cm.extractAbsContent( reply );
							items = (List) o.toObject( absEquals.getAbsTerm(SLVocabulary.EQUALS_RIGHT) );
						}
					}
					catch(Exception e){
						throw new FIPAException("Error decoding INFORM content. "+e);
					}
					
					return toArray(items);
				}
				else {
					// We received a REFUSE, NOT_UNDERSTOOD, FAILURE or OUT_OF_SEQUENCE --> ERROR
					throw new FIPAException(reply.getContent());
				}
			}
		}
		// The timeout has expired
		return null;
  }



  /**
In some cases it is more convenient to execute this tasks in a non-blocking way. 
This method returns a non-blocking behaviour that can be added to the queue of the agent behaviours, as usual, by using <code>Agent.addBehaviour()</code>.
<p>
 Several ways are available to get the result of this behaviour and the programmer can select one according to his preferred programming style:
<ul>
<li>
call getLastMsg() and getSearchResults() where both throw a NotYetReadyException if the task has not yet finished;
<li>create a SequentialBehaviour composed of two sub-behaviours:  the first subbehaviour is the returned RequestFIPAServiceBehaviour, while the second one is application-dependent and is executed only when the first is terminated;
<li>use directly the class RequestFIPAServiceBehaviour by extending it and overriding all the handleXXX methods that handle the states of the fipa-request interaction protocol.
</ul>
* @param a is the agent performing the task
* @param dfName is the AID of the DF that should perform the requested action
* @param actionName is the name of the action (one of the constants defined
* in FIPAAgentManagementOntology: REGISTER / DEREGISTER / MODIFY / SEARCH).
* @param dfd is the agent description
* @param constraints are the search constraints (can be null if this is
* not a search operation)
* @return the behaviour to be added to the agent
     @exception FIPAException A suitable exception can be thrown 
     to indicate some error condition 
     locally discovered (e.g.the agentdescription is not valid.)
@see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
     **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    return new RequestFIPAServiceBehaviour(a,dfName,actionName,dfd,constraints);
  }

  /**
   * The default DF is used.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
  }

  /**
   * The default DF is used.
   the default SearchContraints are used.
a default AgentDescription is used, where only the agent AID is set.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName) throws FIPAException {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(a.getAID());
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
  }


  /**
   the default SearchContraints are used.
a default AgentDescription is used, where only the agent AID is set.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName) throws FIPAException {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(a.getAID());
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,dfName,actionName,dfd,constraints);
  }


  /**
   * The defautl DF is used.
   the default SearchContraints are used.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, DFAgentDescription dfd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
  }

  /**
   *   the default SearchContraints are used.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,dfName,actionName,dfd,constraints);
  }

}

