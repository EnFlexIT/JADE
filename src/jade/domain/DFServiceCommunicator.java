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
import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;
import jade.onto.OntologyException;
import jade.lang.Codec;
import jade.lang.sl.SL0Codec;
import jade.onto.Ontology;

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
   @version $Date$ $Revision$ 
   @deprecated Use DFService instead.
 * 
 **/
public class DFServiceCommunicator extends FIPAServiceCommunicator {

  private static Codec c = new SL0Codec();
  private static Ontology o = FIPAAgentManagementOntology.instance();

  /**
   * check that the <code>DFAgentDescription</code> contains the mandatory
   * slots, i.e. the agent name and, for each servicedescription, the
   * service name and the service type
   * @throw a MissingParameter exception is it is not valid
   */
  static void checkIsValid(DFAgentDescription dfd) throws MissingParameter {
		DFService.checkIsValid(dfd);
  }

  /**
     Register a DFDescriptiont with a <b>DF</b> agent. 
     @param a is the Agent performing the registration (it is needed in order
     to send/receive messages
     @param dfName The AID of the <b>DF</b> agent to register with.
     @param dfd A <code>DFAgentDescriptor</code> object containing all
     data necessary to the registration. If the Agent name is empty, than
     it is set according to the <code>a</code> parameter.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition or when
     the method locally discovers that the DFDescription is not valid.
   */
  public static void register(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
		DFService.register(a, dfName, dfd);
  }


  /**
   * registers a <code>DFAgentDescription</code> with the default DF
   * @see #register(Agent,AID,DFAgentDescription)
   **/
  public static void register(Agent a, DFAgentDescription dfd) throws FIPAException {
		DFService.register(a, dfd);
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
		DFService.deregister(a, dfName, dfd);
  }

  /**
   * The default DF of the platform is used.
@see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
  **/
  public static void deregister(Agent a, DFAgentDescription dfd) throws FIPAException {
		DFService.deregister(a, dfd);
  }

  /**
   * A default Agent Description is used which contains only the AID
   * of this agent.
@see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
  **/
  public static void deregister(Agent a, AID dfName) throws FIPAException {
		DFService.deregister(a, dfName);
  }

  /**
   * The default DF of the platform is used.
   * A default Agent Description is used which contains only the AID
   * of this agent.
@see #deregister(Agent a, AID dfName, DFAgentDescription dfd) 
  **/
  public static void deregister(Agent a) throws FIPAException {
		DFService.deregister(a);
  }


  /**
     Modifies data contained within a <b>DF</b>
     agent. 
     @param a is the Agent performing the request of modification 
     @param dfName The AID of the <b>DF</b> agent holding the data
     to be changed.
     @param dfd A <code>DFAgentDescriptor</code> object containing all
     new data values; 
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public static void modify(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
		DFService.modify(a, dfName, dfd);
  }

  /**
   * The default DF of the platform is used.
@see #modify(Agent a, AID dfName, DFAgentDescription dfd)
  **/
  public static void modify(Agent a, DFAgentDescription dfd) throws FIPAException {
		DFService.modify(a, dfd);
  }

  /**
     Searches for data contained within a <b>DF</b> agent. 
     @param a is the Agent performing the request of search 
     @param dfName The AID of the <b>DF</b> agent to start search from.
     @param dfd A <code>DFAgentDescription</code> object containing
     data to search for; this parameter is used as a template to match
     data against.
     @param constraints of the search 
     @return A <code>List</code> 
     containing all found
     <code>DFAgentDescription</code> objects matching the given
     descriptor, subject to given search constraints for search depth
     and result size.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public static java.util.List search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
		DFAgentDescription[] r = DFService.search(a, dfName, dfd, constraints);
		java.util.List l = new java.util.ArrayList();
		for (int i = 0; i < r.length; ++i) {
			l.add(r[i]);
		}
		return l;
  }


  /**
   * The default DF is used.
@see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static java.util.List search(Agent a, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
		DFAgentDescription[] r = DFService.search(a, dfd, constraints);
		java.util.List l = new java.util.ArrayList();
		for (int i = 0; i < r.length; ++i) {
			l.add(r[i]);
		}
		return l;
  }

  /**
   * The default DF is used.
   * The default SearchConstraints are used. According to FIPA they are
   * defaulted to null value for all slots.
@see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static java.util.List search(Agent a, DFAgentDescription dfd) throws FIPAException {
		DFAgentDescription[] r = DFService.search(a, dfd);
		java.util.List l = new java.util.ArrayList();
		for (int i = 0; i < r.length; ++i) {
			l.add(r[i]);
		}
		return l;
  }

  /**
   * The default SearchConstraints are used. According to FIPA they are
   * defaulted to null value for all slots.
@see #search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static java.util.List search(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
  	DFAgentDescription[] r = DFService.search(a, dfName, dfd);
		java.util.List l = new java.util.ArrayList();
		for (int i = 0; i < r.length; ++i) {
			l.add(r[i]);
		}
		return l;
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
    return DFService.getNonBlockingBehaviour(a,dfName,actionName,dfd,constraints);
  }

  /**
   * The default DF is used.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    return DFService.getNonBlockingBehaviour(a,actionName,dfd,constraints);
  }

  /**
   * The default DF is used.
   the default SearchContraints are used.
a default AgentDescription is used, where only the agent AID is set.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName) throws FIPAException {
    return DFService.getNonBlockingBehaviour(a,actionName);
  }


  /**
   the default SearchContraints are used.
a default AgentDescription is used, where only the agent AID is set.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName) throws FIPAException {
    return DFService.getNonBlockingBehaviour(a,dfName,actionName);
  }


  /**
   * The defautl DF is used.
   the default SearchContraints are used.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, DFAgentDescription dfd) throws FIPAException {
    return DFService.getNonBlockingBehaviour(a,actionName,dfd);
  }

  /**
   *   the default SearchContraints are used.
   * @see #getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) 
  **/
  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd) throws FIPAException {
    return DFService.getNonBlockingBehaviour(a,dfName,actionName,dfd);
  }

}

