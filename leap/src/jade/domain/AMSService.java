/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.domain;

import jade.domain.FIPAAgentManagement.*;
import jade.core.Agent;
import jade.core.AID;
import jade.core.Runtime;
import jade.lang.acl.ACLMessage;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.lang.sl.SL0Parser;
import jade.onto.Frame;
import jade.onto.OntologyException;
import java.io.Reader;

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
 * the default Search Constraints.
 * Notice that all these methods blocks every activity of the agent until the
 * action (i.e. register/deregister/modify/search) has been successfully
 * executed or a jade.domain.FIPAException exception has been thrown
 * (e.g. because a FAILURE message has been received by the AMS).
 * @author Giovanni Caire - TILAB S.p.A.
 * @version $Date$ $Revision$
 */
public class AMSService extends FIPAServiceCommunicator {

  /**
   * check that the <code>AMSAgentDescription</code> contains the mandatory
   * slots, i.e. the agent name and the agent state.
   * @throw a MissingParameter exception is it is not valid
   */
  static void checkIsValid(AMSAgentDescription amsd) throws MissingParameter {
    if (amsd.getName() == null) {
      throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "name");
    } 

    if (amsd.getState() == null) {
      throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "state");
    } 
  } 

  /**
   * Register a AMSAgentDescription with a <b>AMS</b> agent.
   * However,
   * since <b>AMS</b> registration and
   * deregistration are automatic in JADE, this method should not be
   * used by application programmers to register with the default AMS.
   * @param a is the Agent performing the registration
   * @param AMSName The AID of the <b>AMS</b> agent to register with.
   * @param amsd A <code>AMSAgentDescriptor</code> object containing all
   * data necessary to the registration. If the Agent name is empty, than
   * it is set according to the <code>a</code> parameter. If the Agent state is
   * empty, than it is set to ACTIVE.
   * @exception FIPAException A suitable exception can be thrown when
   * a <code>refuse</code> or <code>failure</code> messages are
   * received from the AMS to indicate some error condition or when
   * the method locally discovers that the amsdescription is not valid.
   */
  public static void register(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
    //Runtime.instance().gc(20);

    ACLMessage request = createRequestMessage(a, AMSName);

    // Preparing mandatory name and state fields
    if (amsd.getName() == null) {
      amsd.setName(a.getAID());
    } 

    if (amsd.getState() == null) {
      amsd.setState(AMSAgentDescription.ACTIVE);
    } 

    checkIsValid(amsd);

    String       stringAgentAID = amsd.getName().toString();
    String       stringAmsAID = AMSName.toString();
    StringBuffer content = new StringBuffer("((action ");

    content.append(stringAmsAID);
    content.append(" ("+FIPAAgentManagementOntology.REGISTER);
    content.append(" ("+FIPAAgentManagementOntology.AMSAGENTDESCRIPTION);

    // AMSDescription.name (mandatory)
    content.append(" :name "+stringAgentAID);

    // AMSDescription.state (mandatory)
    content.append(" :state "+amsd.getState());

    // AMSDescription.ownership (optional)
    if (amsd.getOwnership() != null) {
      content.append(" :ownership "+amsd.getOwnership());
    } 

    content.append(" )))) ");    // Close AMSD object, Register, Action, Content

    // Write the action in the :content slot of the request
    request.setContent(content.toString());

    //Runtime.instance().gc(21);
    // Send message and collect reply
    doFipaRequestClient(a, request);
    //Runtime.instance().gc(22);
  } 

  /**
   * Deregister a AMSAgentDescription from a <b>AMS</b> agent. However, since <b>AMS</b> registration and
   * deregistration are automatic in JADE, this method should not be
   * used by application programmers to deregister with the default AMS.
   * @param AMSName The AID of the <b>AMS</b> agent to deregister from.
   * @param amsd A <code>AMSAgentDescription</code> object containing all
   * data necessary to the deregistration.
   * @exception FIPAException A suitable exception can be thrown when
   * a <code>refuse</code> or <code>failure</code> messages are
   * received from the AMS to indicate some error condition or when
   * the method locally discovers that the amsdescription is not valid.
   */
  public static void deregister(Agent a, AID AMSName, 
                                AMSAgentDescription amsd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, AMSName);

    // Preparing required amsd fields
    if (amsd.getName() == null) {
      amsd.setName(a.getAID());
    } 

    if (amsd.getState() == null) {
      amsd.setState(AMSAgentDescription.ACTIVE);
    } 

    checkIsValid(amsd);

    String       stringAgentAID = amsd.getName().toString();
    String       stringAmsAID = AMSName.toString();
    StringBuffer content = new StringBuffer("((action ");

    content.append(stringAmsAID);
    content.append(" ("+FIPAAgentManagementOntology.DEREGISTER);
    content.append(" ("+FIPAAgentManagementOntology.AMSAGENTDESCRIPTION);

    // AMSDescription.name (mandatory)
    content.append(" :name "+stringAgentAID);
    content.append(" ))))");    // Close AMSD object, Deregister, Action, Content

    // Write the action in the :content slot of the request
    request.setContent(content.toString());

    // Send message and collect reply
    doFipaRequestClient(a, request);
  } 

  /**
   * Modifies data contained within a <b>AMS</b>
   * agent.
   * @param AMSName The GUID of the <b>AMS</b> agent holding the data
   * to be changed.
   * @param amsd The new <code>AMSAgentDescriptor</code> object
   * that should modify the existing one.
   * @exception FIPAException A suitable exception can be thrown when
   * a <code>refuse</code> or <code>failure</code> messages are
   * received from the AMS to indicate some error condition or when
   * the method locally discovers that the amsdescription is not valid.
   */
  public static void modify(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, AMSName);

    // Preparing required amsd fields
    if (amsd.getName() == null) {
      amsd.setName(a.getAID());
    } 

    checkIsValid(amsd);

    String       stringAgentAID = amsd.getName().toString();
    String       stringAmsAID = AMSName.toString();
    StringBuffer content = new StringBuffer("((action ");

    content.append(stringAmsAID);
    content.append(" ("+FIPAAgentManagementOntology.MODIFY);
    content.append(" ("+FIPAAgentManagementOntology.AMSAGENTDESCRIPTION);

    // AMSDescription.name (mandatory)
    content.append(" :name "+stringAgentAID);

    // AMSDescription.state (optional)
    if (amsd.getState() != null) {
      content.append(" :state "+amsd.getState());
    } 

    // AMSDescription.ownership (optional)
    if (amsd.getOwnership() != null) {
      content.append(" :ownership "+amsd.getOwnership());
    } 

    content.append(" ))))");    // Close AMSD object, Deregister, Action, Content

    // Write the action in the :content slot of the request
    request.setContent(content.toString());

    // Send message and collect reply
    doFipaRequestClient(a, request);
  } 

  /**
   * Searches for data contained within a <b>AMS</b> agent.
   * @param a is the Agent performing the search
   * @param AMSName The GUID of the <b>AMS</b> agent to start search from.
   * @param amsd A <code>AMSAgentDescriptor</code> object containing
   * data to search for; this parameter is used as a template to match
   * data against.
   * @param constraints of the search
   * @return A <code>List</code>
   * containing all found
   * <code>AMSAgentDescription</code> objects matching the given
   * descriptor, subject to given search constraints for search depth
   * and result size.
   * @exception FIPAException A suitable exception can be thrown when
   * a <code>refuse</code> or <code>failure</code> messages are
   * received from the AMS to indicate some error condition.
   */
  public static AMSAgentDescription[] search(Agent a, AID AMSName, AMSAgentDescription amsd, 
                                             SearchConstraints constraints) throws FIPAException {

    // Set default values (if null) for optional parameters
    if (AMSName == null) {
      AMSName = a.getAMS();
    } 

    if (constraints == null) {
      constraints = new SearchConstraints();
    } 

    ACLMessage   request = createRequestMessage(a, AMSName);
    String       stringAmsAID = AMSName.toString();
    StringBuffer content = new StringBuffer("((action ");

    content.append(stringAmsAID);
    content.append(" ("+FIPAAgentManagementOntology.SEARCH);
    content.append(" ("+FIPAAgentManagementOntology.AMSAGENTDESCRIPTION);

    // AMSDescription.name (optional)
    if (amsd.getName() != null) {
      String stringAgentAID = amsd.getName().toString();

      content.append(" :name "+stringAgentAID);
    } 

    // AMSDescription.state (optional)
    if (amsd.getState() != null) {
      content.append(" :state "+amsd.getState());
    } 

    // AMSDescription.ownership (optional)
    if (amsd.getOwnership() != null) {
      content.append(" :ownership "+amsd.getOwnership());
    } 

    content.append(" )");    // Close AMSD object

    // Mandatory search-constraints object
    content.append(" ("+FIPAAgentManagementOntology.SEARCHCONSTRAINTS);

    // SearchConstraints.max-depth (optional)
    if (constraints.getMaxDepth() != null) {
      content.append(" :max-depth "+constraints.getMaxDepth().toString());
    } 

    // SearchConstraints.max-results (optional)
    if (constraints.getMaxResults() != null) {
      content.append(" :max-results "+constraints.getMaxResults().toString());
    } 

    content.append(" ))))");    // Close SearchConstraints object, Search, Action, Content

    // Write the action in the :content slot of the request
    request.setContent(content.toString());

    // Send message and collect reply
    ACLMessage reply = doFipaRequestClient(a, request);

    return getAMSSearchResult(reply);
  } 

  /**
   * Utility methods that extracts the list of AMSD included in the
   * result of a search action on the AMS
   */
  static AMSAgentDescription[] getAMSSearchResult(ACLMessage msg) throws FIPAException {
    AMSAgentDescription[] descriptions = null;
    String                content = msg.getContent();

    if (content != null) {
      try {

        // The content has the form:
        // (result (action...) (sequence D1 D2...)
        SL0Parser parser = new SL0Parser((Reader) null);
        List      l = parser.parse(content);
        Frame     result = (Frame) l.get(0);
        Frame     descSeq = (Frame) result.getSlot(1);

        descriptions = new AMSAgentDescription[descSeq.size()];

        for (int i = 0; i < descSeq.size(); ++i) {
          Frame descF = (Frame) descSeq.getSlot(i);
          descriptions[i] = convertFrameToAMSD(descF);
        } 

        parser = null;
      } 
      catch (Exception e) {
        throw new FIPAException("Error parsing search result");
      } 
    } 

    return descriptions;
  } 

  /**
   * Utility methods that converts a Frame object representing
   * an AMSAgentDescription into an AMSAgentDescription object
   */
  public static AMSAgentDescription convertFrameToAMSD(Frame f) throws OntologyException {
    AMSAgentDescription amsd = null;

    if (f != null) {
      amsd = new AMSAgentDescription();

      // Name (i.e. AID) (optional)
      Frame f1 = (Frame) getFrameOptionalSlot(f, "name");

      if (f1 != null) {
        amsd.setName(convertFrameToAID(f1));
      } 

      // State (optional)
      amsd.setState((String) getFrameOptionalSlot(f, "state"));

      // Ownership (optional)
      amsd.setOwnership((String) getFrameOptionalSlot(f, "ownership"));
    } 

    return amsd;
  } 

  /**
   * In some cases it is more convenient to execute this tasks in a non-blocking way.
   * This method returns a non-blocking behaviour that can be added to the queue of the agent behaviours, as usual, by using <code>Agent.addBehaviour()</code>.
   * <p>
   * Several ways are available to get the result of this behaviour and the programmer can select one according to his preferred programming style:
   * <ul>
   * <li>
   * call getLastMsg() and getSearchResults() where both throw a NotYetReadyException if the task has not yet finished;
   * <li>create a SequentialBehaviour composed of two sub-behaviours:  the first subbehaviour is the returned RequestFIPAServiceBehaviour, while the second one is application-dependent and is executed only when the first is terminated;
   * <li>use directly the class RequestFIPAServiceBehaviour by extending it and overriding all the handleXXX methods that handle the states of the fipa-request interaction protocol.
   * </ul>
   * @param a is the agent performing the task
   * @param AMSName is the AID that should perform the requested action
   * @param actionName is the name of the action (one of the constants defined
   * in FIPAAgentManagementOntology: REGISTER / DEREGISTER / MODIFY / SEARCH).
   * @param amsd is the agent description
   * @param constraints are the search constraints (can be null if this is
   * not a search operation)
   * @return the behaviour to be added to the agent
   * @exception FIPAException A suitable exception can be thrown
   * to indicate some error condition
   * locally discovered (e.g.the amsdescription is not valid.)
   * @see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
   */
  /*
   * public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID AMSName,
   * String actionName, AMSAgentDescription amsd,
   * SearchConstraints constraints) throws FIPAException {
   * return new RequestFIPAServiceBehaviour(a, AMSName, actionName, amsd, constraints);
   * }
   */

  /**
   * the default AMS is used.
   * @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   */

  /*
   * REMOVED from the J2ME version
   * public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
   * return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
   * }
   */

  /**
   * the default AMS is used.
   * the default SearchContraints are used.
   * a default AgentDescription is used, where only the agent AID is set.
   * @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   * @see #search(Agent,AID,AMSAgentDescription)
   */

  /*
   * REMOVED from the J2ME version
   * public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName) throws FIPAException {
   * AMSAgentDescription amsd = new AMSAgentDescription();
   * amsd.setName(a.getAID());
   * SearchConstraints constraints = new SearchConstraints();
   * return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
   * }
   */

  /**
   * the default SearchContraints are used.
   * a default AgentDescription is used, where only the agent AID is set.
   * @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   * @see #search(Agent,AID,AMSAgentDescription)
   */

  /*
   * REMOVED from the J2ME version
   * public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID amsName, String actionName) throws FIPAException {
   * AMSAgentDescription amsd = new AMSAgentDescription();
   * amsd.setName(a.getAID());
   * SearchConstraints constraints = new SearchConstraints();
   * return getNonBlockingBehaviour(a,amsName,actionName,amsd,constraints);
   * }
   */

  /**
   * the default AMS is used.
   * the default SearchContraints are used.
   * a default AgentDescription is used, where only the agent AID is set.
   * @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   * @see #search(Agent,AID,AMSAgentDescription)
   */

  /*
   * REMOVED from the J2ME version
   * public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, AMSAgentDescription amsd) throws FIPAException {
   * SearchConstraints constraints = new SearchConstraints();
   * return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
   * }
   */

  /**
   * the default AMS is used.
   * the default SearchContraints are used.
   * @see #getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints)
   * @see #search(Agent,AID,AMSAgentDescription)
   */

  /*
   * REMOVED from the J2ME version
   * public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID amsName, String actionName, AMSAgentDescription amsd) throws FIPAException {
   * SearchConstraints constraints = new SearchConstraints();
   * return getNonBlockingBehaviour(a,amsName,actionName,amsd,constraints);
   * }
   */
}

