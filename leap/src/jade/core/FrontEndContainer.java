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

import jade.lang.acl.ACLMessage;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.util.Logger;
import jade.security.AuthException;
//#MIDP_EXCLUDE_BEGIN
import jade.core.behaviours.Behaviour;
import jade.security.*;
import jade.security.dummy.*;
//#MIDP_EXCLUDE_END

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
@author Giovanni Caire - TILAB
*/

class FrontEndContainer implements FrontEnd, AgentToolkit, Runnable {
	public static final String CONN_MGR_CLASS_KEY = "connection-manager";	
	private static final String CONN_MGR_CLASS_DEFAULT = "jade.imtp.leap.JICP.FrontEndDispatcher";
	
	// The table of local agents
	private Hashtable localAgents = new Hashtable(1);
	
	// The ID of this container
	private ContainerID myId;
	
	// The name of this container 
	// The name of the platform this container belongs to
	// The addresses of the platform this container belongs to
	private String[] platformInfo;
	
	// The AID of the AMS
	private AID amsAID;
	
	// The AID of the default DF
	private AID dfAID;
	
	// The buffer of messages to be sent to the BackEnd
	private Vector pending = new Vector(4);
	
	// The BackEnd this FrontEndContainer is connected to
	private BackEnd myBackEnd;
	
	// The manager of the connection with the BackEnd
	private FEConnectionManager myConnectionManager;
	
	// The configuration properties for this FrontEndContainer
	private Properties configProperties;
	
	// Flag indicating that the shutdown procedure is in place
	private boolean exiting = false;
	
	//#MIDP_EXCLUDE_BEGIN
	private Authority authority = new DummyAuthority();
	//#MIDP_EXCLUDE_END

	/**
	   Construct a FrontEndContainer and connect to the BackEnd.
	 */
	FrontEndContainer(Properties p) {
		configProperties = p;
		
		// Start the therad for asynchronous message delivery
		Thread t = new Thread(this);
		t.start();
			
		// Connect to the BackEnd
		try {
			String connMgrClass = configProperties.getProperty(CONN_MGR_CLASS_KEY);
			if (connMgrClass == null) {
				connMgrClass = CONN_MGR_CLASS_DEFAULT;
			}
			
			myConnectionManager = (FEConnectionManager) Class.forName(connMgrClass).newInstance();
			myBackEnd = myConnectionManager.getBackEnd(this, configProperties);
		}
		catch (IMTPException imtpe) {
		  Logger.println("IMTP error. "+imtpe);
			imtpe.printStackTrace();
			MicroRuntime.handleTermination(true);
			return;
		}
		catch (Exception e) {
		  Logger.println("Unexpected error. "+e);
			e.printStackTrace();
			MicroRuntime.handleTermination(true);
			return;
		}
		
		// Lanch agents
		String agents = configProperties.getProperty(MicroRuntime.AGENTS_KEY);
		try {
			// Create all agents without starting them
			List specs = Specifier.parseSpecifierList(agents);
			Iterator it = specs.iterator();
			while (it.hasNext()) {
				Specifier s = (Specifier) it.next();
				try {
					initAgent(s.getName(), s.getClassName(), s.getArgs());
				}
				catch (Exception e) {
		  		Logger.println("Exception creating new agent. "+e);
				}
			}
			
			// Start agents only after they are all there
			synchronized (localAgents) {
				Enumeration e = localAgents.keys();
				while (e.hasMoreElements()) {
					String name = (String) e.nextElement();
					AID id = new AID(name, AID.ISLOCALNAME);
					Agent a = (Agent) localAgents.get(name);
					a.powerUp(id, new Thread(a));
				}
			}
		}
		catch (Exception e1) {
		  Logger.println("Exception parsing agent specifiers. "+e1);
			e1.printStackTrace();
		}
	}
	
	/////////////////////////////////////
	// FrontEnd interface implementation
	/////////////////////////////////////
	/**
	   Request the FrontEnd container to create a new agent.
	   @param name The name of the new agent.
	   @param className The class of the new agent.
	   @param args The arguments to be passed to the new agent.
	 */
  public final void createAgent(String name, String className, String[] args) throws IMTPException {
  	try {
  		initAgent(name, className, (Object[]) args);
			AID id = new AID(name, AID.ISLOCALNAME);
			Agent a = (Agent) localAgents.get(name);
			a.powerUp(id, new Thread(a));
			//#NODEBUG_EXCLUDE_BEGIN
    	//java.lang.Runtime rt = java.lang.Runtime.getRuntime();
			//rt.gc();
      //System.out.println("Used memory = "+((rt.totalMemory()-rt.freeMemory())/1024)+"K");
			//#NODEBUG_EXCLUDE_END
  	}
  	catch (Exception e) {
  		String msg = "Exception creating new agent. ";
  		Logger.println(msg+e);
  		throw new IMTPException(msg, e);
  	}
  }

  /**
	   Request the FrontEnd container to kill an agent.
	   @param name The name of the agent to kill.
	 */
  public final void killAgent(String name) throws NotFoundException, IMTPException {
    synchronized (localAgents) {
    	Agent agent = (Agent) localAgents.get(name);
    	if(agent == null) {
      	throw new NotFoundException("KillAgent failed to find " + name);
    	}
    	agent.doDelete();
    	// Note that the agent will be removed from the local table when its 
    	// handleEnd() is called.
    }
  }
  
  /**
	   Request the FrontEnd container to suspend an agent.
	   @param name The name of the agent to suspend.
	 */
  public final void suspendAgent(String name) throws NotFoundException, IMTPException {
    synchronized (localAgents) {
    	Agent agent = (Agent) localAgents.get(name);
    	if(agent == null) {
      	throw new NotFoundException("SuspendAgent failed to find " + name);
    	}
    	agent.doSuspend();
    }
  }
  
  /**
	   Request the FrontEnd container to resume an agent.
	   @param name The name of the agent to resume.
	 */
  public final void resumeAgent(String name) throws NotFoundException, IMTPException {
    synchronized (localAgents) {
    	Agent agent = (Agent) localAgents.get(name);
    	if(agent == null) {
      	throw new NotFoundException("ResumeAgent failed to find " + name);
    	}
    	agent.doActivate();
    }
  }
  
  /**
	   Pass an ACLMessage to the FrontEnd for posting.
	   @param msg The message to be posted.
	   @param sender The name of the receiver agent.
	 */
  public final void messageIn(ACLMessage msg, String receiver) throws NotFoundException, IMTPException {
    synchronized (localAgents) {
    	Agent agent = (Agent) localAgents.get(receiver);
    	if(agent == null) {
      	throw new NotFoundException("Receiver "+receiver+" not found");
    	}
    	agent.postMessage(msg);
    }
	}
	
  /**
	   Request the FrontEnd container to exit.
	 */
  public final void exit(boolean self) throws IMTPException {
  	if (!exiting) {
  		exiting = true;
	  	Logger.println("Container shut down activated");
	    
	  	// Kill all agents 
	  	Vector v = null;
	  	synchronized (localAgents) {
	  		// Create a temporary Vector including all the local agents
	  		// to avoid ConcurrentModification with handleEnd()
		    Enumeration e = localAgents.elements();
		    v = new Vector(localAgents.size());
		    while (e.hasMoreElements()) {
		    	v.addElement(e.nextElement());
		    }
	  	}
	
	  	Enumeration e = v.elements();
	  	while (e.hasMoreElements()) {
	      // Kill agent and wait for its termination
	      Agent a = (Agent) e.nextElement();
	      a.doDelete();
	      a.join();
	      a.resetToolkit();
	    }
	
			// Shut down the connection with the BackEnd. The BackEnd will 
	    // exit and deregister with the main
	  	myConnectionManager.shutdown();
	    
	    // Notify the JADE Runtime that the container has terminated execution
	    MicroRuntime.handleTermination(self);
  	}
  }
  
  
	/////////////////////////////////////
	// AgentToolkit interface implementation
	/////////////////////////////////////
  public final Location here() {
  	return myId;
  }
  
  public final void handleStart(String localName, Agent instance) {
  	// Will never be called --> just do nothing
  }
  
  public final void handleEnd(AID agentID) {
    try {
    	String name = agentID.getLocalName();
    	synchronized (localAgents) {
	      localAgents.remove(name);
    	}
  	  myBackEnd.deadAgent(name);
  	  
  	  // If there are no more agents and the exitwhenempty option 
  	  // is set, activate shutdown
  	  if ("true".equals(configProperties.getProperty("exitwhenempty"))) {
  	  	if (localAgents.isEmpty()) {
		  	  exit(true);
  	  	}
  	  }
    }
    catch(IMTPException re) {
		  Logger.println(re.toString());
    }
  }
  
  public final void handleChangedAgentState(AID agentID, AgentState from, AgentState to) {
  	// FIXME: This should call myBackEnd.suspendedAgent()/resumedAgent()
  }
  
  public final void handleSend(ACLMessage msg, AID sender) throws AuthException {
		Iterator it = msg.getAllIntendedReceiver();
		// If some receiver is local --> directly post the message
		int remoteCnt = 0;
		while (it.hasNext()) {
			remoteCnt++;
			AID id = (AID) it.next();
			Agent a = (Agent) localAgents.get(id.getLocalName());
			if (a != null) {
				a.postMessage((ACLMessage) msg.clone());
				remoteCnt--;
			}
		}
		// If some receiver is remote --> pass the message to the BackEnd		
		if (remoteCnt > 0) {
			post(msg, sender.getLocalName());
			/*try {
				myBackEnd.messageOut(msg, sender.getLocalName());
			}
			catch (IMTPException imtpe) {
				// FIXME: notify failure to sender
		  	Logger.println(imtpe.toString());
			}
			catch (NotFoundException nfe) {
				// Note that "NotFound" here is referred to the sender and
				// indicates an inconsistency between the FrontEnd and the BackEnd
				// FIXME: recover the inconsistency
		  	Logger.println(nfe.toString());
			}*/
		}
  }
  
  public final void setPlatformAddresses(AID id) {
  	id.clearAllAddresses();
  	for (int i = 2; i < platformInfo.length; ++i) {
  		id.addAddresses(platformInfo[i]);
  	}
  }
  
  public final AID getAMS() {
  	return amsAID;
  }
  
  public final AID getDefaultDF() {
  	return dfAID;
  }

  public String getProperty(String key, String aDefault) {
  	String ret = configProperties.getProperty(key);
  	return (ret != null ? ret : aDefault); 
  }

  //#MIDP_EXCLUDE_BEGIN
  public void handleMove(AID agentID, Location where) throws AuthException, IMTPException, NotFoundException {
  }
  
  public void handleClone(AID agentID, Location where, String newName) throws AuthException, IMTPException, NotFoundException {
  }
  
  public void handlePosted(AID agentID, ACLMessage msg) throws AuthException {
  }
  
  public void handleReceived(AID agentID, ACLMessage msg) throws AuthException {
  }
  
  public void handleBehaviourAdded(AID agentID, Behaviour b) {
  }
  
  public void handleBehaviourRemoved(AID agentID, Behaviour b) {
  }
  
  public void handleChangeBehaviourState(AID agentID, Behaviour b, String from, String to) {
  }
  
  public void handleChangedAgentPrincipal(AID agentID, AgentPrincipal from, CertificateFolder certs) {
  }
  
  public Authority getAuthority() {
  	return authority; 
  }
  //#MIDP_EXCLUDE_END
  
  
  ///////////////////////////////
  // Private methods
  ///////////////////////////////
  private final void initInfo(String[] info) {
  	myId = new ContainerID(info[0], null);
  	AID.setPlatformID(info[1]);
  	platformInfo = info;
  	// FIXME: Set platformName to AID static variable
  	amsAID = new AID("ams", AID.ISLOCALNAME);
  	setPlatformAddresses(amsAID);
  	dfAID = new AID("df", AID.ISLOCALNAME);
  	setPlatformAddresses(dfAID);
  }

  private final void initAgent(String name, String className, Object[] args) throws Exception {
    Agent previous = null;
    try {
    	// Create the new agent and add it to the local agents table
      Agent agent = (Agent) Class.forName(className).newInstance();
      agent.setArguments(args);
      agent.setToolkit(this);
      previous = (Agent) localAgents.put(name, agent);
      // Notify the BackEnd (get back platform info if this is the first agent) 
			String[] info = myBackEnd.bornAgent(name);
			if (info != null) {
				initInfo(info);
			}
		}
		catch (Exception e) {
			// If an exception occurs, roll back and restore the previous agent if any.
      localAgents.remove(name);
      if(previous != null) {
        localAgents.put(name, previous);
      }
      // Re-throw the exception
      throw e;
    }
  }
  
  private synchronized void post(ACLMessage msg, String sender) {
  	synchronized(pending) {
	  	pending.addElement(msg.clone());
	  	pending.addElement(sender);
	  	pending.notifyAll();
  	}
  }
  
  public void run() {
  	ACLMessage msg = null;
  	String sender = null;
  	
  	while (true) {
	  	synchronized(pending) {
	  		while (pending.size() == 0) {
	  			try {
		  			pending.wait();
	  			}
	  			catch (InterruptedException ie) {
	  				// Should never happen
	  				Logger.println(ie.toString());
	  			}
	  		}
	  		msg = (ACLMessage) pending.elementAt(0);
	  		sender = (String) pending.elementAt(1);
	  		pending.removeElementAt(1);
	  		pending.removeElementAt(0);
	  	}
	  	
	  	try {
		  	myBackEnd.messageOut(msg, sender);
	  	}
	  	catch (Exception e) {
	  		// Should never happen. Note that "NotFound" here is referred 
	  		// to the sender.
	  		Logger.println(e.toString());
	  	}
  	}
  }
}

