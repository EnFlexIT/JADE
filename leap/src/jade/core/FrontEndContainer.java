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
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import jade.util.Logger;
import jade.security.JADESecurityException;
//#MIDP_EXCLUDE_BEGIN
import jade.core.behaviours.Behaviour;
import jade.security.*;
//#MIDP_EXCLUDE_END

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

/**
   @author Giovanni Caire - TILAB
   @author Jerome Picault - Motorola Labs
*/

class FrontEndContainer implements FrontEnd, AgentToolkit, Runnable {
	private static final String CONN_MGR_CLASS_DEFAULT = "jade.imtp.leap.JICP.BIFEDispatcher";

  Logger logger = Logger.getMyLogger(this.getClass().getName());
	
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
	private Vector pending;;
	
	// The BackEnd this FrontEndContainer is connected to
	private BackEnd myBackEnd;
	
	// The manager of the connection with the BackEnd
	private FEConnectionManager myConnectionManager;
	
	// The configuration properties for this FrontEndContainer
	private Properties configProperties;
	
	// Flag indicating that the shutdown procedure is in place
	private boolean exiting = false;

	/**
	   Construct a FrontEndContainer and connect to the BackEnd.
	 */
	FrontEndContainer(Properties p) {
		configProperties = p;
			
		// Connect to the BackEnd
		try {
			String connMgrClass = configProperties.getProperty(MicroRuntime.CONN_MGR_CLASS_KEY);
			if (connMgrClass == null) {
				connMgrClass = CONN_MGR_CLASS_DEFAULT;
			}
			
			myConnectionManager = (FEConnectionManager) Class.forName(connMgrClass).newInstance();
			myBackEnd = myConnectionManager.getBackEnd(this, configProperties);
		}
		catch (IMTPException imtpe) {
	  	logger.log(Logger.SEVERE,"IMTP error "+imtpe);
			imtpe.printStackTrace();
			MicroRuntime.handleTermination(true);
			return;
		}
		catch (Exception e) {
	  	logger.log(Logger.SEVERE,"Unexpected error "+e);
			e.printStackTrace();
			MicroRuntime.handleTermination(true);
			return;
		}
		
		// Lanch agents
		String agents = configProperties.getProperty(MicroRuntime.AGENTS_KEY);
		try {
			// Create all agents without starting them
			Vector specs = Specifier.parseSpecifierList(agents);
			for (Enumeration en=specs.elements(); en.hasMoreElements(); ) {
				Specifier s = (Specifier) en.nextElement();
				try {
					initAgent(s.getName(), s.getClassName(), s.getArgs());
				}
				catch (Exception e) {
	  			logger.log(Logger.SEVERE,"Exception creating new agent "+e);
				}
			}
			
			// Start agents only after they are all there
			synchronized (this) {
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
	  	logger.log(Logger.SEVERE,"Exception parsing agent specifiers "+e1);
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
			logger.log(Logger.SEVERE,msg+e);
  		throw new IMTPException(msg, e);
  	}
  }

  /**
	   Request the FrontEnd container to kill an agent.
	   @param name The name of the agent to kill.
	 */
  public final void killAgent(String name) throws NotFoundException, IMTPException {
  	Agent agent = (Agent) localAgents.get(name);
  	if(agent == null) {
    	throw new NotFoundException("KillAgent failed to find " + name);
  	}
  	// Note that the agent will be removed from the local table in 
  	// the handleEnd() method.
  	agent.doDelete();
  }
  
  /**
	   Request the FrontEnd container to suspend an agent.
	   @param name The name of the agent to suspend.
	 */
  public final void suspendAgent(String name) throws NotFoundException, IMTPException {
  	Agent agent = (Agent) localAgents.get(name);
  	if(agent == null) {
    	throw new NotFoundException("SuspendAgent failed to find " + name);
  	}
  	agent.doSuspend();
  }
  
  /**
	   Request the FrontEnd container to resume an agent.
	   @param name The name of the agent to resume.
	 */
  public final void resumeAgent(String name) throws NotFoundException, IMTPException {
  	Agent agent = (Agent) localAgents.get(name);
  	if(agent == null) {
    	throw new NotFoundException("ResumeAgent failed to find " + name);
  	}
  	agent.doActivate();
	}
  
  /**
	   Pass an ACLMessage to the FrontEnd for posting.
	   @param msg The message to be posted.
	   @param sender The name of the receiver agent.
	 */
  public final void messageIn(ACLMessage msg, String receiver) throws NotFoundException, IMTPException {
  	if (receiver != null) {
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
  		logger.log(Logger.INFO,"Container shut down activated");
	    
	  	// Kill all agents 
	  	synchronized (this) {
		    Enumeration e = localAgents.elements();
		  	while (e.hasMoreElements()) {
		      // Kill agent and wait for its termination
		      Agent a = (Agent) e.nextElement();
		      a.doDelete();
		      a.join();
		      a.resetToolkit();
		    }
	  		localAgents.clear();
	  	}
  		logger.log(Logger.FINE,"Local agents terminated");
	  	
			// Shut down the connection with the BackEnd. The BackEnd will 
	    // exit and deregister with the main
	    myConnectionManager.shutdown();
  		logger.log(Logger.FINE,"Connection manager closed");
	  	
	    // Notify the JADE Runtime that the container has terminated execution
	    MicroRuntime.handleTermination(self);
	    
	    // Stop the TimerDispatcher if it was activated
	    TimerDispatcher.getTimerDispatcher().stop();
  	}
  }
  
  /**
	   Request the FrontEnd container to synch.
	 */
  public final void synch() throws IMTPException {
		synchronized (this) {
			Enumeration e = localAgents.keys();
			while (e.hasMoreElements()) {
				String name = (String) e.nextElement();
		  	logger.log(Logger.INFO,"Resynching agent "+name);
		    try {
		    	// Notify the BackEnd (get back platform info if this is the first agent) 
		      String[] info = myBackEnd.bornAgent(name);
					if (info != null) {
						initInfo(info);
					}
				}
				catch (IMTPException imtpe) {
					// The connection is likely down again. Rethrow the exception
					// to make the BE repeat the synchronization process
			  	logger.log(Logger.WARNING,"IMTPException resynching. "+imtpe);
			  	throw imtpe;					
				}
				catch (Exception ex) {
			  	logger.log(Logger.SEVERE,"Exception resynching agent "+name+". "+ex);
					ex.printStackTrace();
					// An agent with the same name has come up in the meanwhile.
					// FIXME: Kill the agent or notify a warning
		    }
			}
		}
  }
  	
	/////////////////////////////////////
	// AgentToolkit interface implementation
	/////////////////////////////////////
  //#MIDP_EXCLUDE_BEGIN
  public jade.wrapper.AgentContainer getContainerController(JADEPrincipal principal, Credentials credentials){
    return null;
  }
  //#MIDP_EXCLUDE_END

  public final Location here() {
  	return myId;
  }
  
  public final void handleEnd(AID agentID) {
  	String name = agentID.getLocalName();
  	// Wait for messages (if any) sent by this agent to be transmitted
  	synchronized (pending) {
  		while (pending.contains(name)) {
  			try {
	  			pending.wait();
  			}
  			catch (Exception e) {}
  		}
  	}
  	
  	if (!exiting) {
  		// If this agent is ending because the container is exiting
  		// just do nothing. The BackEnd will notify the main.
	    try {
	    	synchronized (this) {
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
		  	logger.log(Logger.SEVERE,re.toString());
	    }
  	}
  }
  
  public final void handleChangedAgentState(AID agentID, int from, int to) {
  	// FIXME: This should call myBackEnd.suspendedAgent()/resumedAgent()
  }

  // Note that the needClone argument is ignored since the
  // FrontEnd must always clone
  public final void handleSend(ACLMessage msg, AID sender, boolean needClone) {
		Iterator it = msg.getAllIntendedReceiver();
		// If some receiver is local --> directly post the message
		boolean hasRemoteReceivers = false;
		while (it.hasNext()) {
			AID id = (AID) it.next();
			Agent a = (Agent) localAgents.get(id.getLocalName());
			if (a != null) {
				ACLMessage m = (ACLMessage) msg.clone();
				a.postMessage(m);
			}
			else {
				hasRemoteReceivers = true;
			}
		}
		// If some receiver is remote --> pass the message to the BackEnd		
		if (hasRemoteReceivers) {
			post(msg, sender.getLocalName());
		}
  }
  
  public final void setPlatformAddresses(AID id) {
  	id.clearAllAddresses();
  	for (int i = 3; i < platformInfo.length; ++i) {
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
  public void handleMove(AID agentID, Location where) throws JADESecurityException, IMTPException, NotFoundException {
  }
  
  public void handleClone(AID agentID, Location where, String newName) throws JADESecurityException, IMTPException, NotFoundException {
  }
  
  public void handleSave(AID agentID, String repository) throws ServiceException, NotFoundException, IMTPException {
  }

  public void handleReload(AID agentID, String repository) throws ServiceException, NotFoundException, IMTPException {
  }

  public void handleFreeze(AID agentID, String repository, ContainerID bufferContainer) throws ServiceException, NotFoundException, IMTPException {
  }

  public void handlePosted(AID agentID, ACLMessage msg) {
  }
  
  public void handleReceived(AID agentID, ACLMessage msg) {
  }
  
  public void handleBehaviourAdded(AID agentID, Behaviour b) {
  }
  
  public void handleBehaviourRemoved(AID agentID, Behaviour b) {
  }
  
  public void handleChangeBehaviourState(AID agentID, Behaviour b, String from, String to) {
  }
  //#MIDP_EXCLUDE_END
  
  public ServiceHelper getHelper(Agent a, String serviceName) throws ServiceException {
		String helperClassName = configProperties.getProperty(serviceName);
		if (helperClassName != null) {
			try {
	  		ServiceHelper sh = (ServiceHelper) Class.forName(helperClassName).newInstance();
	  		return sh;
			}
			catch (Throwable t) {
				throw new ServiceException("Error creating helper for service "+serviceName, t);
			}
		}
		else {
			throw new ServiceException("Missing helper class name for service "+serviceName);
		}
  }

  
  ///////////////////////////////
  // Private methods
  ///////////////////////////////
  private final void initInfo(String[] info) {
  	myId = new ContainerID(info[1], null);
  	AID.setPlatformID(info[2]);
  	platformInfo = info;
  	amsAID = new AID("ams", AID.ISLOCALNAME);
  	setPlatformAddresses(amsAID);
  	dfAID = new AID("df", AID.ISLOCALNAME);
  	setPlatformAddresses(dfAID);
  }

  private final void initAgent(String name, String className, Object[] args) throws Exception {
    synchronized (this) {
	  	Agent previous = null;
	    try {
	    	// Create the new agent and add it to the local agents table
	      Agent agent = (Agent) Class.forName(className).newInstance();
	      agent.setArguments(args);
	      agent.setToolkit(this);
	      // Notify the BackEnd (get back platform info if this is the first agent) 
	      String[] info = myBackEnd.bornAgent(name);
	      name = info[0];
				if (info.length > 1) {
					initInfo(info);
				}
	      previous = (Agent) localAgents.put(name, agent);
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
  }
  
  private void post(ACLMessage msg, String sender) {
  	if (pending == null) {
  		// Lazily create the vector of pending messages and the Thread 
  		// for asynchronous message delivery
  		pending = new Vector(4);
			Thread t = new Thread(this);
			t.start();
  	}
  			
  	synchronized(pending) {
	  	pending.addElement(msg.clone());
	  	pending.addElement(sender);
			int size = pending.size();
	  	if (size > 100 && size < 110) {
  			logger.log(Logger.INFO,size+" pending messages");
	  	}
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
  					logger.log(Logger.SEVERE,ie.toString());
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
  			logger.log(Logger.SEVERE,e.toString());
	  	}
	  	// Notify terminating agents (if any) waiting for their messages to be delivered
	  	synchronized (pending) {
	  		pending.notifyAll();
	  	}
  	}
  }
}

