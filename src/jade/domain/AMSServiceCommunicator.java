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
     Register a AMSAgentDescription with a <b>AMS</b> agent. However, since <b>AMS</b> registration and
     deregistration are automatic in JADE, this method should not be
     used by application programmers to register with the default AMS.
     While this task can
     be accomplished with regular message passing according to
     <b>FIPA</b> protocols, this method is meant to ease this common
     duty.
     @param a is the Agent performing the registration (it is needed in order
     to send/receive messages
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
   * @see register(Agent,AID,AMSAgentDescription)
   **/
  public static void register(Agent a, AMSAgentDescription amsd) throws FIPAException {
    register(a,a.getAMS(),amsd);
  }

  /**
     Deregister a AMSAgentDescription from a <b>AMS</b> agent. However, since <b>AMS</b> registration and
     deregistration are automatic in JADE, this method should not be
     used by application programmers to deregister with the default AMS.
     While this task can
     be accomplished with regular message passing according to
     <b>FIPA</b> protocols, this method is meant to ease this common
     duty.
     @param AMSName The AID of the <b>AMS</b> agent to deregister from.
     @param amsd A <code>AMSAgentDescription</code> object containing all
     data necessary to the deregistration.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition.
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


  public static void deregister(Agent a, AMSAgentDescription amsd) throws FIPAException {
    deregister(a,a.getAMS(),amsd);
  }

  public static void deregister(Agent a, AID AMSName) throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(a.getAID());
    deregister(a,AMSName,amsd);
  }

  public static void deregister(Agent a) throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(a.getAID());
    deregister(a,amsd);
  }


  /**
     Modifies data contained within a <b>AMS</b>
     agent. While this task can be accomplished with regular message
     passing according to <b>FIPA</b> protocols, this method is
     meant to ease this common duty.
     @param a is the Agent performing the registration (it is needed in order
     to send/receive messages
     @param AMSName The GUID of the <b>AMS</b> agent holding the data
     to be changed.
     @param amsd A <code>AMSAgentDescriptor</code> object containing all
     new data values; every non null slot value replaces the
     corresponding value held inside the <b>AMS</b> agent.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the AMS to indicate some error condition.
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

  public static void modify(Agent a, AMSAgentDescription amsd) throws FIPAException {
    modify(a,a.getAMS(),amsd);
  }

  /**
     Searches for data contained within a <b>AMS</b> agent. While
     this task can be accomplished with regular message passing
     according to <b>FIPA</b> protocols, this method is meant to
     ease this common duty. Nevertheless, a complete, powerful search
     interface is provided; search constraints can be given and
     recursive searches are possible. The only shortcoming is that
     this method blocks the whole agent until the search terminates. A
     special <code>SearchAMSBehaviour</code> can be used to perform
     <b>AMS</b> searches without blocking.
     @param a is the Agent performing the registration (it is needed in order
     to send/receive messages
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



  public static List search(Agent a, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
    return search(a,a.getAMS(),amsd,constraints);
  }

  public static List search(Agent a, AMSAgentDescription amsd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return search(a,a.getAMS(),amsd,constraints);
  }

  public static List search(Agent a, AID AMSName, AMSAgentDescription amsd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return search(a,AMSName,amsd,constraints);
  }



  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID AMSName, String actionName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
    return new RequestFIPAServiceBehaviour(a,AMSName,actionName,amsd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, AMSAgentDescription amsd, SearchConstraints constraints) throws FIPAException {
    return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName) throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(a.getAID());
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName) throws FIPAException {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(a.getAID());
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,dfName,actionName,amsd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, AMSAgentDescription amsd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,a.getAMS(),actionName,amsd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName, AMSAgentDescription amsd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,dfName,actionName,amsd,constraints);
  }

}
