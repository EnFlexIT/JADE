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

import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Set;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.InputQueue; 

import java.util.Hashtable; 

import jade.core.*;
import jade.core.behaviours.*;

import jade.core.event.PlatformEvent;
import jade.core.event.MTPEvent;

import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;
import jade.domain.mobility.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.lang.Codec.*;
import jade.content.onto.basic.Action;

import jade.mtp.MTPException;
import jade.mtp.MTPDescriptor;

import jade.security.Authority;
import jade.security.JADEPrincipal;
import jade.security.AgentPrincipal;
import jade.security.ContainerPrincipal;
import jade.security.IdentityCertificate;
import jade.security.DelegationCertificate;
import jade.security.CertificateFolder;
import jade.security.AuthException;
import jade.security.CertificateException;
import jade.security.PrivilegedExceptionAction;

/**
  Standard <em>Agent Management System</em> agent. This class
  implements <em><b>FIPA</b></em> <em>AMS</em> agent. <b>JADE</b>
  applications cannot use this class directly, but interact with it
  through <em>ACL</em> message passing.

  @author Giovanni Rimassa - Universita` di Parma
  @author Giovanni Caire - TILAB
  @version $Date$ $Revision$
*/
public class ams extends Agent implements AgentManager.Listener {

  Profile bootProfile = null;    
  
  // The AgentPlatform where information about agents is stored
  private AgentManager myPlatform;

  // The codec for the SL language
  private Codec codec = new SLCodec();
   
  // Group of tools registered with this AMS
  private List tools = new ArrayList();

  // ACL Message to use for tool notification
  private ACLMessage toolNotification = new ACLMessage(ACLMessage.INFORM);

  // Buffer for AgentPlatform notifications
  private InputQueue eventQueue = new InputQueue();

  private Hashtable pendingDeadAgents = new Hashtable();
  private Hashtable pendingClonedAgents = new Hashtable();
  private Hashtable pendingMovedAgents = new Hashtable();
  private Hashtable pendingRemovedContainers = new Hashtable();

  private APDescription theProfile = new APDescription();
      
  /**
     This constructor creates a new <em>AMS</em> agent. Since a direct
     reference to an Agent Platform implementation must be passed to
     it, this constructor cannot be called from application
     code. Therefore, no other <em>AMS</em> agent can be created
     beyond the default one.
  */
  public ams(AgentManager ap, Profile aBootProfile) {
		System.out.println("New AMS active");
    myPlatform = ap;
    myPlatform.addListener(this);
    bootProfile = aBootProfile;
  }

  /**
     AMS initialization
   */
  protected void setup() {
    // Fill Agent Platform Description. 
    theProfile.setName("\"" + getHap() + "\"");
    theProfile.setDynamic(new Boolean(false));
    theProfile.setMobility(new Boolean(false));
    APTransportDescription mtps = new APTransportDescription();
    theProfile.setTransportProfile(mtps);
    writeAPDescription(theProfile);
    
    // Register the supported ontologies
    getContentManager().registerOntology(FIPAManagementOntology.getInstance());
    getContentManager().registerOntology(JADEManagementOntology.getInstance());
    getContentManager().registerOntology(IntrospectionOntology.getInstance());
    // Use fully qualified name to avoid conflict with old jade.domain.MobilityOntology
    getContentManager().registerOntology(jade.domain.mobility.MobilityOntology.getInstance());

    // register the supported languages: all profiles of SL are ok for ams
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL1);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL2);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);	
		
    // The behaviour managing FIPA requests
    MessageTemplate mtF = MessageTemplate.and(
    	MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
    	MessageTemplate.MatchOntology(FIPAManagementVocabulary.NAME));
    Behaviour fipaResponderB = new AMSFipaAgentManagementBehaviour(this, mtF);
    addBehaviour(fipaResponderB);

    // The behaviour managing JADE requests
    // MobilityOntology is matched for JADE 2.5 Backward compatibility
    MessageTemplate mtJ = MessageTemplate.and(
    	MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
    	MessageTemplate.or(
    		MessageTemplate.MatchOntology(JADEManagementVocabulary.NAME), 
    		MessageTemplate.MatchOntology(jade.domain.mobility.MobilityOntology.NAME)));
    Behaviour jadeResponderB = new AMSJadeAgentManagementBehaviour(this, mtJ);
    addBehaviour(jadeResponderB);

    // The behaviours dealing with platform tools
    Behaviour registerTool = new RegisterToolBehaviour();
    Behaviour deregisterTool = new DeregisterToolBehaviour();
    Behaviour eventManager = new EventManager();
    addBehaviour(registerTool);
    addBehaviour(deregisterTool);
    addBehaviour(eventManager);
    eventQueue.associate(eventManager);

    // Initialize the message used for tools notification
    toolNotification.setLanguage("FIPA-SL0");
    toolNotification.setOntology(IntrospectionOntology.NAME);
    toolNotification.setInReplyTo("tool-subscription");   
  }

  //////////////////////////////////////////////////////////////////
  // Methods implementing the actions of the JADEManagementOntology.
  // All these methods 
  // - extract the necessary information from the requested Action 
  //   object and format them properly (if necessary)
  // - Call the corresponding method of the AgentManager using the 
  //   permissions of the requester agent.
  // - Convert eventual JADE-internal Exceptions into proper FIPAException.
  // These methods are package-scoped as they are called by the 
  // AMSJadeAgentManagementBehaviour.
  //////////////////////////////////////////////////////////////////
  
	// CREATE AGENT
	void createAgentAction(CreateAgent ca, AID requester) throws FIPAException {
		final String agentName = ca.getAgentName();
		final AID agentID = new AID(agentName, AID.ISLOCALNAME);
		final String className = ca.getClassName();
		final ContainerID container = ca.getContainer();
		// Prepare arguments as a String[]
		Iterator it = ca.getAllArguments(); 
		ArrayList listArg = new ArrayList();
		while (it.hasNext()) {
			listArg.add(it.next().toString());
		}
		final String[] args = new String[listArg.size()];
		for (int n = 0; n < listArg.size(); n++) {
			args[n] = (String)listArg.get(n);
		}
	
		try {
			// IdentityCertificate: The new agent will have the same 
			// ownership as the requester
			final String ownership = getAgentOwnership(requester);
		
			Authority authority = getAuthority();
			AgentPrincipal agentPrincipal = authority.createAgentPrincipal(agentID, ownership);
		  CertificateFolder requesterCredentials = myPlatform.getAMSDelegation(requester);
			IdentityCertificate identity = authority.createIdentityCertificate();
			identity.setSubject(agentPrincipal);
			authority.sign(identity, requesterCredentials);
		
			// DelegationCertificate: The new agent will have the same 
			// permissions of the requester unless the requester specified 
			// a restricted set of permissions 
			DelegationCertificate delegation = null;
			if (ca.getDelegation() != null) {
				// Restricted set of permissions
				delegation = authority.createDelegationCertificate(ca.getDelegation());
			}
			else {
				// All requester permissions
				delegation = authority.createDelegationCertificate();
				delegation.setSubject(agentPrincipal);
				DelegationCertificate requesterDelegation = (DelegationCertificate) requesterCredentials.getDelegationCertificates().get(0);
				delegation.addPermissions(requesterDelegation.getPermissions());
				authority.sign(delegation, requesterCredentials);
			}

			final CertificateFolder agentCerts = new CertificateFolder(identity, delegation);
	
	    authority.doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws UnreachableException, AuthException {
					myPlatform.create(agentName, className, args, container, ownership, agentCerts);
					return null;
		    }
			}, requesterCredentials);
		}
		catch(CertificateException ce) {
			throw new Unauthorised();
		}
		catch(AuthException ae) {
			log("Agent "+requester.getName()+" does not have permission to perform action CreateAgent");
			throw new Unauthorised();
		}
		catch (UnreachableException ue) {
	    throw new InternalError("Destination container unreachable. "+ue.getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
	    throw new InternalError("Unexpected exception. "+e.getMessage());
		}
	}
	
	// KILL AGENT
	void killAgentAction(KillAgent ka, AID requester) throws FIPAException {
  	final AID agentID = ka.getAgent();
	  CertificateFolder requesterCredentials = myPlatform.getAMSDelegation(requester);

    try {
	    getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws UnreachableException, AuthException, NotFoundException {
        	myPlatform.kill(agentID);
					return null;
		    }
			}, requesterCredentials); 

    }
		catch(AuthException ae) {
			log("Agent "+requester.getName()+" does not have permission to perform action KillAgent");
			throw new Unauthorised();
		}
    catch (UnreachableException ue) {
      throw new InternalError("Container not reachable. "+ue.getMessage());
    }
    catch (NotFoundException nfe) {
      throw new InternalError("Agent not found. "+nfe.getMessage());
    }
 		catch (Exception e) {
			e.printStackTrace();
    	throw new InternalError("Unexpected exception. "+e.getMessage());   
	  }
	}
	
	// CLONE AGENT
	void cloneAgentAction(CloneAction ca, AID requester) throws FIPAException {
		MobileAgentDescription dsc = ca.getMobileAgentDescription();
		final AID agentID = dsc.getName();
		final ContainerID where = (ContainerID) dsc.getDestination();
		final String newName = ca.getNewName();
	  CertificateFolder requesterCredentials = myPlatform.getAMSDelegation(requester);
		
	  try {
	    getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws UnreachableException, AuthException, NotFoundException {
					myPlatform.copy(agentID, where, newName);
					return null;
		    }
			}, requesterCredentials);
		}
		catch(AuthException ae) {
			log("Agent "+requester.getName()+" does not have permission to perform action CloneAgent");
			throw new Unauthorised();
		}
    catch (UnreachableException ue) {
      throw new InternalError("Container not reachable. "+ue.getMessage());
    }
    catch (NotFoundException nfe) {
      throw new InternalError("NotFoundException. "+nfe.getMessage());
    }
 		catch (Exception e) {
			e.printStackTrace();
    	throw new InternalError("Unexpected exception. "+e.getMessage());   
	  }
	}
	
	// MOVE AGENT
	void moveAgentAction(MoveAction ma, AID requester) throws FIPAException {
		MobileAgentDescription dsc = ma.getMobileAgentDescription();
		final AID agentID = dsc.getName();
		final ContainerID where = (ContainerID) dsc.getDestination();
	  CertificateFolder requesterCredentials = myPlatform.getAMSDelegation(requester);
		
	  try {
	    getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws UnreachableException, AuthException, NotFoundException {
					myPlatform.move(agentID, where);
					return null;
		    }
			}, requesterCredentials);
		}
		catch(AuthException ae) {
			log("Agent "+requester.getName()+" does not have permission to perform action MoveAgent");
			throw new Unauthorised();
		}
    catch (UnreachableException ue) {
      throw new InternalError("Container not reachable. "+ue.getMessage());
    }
    catch (NotFoundException nfe) {
      throw new InternalError("NotFoundException. "+nfe.getMessage());
    }
 		catch (Exception e) {
			e.printStackTrace();
    	throw new InternalError("Unexpected exception. "+e.getMessage());   
	  }
	}
	
	// KILL CONTAINER
	void killContainerAction(KillContainer kc, AID requester) throws FIPAException {
    final ContainerID cid = kc.getContainer();
    CertificateFolder requesterCredentials = myPlatform.getAMSDelegation(requester);
		
    try{
	    getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws AuthException, NotFoundException {
	    		myPlatform.killContainer(cid);
					return null;
		    }
			}, requesterCredentials);
		}
		catch(AuthException ae) {
			log("Agent "+requester.getName()+" does not have permission to perform action KillContainer");
			throw new Unauthorised();
		}
    catch(NotFoundException nfe) {
    	throw new InternalError("Container not found. "+nfe.getMessage());   
    }
    catch(Exception e) {
			e.printStackTrace();
    	throw new InternalError("Unexpected exception. "+e.getMessage());   
    }
	}
	
	// INSTALL MTP
	MTPDescriptor installMTPAction(InstallMTP im, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
		try {
	    return myPlatform.installMTP(im.getAddress(), im.getContainer(), im.getClassName());
		}
		catch(NotFoundException nfe) {
	    throw new InternalError("Container not found. "+nfe.getMessage());
		}
		catch(UnreachableException ue) {
	    throw new InternalError("Container unreachable. "+ue.getMessage());
		}
		catch(MTPException mtpe) {
	    throw new InternalError("Error in MTP installation. "+mtpe.getMessage());
	  }
	}
	
	// UNINSTALL MTP
	void uninstallMTPAction(UninstallMTP um, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
		try {
	    myPlatform.uninstallMTP(um.getAddress(), um.getContainer());
		}
		catch(NotFoundException nfe) {
	    throw new InternalError("Container not found. "+nfe.getMessage());
		}
		catch(UnreachableException ue) {
	    throw new InternalError("Container unreachable. "+ue.getMessage());
		}
		catch(MTPException mtpe) {
	    throw new InternalError("Error in MTP de-installation. "+mtpe.getMessage());
		}
	}
	
	// SNIFF ON
	void sniffOnAction(SniffOn so, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
		try {
	    myPlatform.sniffOn(so.getSniffer(), so.getCloneOfSniffedAgents());
		}
		catch(NotFoundException nfe) {
	    throw new InternalError("Agent not found. "+nfe.getMessage());
		}
		catch(UnreachableException ue) {
	    throw new InternalError("Container unreachable. "+ue.getMessage());
		}
	}
	
	// SNIFF OFF
	void sniffOffAction(SniffOff so, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
		try {
	    myPlatform.sniffOff(so.getSniffer(), so.getCloneOfSniffedAgents());
		}
		catch(NotFoundException nfe) {
	    throw new InternalError("Agent not found. "+nfe.getMessage());
		}
		catch(UnreachableException ue) {
	    throw new InternalError("Container unreachable. "+ue.getMessage());
		}
	}
	
	// DEBUG ON
	void debugOnAction(DebugOn don, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
		try {
	    myPlatform.debugOn(don.getDebugger(), don.getCloneOfDebuggedAgents());
		}
		catch(NotFoundException nfe) {
	    throw new InternalError("Agent not found. "+nfe.getMessage());
		}
		catch(UnreachableException ue) {
	    throw new InternalError("Container unreachable. "+ue.getMessage());
		}
	}
	
	// DEBUG OFF
	void debugOffAction(DebugOff doff, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
		try {
	    myPlatform.debugOff(doff.getDebugger(), doff.getCloneOfDebuggedAgents());
		}
		catch(NotFoundException nfe) {
	    throw new InternalError("Agent not found. "+nfe.getMessage());
		}
		catch(UnreachableException ue) {
	    throw new InternalError("Container unreachable. "+ue.getMessage());
		}
	}
	
	// WHERE IS AGENT
	Location whereIsAgentAction(WhereIsAgentAction wia, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
		try {
	    return myPlatform.getContainerID(wia.getAgentIdentifier());
		}
		catch(NotFoundException nfe) {
	    throw new InternalError("Agent not found. "+nfe.getMessage());
		}
	}
	
	// QUERY PLATFORM LOCATIONS
	List queryPlatformLocationsAction(QueryPlatformLocationsAction qpl, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
	  ContainerID[] ids = myPlatform.containerIDs();
	  List l = new ArrayList();
	  for (int i = 0; i < ids.length; ++i) {
	  	l.add(ids[i]);
	  }
	  return l;
	}
	
	// QUERY AGENTS ON LOCATION
	List queryAgentsOnLocationAction(QueryAgentsOnLocation qaol, AID requester) throws FIPAException {
		// FIXME: Permissions for this action are not yet defined
		try {
	    return myPlatform.containerAgents((ContainerID) qaol.getLocation());
		}
		catch(NotFoundException nfe) {
	    throw new InternalError("Location not found. "+nfe.getMessage());
		}
	}
    
  //////////////////////////////////////////////////////////////////
  // Methods implementing the actions of the FIPAManagementOntology.
  // All these methods 
  // - extract the necessary information from the requested Action 
  //   object and check whether mandatory slots are present.
  // - Call the corresponding method of the AgentManager using the 
  //   permissions of the requester agent.
  // - Convert eventual JADE-internal Exceptions into proper FIPAException.
  // These methods are package-scoped as they are called by the 
  // AMSFipaAgentManagementBehaviour.
  //////////////////////////////////////////////////////////////////
      
  // REGISTER
  void registerAction(Register r, AID requester) throws FIPAException {
    final AMSAgentDescription amsd = (AMSAgentDescription) r.getDescription();
    // Check mandatory slots
    AID id = amsd.getName();
    log("Agent "+id+" registering with the AMS");
    if (id == null || id.getName() == null || id.getName().length() == 0) {
			throw new MissingParameter(FIPAManagementVocabulary.AMSAGENTDESCRIPTION, FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
    }
    
  	CertificateFolder requesterCredentials = myPlatform.getAMSDelegation(requester);
		
    try{
	    getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws AlreadyRegistered, AuthException {
	    		myPlatform.amsRegister(amsd);
					return null;
		    }
			}, requesterCredentials);
		}
		catch(AlreadyRegistered ar) {
			throw ar;
		}
		catch(AuthException ae) {
			log("Agent "+requester.getName()+" does not have permission to perform action Register");
			throw new Unauthorised();
		}
    catch(Exception e) {
			e.printStackTrace();
    	throw new InternalError("Unexpected exception. "+e.getMessage());   
    }
  }

  // DEREGISTER
  void deregisterAction(Deregister d, AID requester) throws FIPAException {
    final AMSAgentDescription amsd = (AMSAgentDescription) d.getDescription();
    // Check mandatory slots
    AID id = amsd.getName();
    log("Agent "+id+" de-registering with the AMS");
    if (id == null || id.getName() == null || id.getName().length() == 0) {
			throw new MissingParameter(FIPAManagementVocabulary.AMSAGENTDESCRIPTION, FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
    }
    
  	CertificateFolder requesterCredentials = myPlatform.getAMSDelegation(requester);
		
    try{
	    getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws NotRegistered, AuthException {
	    		myPlatform.amsDeregister(amsd);
					return null;
		    }
			}, requesterCredentials);
		}
		catch(NotRegistered nr) {
			throw nr;
		}
		catch(AuthException ae) {
			log("Agent "+requester.getName()+" does not have permission to perform action Deregister");
			throw new Unauthorised();
		}
    catch(Exception e) {
			e.printStackTrace();
    	throw new InternalError("Unexpected exception. "+e.getMessage());   
    }
  }

  // MODIFY
  void modifyAction(Modify m, AID requester) throws FIPAException {
    final AMSAgentDescription amsd = (AMSAgentDescription) m.getDescription();
    // Check mandatory slots
    AID id = amsd.getName();
    if (id == null || id.getName() == null || id.getName().length() == 0) {
			throw new MissingParameter(FIPAManagementVocabulary.AMSAGENTDESCRIPTION, FIPAManagementVocabulary.DFAGENTDESCRIPTION_NAME);
    }
    
  	CertificateFolder requesterCredentials = myPlatform.getAMSDelegation(requester);
		
    try{
	    getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
		    public Object run() throws NotRegistered, NotFoundException, UnreachableException, AuthException {
	    		myPlatform.amsModify(amsd);
					return null;
		    }
			}, requesterCredentials);
		}
		catch(NotRegistered nr) {
			throw nr;
		}
    catch (UnreachableException ue) {
      throw new InternalError("Container not reachable. "+ue.getMessage());
    }
    catch (NotFoundException nfe) {
      throw new InternalError("Agent not found. "+nfe.getMessage());
    }
		catch(AuthException ae) {
			log("Agent "+requester.getName()+" does not have permission to perform action Modify");
			throw new Unauthorised();
		}
    catch(Exception e) {
			e.printStackTrace();
    	throw new InternalError("Unexpected exception. "+e.getMessage());   
    }
  }

  // SEARCH
  List searchAction(Search s, AID requester) {
  	AMSAgentDescription template = (AMSAgentDescription) s.getDescription();
  	long max = -1;
  	SearchConstraints sc = s.getConstraints();
  	if (sc != null) {
  		Long l = sc.getMaxResults();
  		if (l != null) {
  			max = l.longValue();
  		}
  	}
  	return myPlatform.amsSearch(template, max);
  }

  // GET_DESCRIPTION
  APDescription getDescriptionAction(AID requester) {
  	APTransportDescription tdsc = theProfile.getTransportProfile();
  	tdsc.clearAllAvailableMtps();
  	Iterator mtps = platformMTPs().iterator();
  	while (mtps.hasNext()) {
  		MTPDescriptor dr = (MTPDescriptor) mtps.next();
    	MTPDescription dn = new MTPDescription();
    	dn.setMtpName(dr.getName());
    	String[] addresses = dr.getAddresses();
    	for (int i = 0; i < addresses.length; ++i) {
    		dn.addAddresses(addresses[i]);
    	}
    	tdsc.addAvailableMtps(dn);
  	}
  	
  	return theProfile;
  } 

  //////////////////////////////////////////////////////////////////
  // TOOLS REGISTRATION and NOTIFICATION
  //////////////////////////////////////////////////////////////////
  /**
     Inner calss RegisterToolBehaviour.
     This behaviour handles tools subscriptions to be notified
     about platform events.
   */
  private class RegisterToolBehaviour extends CyclicBehaviour {

    private MessageTemplate subscriptionTemplate;

    RegisterToolBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage("FIPA-SL0");
      MessageTemplate mt2 = MessageTemplate.MatchOntology(IntrospectionOntology.NAME);
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
  toolNotification.clearAllReceiver();
  toolNotification.addReceiver(newTool);
	    
	try {
	  // Send back the whole container list.
	  ContainerID[] ids = myPlatform.containerIDs();
	  for(int i = 0; i < ids.length; i++) {
	    ContainerID cid = ids[i];

	    AddedContainer ac = new AddedContainer();
	    ac.setContainer(cid);
	    ac.setOwnership(getContainerOwnership(cid));

	    EventRecord er = new EventRecord(ac, here());
	    Occurred o = new Occurred();
	    o.setWhat(er);

	    try {
	    	getContentManager().fillContent(toolNotification, o);
	    	send(toolNotification);
	 		} 
	 		catch (Exception e) {
	 			e.printStackTrace();
	 		}
	 		
		  // Send the list of the MTPs installed on this container
		  Iterator mtps = myPlatform.containerMTPs(cid).iterator();
		  while (mtps.hasNext()) {
		    AddedMTP amtp = new AddedMTP();
		    amtp.setAddress(((MTPDescriptor) mtps.next()).getAddresses()[0]);
		    amtp.setWhere(cid); 
	
		    er = new EventRecord(amtp, here());
		    o = new Occurred();
		    o.setWhat(er);
	
		    try {
		    	getContentManager().fillContent(toolNotification, o);
		    	send(toolNotification);
		 		}
		 		catch (Exception e) {
		 			e.printStackTrace();
		 		}
		  }
	  }

	  // Send all agent names, along with their container name.
	  AID[] agents = myPlatform.agentNames();
	  for (int i = 0; i < agents.length; i++) {

	    AID agentName = agents[i];
	    ContainerID cid = myPlatform.getContainerID(agentName);
	    AMSAgentDescription amsd = myPlatform.getAMSDescription(agentName);
	    
	    BornAgent ba = new BornAgent();
	    // Note that "agentName" may not include agent addresses
			AID id = agentName;
	    if (amsd != null) {
				if (amsd.getName() != null) {
					id = amsd.getName();
				}
	    	ba.setState(amsd.getState());
	    	ba.setOwnership(amsd.getOwnership());
	    }
	    ba.setAgent(id);
	    ba.setWhere(cid);

	    EventRecord er = new EventRecord(ba, here());
	    Occurred o = new Occurred();
	    o.setWhat(er);

	    try {
	    	getContentManager().fillContent(toolNotification, o);
	    	send(toolNotification);
	 		} 
	 		catch (Exception e) {
	 			e.printStackTrace();
	 		}
	  }

	  // Notification of the APDescription
	  PlatformDescription ap = new PlatformDescription();
	  ap.setPlatform(getDescriptionAction(null));

	  EventRecord er = new EventRecord(ap,here());
	  Occurred o = new Occurred();
	  o.setWhat(er);

	  try {
			getContentManager().fillContent(toolNotification, o);
			send(toolNotification);
	  }
	  catch (Exception e) {
	   	e.printStackTrace();
	  }

	  // Add the new tool to tools list.
	  tools.add(newTool);
	}
	catch(NotFoundException nfe) {
	  nfe.printStackTrace();
	} catch (Exception e) {
		e.printStackTrace();
	}
      }
      else
	block();

    }

  } // End of RegisterToolBehaviour inner class

  /**
     Inner calss DeregisterToolBehaviour.
     This behaviour handles tools un-subscriptions.
   */
  private class DeregisterToolBehaviour extends CyclicBehaviour {

    private MessageTemplate cancellationTemplate;

    DeregisterToolBehaviour() {

      MessageTemplate mt1 = MessageTemplate.MatchLanguage("FIPA-SL0");
      MessageTemplate mt2 = MessageTemplate.MatchOntology(IntrospectionOntology.NAME);
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

  } // End of DeregisterToolBehaviour inner class

  /**
     Inner interface Handler.
     Perform additional operations related to a given platform event
   */
  private interface Handler {
    void handle(Event ev);
  }  // END of Handler inner interface

  /**
     Inner class EventManager.
     This behaviour notifies 
     - all registered tools about all platform events
     - the agent that had requested the AMS to perform an action 
     about the platform event forced by that action. Note that 
     this is done only for actions that produce an "asynchronous 
     event".
   */
  private class EventManager extends CyclicBehaviour {

    private Map handlers = new HashMap();

    public EventManager() {
      handlers.put(RemovedContainer.NAME, new Handler() {
        public void handle(Event ev) {
      		// If this event was forced by an action requested by
        	// an agent --> notify him.
          RemovedContainer rc = (RemovedContainer)ev;
          ContainerID cid = rc.getContainer();
          ACLMessage notification = (ACLMessage) pendingRemovedContainers.remove(cid);
          if (notification != null) {
          	send(notification);
          }
        }
      });
      handlers.put(DeadAgent.NAME, new Handler() {
        public void handle(Event ev) {
      		// If this event was forced by an action requested by
        	// an agent --> notify him.
          DeadAgent da = (DeadAgent)ev;
          AID agentID = da.getAgent();
          ACLMessage notification = (ACLMessage) pendingDeadAgents.remove(agentID);
          if (notification != null) {
          	send(notification);
          }
        }
      });
      handlers.put(MovedAgent.NAME, new Handler() {
        public void handle(Event ev) {
      		// If this event was forced by an action requested by
        	// an agent --> notify him.
          MovedAgent ma = (MovedAgent)ev;
          AID agentID = ma.getAgent();
          ACLMessage notification = (ACLMessage) pendingMovedAgents.remove(agentID);
          if (notification != null) {
          	send(notification);
          }
        }
      });
      handlers.put(BornAgent.NAME, new Handler() {
        public void handle(Event ev) {
      		// If this event was forced by an action requested by
        	// an agent --> notify him.
          BornAgent ba = (BornAgent)ev;
          AID agentID = ba.getAgent();
          ACLMessage notification = (ACLMessage) pendingClonedAgents.remove(agentID);
          if (notification != null) {
          	send(notification);
          }
        }
      });
    }

    public void action() {
    	try {
	    	EventRecord er = (EventRecord) eventQueue.get();
  	  	if (er != null) {
			    // Perform event-specific actions (if any)
			    Event ev = er.getWhat();
			    log("EventManager serving event "+ev.getName());
			    Handler handler = (Handler)handlers.get(ev.getName());
			    if(handler != null) {
			      handler.handle(ev);
			    }
			
			    // Notify all tools about the event
				  toolNotification.clearAllReceiver();
				  Iterator toolIt = tools.iterator();				
				  while(toolIt.hasNext()) {
				    AID tool = (AID)toolIt.next();
				    toolNotification.addReceiver(tool);
				  }
				  Occurred o = new Occurred();
				  o.setWhat(er);
			    getContentManager().fillContent(toolNotification, o);
			    myAgent.send(toolNotification);
				}
	    	else {
		      block();
	    	}
    	}
    	catch (Throwable t) {
    		// Should never happen
    		t.printStackTrace();
    	}
    }
  } // END of EventManager inner class

  
  //////////////////////////////////////////////////////////////////
  // Platform events input methods.
  // The following methods are called when a platform event is notified
  // to the Main and are executed outside the AMS thread. 
  // They result in preparing a proper IntrospectionOntology event (i.e. a
  // description of the event that has just happened) and inserting it in
  // AMS event queue. The EventManager behaviour will handle them within
  // the AMS thread.
  //////////////////////////////////////////////////////////////////

  /**
     Put a BornAgent event in the AMS event queue
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
		eventQueue.put(er);
  }

  /**
     Put a DeadAgent event in the AMS event queue
   */
  public void deadAgent(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    AID agentID = ev.getAgent();

    DeadAgent da = new DeadAgent();
    da.setAgent(agentID);
    da.setWhere(cid);

    EventRecord er = new EventRecord(da, here());
		er.setWhen(ev.getTime());
		eventQueue.put(er);
  }

  /**
     Put a SuspendedAgent event in the AMS event queue
   */
  public void suspendedAgent(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    AID name = ev.getAgent();

    SuspendedAgent sa = new SuspendedAgent();
    sa.setAgent(name);
    sa.setWhere(cid);

    EventRecord er = new EventRecord(sa, here());
    er.setWhen(ev.getTime());
		eventQueue.put(er);
  }

  /**
     Put a ResumedAgent event in the AMS event queue
   */
  public void resumedAgent(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    AID name = ev.getAgent();

    ResumedAgent ra = new ResumedAgent();
    ra.setAgent(name);
    ra.setWhere(cid);

    EventRecord er = new EventRecord(ra, here());
    er.setWhen(ev.getTime());
		eventQueue.put(er);
  }

  /**
     Put a MovedAgent event in the AMS event queue
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
		eventQueue.put(er);
  }

	/**
     Put a ChangedAgentOwnership event in the AMS event queue
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
		eventQueue.put(er);
	}

  /**
     Put an AddedContainer event in the AMS event queue
   */
  public void addedContainer(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    String name = cid.getName();

    AddedContainer ac = new AddedContainer();
    ac.setContainer(cid);

    EventRecord er = new EventRecord(ac, here());
    er.setWhen(ev.getTime());
		eventQueue.put(er);
  }

  /**
     Put a RemovedContainer event in the AMS event queue
  */
  public void removedContainer(PlatformEvent ev) {
    ContainerID cid = ev.getContainer();
    String name = cid.getName();

    RemovedContainer rc = new RemovedContainer();
    rc.setContainer(cid);

    EventRecord er = new EventRecord(rc, here());
    er.setWhen(ev.getTime());
		eventQueue.put(er);
  }

	/**
     Put a XXX event in the AMS event queue
	 */
	public synchronized void changedContainerPrincipal(PlatformEvent ev) {
		// FIXME: There is no element in the IntrospectionOntology 
		// corresponding to this event
	}

  /**
     Put a AddedMTP event in the AMS event queue
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
		eventQueue.put(er);
		
    // The PlatformDescription has changed --> Generate a suitable event
    PlatformDescription ap = new PlatformDescription();
    ap.setPlatform(getDescriptionAction(null));
    er = new EventRecord(ap, here());
    er.setWhen(ev.getTime());
		eventQueue.put(er);
  }

  /**    
     Put a RemovedMTP event in the AMS event queue
   */
  public synchronized void removedMTP(MTPEvent ev) {

    Channel ch = ev.getChannel();
    ContainerID cid = ev.getPlace();
    String proto = ch.getProtocol();
    String address = ch.getAddress();

    RemovedMTP rmtp = new RemovedMTP();
    rmtp.setAddress(address);
    rmtp.setProto(proto);
    rmtp.setWhere(cid);

    EventRecord er = new EventRecord(rmtp, here());
    er.setWhen(ev.getTime());
		eventQueue.put(er);
		
    // The PlatformDescription has changed --> Generate a suitable event
    PlatformDescription ap = new PlatformDescription();
    ap.setPlatform(getDescriptionAction(null));
    er = new EventRecord(ap, here());
    er.setWhen(ev.getTime());
		eventQueue.put(er);
  }

  public void messageIn(MTPEvent ev) { System.out.println("Message In."); }
  public void messageOut(MTPEvent ev) { System.out.println("Message Out."); }

  //////////////////////////////////////////////////
  // Utility methods
  //////////////////////////////////////////////////
 
  /**
     Redefine the getAuthority() method to return the platform main 
     authority
   */
	public Authority getAuthority() {
		return myPlatform.getAuthority();
	}

	/**
	   Return the ownership of a container
	 */
	private String getContainerOwnership(ContainerID container) {
		// FIXME: should use AgentManager to do that
		return ContainerPrincipal.NONE;
	}

	/**
	   Return the ownership of an agent 
	 */
	private String getAgentOwnership(AID agent) {
		String ownership = null;
		try {
			AMSAgentDescription amsd = myPlatform.getAMSDescription(agent);
			ownership = amsd.getOwnership();
		}
		catch (Exception e) {
			// Do nothing
		}
		return (ownership != null ? ownership : AgentPrincipal.NONE);
	}
		
	/**
	   Write the AP description in a text file
	 */
  private void writeAPDescription(APDescription description) {
    //Write the APDescription file.
    try {
      FileWriter f = new FileWriter(bootProfile.getParameter(Profile.FILE_DIR, "") + "APDescription.txt");
      f.write(description.toString());
	  f.write('\n');
	  f.flush();
      f.close();
    } catch(java.io.IOException ioe) {
      ioe.printStackTrace();
    }
  }

  /**
     Return a list of all MTPs in the platform
   */
  private List platformMTPs() {
  	List mtps = new ArrayList();
  	ContainerID[] cc = myPlatform.containerIDs();
  	for (int i = 0; i < cc.length; ++i) {
  		try {
  			List l = myPlatform.containerMTPs(cc[i]);
	  		Iterator it = l.iterator();
	  		while (it.hasNext()) {
	  			mtps.add(it.next());
	  		}
  		}
  		catch (NotFoundException nfe) {
  			// The container has died while we were looping --> ignore it 
  		}
  	}
  	return mtps;
  }

  /**
     Store a notification message to be sent at a later time.
     Package-scoped as it is called by the AMSJadeAgentManagementBehaviour
   */
 	void storeNotification(Concept action, Object key, ACLMessage notification) {
 		if (action instanceof KillAgent) {
 			pendingDeadAgents.put(key, notification);
 		}
 		else if (action instanceof CloneAction) {
 			pendingClonedAgents.put(key, notification);
 		}
 		else if (action instanceof MoveAction) {
 			pendingMovedAgents.put(key, notification);
 		}
 		else if (action instanceof KillContainer) {
 			pendingRemovedContainers.put(key, notification);
 		}
  }
  
  private void log(String s) {
  	System.out.println("AMS - "+s);
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
}
