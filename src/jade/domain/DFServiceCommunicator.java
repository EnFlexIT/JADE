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
import jade.onto.OntologyException;
import jade.lang.Codec;
import jade.lang.sl.SL0Codec;
import jade.onto.Ontology;

/**
 * This class provides a set of static methods to communicate with
 * a DF Service that complies with FIPA specifications.
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
    if (dfd.getName()==null) 
      throw new MissingParameter(FIPAAgentManagementOntology.DFAGENTDESCRIPTION, "name");
    Iterator i = dfd.getAllServices();
    ServiceDescription sd;
    while (i.hasNext()) {
      sd = (ServiceDescription)i.next();
      if (sd.getName() == null)
	throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "name");
      if (sd.getType() == null)
	throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "type");
    }
  }

  /**
     Register a DFDescriptiont with a <b>DF</b> agent. While this task can
     be accomplished with regular message passing according to
     <b>FIPA</b> protocols, this method is meant to ease this common
     duty.
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
    ACLMessage request = createRequestMessage(a, dfName);

    if (dfd.getName() == null)
      dfd.setName(a.getAID());
    checkIsValid(dfd);

    // Build a DF action object for the request
    Register r = new Register();
    r.set_0(dfd);

    Action act = new Action();
    act.set_0(dfName);
    act.set_1(r);

    synchronized(c) { //must be synchronized because this is a static method
      request.setContent(encode(act,c,o));
    }
    // Send message and collect reply
    doFipaRequestClient(a,request);
  }


  /**
   * registers a <code>DFAgentDescription</code> with the default DF
   * @see #register(Agent,AID,DFAgentDescription)
   **/
  public static void register(Agent a, DFAgentDescription dfd) throws FIPAException {
    register(a,a.getDefaultDF(),dfd);
  }

  /**
     Deregister a DFAgentDescription from a <b>DF</b> agent. While this task can
     be accomplished with regular message passing according to
     <b>FIPA</b> protocols, this method is meant to ease this common
     duty.
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
    d.set_0(dfd);

    Action act = new Action();
    act.set_0(dfName);
    act.set_1(d);

    synchronized(c) { //must be synchronized because this is a static method
      request.setContent(encode(act,c,o));
    }

    // Send message and collect reply
    doFipaRequestClient(a,request);
  }


  public static void deregister(Agent a, DFAgentDescription dfd) throws FIPAException {
    deregister(a,a.getDefaultDF(),dfd);
  }

  public static void deregister(Agent a, AID dfName) throws FIPAException {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(a.getAID());
    deregister(a,dfName,dfd);
  }

  public static void deregister(Agent a) throws FIPAException {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(a.getAID());
    deregister(a,dfd);
  }


  /**
     Modifies data contained within a <b>DF</b>
     agent. While this task can be accomplished with regular message
     passing according to <b>FIPA</b> protocols, this method is
     meant to ease this common duty.
     @param a is the Agent performing the registration (it is needed in order
     to send/receive messages
     @param dfName The GUID of the <b>DF</b> agent holding the data
     to be changed.
     @param dfd A <code>DFAgentDescriptor</code> object containing all
     new data values; every non null slot value replaces the
     corresponding value held inside the <b>DF</b> agent.
     @exception FIPAException A suitable exception can be thrown when
     a <code>refuse</code> or <code>failure</code> messages are
     received from the DF to indicate some error condition.
  */
  public static void modify(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
    ACLMessage request = createRequestMessage(a, dfName);

    if (dfd.getName() == null)
      dfd.setName(a.getAID());
    checkIsValid(dfd);
    // Build a DF action object for the request
    Modify m = new Modify();
    m.set_0(dfd);

    Action act = new Action();
    act.set_0(dfName);
    act.set_1(m);

    synchronized(c) { //must be synchronized because this is a static method
      // Write the action in the :content slot of the request
      request.setContent(encode(act,c,o));
    }

    // Send message and collect reply
    doFipaRequestClient(a,request);
  }

  public static void modify(Agent a, DFAgentDescription dfd) throws FIPAException {
    modify(a,a.getDefaultDF(),dfd);
  }

  /**
     Searches for data contained within a <b>DF</b> agent. While
     this task can be accomplished with regular message passing
     according to <b>FIPA</b> protocols, this method is meant to
     ease this common duty. Nevertheless, a complete, powerful search
     interface is provided; search constraints can be given and
     recursive searches are possible. The only shortcoming is that
     this method blocks the whole agent until the search terminates. A
     special <code>SearchDFBehaviour</code> can be used to perform
     <b>DF</b> searches without blocking.
     @param a is the Agent performing the registration (it is needed in order
     to send/receive messages
     @param dfName The GUID of the <b>DF</b> agent to start search from.
     @param dfd A <code>DFAgentDescriptor</code> object containing
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
  public static List search(Agent a, AID dfName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    ACLMessage request = createRequestMessage(a, dfName);

    // Build a DF action object for the request
    Search s = new Search();
    s.set_0(dfd);
    s.set_1(constraints);

    Action act = new Action();
    act.set_0(dfName);
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
    Iterator i = r.getAll_1(); //this is the set of DFAgentDescription
    List l = new ArrayList(); 
    while (i.hasNext())
      l.add(i.next());
    return l; 
  }



  public static List search(Agent a, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    return search(a,a.getDefaultDF(),dfd,constraints);
  }

  public static List search(Agent a, DFAgentDescription dfd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return search(a,a.getDefaultDF(),dfd,constraints);
  }

  public static List search(Agent a, AID dfName, DFAgentDescription dfd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return search(a,dfName,dfd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    return new RequestFIPAServiceBehaviour(a,dfName,actionName,dfd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException {
    return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName) throws FIPAException {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(a.getAID());
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName) throws FIPAException {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(a.getAID());
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,dfName,actionName,dfd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, String actionName, DFAgentDescription dfd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,a.getDefaultDF(),actionName,dfd,constraints);
  }

  public static RequestFIPAServiceBehaviour getNonBlockingBehaviour(Agent a, AID dfName, String actionName, DFAgentDescription dfd) throws FIPAException {
    SearchConstraints constraints = new SearchConstraints();
    return getNonBlockingBehaviour(a,dfName,actionName,dfd,constraints);
  }

}

