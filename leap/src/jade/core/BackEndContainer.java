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

package jade.core;

//#MIDP_EXCLUDE_FILE

import jade.lang.acl.ACLMessage;
import jade.domain.FIPAAgentManagement.Envelope;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.security.*;

import java.util.Hashtable;
import java.util.Enumeration;

/**
@author Giovanni Caire - TILAB
*/

public class BackEndContainer extends AgentContainerImpl implements BackEnd {
	private static final String OUTGOING_NAME = "out";
	private long outCnt = 0;
	
	// The FrontEnd this BackEndContainer is connected to
	private FrontEnd myFrontEnd;
	
	// The manager of the connection with the FrontEnd
	private BEConnectionManager myConnectionManager;
	
	private Hashtable agentImages = new Hashtable();
	private Hashtable pendingImages = new Hashtable();
	
	private Platform myPlatform;
	private boolean refreshPlatformInfo = true;
	
	
	public BackEndContainer(Profile p, BEConnectionManager cm) {
		super(p);
		myConnectionManager = cm;
		try {
			myFrontEnd = cm.getFrontEnd(this, null);
			Runtime.instance().beginContainer();
			joinPlatform();
			myPlatform = p.getPlatform();
		}
		catch (IMTPException imtpe) {
			// Should never happen
			imtpe.printStackTrace();
		}
		catch (ProfileException pe) {
			// Should never happen
			pe.printStackTrace();
		}
	}
	
	/////////////////////////////////////
	// BackEnd interface implementation
	/////////////////////////////////////
	/**
	   A new agent has just started on the FrontEnd.
	   - Create an image for the new agent and set its CertificateFolder
	     unless there is already a pending image (see createAgent()).
	   - Notify the Main
	   - Return the platform info to the FrontEnd if required
	 */
  public String[] bornAgent(String name) throws IMTPException {
  	AID id = new AID(name, AID.ISLOCALNAME);
  	AgentImage image = (AgentImage) pendingImages.remove(id);
  	if (image == null) {
  		// The agent spontaneously born on the FrontEnd --> its image still has to be created
  		image = new AgentImage(id);
  		// Create and set security information
  		try {
	    	CertificateFolder certs = createCertificateFolder(id);
	  		image.setPrincipal(certs);
	    	image.setOwnership(((AgentPrincipal) certs.getIdentityCertificate().getSubject()).getOwnership());
  		}
  		catch (AuthException ae) {
  			// Should never happen
  			ae.printStackTrace();
  		}
  	}
  	AgentImage previous = (AgentImage) agentImages.put(id, image);
  	try {
  		ContainerID cid = (ContainerID) here();
	  	myPlatform.bornAgent(id, cid, image.getCertificateFolder());
	  	image.setToolkit(this);
	  	// Prepare platform info to return if necessary
	  	String[] info = null;
	  	if (refreshPlatformInfo) {
	  		AID ams = getAMS();
	  		String[] addresses = ams.getAddressesArray();
	  		info = new String[2+addresses.length];
	  		info[0] = cid.getName();
	  		info[1] = ams.getHap();
	  		for (int i = 0; i < addresses.length; ++i) {
	  			info[i+2] = addresses[i];
	  		}
	  		refreshPlatformInfo = false;
	  	}
  		return info;
  	}
  	catch (Exception e) {
    	// Roll back if necessary and throw an IMTPException
  		agentImages.remove(id);
  		if (previous != null) {
  			agentImages.put(id, previous);
  		}
  		throw new IMTPException("Error creating agent "+name+". ", e);
  	}
  }

  /**
     An agent has just died on the FrontEnd.
     Remove its image and notify the Main
	 */
  public void deadAgent(String name) throws IMTPException {
  	AID id = new AID(name, AID.ISLOCALNAME);
  	AgentImage image = (AgentImage) agentImages.remove(id);
  	if (image != null) {
  		try {
	  		myPlatform.deadAgent(id);
  		}
  		catch (Exception e) {
  			// There is nothing we can do
  			e.printStackTrace();
  		}
  	}
  }
  
  /**
	 */
  public void suspendedAgent(String name) throws NotFoundException, IMTPException {
  	System.out.println("BackEndContainer.suspendedAgent() not yet implemented");
  	// FIXME: to be implemented
  }
  
  /**
	 */
  public void resumedAgent(String name) throws NotFoundException, IMTPException {
  	System.out.println("BackEndContainer.resumedAgent() not yet implemented");
  	// FIXME: to be implemented
  }
  
  /**
     An agent on the FrontEnd has sent a message.
     Note that the NotFoundException here is referred to the sender and
     indicates an inconsistency between the FrontEnd and the BackEnd
	 */
  public void messageOut(final ACLMessage msg, String sender) throws NotFoundException, IMTPException {
		// Check whether the sender exists
  	final AID id = new AID(sender, AID.ISLOCALNAME);
  	AgentImage image = (AgentImage) agentImages.get(id);
  	if (image == null) {
  		throw new NotFoundException("No image for agent "+sender+" on the BackEndContainer");
  	}
  	
  	// Set the sender field if not yet set
  	try {
			if (msg.getSender().getName().length() < 1) {
				msg.setSender(id);
			}
		}
		catch (NullPointerException e) {
			msg.setSender(id);
		}

		// Mark this thread as an outgoing message dispatcher. This is 
		// necessary, in the case some of the receivers lives in the FrontEnd,
		// to avoid sending him back the message (see dispatch()).
		Thread.currentThread().setName(OUTGOING_NAME+outCnt++);
		
		try {
  		// An AuthException will be thrown if the sender does not have
  		// - the permission to send a message on behalf of msg.getSender()
			// - the permission to send a message to one of the receivers
	  	getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
				public Object run() throws AuthException {
					handleSend(msg, id);
					return null;
				}
			}, image.getCertificateFolder());
		}
		catch (AuthException e) {
			// FIXME: This will probably disappear as all the AuthExecptions
			// should be handled within the "unicastPostMessage loop" inside
			// handleSend()
			System.out.println("AuthException: "+e.getMessage() );
		} 
		catch (Exception e) {
  		// Should never happen
			e.printStackTrace();
		} 
  }
  
	///////////////////////////////////////////
	// AgentContainerImpl methods re-definition
	///////////////////////////////////////////
  /**
     Force the creation of an agent on the FrontEnd.
     Note that the agent to create can have a different owner with respect 
     to the owner of this "container" --> Its image holding the agent's
     ownership information must be created now and not in the bornAgent()
     method. This image is stored in the pendingImages map for later 
     retrieval (see bornAgent()).
   */
  public void createAgent(AID agentID, String className, Object[] args, String ownership, CertificateFolder certs, boolean startIt) throws IMTPException {
    AgentImage image = new AgentImage(agentID);
    // Set security information 
    if (certs != null) {
    	image.setPrincipal(certs);
    }
    if(ownership != null) {
    	image.setOwnership(ownership);
    }
    else if (certs.getIdentityCertificate() != null) {
    	image.setOwnership(((AgentPrincipal) certs.getIdentityCertificate().getSubject()).getOwnership());
    }

    // Store the image so that it can be retrieved when the new agent starts
    AgentImage previous = (AgentImage) pendingImages.put(agentID, image);
    
    try {
    	// Arguments can only be Strings
    	String[] sargs = null;
    	if (args != null) {
    		sargs = new String[args.length];
    		for (int i = 0; i < args.length; ++i) {
    			sargs[i] = (String) args[i];
    		}
    	}
    	myFrontEnd.createAgent(agentID.getLocalName(), className, sargs);
    }
    catch (IMTPException imtpe) {
    	// Roll back if necessary and forward the exception
    	pendingImages.remove(agentID);
    	if (previous != null) {
    		pendingImages.put(agentID, previous);
    	}
    	throw imtpe;
    }
    catch (ClassCastException cce) {
    	// Roll back if necessary and forward the exception
    	pendingImages.remove(agentID);
    	if (previous != null) {
    		pendingImages.put(agentID, previous);
    	}
    	throw new IMTPException("Non-String argument");
    }
  }

  /**
     Force the termination of an agent on the FrontEnd.
     Note that deadAgent() is immediately called so that the agent is
     considered dead by the platform even if the killAgent command
     cannot reach the FrontEnd for disconnection problems (see deadAgent()). 
   */
  public void killAgent(AID agentID) throws IMTPException, NotFoundException {
  	if (agentImages.get(agentID) != null) {
  		String name = agentID.getLocalName();
  		myFrontEnd.killAgent(name);
	  	deadAgent(name);
  	}
  	else {
			throw new NotFoundException("KillAgent failed to find " + agentID);
  	}
  }

  /**
   */
  public void suspendAgent(AID agentID) throws IMTPException, NotFoundException {
  	if (agentImages.get(agentID) != null) {
  		myFrontEnd.suspendAgent(agentID.getLocalName());
  	}
  	else {
			throw new NotFoundException("SuspendAgent failed to find " + agentID);
  	}
  }

  /**
   */
  public void resumeAgent(AID agentID) throws IMTPException, NotFoundException {
  	if (agentImages.get(agentID) != null) {
  		myFrontEnd.resumeAgent(agentID.getLocalName());
  	}
  	else {
			throw new NotFoundException("ResumeAgent failed to find " + agentID);
  	}
  }

  /**
     Dispatch a message to an agent in the FrontEnd.
     If this method is called by a thread that is serving a message 
     sent by an agent in the FrontEnd too, nothing is done as the
     dispatch has already taken place in the FrontEnd (see messageOut()).
   */
  public void dispatch(final ACLMessage msg, final AID receiverID) throws IMTPException, NotFoundException {
  	// Try first in the real LADT
  	try {
  		super.dispatch(msg, receiverID);
  	}
  	catch (NotFoundException nfe) {
  		// The receiver must be in the FrontEnd
	  	AgentImage image = (AgentImage) agentImages.get(receiverID);
	  	if (image != null) {
	  		if (Thread.currentThread().getName().startsWith(OUTGOING_NAME)) {
	  			// The message was sent by an agent living in the FrontEnd. The
	  			// receiverID (living in the FrontEnd too) has already received
	  			// the message.
	  			return;
	  		}
	  		
	  		// FIXME: The right way to do things should be i) check permission
	  		// ii) call messageIn() iii) notify listeners. On the other hand 
	  		// handlePosted() currently does i) and iii). 
	  		try {
		  		// An AuthException will be thrown if the receiver does not have
		  		// the permission to receive messages from the sender of this message
		  		getAuthority().doAsPrivileged(new PrivilegedExceptionAction() {
						public Object run() throws AuthException {
			  			handlePosted(receiverID, msg);
							return null;
						}
					}, image.getCertificateFolder());
	  		}
	  		catch (AuthException ae) {
	  			String errorMsg = new String("\"Agent "+receiverID.getName()+" not authorized to receive messages from agent "+msg.getSender().getName());
			  	System.out.println(errorMsg+". "+ae.getMessage());
	      	notifyFailureToSender(msg, receiverID, new InternalError(errorMsg));
	  		}
	  		catch (Exception e) {
	  			// Should never happen
	  			e.printStackTrace();
	  		}
	  		// Forward the message to the FrontEnd
		  	myFrontEnd.messageIn(msg, receiverID.getLocalName());
	  	}
	  	else {
				throw new NotFoundException("DispatchMessage failed to find " + receiverID);
	  	}
  	}
  }
  
  /**
   */
  public void changeAgentPrincipal(AID agentID, CertificateFolder certs) throws IMTPException, NotFoundException {
  	AgentImage image = (AgentImage) agentImages.get(agentID);
  	if (image == null) {
      throw new NotFoundException("ChangeAgentPrincipal failed to find " + agentID);
  	}
  	image.setPrincipal(certs);
  }
  
  /**
     This method is re-defined to avoid NullPointerException. In fact
     a search in the LADT would be done for the agent to be debugged, but
     the LADT is obviously empty.
   */
  public void enableDebugger(AID debuggerName, AID toBeDebugged) throws IMTPException {
  	throw new IMTPException("Unsupported operation");
  }

  /**
     This method is re-defined to avoid NullPointerException. In fact
     a search in the LADT would be done for the agent to be debugged, but
     the LADT is obviously empty.
   */
  public void disableDebugger(AID debuggerName, AID notToBeDebugged) throws IMTPException {
  	throw new IMTPException("Unsupported operation");
  }
  
  /**
   */
  public void exit() throws IMTPException {
  	// Forward the exit command to the FrontEnd
  	try {
  		myFrontEnd.exit(false);
  	}
  	catch (IMTPException imtpe) {
  		// The FrontEnd is disconnected. 
  		// "Kill" all agent images and force the shutdown of the connection
  		Enumeration e = agentImages.keys();
  		while (e.hasMoreElements()) {
  			AID id = (AID) e.nextElement();
  			try {
  				myPlatform.deadAgent(id);
  			}
  			catch (Exception ex) {
  				ex.printStackTrace();
  			}
  		}
  		agentImages.clear();  		
  		myConnectionManager.shutdown(false);
  	}
    shutDown();
	}

  /**
   */
	void notifyFailureToSender(ACLMessage msg, AID receiver, InternalError ie) {
		// If the message was sent by an agent living on the FrontEnd, the
		// FAILURE has to be notified only if the receiver does not live
		// on the FrontEnd too. In this case in fact the message has
		// been delivered even if we have an exception. 
		if (Thread.currentThread().getName().startsWith(OUTGOING_NAME)) {
			if (agentImages.get(receiver) == null) {
				Thread.currentThread().setName("dummy");
			}
		}
		super.notifyFailureToSender(msg, receiver, ie);
	}
	
	/**
	   Inner class AgentImage
	 */
	private class AgentImage extends Agent {
	  private AgentImage(AID id) {
	  	super(id);
	  }  	
	}
}

