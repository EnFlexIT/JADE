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
import jade.lang.acl.ACLMessage;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.lang.sl.SL0Parser;
import jade.onto.Frame;
import jade.onto.OntologyException;
import java.io.Reader;

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
 * @author Giovanni Caire (TILAB S.p.a.)
 * @version $Date$ $Revision$
 * 
 */
public class DFService extends FIPAServiceCommunicator {

  /**
   * Check that the <code>DFAgentDescription</code> contains the mandatory
   * slots, i.e. the agent name and, for each servicedescription, the
   * service name and the service type
   * @throw a MissingParameter exception is it is not valid
   */
  static void checkIsValid(DFAgentDescription dfd) throws MissingParameter {
    if (dfd.getName() == null) {
      throw new MissingParameter(FIPAAgentManagementOntology.DFAGENTDESCRIPTION, "name");
    } 

    Iterator i = dfd.getAllServices();

    while (i.hasNext()) {
      ServiceDescription sd = (ServiceDescription) i.next();

      if (sd.getName() == null) {
        throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "name");
      } 

      if (sd.getType() == null) {
        throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "type");
      } 
    } 
  } 

  /**
   * Register a DFDescriptiont with a <b>DF</b> agent.
   * @param a is the Agent performing the registration (it is needed in order
   * to send/receive messages
   * @param dfName The AID of the <b>DF</b> agent to register with.
   * @param dfd A <code>DFAgentDescriptor</code> object containing all
   * data necessary to the registration. If the Agent name is empty, than
   * it is set according to the <code>a</code> parameter.
   * @exception FIPAException A suitable exception can be thrown when
   * a <code>refuse</code> or <code>failure</code> messages are
   * received from the DF to indicate some error condition or when
   * the method locally discovers that the DFDescription is not valid.
   */
  public static void register(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, dfName);

    // Preparing mandatory name field
    if (dfd.getName() == null) {
      dfd.setName(a.getAID());
    } 

    checkIsValid(dfd);

    String       stringAgentAID = dfd.getName().toString();
    String       stringDfAID = dfName.toString();
    StringBuffer content = new StringBuffer("((action ");

    content.append(stringDfAID);
    content.append(" ("+FIPAAgentManagementOntology.REGISTER);
    content.append(" ("+FIPAAgentManagementOntology.DFAGENTDESCRIPTION);

    // DFDescription.name (mandatory)
    content.append(" :name "+stringAgentAID);
    appendOptionalContent(content, dfd);

    // Close DFD object, Register, Action, Content
    content.append(" )))) ");

    // Write the action in the :content slot of the request
    request.setContent(content.toString());

    // Send message and collect reply
    doFipaRequestClient(a, request);
  } 

  /**
   * Method declaration
   * 
   * @param content
   * @param seqName
   * @param it
   * 
   * @see
   */
  private static void writeSequenceOfStrings(StringBuffer content, String seqName, Iterator it) {

    // Can't use a primitive boolean as this is a static method
    Boolean closeParenthesisFlag = null;

    if (it.hasNext()) {
      content.append(" :"+seqName+" (sequence");

      closeParenthesisFlag = new Boolean(true);
    } 
    else {
      closeParenthesisFlag = new Boolean(false);
    } 

    while (it.hasNext()) {
      String s = (String) it.next();

      content.append(" "+s);
    } 

    if (closeParenthesisFlag.booleanValue()) {
      content.append(")");
    } 
  } 

  /**
   * Deregister a DFAgentDescription from a <b>DF</b> agent.
   * @param dfName The AID of the <b>DF</b> agent to deregister from.
   * @param dfd A <code>DFAgentDescription</code> object containing all
   * data necessary to the deregistration.
   * @exception FIPAException A suitable exception can be thrown when
   * a <code>refuse</code> or <code>failure</code> messages are
   * received from the DF to indicate some error condition or when
   * the method locally discovers that the amsdescription is not valid.
   */
  public static void deregister(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, dfName);

    // Preparing required (optional) message fields
    if (dfd.getName() == null) {
      dfd.setName(a.getAID());
    } 

    checkIsValid(dfd);

    String       stringAgentAID = dfd.getName().toString();
    String       stringDfAID = dfName.toString();
    StringBuffer content = new StringBuffer("((action ");

    content.append(stringDfAID);
    content.append(" ("+FIPAAgentManagementOntology.DEREGISTER);
    content.append(" ("+FIPAAgentManagementOntology.DFAGENTDESCRIPTION);

    // DFDescription.name (mandatory)
    content.append(" :name "+stringAgentAID);
    content.append(" )))) ");    // Close DFD object, Register, Action, Content

    // Write the action in the :content slot of the request
    request.setContent(content.toString());

    // Send message and collect reply
    doFipaRequestClient(a, request);
  } 

  /**
   * Modifies data contained within a <b>DF</b>
   * agent.
   * @param dfName The AID of the <b>DF</b> agent holding the data
   * to be changed.
   * @param dfd The new <code>DFAgentDescriptor</code> object
   * that should modify the existing one.
   * @exception FIPAException A suitable exception can be thrown when
   * a <code>refuse</code> or <code>failure</code> messages are
   * received from the DF to indicate some error condition or when
   * the method locally discovers that the dfdescription is not valid.
   */
  public static void modify(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, dfName);

    // Preparing required dfd field
    if (dfd.getName() == null) {
      dfd.setName(a.getAID());
    } 

    checkIsValid(dfd);

    String       stringAgentAID = dfd.getName().toString();
    String       stringDfAID = dfName.toString();
    StringBuffer content = new StringBuffer("((action ");

    content.append(stringDfAID);
    content.append("("+FIPAAgentManagementOntology.MODIFY);
    content.append("("+FIPAAgentManagementOntology.DFAGENTDESCRIPTION);

    // DFDescription.name (optional)
    if (dfd.getName() != null) {
      content.append(":name "+stringAgentAID);
    } 

    appendOptionalContent(content, dfd);
    content.append(" ))))");    // Close DFDescription object, modify, Action, Content

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
  public static DFAgentDescription[] search(Agent a, AID dfName, DFAgentDescription dfd, 
                                            SearchConstraints constraints) throws FIPAException {
    // Set default values (if null) for optional parameters
    if (dfName == null) {
      dfName = a.getDefaultDF();
    } 

    if (constraints == null) {
      constraints = new SearchConstraints();
    } 

    ACLMessage   request = createRequestMessage(a, dfName);
    String       stringDfAID = dfName.toString();
    StringBuffer content = new StringBuffer("((action ");

    content.append(stringDfAID);
    content.append(" ("+FIPAAgentManagementOntology.SEARCH);
    content.append(" ("+FIPAAgentManagementOntology.DFAGENTDESCRIPTION);

    // DFDescription.name (optional)
    if (dfd.getName() != null) {
      String stringAgentAID = dfd.getName().toString();

      content.append(" :name "+stringAgentAID);
    } 

    appendOptionalContent(content, dfd);

    // End of DFdescription object
    content.append(" )");

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

    request = null;

    return getDFSearchResult(reply);
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
   * @param dfName is the AID of the DF that should perform the requested action
   * @param actionName is the name of the action (one of the constants defined
   * in FIPAAgentManagementOntology: REGISTER / DEREGISTER / MODIFY / SEARCH).
   * @param dfd is the agent description
   * @param constraints are the search constraints (can be null if this is
   * not a search operation)
   * @return the behaviour to be added to the agent
   * @exception FIPAException A suitable exception can be thrown
   * to indicate some error condition
   * locally discovered (e.g.the dfdescription is not valid.)
   * @see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
   */
  /*
   * public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName,
   * String actionName, DFAgentDescription dfd,
   * SearchConstraints constraints) throws FIPAException {
   * return new RequestFIPAServiceBehaviour(a, dfName, actionName, dfd, constraints);
   * }
   */

  /**
   * Utility methods that extracts the list of DFD included in the
   * result of a search action on the DF
   */
  static DFAgentDescription[] getDFSearchResult(ACLMessage msg) throws FIPAException {
    DFAgentDescription[] descriptions = null;
    String               content = msg.getContent();

    if (content != null) {
      try {

        // The content has the form:
        // (result (action...) (sequence D1 D2...)
        SL0Parser parser = new SL0Parser((Reader) null);
        List      l = parser.parse(content);
        Frame     result = (Frame) l.get(0);
        Frame     descSeq = (Frame) result.getSlot(1);

        descriptions = new DFAgentDescription[descSeq.size()];

        for (int i = 0; i < descSeq.size(); ++i) {
          Frame descF = (Frame) descSeq.getSlot(i);
          descriptions[i] = convertFrameToDFD(descF);
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
   * a DFAgentDescription into a DFAgentDescription object
   */
  public static DFAgentDescription convertFrameToDFD(Frame f) throws OntologyException {
    DFAgentDescription dfd = null;

    if (f != null) {
      dfd = new DFAgentDescription();

      // Name (i.e. AID) (optional)
      Frame f1 = (Frame) getFrameOptionalSlot(f, "name");

      if (f1 != null) {
        dfd.setName(convertFrameToAID(f1));
      } 

      // Protocols (optional)
      f1 = (Frame) getFrameOptionalSlot(f, "protocols");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          dfd.addProtocols((String) f1.getSlot(i));
        } 
      } 

      // Languages (optional)
      f1 = (Frame) getFrameOptionalSlot(f, "languages");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          dfd.addLanguages((String) f1.getSlot(i));
        } 
      } 

      // Ontologies (optional)
      f1 = (Frame) getFrameOptionalSlot(f, "ontologies");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          dfd.addOntologies((String) f1.getSlot(i));
        } 
      } 

      // Services (optional)
      f1 = (Frame) getFrameOptionalSlot(f, "services");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          Frame f2 = (Frame) f1.getSlot(i);

          dfd.addServices(convertFrameToSD(f2));
        } 
      } 
    } 

    return dfd;
  } 

  /**
   * Utility methods that converts a Frame object representing
   * a ServiceDescription into a ServiceDescription object
   */
  public static ServiceDescription convertFrameToSD(Frame f) throws OntologyException {
    ServiceDescription sd = null;

    if (f != null) {
      sd = new ServiceDescription();

      // Name (optional)
      sd.setName((String) getFrameOptionalSlot(f, "name"));

      // Type (optional)
      sd.setType((String) getFrameOptionalSlot(f, "type"));

      // Ownership (optional)
      sd.setOwnership((String) getFrameOptionalSlot(f, "ownership"));

      // Protocols (optional)
      Frame f1 = (Frame) getFrameOptionalSlot(f, "protocols");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          sd.addProtocols((String) f1.getSlot(i));
        } 
      } 

      // Languages (optional)
      f1 = (Frame) getFrameOptionalSlot(f, "languages");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          sd.addLanguages((String) f1.getSlot(i));
        } 
      } 

      // Ontologies (optional)
      f1 = (Frame) getFrameOptionalSlot(f, "ontologies");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          sd.addOntologies((String) f1.getSlot(i));
        } 
      } 

      // Properties (optional)
      f1 = (Frame) getFrameOptionalSlot(f, "properties");

      if (f1 != null) {
        for (int i = 0; i < f1.size(); ++i) {
          sd.addProperties(convertFrameToProperty(f1));
        } 
      } 
    } 

    return sd;
  } 

  /**
   * Utility methods that converts a Frame object representing
   * a Properrty into a Property object
   */
  public static Property convertFrameToProperty(Frame f) throws OntologyException {
    Property prop = null;

    if (f != null) {
      prop = new Property();

      // name (mandatory)
      prop.setName((String) getFrameOptionalSlot(f, "name"));

      // value (mandatory)
      prop.setValue((String) getFrameOptionalSlot(f, "value"));
    } 

    return prop;
  } 

  /**
   * Utility method that writes into a message's content optional slots
   * (called in several methods, enables to reduce class size!)
   */
  private static void appendOptionalContent(StringBuffer content, DFAgentDescription dfd) {

    // DFDescription.protocols (Optional)
    writeSequenceOfStrings(content, "protocols", dfd.getAllProtocols());

    // DFDescription.languages (Optional)
    writeSequenceOfStrings(content, "languages", dfd.getAllLanguages());

    // DFDescription.ontologies (Optional)
    writeSequenceOfStrings(content, "ontologies", dfd.getAllOntologies());

    // DFDescription.services (optional)
    Iterator it = dfd.getAllServices();

    // Can't use a primitive boolean as this is a static method
    Boolean  closeParenthesisFlag = null;

    if (it.hasNext()) {
      content.append(" :services (sequence");

      closeParenthesisFlag = new Boolean(true);
    } 
    else {
      closeParenthesisFlag = new Boolean(false);
    } 

    while (it.hasNext()) {
      content.append(" ("+FIPAAgentManagementOntology.SERVICEDESCRIPTION);

      ServiceDescription sd = (ServiceDescription) it.next();

      // ServiceDescription.name (Optional)
      if (sd.getName() != null) {
        content.append(" :name "+sd.getName());
      } 

      // ServiceDescription.type (Optional)
      if (sd.getType() != null) {
        content.append(" :type "+sd.getType());
      } 

      // ServiceDescription.protocols (Optional)
      writeSequenceOfStrings(content, "protocols", sd.getAllProtocols());

      // ServiceDescription.languages (Optional)
      writeSequenceOfStrings(content, "languages", sd.getAllLanguages());

      // ServiceDescription.ontologies (Optional)
      writeSequenceOfStrings(content, "ontologies", sd.getAllOntologies());

      // ServiceDescription.ownership (Optional)
      if (sd.getOwnership() != null) {
        content.append(" :ownership "+sd.getOwnership());
      } 

      // ServiceDescription.properties (Optional)
      Iterator itp = sd.getAllProperties();

      // Can't use a primitive boolean as this is a static method
      Boolean  closeFlagProperties = null;

      if (itp.hasNext()) {
        content.append(" :properties (sequence");

        closeFlagProperties = new Boolean(true);
      } 
      else {
        closeFlagProperties = new Boolean(false);
      } 

      while (itp.hasNext()) {
        content.append(" ("+FIPAAgentManagementOntology.PROPERTY);

        Property prop = (Property) itp.next();

        // ServiceDescription.name (Mandatory)
        content.append(" :name "+prop.getName());

        // ServiceDescription.type (Mandatory)
        content.append(" :value "+prop.getValue());

        // Close Property object
        content.append(")");
      } 

      if (closeFlagProperties.booleanValue()) {
        content.append(")");    // Close sequence
      } 

      content.append(")");      // Close ServiceDescription object
    } 

    if (closeParenthesisFlag.booleanValue()) {
      content.append(")");    // Close sequence
    } 
  } 

}

