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

import java.util.*;

import jade.domain.FIPAAgentManagement.*;
import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;
import jade.lang.Codec;
import jade.lang.sl.SL0Codec;
import jade.onto.Ontology;

/**
 * This class provides a set of static methods to communicate with
 * a AMS Service that complies with FIPA specifications.
 * Notice that JADE calls automatically the register and deregister methods 
 * with the default AMS respectively before calling <code>Agent.setup()</code> 
 * method and just 
 * after <code>Agent.takeDown()</code> method returns; so there is no need for a normal 
 * programmer to call them. 
 * However, under certain circumstances, a programmer might need to call its 
 * methods. To give some examples: when an agent wishes to register with the 
 * AMS of a remote agent platform, or when an agent wishes to modify its 
 * description by adding a private address to the set of its addresses, ...
 * <p>
 * It includes methods to register, deregister, modify and search with an AMS. 
 * Each of this method has version with all the needed parameters, or with a 
 * subset of them where, those parameters that can be omitted have been 
 * defaulted to the default AMS of the platform, the AID of the sending agent,
 *  the default Search Constraints.
 * Notice that all these methods blocks every activity of the agent until the
 * action (i.e. register/deregister/modify/search) has been successfully 
 * executed or a jade.domain.FIPAException exception has been thrown 
 * (e.g. because a FAILURE message has been received by the AMS). 
 * In some cases, instead, it is more convenient to execute this task in a 
 * non-blocking way. The method getNonBlockingBehaviour() returns a 
 * non-blocking behaviour of type RequestFIPAServiceBehaviour that can be 
 * added to the queue of the agent behaviours, as usual, by using 
 * <code>Agent.addBehaviour()</code>. 
 * @author Fabio Bellifemine - CSELT S.p.A.
  @version $Date$ $Revision$ 
 **/
public class AMSServiceCommunicator extends FIPAServiceCommunicator {
  private static Codec c = new SL0Codec();
  private static Ontology o = FIPAAgentManagementOntology.instance();

  /**
   * check that the <code>AMSAgentDescription</code> contains the mandatory
   * slots, i.e. the agent name and the agent state. 
   * @throw a MissingParameter exception is it is not valid
   */
  static void checkIsValid(AMSAgentDescription amsd) throws MissingParameter {
    if (amsd.getName()==null) 
      throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "name");
    if (amsd.getState()==null) 
      throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "state");
  }

  /**
     Register a AMSAgentDescription with a <b>AMS</b> agent. 
However, 
since <b>AMS</b> registration and
     deregistration are automatic in JADE, this method should not be
     used by application programmers to register with the default AMS.
     @param a is the Agent performing the registration 
     @param AMSName The AID of the <b>AMS</b> agent to register with.
     @param amsd A <code>AMSAgentDescriptor</code> object containing all
     data necessary to the registration. If the Agent name is empty, than
     it is set according to the <code>a</code> parameter. If the Agent state is
     empty, than it is set to ACTIVE.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition or when
     the method locally discovers that the amsdescription is not valid.
   */
  public static void register(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, AMSName);

    if (amsd.getName() == null)
      amsd.setName(a.getAID());
    if (amsd.getState() == null)
      amsd.setState(AMSAgentDescription.ACTIVE);
    checkIsValid(amsd);

    // Build a AMS action object for the request
    Register r = new Register();
    r.set_0(amsd);

    Action act = new Action();
    act.set_0(AMSName);
    act.set_1(r);

    synchronized(c) { //must be synchronized because this is a static method
      // Write the action in the :content slot of the request
      request.setContent(encode(act,c,o));
    }

    // Send message and collect reply
    doFipaRequestClient(a,request);
  }


  /**
   * registers a <code>AMSAgentDescription</code> with the default AMS
   * @see #register(Agent,AID,AMSAgentDescription)
   **/
  public static void register(Agent a, AMSAgentDescription amsd) throws FIPAException {
    register(a,a.getAMS(),amsd);
  }

  /**
     Deregister a AMSAgentDescription from a <b>AMS</b> agent. However, since <b>AMS</b> registration and
     deregistration are automatic in JADE, this method should not be
     used by application programmers to deregister with the default AMS.
     @param AMSName The AID of the <b>AMS</b> agent to deregister from.
     @param amsd A <code>AMSAgentDescription</code> object containing all
     data necessary to the deregistration.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition or when
     the method locally discovers that the amsdescription is not valid.
  */
  public static void deregister(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {

    ACLMessage request = createRequestMessage(a, AMSName);

    if (amsd.getName() == null)
      amsd.setName(a.getAID());
    if (amsd.getState() == null)
      amsd.setState(AMSAgentDescription.ACTIVE);
    // Build a AMS action object for the request
    Deregister d = new Deregister();
    d.set_0(amsd);

    Action act = new Action();
    act.set_0(AMSName);
    act.set_1(d);

    synchronized(c) { //must be synchronized because this is a static method
      // Write the action in the :content slot of the request
      request.setContent(encode(act,c,o));
    }

    // Send message and collect reply
    doFipaRequestClient(a,request);
  }

  /**
The AID of the AMS is defaulted to the AMS of this platform.
@see #deregister(Agent a, AID AMSName, AMSAgentDescription amsd)
  **/
  public static void deregister(Agent a, AMSAgentDescription amsd) throws FIPAException {
    deregister(a,a.getAMS(),amsd);
  }

  /**
A default AMSAgentDescription is used for this agent, where only AID and state
are set (state is set to ACTIVE).
@see #deregister(Agent a, AID AMSName, AMSAgentDescription amsd)
**/
  public static void deregister(Agent a, AID AMSName) throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(a.getAID());
    deregister(a,AMSName,amsd);
  }

  /**
A default AMSAgentDescription is used for this agent, where only AID and state
are set.
The AID of the AMS is defaulted to the AMS of this platform.
@see #deregister(Agent a, AID AMSName, AMSAgentDescription amsd)
**/
  public static void deregister(Agent a) throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(a.getAID());
    deregister(a,amsd);
  }


  /**
     Modifies data contained within a <b>AMS</b>
     agent. 
     @param AMSName The GUID of the <b>AMS</b> agent holding the data
     to be changed.
     @param amsd The new <code>AMSAgentDescriptor</code> object 
     that should modify the existing one. 
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition or when
     the method locally discovers that the amsdescription is not valid.
  */
  public static void modify(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, AMSName);

    if (amsd.getName() == null)
      amsd.setName(a.getAID());
    checkIsValid(amsd);
    // Build a AMS action object for the request
    Modify m = new Modify();
    m.set_0(amsd);

    Action act = new Action();
    act.set_0(AMSName);
    act.set_1(m);

    synchronized(c) { //must be synchronized because this is a static method
      // Write the action in the :content slot of the request
      request.setContent(encode(act,c,o));
    }

    // Send message and collect reply
    doFipaRequestClient(a,request);
  }

  /**
The AID of the AMS is defaulted to the AMS of this platform.
@see #modify(Agent a, AID AMSName, AMSAgentDescription amsd)
**/
  public static void modify(Agent a, AMSAgentDescription amsd) throws FIPAException {
    modify(a,a.getAMS(),amsd);
  }

  /**
     Searches for data contained within a <b>AMS</b> agent. 
     @param a is the Agent performing the search 
     @param AMSName The GUID of the <b>AMS</b> agent to start search from.
     @param amsd A <code>AMSAgentDescriptor</code> object containing
     data to search for; this parameter is used as a template to match
     data against.
     @param constraints of the search 
     @return A <code>List</code> 
     containing all found
     <code>AMSAgentDescription</code> objects matching the given
     descriptor, subject to given search constraints for search depth
     and result size.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition.
  */
  public static List search(Agent a, AID AMSName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
    ACLMessage request = createRequestMessage(a, AMSName);

    // Build a AMS action object for the request
    Search s = new Search();
    s.set_0(amsd);
    s.set_1(constraints);

    Action act = new Action();
    act.set_0(AMSName);
    act.set_1(s);

    synchronized(c) { //must be synchronized because this is a static method
      // Write the action in the :content slot of the request
      request.setContent(encode(act,c,o));
    }

    // Send message and collect reply
    ACLMessage inform = doFipaRequestClient(a,request);

    ResultPredicate r = null;
    synchronized(c) { //must be synchronized because this is a static method
      r = extractContent(inform.getContent(),c,o);
    }
    Iterator i = r.getAll_1(); //this is the set of AMSAgentDescription
    List l = new ArrayList(); 
    while (i.hasNext())
      l.add(i.next());
    return l; 
  }


  /**
   * searches with the default AMS
   * @see #search(Agent,AID,AMSAgentDescription,SearchConstraints)
   **/
  public static List search(Agent a, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
    return search(a,a.getAMS(),amsd,constraints);
  }

  /**
   * searches with the default AMS and the default SearchConstraints.
   * The default constraints specified by FIPA are max_results and max_depth
   * both unspecified and left to the choice of the responder AMS.
   * @see #search(Agent,AID,AMSAgentDescription,SearchConstraints)
   **/
  public static List search(Agent a, AMSAgentDescription amsd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return search(a,a.getAMS(),amsd,constraints);
  }

  /**
   * searches with the passed AMS by using the default SearchConstraints.
   * The default constraints specified by FIPA are max_results and max_depth
   * both unspecified and left to the choice of the responder AMS.
   * @see #search(Agent,AID,AMSAgentDescription,SearchConstraints)
   **/
  public static List search(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return search(a,AMSName,amsd,constraints);
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
* @param AMSName is the AID that should perform the requested action
* @param actionName is the name of the action (one of the constants defined
* in FIPAAgentManagementOntology: REGISTER / DEREGISTER / MODIFY / SEARCH).
* @param amsd is the agent description
* @param constraints are the search constraints (can be null if this is
* not a search operation)
* @return the behaviour to be added to the agent
     @exception FIPAException A suitable exception can be thrown 
     to indicate some error condition 
     locally discovered (e.g.the amsdescription is not valid.)
@see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
     **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
    return new RequestFIPAServiceBehaviour(a,AMSName,actionName,amsd,constraints);
  }

  /**
the default AMS is used.
@see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
    return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
  }

  /**
the default AMS is used.
the default SearchContraints are used.
a default AgentDescription is used, where only the agent AID is set.
@see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   * @see #search(Agent,AID,AMSAgentDescription)
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName) throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(a.getAID());
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
  }

  /**
the default SearchContraints are used.
a default AgentDescription is used, where only the agent AID is set.
@see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   * @see #search(Agent,AID,AMSAgentDescription)
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID amsName, String actionName) throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(a.getAID());
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,amsName,actionName,amsd,constraints);
  }

  /**
the default AMS is used.
the default SearchContraints are used.
a default AgentDescription is used, where only the agent AID is set.
@see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   * @see #search(Agent,AID,AMSAgentDescription)
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, AMSAgentDescription amsd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
  }

  /**
the default AMS is used.
the default SearchContraints are used.
@see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   * @see #search(Agent,AID,AMSAgentDescription)
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID amsName, String actionName, AMSAgentDescription amsd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,amsName,actionName,amsd,constraints);
  }

}


