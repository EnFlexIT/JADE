/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.net.InetAddress;

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import java.util.Hashtable; 

import jade.core.*;
import jade.core.behaviours.*;

import jade.core.event.PlatformEvent;
import jade.core.event.MTPEvent;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.Codec;
import jade.lang.sl.SL0Codec;

import jade.onto.Ontology;
import jade.onto.OntologyException;
import jade.onto.Frame;

import jade.onto.basic.Action;
import jade.onto.basic.BasicOntology;
import jade.onto.basic.ResultPredicate;
import jade.onto.basic.DonePredicate;
import jade.onto.basic.TrueProposition;

import jade.mtp.MTPException;

//__SECURITY__BEGIN
import jade.security.Authority;
import jade.security.JADEPrincipal;
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;
import jade.security.AuthException;
//__SECURITY__END

/**
  Standard <em>Agent Management System</em> agent. This class
  implements <em><b>FIPA</b></em> <em>AMS</em> agent. <b>JADE</b>
  applications cannot use this class directly, but interact with it
  through <em>ACL</em> message passing.


  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$


*/
public class ams extends Agent implements AgentManager.Listener {
    
    //registration of an agent.
    void AMSRegisterAction(Action a, AMSAgentDescription amsd,AID sender,String ontology)throws AlreadyRegistered,AuthException,MissingParameter {
			
	// This agent was created by some other, which is still
	// waiting for an 'inform' message. Recover the buffered
	// message from the Map and send it back.
	CreationInfo creation = (CreationInfo)creations.get(amsd.getName());
	//!!! michele removed next lines: now a global name is stored
	/*
	  // The message in creations can be registered with only the localName
	  // without the platformID
	  if (creation == null) {
	  String name = amsd.getName().getName();
	  int atPos = name.lastIndexOf('@');
	  if (atPos > 0) {
	  name = name.substring(0, atPos);
	  creation = (CreationInfo)creations.remove(name);
	  }
			}
	*/
	
	try {
	    // Write new agent data in AMS Agent Table
	    AMSRegister(amsd, sender);
	
	    // Inform agent creator that registration was successful.
	    if (creation != null && creation.getReply() != null) {
		send(creation.getReply());
	    }
	}
	catch (AlreadyRegistered are) {
	    //sendReply(ACLMessage.AGREE, createAgreeContent(a));
	    //String ontoName = getRequest().getOntology();
	    //sendReply(ACLMessage.FAILURE, createExceptionalMsgContent(a, ontoName, are));
	    // Inform agent creator that registration failed.
	    if (creation != null && creation.getReply() != null) {
		ACLMessage creationReply = creation.getReply();
		creationReply.setPerformative(ACLMessage.FAILURE);
		creationReply.setContent(createExceptionalContent(a, ontology, are));
		send(creationReply);
	    }
	    throw are;
	}catch(MissingParameter mp){
	    if (creation != null && creation.getReply() != null) {
		ACLMessage creationReply = creation.getReply();
		creationReply.setPerformative(ACLMessage.FAILURE);
		creationReply.setContent(createExceptionalContent(a, ontology, mp));
		send(creationReply);
	    }
	    throw mp;
	}
    }

    //return the APDescription.
    APDescription getDescriptionAction(){
	return theProfile;
    } 

  // These Behaviours handle interactions with platform tools.

  private class RegisterToolBehaviour extends CyclicBehaviour {

    private MessageTemplate subscriptionTemplate;

    RegisterToolBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEIntrospectionOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyWith("tool-subscription");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE);
      subscriptionTemplate = MessageTemplate.and(mt1, mt2);
      subscriptionTemplate = MessageTemplate.and(subscriptionTemplate, mt12);

    }

    public void action() {

      // Receive 'subscribe' ACL messages.
      ACLMessage current = receive(subscriptionTemplate);
      if(current != null) {
	// FIXME: Should parse 'iota ?x ...'

	// Get new tool name from subscription message
	AID newTool = current.getSender();

	try {

	  // Send back the whole container list.
	  ContainerID[] ids = myPlatform.containerIDs();
	  for(int i = 0; i < ids.length; i++) {

	    ContainerID cid = ids[i];

	    AddedContainer ac = new AddedContainer();
	    ac.setContainer(cid);
	    ac.setOwnership(getContainerPrincipal(cid).getOwnership());

	    EventRecord er = new EventRecord(ac, here());
	    Occurred o = new Occurred();
	    o.set_0(er);

	    List l = new ArrayList(1);
	    l.add(o);

	    toolNotification.clearAllReceiver();
	    toolNotification.addReceiver(newTool);
	    fillMsgContent(toolNotification, l);

	    send(toolNotification);

	  }

	  // Send all agent names, along with their container name.
	  AID[] agents = myPlatform.agentNames();
	  for (int i = 0; i < agents.length; i++) {

	    AID agentName = agents[i];
	    ContainerID cid = myPlatform.getContainerID(agentName);

	    BornAgent ba = new BornAgent();
	    ba.setAgent(agentName);
	    ba.setWhere(cid);
	    ba.setState(getAgentState(agentName));
	    ba.setOwnership(getAgentOwnership(agentName));

	    EventRecord er = new EventRecord(ba, here());
	    Occurred o = new Occurred();
	    o.set_0(er);

	    List l = new ArrayList(1);
	    l.add(o);

	    toolNotification.clearAllReceiver();
	    toolNotification.addReceiver(newTool);
	    fillMsgContent(toolNotification, l);

	    send(toolNotification);
	  }

	  // Send the list of the installed MTPs
	  String[] addresses = myPlatform.platformAddresses();
	  for(int i = 0; i < addresses.length; i++) {
	    AddedMTP amtp = new AddedMTP();
	    amtp.setAddress(addresses[i]);
	    amtp.setWhere(new ContainerID(AgentManager.MAIN_CONTAINER_NAME, null)); // FIXME: should use AgentManager to know the container

	    EventRecord er = new EventRecord(amtp, here());
	    Occurred o = new Occurred();
	    o.set_0(er);

	    List l = new ArrayList(1);
	    l.add(o);

	    toolNotification.clearAllReceiver();
	    toolNotification.addReceiver(newTool);
	    fillMsgContent(toolNotification, l);

	    send(toolNotification);
	  }

	  //Notification to the RMA of the APDescription
	   PlatformDescription ap = new PlatformDescription();
	   ap.setPlatform(theProfile);

	   EventRecord er = new EventRecord(ap,here());
	   Occurred o = new Occurred();
	   o.set_0(er);

	   List l = new ArrayList(1);
	   l.add(o);
	   toolNotification.clearAllReceiver();
	   toolNotification.addReceiver(newTool);
	   fillMsgContent(toolNotification, l);
	   send(toolNotification);

	  // Add the new tool to tools list.
	  tools.add(newTool);

	}
	catch(NotFoundException nfe) {
	  nfe.printStackTrace();
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
      }
      else
	block();

    }

  } // End of RegisterToolBehaviour class


  private class DeregisterToolBehaviour extends CyclicBehaviour {

    private MessageTemplate cancellationTemplate;

    DeregisterToolBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage(SL0Codec.NAME);
      MessageTemplate mt2 = MessageTemplate.MatchOntology(JADEIntrospectionOntology.NAME);
      MessageTemplate mt12 = MessageTemplate.and(mt1, mt2);

      mt1 = MessageTemplate.MatchReplyWith("tool-cancellation");
      mt2 = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
      cancellationTemplate = MessageTemplate.and(mt1, mt2);
      cancellationTemplate = MessageTemplate.and(cancellationTemplate, mt12);

    }

    public void action() {

      // Receive 'cancel' ACL messages.
      ACLMessage current = receive(cancellationTemplate);
      if(current != null) {
	// FIXME: Should parse the content

	// Remove this tool to tools agent group.
	tools.remove(current.getSender());

      }
      else
	block();

    }

  } // End of DeregisterToolBehaviour class

  private interface Handler {
      void handle(Event ev);
  }

  private class NotifyToolsBehaviour extends CyclicBehaviour {

    private Map handlers = new HashMap();

    public NotifyToolsBehaviour() {
        // Fill the handlers map with all the event handlers...
        handlers.put(AddedContainer.NAME, new Handler() {
            public void handle(Event ev) {

                AddedContainer ac = (AddedContainer)ev;
                ContainerID cid = ac.getContainer();
                String name = cid.getName();

                // Add a new location to the locations list
                mobilityMgr.addLocation(name, cid);
            }
        });
        handlers.put(RemovedContainer.NAME, new Handler() {
            public void handle(Event ev) {

                RemovedContainer rc = (RemovedContainer)ev;
                ContainerID cid = rc.getContainer();
                String name = cid.getName();

                // Remove the location from the location list
                mobilityMgr.removeLocation(name);
            }
        });
        handlers.put(BornAgent.NAME, new Handler() {
            public void handle(Event ev) {

                BornAgent ba = (BornAgent)ev;
                AID agentID = ba.getAgent();
                String ownership = ba.getOwnership();
                if(creations.get(agentID) == null)
                    creations.put(agentID, new CreationInfo(null, null, ownership, null));
            }
        });
        handlers.put(DeadAgent.NAME, new Handler() {
            public void handle(Event ev) {

                DeadAgent da = (DeadAgent)ev;
                AID agentID = da.getAgent();

		// Deregister the agent, if it's still there.
		try {
                    AMSAgentDescription amsd = new AMSAgentDescription();
                    amsd.setName(agentID);
                    AMSDeregister(amsd, agentID);
		}
		catch(NotRegistered nr) {
                    // the agent deregistered already during his doDelete() method.
		}
		catch(FIPAException fe) {
                    fe.printStackTrace();
		}
		catch(AuthException ae) {
                    ae.printStackTrace();
		}
		creations.remove(agentID);
            }
        });
        handlers.put(SuspendedAgent.NAME, new Handler() {
            public void handle(Event ev) {
                SuspendedAgent sa = (SuspendedAgent)ev;
                AID name = sa.getAgent();
		AMSAgentDescription amsd = (AMSAgentDescription)agentDescriptions.deregister(name);
		if (amsd != null) {
        	    // Registry needs an update here
                    amsd.setState(AMSAgentDescription.SUSPENDED);
                    agentDescriptions.register(name, amsd);
		}
            }
        });
        handlers.put(ResumedAgent.NAME, new Handler() {
            public void handle(Event ev) {
                ResumedAgent ra = (ResumedAgent)ev;
                AID name = ra.getAgent();
		AMSAgentDescription amsd = (AMSAgentDescription)agentDescriptions.deregister(name);
		if(amsd != null) {
                    // Registry needs an update here
                    amsd.setState(AMSAgentDescription.ACTIVE);
                    agentDescriptions.register(name, amsd);
		}
            }
        });
        handlers.put(ChangedAgentOwnership.NAME, new Handler() {
            public void handle(Event ev) {
                ChangedAgentOwnership cao = (ChangedAgentOwnership)ev;
                AID name = cao.getAgent();
                String ownership = cao.getTo();
		AMSAgentDescription amsd = (AMSAgentDescription)agentDescriptions.deregister(name);
		if (amsd != null) {
                    // Registry needs an update here
                    amsd.setOwnership(ownership);
                    agentDescriptions.register(name, amsd);
		}
            }
        });
        handlers.put(AddedMTP.NAME, new Handler() {
            public void handle(Event ev) {

                AddedMTP amtp = (AddedMTP)ev;
                String address = amtp.getAddress();
                String proto = amtp.getProto();

                // Add the new address to the platform profile
                APTransportDescription mtps = theProfile.getTransportProfile();
                MTPDescription desc = findMTPDescription(mtps, proto);
                desc.addAddresses(address);

                // Update the APDescription file.
                if(getState() != AP_INITIATED)
                    writeAPDescription();

                // Retrieve all agent descriptors
                AMSAgentDescription amsd = new AMSAgentDescription();
                List l = agentDescriptions.search(amsd);

                // Add the new address to all the agent descriptors
                Iterator it = l.iterator();
                while(it.hasNext()) {
                    AMSAgentDescription ad = (AMSAgentDescription)it.next();
                    AID name = ad.getName();
                    name.addAddresses(address);
                }
            }
        });
        handlers.put(RemovedMTP.NAME, new Handler() {
            public void handle(Event ev) {
                RemovedMTP rmtp = (RemovedMTP)ev;
                String address = rmtp.getAddress();
                String proto = rmtp.getProto();

                // Remove the dead address from the platform profile
                APTransportDescription mtps = theProfile.getTransportProfile();
                MTPDescription desc = findMTPDescription(mtps, proto);
                Iterator addresses = desc.getAllAddresses();
                while(addresses.hasNext()) {
                    // Remove all MTPs that have the 'address' String in their
                    // address list.
                    String nextAddr = (String)addresses.next();
                    if(nextAddr.equalsIgnoreCase(address))
                        addresses.remove();
                }

                // Check if there are other addresses left for this MTP: if not,
                // remove the MTP from the 'ap-platform-description' object
                addresses = desc.getAllAddresses();
                if(!addresses.hasNext())
                    mtps.removeAvailableMtps(desc);

                // Update the APDescription file
                writeAPDescription();

                // Remove the dead address from all the registered agents
                AID[] agents = myPlatform.agentNames();
                AMSAgentDescription amsd = new AMSAgentDescription();
                for(int i = 0; i < agents.length; i++) {
                    amsd.setName(agents[i]);
                    List l = agentDescriptions.search(amsd);
                    if(!l.isEmpty()) {
                        AMSAgentDescription amsDesc = (AMSAgentDescription)l.get(0);
                        AID name = amsDesc.getName();
                        name.removeAddresses(address);
                    }
                }
            }
        });

    }

    public void action() {

      synchronized(eventQueue) { // Mutual exclusion with handleXXX() methods to avoid ConcurrentModificationException

	// Look into the event buffer
	Iterator it = eventQueue.iterator();
	Occurred o = new Occurred();
	while(it.hasNext()) {

	  // Write the event into the notification message
	  EventRecord er = (EventRecord)it.next();
	  o.set_0(er);

          // Handle the event, updating AMS knowledge bases
          Event ev = er.getWhat();
          handleEvent(ev);

	  List l = new ArrayList(1);
	  l.add(o);
	  try {
	    fillMsgContent(toolNotification, l);
	  }
	  catch(FIPAException fe) {
	    fe.printStackTrace();
	  }

	  // Put all tools in the receiver list
	  toolNotification.clearAllReceiver();
	  Iterator toolIt = tools.iterator();

	  while(toolIt.hasNext()) {
	    AID tool = (AID)toolIt.next();
	    toolNotification.addReceiver(tool);
	  }

	  // Schedule the message send for later, when the critical
	  // section ends and this Behaviour yields the CPU...
	  addBehaviour(new SenderBehaviour(ams.this, (ACLMessage)toolNotification.clone()));

	  it.remove();
	}

      } // End of synchronized block

      block();

    }

    // Handle a given platform event, running the handler associated with the given event name
    private void handleEvent(Event ev) {
        Handler handler = (Handler)handlers.get(ev.getName());
        if(handler != null)
            handler.handle(ev);
    }

  } // End of NotifyToolsBehaviour class

 
    // when a AuthException occurs a Unauthorised exception is thrown
    void killContainerAction(KillContainer action,AID sender) throws Unauthorised, jade.domain.FIPAAgentManagement.InternalError{
	try{
	    //KillContainer kc = (KillContainer)a.get_1();
	    ContainerID cid = action.getContainer();
	    checkAction(Authority.CONTAINER_KILL, cid, sender);
	    myPlatform.killContainer(cid);
	    //sendReply(ACLMessage.AGREE, createAgreeContent(a));
	    //sendReply(ACLMessage.INFORM, doneAction(a));
	}
        catch(AuthException au) {
	    throw new Unauthorised();
	}
        catch(NotFoundException nfe) {
            throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");   
        }
    }

    //create an agent.
    ACLMessage createAgentAction(CreateAgent ca, ACLMessage request, ACLMessage reply) throws jade.domain.FIPAAgentManagement.InternalError, Unauthorised,AuthException{
	//CreateAgent ca = (CreateAgent)a.get_1();
	
	final String agentName = ca.getAgentName();
	final AID agentID = new AID(agentName, AID.ISLOCALNAME);
	final String className = ca.getClassName();
	final ContainerID container = ca.getContainer();
	Iterator arg = ca.getAllArguments(); //return an iterator of all arguments
	//create the array of string
	ArrayList listArg = new ArrayList();
	while (arg.hasNext())
	    listArg.add(arg.next().toString());
	final String[] arguments = new String[listArg.size()];
	for (int n = 0; n < listArg.size(); n++)
	    arguments[n] = (String)listArg.get(n);
	
	AID creator = request.getSender();
	final String ownership = getAgentOwnership(creator);
	
	Authority authority = getAuthority();
	AgentPrincipal agent = authority.createAgentPrincipal(agentID, ownership);
	
	DelegationCertificate creatorDelegation = (DelegationCertificate)delegations.get(creator.getName());
	
	final IdentityCertificate identity = authority.createIdentityCertificate();
	identity.setSubject(agent);
	authority.sign(identity, new CertificateFolder(getCertificateFolder().getIdentityCertificate(), creatorDelegation));
	
	final DelegationCertificate delegation = authority.createDelegationCertificate();
	if (ca.getDelegation() != null) {
	    delegation.decode(ca.getDelegation());
	}
	else {
	    delegation.setSubject(agent);
	    if (creatorDelegation != null)
		delegation.addPermissions(creatorDelegation.getPermissions());
	    authority.sign(delegation, new CertificateFolder(getCertificateFolder().getIdentityCertificate(), creatorDelegation));
	}
	final CertificateFolder agentCerts = new CertificateFolder(identity, delegation);
	
	//sendReply(ACLMessage.AGREE, createAgreeContent(a));
	
	try {
	    authority.doAsPrivileged(new jade.security.PrivilegedExceptionAction() {
		    public Object run() throws UnreachableException, AuthException {
			myPlatform.create(agentName, className, arguments, container, ownership, agentCerts);
			return null;
		    }
		}, new CertificateFolder(getCertificateFolder().getIdentityCertificate(), creatorDelegation));

	    // An 'inform Done' message will be sent to the requester only
	    // when the newly created agent will register itself with the
	    // AMS. The new agent's name will be used as the key in the map.
	    //ACLMessage reply = request.createReply();
	    //reply = (ACLMessage)reply.clone();
	    //reply.setPerformative(ACLMessage.INFORM);
	    //reply.setContent(doneAction(a));
	    
	    creations.put(agentID, new CreationInfo(request, reply, ownership, agentCerts));
	    return null;
	}
	catch (UnreachableException ue) {
	    throw new jade.domain.FIPAAgentManagement.InternalError(ue.getMessage());
	}
	catch (AuthException ae) {
	    //When this exception occurs a Refuse message will be sent with a Unauthorised Exception as content
	    //the exception with no parameter.
	    Unauthorised ue = new Unauthorised();
	    throw ue;	
	    //ACLMessage failure = getReply();
	    //failure.setPerformative(ACLMessage.FAILURE);
	    //failure.setContent(createExceptionalMsgContent(a, getRequest().getOntology(), new FIPAException(ae.getMessage())));
	    //send(failure);
	}
	catch (Exception e) {
	    // Never thrown
	    e.printStackTrace();
	    
	}
	return null;
    }

 
    //kill an agent.
    void killAgentAction(KillAgent action) throws jade.domain.FIPAAgentManagement.InternalError, NotRegistered, Unauthorised{
	
	// Kill an agent
      AID agentID = action.getAgent();
      String password = action.getPassword();

      try {
        myPlatform.kill(agentID);
	//sendReply(ACLMessage.AGREE, createAgreeContent(a));
        //sendReply(ACLMessage.INFORM, doneAction(a));
      }
      catch (UnreachableException ue) {
	  ue.printStackTrace();
        throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
      }
      catch (NotFoundException nfe) {
        throw new NotRegistered();
      }
      catch(AuthException au){
	  au.printStackTrace();
	  throw new Unauthorised();
      }

    }

    //performs the SniffOn action.
    void sniffOnAction(SniffOn action)throws jade.domain.FIPAAgentManagement.InternalError,NotRegistered{
	try {
	    myPlatform.sniffOn(action.getSniffer(), action.getCloneOfSniffedAgents());
	    //sendReply(ACLMessage.AGREE, createAgreeContent(a));
	    //sendReply(ACLMessage.INFORM, doneAction(a));
	}
	catch(UnreachableException ue) {
	    throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
	}
	catch(NotFoundException nfe) {
	    throw new NotRegistered();
	}
    }

    //performs the sniffOff aciton
    void sniffOffAction(SniffOff action)throws jade.domain.FIPAAgentManagement.InternalError,NotRegistered{
	try {
	    myPlatform.sniffOff(action.getSniffer(), action.getCloneOfSniffedAgents());
	    // sendReply(ACLMessage.AGREE, createAgreeContent(a));
	    //sendReply(ACLMessage.INFORM,doneAction(a));
	}
	catch(UnreachableException ue) {
	    throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
	}
	catch(NotFoundException nfe) {
	    throw new NotRegistered();
	}
    }


    //performs the debugOn action.
    void debugOnAction(DebugOn action)throws jade.domain.FIPAAgentManagement.InternalError,NotRegistered{
	try {
	    myPlatform.debugOn(action.getDebugger(), action.getCloneOfDebuggedAgents());
	    //   sendReply(ACLMessage.AGREE, createAgreeContent(a));
	    //sendReply(ACLMessage.INFORM, doneAction(a));
	}
	catch(UnreachableException ue) {
	    throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
	}
	catch(NotFoundException nfe) {
	    throw new NotRegistered();
	}
    }

 
    //performs s debugOff action
    void debugOffAction(DebugOff action)throws jade.domain.FIPAAgentManagement.InternalError,NotRegistered{
	try {
	    myPlatform.debugOff(action.getDebugger(), action.getCloneOfDebuggedAgents());
	    //sendReply(ACLMessage.AGREE, createAgreeContent(a));
	    //sendReply(ACLMessage.INFORM, doneAction(a));
	}
	catch(UnreachableException ue) {
	    throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
	}
	catch(NotFoundException nfe) {
	    throw new NotRegistered();
	}
    }

    //install new MTP.
    void installMTPAction(InstallMTP imtp) throws FailureException, RefuseException{
	try {
	    myPlatform.installMTP(imtp.getAddress(), imtp.getContainer(), imtp.getClassName());
	}
	catch(NotFoundException nfe) {
	    // throw new jade.domain.FIPAAgentManagement.UnrecognisedParameterValue("MTP", nfe.getMessage());
	    throw new jade.domain.FIPAAgentManagement.InternalError(nfe.getMessage());
	}
	catch(UnreachableException ue) {
	    throw new jade.domain.FIPAAgentManagement.InternalError(ue.getMessage());
	}
	catch(MTPException mtpe) {
	     throw new jade.domain.FIPAAgentManagement.UnrecognisedParameterValue("MTP", mtpe.getMessage());
	    }

    }  

    //unistall an MTP.
    void unistallMTPAction(UninstallMTP umtp) throws FailureException, RefuseException{
	try {
	    myPlatform.uninstallMTP(umtp.getAddress(), umtp.getContainer());
	}
	catch(NotFoundException nfe) {
	    throw new jade.domain.FIPAAgentManagement.UnrecognisedParameterValue("MTP", nfe.getMessage());	  
	}
	catch(UnreachableException ue) {
	    throw new jade.domain.FIPAAgentManagement.InternalError(ue.getMessage());
	}
	catch(MTPException mtpe) {
	    throw new jade.domain.FIPAAgentManagement.UnrecognisedParameterValue("MTP", mtpe.getMessage());
	}
    }

	private class CreationInfo {
		private ACLMessage request;
		private ACLMessage reply;
		private String ownership;
		private CertificateFolder certs;

		public CreationInfo(ACLMessage request, ACLMessage reply, String ownership, CertificateFolder certs) {
			this.request = request;
			this.reply = reply;
			this.ownership = ownership;
			this.certs = certs;
		}

		public ACLMessage getRequest() {
			return request;
		}

		public ACLMessage getReply() {
			return reply;
		}

		public String getOwnership() {
			return ownership;
		}

		public CertificateFolder getCertificateFolder() {
			return certs;
		}

	}

  // The AgentPlatform where information about agents is stored
  /**
  @serial
  */
  private AgentManager myPlatform;


  // Contains a main Behaviour and some utilities to handle JADE mobility
  /**
  @serial
  */
  private MobilityManager mobilityMgr;

  // Behaviour to listen to incoming 'subscribe' messages from tools.
  /**
  @serial
  */
  private RegisterToolBehaviour registerTool;

  // Behaviour to broadcats AgentPlatform notifications to each
  // registered tool.
  /**
  @serial
  */
  private NotifyToolsBehaviour notifyTools;

  // Behaviour to listen to incoming 'cancel' messages from tools.
  /**
  @serial
  */
  private DeregisterToolBehaviour deregisterTool;

  // Group of tools registered with this AMS
  /**
  @serial
  */
  private List tools;

  // ACL Message to use for tool notification
  /**
  @serial
  */
  private ACLMessage toolNotification = new ACLMessage(ACLMessage.INFORM);

  // Buffer for AgentPlatform notifications
  /**
  @serial
  */
  private List eventQueue = new ArrayList(10);

  /** The HashTable is synchronized, while the HashMap is not!
  @serial
  */
  private Hashtable creations = new Hashtable();

  /**
  @serial
  */
  private Map delegations = new HashMap();

  /**
  @serial
  */
  private Map containers = new HashMap();

  /**
  @serial
  */
  private APDescription theProfile = new APDescription();
    
    private AMSFipaAgentManagementBehaviour fipaResponderB;
    private AMSJadeAgentManagementBehaviour jadeResponderB;

  /**
     This constructor creates a new <em>AMS</em> agent. Since a direct
     reference to an Agent Platform implementation must be passed to
     it, this constructor cannot be called from application
     code. Therefore, no other <em>AMS</em> agent can be created
     beyond the default one.
  */
  public ams(AgentManager ap) {

    // Fill Agent Platform Profile with data.
    theProfile.setDynamic(new Boolean(false));
    theProfile.setMobility(new Boolean(false));
    APTransportDescription mtps = new APTransportDescription();
    theProfile.setTransportProfile(mtps);

    myPlatform = ap;
    myPlatform.addListener(this);

 
    MessageTemplate mtF = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME));
    fipaResponderB = new AMSFipaAgentManagementBehaviour(this,mtF);

 
    MessageTemplate mtJ = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME));
    jadeResponderB = new AMSJadeAgentManagementBehaviour(this,mtJ);

    mobilityMgr = new MobilityManager(this);
    registerTool = new RegisterToolBehaviour();
    deregisterTool = new DeregisterToolBehaviour();
    notifyTools = new NotifyToolsBehaviour();

    tools = new ArrayList();

    toolNotification.setSender(new AID());
    toolNotification.setLanguage(SL0Codec.NAME);
    toolNotification.setOntology(JADEIntrospectionOntology.NAME);
    toolNotification.setInReplyTo("tool-subscription");
   
  }

  /**
   This method starts the <em>AMS</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {

    // Fill the ':name' slot of the Agent Platform Profile with the Platform ID.
    theProfile.setName("\"" + getHap() + "\"");
    writeAPDescription();

    // Register the supported ontologies
    registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());
    registerOntology(JADEAgentManagementOntology.NAME, JADEAgentManagementOntology.instance());
    registerOntology(JADEIntrospectionOntology.NAME, JADEIntrospectionOntology.instance());
    registerOntology(MobilityOntology.NAME, MobilityOntology.instance());

    // register the supported languages
    registerLanguage(SL0Codec.NAME, new SL0Codec());

    // Add a Behaviour for all ams actions following from a
    // 'fipa-request' interaction with 'fipa-agent-management' ontology.
    addBehaviour(fipaResponderB);

    // Add a Behaviour for all ams actions following from a
    // 'fipa-request' interaction with 'jade-agent-management' ontology.
    addBehaviour(jadeResponderB);

    // Add a main behaviour to manage mobility related messages
    addBehaviour(mobilityMgr.getMain());

    // Add a Behaviour to accept incoming tool registrations and a
    // Behaviour to broadcast events to registered tools.
    addBehaviour(registerTool);
    addBehaviour(deregisterTool);
    addBehaviour(notifyTools);

  }

	public Authority getAuthority() {
		return myPlatform.getAuthority();
	}

	public void setDelegation(AID agent, DelegationCertificate delegation) {
		delegations.put(agent.getName(), delegation);
	}

  /**
  * checks that all the mandatory slots for a register/modify/deregister action
  * are present.
  * @param actionName is the name of the action (one of
  * <code>FIPAAgentManagementOntology.REGISTER</code>,
  * <code>FIPAAgentManagementOntology.MODIFY</code>,
  * <code>FIPAAgentManagementOntology.DEREGISTER</code>)
  * @param amsd is the AMSAgentDescription to be checked for
  * @throws MissingParameter if one of the mandatory slots is missing
  **/
   void checkMandatorySlots(String actionName, AMSAgentDescription amsd) throws MissingParameter {
    try {
      AID name = amsd.getName();
      if ((name == null)||(name.getName().length() == 0))
	throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "name");
    } catch (Exception e) {
      e.printStackTrace();
      throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "name");
    }
    if (!actionName.equalsIgnoreCase(FIPAAgentManagementOntology.DEREGISTER))
      try {
	String state = amsd.getState();
	if((state == null)||(state.length() == 0))
	  throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "state");
      } catch (Exception e) {
	e.printStackTrace();
	throw new MissingParameter(FIPAAgentManagementOntology.AMSAGENTDESCRIPTION, "state");
      }
  }

  /**
     @serial
   */
  private KB agentDescriptions = new KBAbstractImpl() {
      protected boolean match(Object template, Object fact) {
	try {
	  AMSAgentDescription templateDesc = (AMSAgentDescription)template;
	  AMSAgentDescription factDesc = (AMSAgentDescription)fact;

	  String o1 = templateDesc.getOwnership();
	  if(o1 != null) {
	    String o2 = factDesc.getOwnership();
	    if((o2 == null) || (!o1.equalsIgnoreCase(o2)))
	      return false;
	  }

	  String s1 = templateDesc.getState();
	  if(s1 != null) {
	    String s2 = factDesc.getState();
	    if((s2 == null) || (!s1.equalsIgnoreCase(s2)))
	      return false;
	  }

	  AID id1 = templateDesc.getName();
	  if(id1 != null) {
	    AID id2 = factDesc.getName();
	    if((id2 == null) || (!matchAID(id1, id2)))
	      return false;
	  }

	  return true;
	}
	catch (ClassCastException cce) {
	  return false;
	}
      }
    };

	/** it is called also by Agent.java **/
    //	public void AMSRegister(AMSAgentDescription amsd, AID sender) throws FIPAException, AuthException {
	public void AMSRegister(AMSAgentDescription amsd, AID sender) throws AlreadyRegistered, AuthException,MissingParameter {

	        checkMandatorySlots(FIPAAgentManagementOntology.REGISTER, amsd);
		AMSAgentDescription old = (AMSAgentDescription)agentDescriptions.deregister(amsd.getName());
		if (old != null) {
			agentDescriptions.register(old.getName(), old);
			throw new AlreadyRegistered();
		}

		CreationInfo creation = (CreationInfo)creations.remove(amsd.getName());

		old = new AMSAgentDescription();
		old.setName(amsd.getName());
		old.setState(AMSAgentDescription.ACTIVE);
		if (creation != null)
			old.setOwnership(creation.getOwnership());
		else
			old.setOwnership(AgentPrincipal.NONE);

		String[] addresses = myPlatform.platformAddresses();
		for (int i = 0; i < addresses.length; i++)
			amsd.getName().addAddresses(addresses[i]);

		AMSModify(Authority.AMS_REGISTER, old, amsd, sender, creation);
	}

	/** it is called also by Agent.java **/
 
    public void AMSDeregister(AMSAgentDescription amsd, AID sender) throws NotRegistered, AuthException,MissingParameter {
	checkMandatorySlots(FIPAAgentManagementOntology.DEREGISTER, amsd);
		AMSAgentDescription old = (AMSAgentDescription)agentDescriptions.deregister(amsd.getName());
		if (old == null)
			throw new NotRegistered();
		AMSModify(Authority.AMS_DEREGISTER, old, amsd, sender, null);
	}

 
    void AMSModify(AMSAgentDescription amsd, AID sender) throws NotRegistered, AuthException, MissingParameter {
		checkMandatorySlots(FIPAAgentManagementOntology.MODIFY, amsd);
		AMSAgentDescription old = (AMSAgentDescription)agentDescriptions.deregister(amsd.getName());
		if (old == null)
			throw new NotRegistered();
		agentDescriptions.register(old.getName(), old);
		AMSModify(Authority.AMS_MODIFY, old, amsd, sender, null);
	}

	private void AMSModify(String action, AMSAgentDescription old, AMSAgentDescription amsd, AID sender, CreationInfo creation) throws AuthException {
		Authority authority = getAuthority();
		final AID name = old.getName();

		String oldOwnership = old.getOwnership();
		if (oldOwnership == null)
			oldOwnership = AgentPrincipal.NONE;
		String newOwnership = amsd.getOwnership();
		if (newOwnership == null)
			newOwnership = oldOwnership;
		String oldState = old.getState();
		String newState = amsd.getState();

		amsd.setOwnership(oldOwnership);
		amsd.setState(oldState);

		AgentPrincipal oldAgent = authority.createAgentPrincipal(old.getName(), oldOwnership);

		final byte[] password = extractPassword(newOwnership);
		final String username = extractUsername(newOwnership);
		
		AgentPrincipal newAgent = authority.createAgentPrincipal(name, username);

		// we have to find the "subject" of this action
		IdentityCertificate actorIdentity = null;
		DelegationCertificate actorDelegation = null;
		// we use sender's delegation to ams
		actorIdentity = getCertificateFolder().getIdentityCertificate();
		actorDelegation = (DelegationCertificate)delegations.get(sender.getName());
		CertificateFolder actorCerts = new CertificateFolder(actorIdentity, actorDelegation);

		try {
			authority.checkAction(action, oldAgent, actorCerts);

			if (action.equals(Authority.AMS_DEREGISTER))
				return;

			// change agent principal
			if (! newOwnership.equals(oldOwnership) || password != null) {
				authority.doAsPrivileged(new jade.security.PrivilegedExceptionAction() {
					public Object run() throws NotFoundException, UnreachableException, AuthException {
						myPlatform.take(name, username, password);
						return null;
					}
				}, actorCerts);
				amsd.setOwnership(username);
			}
			agentDescriptions.register(amsd.getName(), amsd);
			old.setOwnership(amsd.getOwnership());

			// change agent state
			if (! oldState.equals(AMSAgentDescription.SUSPENDED) && newState.equals(AMSAgentDescription.SUSPENDED)) {
				authority.doAsPrivileged(new jade.security.PrivilegedExceptionAction() {
					public Object run() throws NotFoundException, UnreachableException, AuthException {
						myPlatform.suspend(name);
						return null;
					}
				}, actorCerts);
				amsd.setState(newState);
			}
			else if (oldState.equals(AMSAgentDescription.SUSPENDED) && ! newState.equals(AMSAgentDescription.SUSPENDED)) {
				authority.doAsPrivileged(new jade.security.PrivilegedExceptionAction() {
					public Object run() throws NotFoundException, UnreachableException, AuthException {
						myPlatform.activate(name);
						return null;
					}
				}, actorCerts);
				amsd.setState(newState);
			}
			agentDescriptions.register(amsd.getName(), amsd);
		}
		catch (NotFoundException ne) {
			ne.printStackTrace();
		}
		catch (UnreachableException ue) {
			ue.printStackTrace();
		}
		catch (AuthException ae) {
			agentDescriptions.register(old.getName(), old);
			throw ae;
		}
		catch (Exception e) {
			// Never thrown
			e.printStackTrace();
		}
	}

	List AMSSearch(AMSAgentDescription amsd, SearchConstraints constraints, ACLMessage reply, AID senderID) throws FIPAException, AuthException {
		// Search has no mandatory slots
		return agentDescriptions.search(amsd);
	}

	// This one is called in response to a 'move-agent' action
	void AMSMoveAgent(AID agent, Location where, AID sender) throws FIPAException, AuthException {
		checkAction(Authority.AGENT_MOVE, agent, sender);
		try {
			myPlatform.move(agent, where);
		}
		catch (UnreachableException ue) {
			throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
		}
		catch (NotFoundException nfe) {
			throw new NotRegistered();
		}
	}

	// This one is called in response to a 'clone-agent' action
	void AMSCloneAgent(AID agent, Location where, String newName, AID sender) throws FIPAException, AuthException {
		checkAction(Authority.AGENT_COPY, agent, sender);
		try {
			myPlatform.copy(agent, where, newName);
		}
		catch(UnreachableException ue) {
			throw new jade.domain.FIPAAgentManagement.InternalError("The container is not reachable");
		}
		catch(NotFoundException nfe) {
			throw new NotRegistered();
		}
	}


	// This one is called in response to a 'where-is-agent' action
	Location AMSWhereIsAgent(AID agent, AID sender) throws FIPAException, AuthException {
		try {
			ContainerID cid = myPlatform.getContainerID(agent);
			String containerName = cid.getName();
			return mobilityMgr.getLocation(containerName);
		}
		catch(NotFoundException nfe) {
			nfe.printStackTrace();
			throw new NotRegistered();
		}
	}

	void checkAction(String action, AID agent, AID sender) throws AuthException {
		getAuthority().checkAction(action, getAgentPrincipal(agent), new CertificateFolder(getCertificateFolder().getIdentityCertificate(), (DelegationCertificate)delegations.get(sender.getName())));
	}
	
	void checkAction(String action, ContainerID container, AID sender) throws AuthException {
		getAuthority().checkAction(action, getContainerPrincipal(container), new CertificateFolder(getCertificateFolder().getIdentityCertificate(), (DelegationCertificate)delegations.get(sender.getName())));
	}
	
	AgentPrincipal getAgentPrincipal(AID agent) {
		return getAuthority().createAgentPrincipal(agent, getAgentOwnership(agent));
	}

	ContainerPrincipal getContainerPrincipal(ContainerID container) {
		ContainerPrincipal principal = (ContainerPrincipal)containers.get(container);
		//!!!
		if (principal == null) {
			Authority authority = getAuthority();
			principal = authority.createContainerPrincipal(container, ContainerPrincipal.NONE);
		}
		return principal;
	}

	String getAgentOwnership(AID agent) {
		AMSAgentDescription amsd = new AMSAgentDescription();
		amsd.setName(new AID(agent.getName(), AID.ISGUID));
		List l = agentDescriptions.search(amsd);
		if (l.size() == 0)
			return AgentPrincipal.NONE;

		amsd = (AMSAgentDescription)l.get(0);
		return amsd.getOwnership();
	}
	
	String getAgentState(AID agent) {
		AMSAgentDescription amsd = new AMSAgentDescription();
		amsd.setName(new AID(agent.getName(), AID.ISGUID));
		List l = agentDescriptions.search(amsd);
		if (l.size() == 0)
			return AMSAgentDescription.ACTIVE;

		amsd = (AMSAgentDescription)l.get(0);
		return amsd.getState();
	}
	
  // Methods to be called from AgentPlatform to notify AMS of special events

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public void addedContainer(PlatformEvent ev) {

    ContainerID cid = ev.getContainer();
    String name = cid.getName();

    // Fire an 'added container' event
    AddedContainer ac = new AddedContainer();
    ac.setContainer(cid);

    EventRecord er = new EventRecord(ac, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public void removedContainer(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    String name = cid.getName();

    // Fire a 'container is dead' event
    RemovedContainer rc = new RemovedContainer();
    rc.setContainer(cid);

    EventRecord er = new EventRecord(rc, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public void bornAgent(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    AID agentID = ev.getAgent();
    String ownership = ((AgentPrincipal)ev.getNewPrincipal()).getOwnership();

    BornAgent ba = new BornAgent();
    ba.setAgent(agentID);
    ba.setWhere(cid);
    ba.setState(AMSAgentDescription.ACTIVE);
    ba.setOwnership(ownership);

    EventRecord er = new EventRecord(ba, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }
    doWake();
  }

    /**
       Post an event to the AMS agent. This method must not be used by
        application agents.
    */
    public void deadAgent(PlatformEvent ev) {
        ContainerID cid = ev.getContainer();
        AID agentID = ev.getAgent();

        DeadAgent da = new DeadAgent();
        da.setAgent(agentID);
        da.setWhere(cid);

        EventRecord er = new EventRecord(da, here());
	er.setWhen(ev.getTime());
	synchronized (eventQueue) {
            eventQueue.add(er);
	}
        doWake();
    }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public void suspendedAgent(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    AID name = ev.getAgent();

    SuspendedAgent sa = new SuspendedAgent();
    sa.setAgent(name);
    sa.setWhere(cid);

    EventRecord er = new EventRecord(sa, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public void resumedAgent(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    AID name = ev.getAgent();

    ResumedAgent ra = new ResumedAgent();
    ra.setAgent(name);
    ra.setWhere(cid);

    EventRecord er = new EventRecord(ra, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }
    doWake();
  }

	/**
		Post an event to the AMS agent. This method must not be used by
		application agents.
	*/
	public void changedAgentPrincipal(PlatformEvent ev) {
            ContainerID cid = ev.getContainer();
            AID name = ev.getAgent();

            ChangedAgentOwnership cao = new ChangedAgentOwnership();
            cao.setAgent(name);
            cao.setWhere(cid);
            cao.setFrom(((AgentPrincipal)ev.getOldPrincipal()).getOwnership());
            cao.setTo(((AgentPrincipal)ev.getNewPrincipal()).getOwnership());

            EventRecord er = new EventRecord(cao, here());
            er.setWhen(ev.getTime());
            synchronized (eventQueue) {
                eventQueue.add(er);
            }
            doWake();
	}

	/**
		Post an event to the AMS agent. This method must not be used by
		application agents.
	*/
	public synchronized void changedContainerPrincipal(PlatformEvent ev) {
            ContainerID cid = ev.getContainer();
            containers.put(cid, ev.getNewPrincipal());
	}

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public void movedAgent(PlatformEvent ev) {
    ContainerID from = ev.getContainer();
    ContainerID to = ev.getNewContainer();
    AID agentID = ev.getAgent();

    MovedAgent ma = new MovedAgent();
    ma.setAgent(agentID);
    ma.setFrom(from);
    ma.setTo(to);

    EventRecord er = new EventRecord(ma, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }
    doWake();
  }

  /**
    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void addedMTP(MTPEvent ev) {
    Channel ch = ev.getChannel();
    ContainerID cid = ev.getPlace();
    String proto = ch.getProtocol();
    String address = ch.getAddress();

    // Generate a suitable AMS event
    AddedMTP amtp = new AddedMTP();
    amtp.setAddress(address);
    amtp.setProto(proto);
    amtp.setWhere(cid);

    EventRecord er = new EventRecord(amtp, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }

    //Notify the update of the APDescription...
    PlatformDescription ap = new PlatformDescription();
    ap.setPlatform(theProfile);
    er = new EventRecord(ap, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }

    doWake();

  }

  /**    Post an event to the AMS agent. This method must not be used by
    application agents.
  */
  public synchronized void removedMTP(MTPEvent ev) {

    Channel ch = ev.getChannel();
    ContainerID cid = ev.getPlace();
    String proto = ch.getProtocol();
    String address = ch.getAddress();

    // Generate a suitable AMS event
    RemovedMTP rmtp = new RemovedMTP();
    rmtp.setAddress(address);
    rmtp.setProto(proto);
    rmtp.setWhere(cid);

    EventRecord er = new EventRecord(rmtp, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }

    //Notify the update of the APDescription...
    PlatformDescription ap = new PlatformDescription();
    ap.setPlatform(theProfile);
    er = new EventRecord(ap, here());
    er.setWhen(ev.getTime());
    synchronized (eventQueue) {
	eventQueue.add(er);
    }

    doWake();

  }

  public void messageIn(MTPEvent ev) { System.out.println("Message In."); }
  public void messageOut(MTPEvent ev) { System.out.println("Message Out."); }



 private void writeAPDescription()
  {
  	 //Write the APDescription file.
    try{
    	FileWriter f = new FileWriter("APDescription.txt");
	f.write(theProfile.toString());
	//f.write(s, 0, s.length());
	f.write('\n');
	f.flush();
    	f.close();
    }catch(java.io.IOException ioe){ioe.printStackTrace();}


  }

  private MTPDescription findMTPDescription(APTransportDescription mtps, String proto) {
    Iterator it = mtps.getAllAvailableMtps();
    while(it.hasNext()) {
      MTPDescription desc = (MTPDescription)it.next();
      if(proto.equalsIgnoreCase(desc.getMtpName()))
	return desc;
    }

    // No MTP was found: create a new one and add it to the
    // 'ap-transport-description' object.
    MTPDescription desc = new MTPDescription();
    desc.setMtpName(proto);
    mtps.addAvailableMtps(desc);
    return desc;

  }


    //FIXME: it's only used into the registerAction when a failure occurs into the registration of an agent.
/**
     * Create the content for a so-called "exceptional" message, i.e.
     * one of NOT_UNDERSTOOD, FAILURE, REFUSE message
     * @param a is the Action that generated the exception
     * @param e is the generated Exception
     * @return a String containing the content to be sent back in the reply
     * message; in case an exception is thrown somewhere, the method
     * try to return anyway a valid content with a best-effort strategy
     **/
    protected String createExceptionalContent(Action a, String ontoName, FIPAException e) {
	ACLMessage temp = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
	temp.setLanguage(SL0Codec.NAME);
	temp.setOntology(ontoName);
	List l = new ArrayList(2);
	if (a == null) {
	    a = new Action();
	    a.set_0(getAID());
	    a.set_1("UnknownAction");
	}
	l.add(a);
	l.add(e);
	try {
	    fillMsgContent(temp,l);
	} catch (Exception ee) { // in any case try to return some good content
	    return e.getMessage();
	}
	return temp.getContent();
    }
} // End of class ams
