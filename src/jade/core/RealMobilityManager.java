/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;

//#MIDP_EXCLUDE_FILE

import jade.util.leap.Iterator;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import jade.lang.acl.ACLMessage;

import jade.security.Authority;
import jade.security.AuthException;

/**
 * Class declaration
 */
class RealMobilityManager implements MobilityManager {
    static final boolean       TRANSFER_ABORT = false;
    static final boolean       TRANSFER_COMMIT = true;

    // This Map holds the mapping between a container and the class loader
    // that can retrieve agent classes from this container.
    private Map                loaders;

    // This Map holds the mapping between an agent that arrived on this
    // container and the container where its classes can be retrieved
    private Map                sites;

    // The container including this MobilityManager
    private AgentContainerImpl myContainer;

    // The table of the agents living in the container including this MobilityManager
    private LADT               localAgents;

    // The Profile of the container including this MobilityManager
    private Profile            myProfile;
    
    // The Platform the container including this MobilityManager belongs to
    private Platform           myPlatform;
    
    // The ResourceManager of the container including this MobilityManager
    private ResourceManager    myResourceManager;
    
		private static final String VERBOSITY_KEY = "jade_core_RealMobilityManager_verbosity";
    private int verbosity = 0;

    /**
     * Inner class Deserializer
     */
    class Deserializer extends ObjectInputStream {
        private AgentContainer ac;

        /**
         */
        public Deserializer(InputStream inner, 
                            AgentContainer classSite) throws IOException {
            super(inner);

            ac = classSite;
        }

        /**
         */
        protected Class resolveClass(ObjectStreamClass v) 
        	throws IOException, ClassNotFoundException {
            JADEClassLoader cl = (JADEClassLoader) loaders.get(ac);

            if (cl == null) {
                cl = new JADEClassLoader(ac, verbosity);
                loaders.put(ac, cl);
            } 
            Class c = cl.loadClass(v.getName());
            return c;
        } 
    }    // END of inner class Deserializer

    /**
     * Constructor declaration
     */
    public RealMobilityManager() {
    	myProfile = null;
    	myPlatform = null;
    	myResourceManager = null;
      myContainer = null;
      localAgents = null;
      loaders = new HashMap();
      sites = new HashMap();
    }

    /**
     */
    public void initialize(Profile p, AgentContainerImpl ac, LADT la) {
    	myProfile = p;
      myContainer = ac;
      localAgents = la;
      try {
	    	myPlatform = myProfile.getPlatform();
	    	myResourceManager = myProfile.getResourceManager();
		    try {
		    	verbosity = Integer.parseInt(myProfile.getParameter(VERBOSITY_KEY, "0"));
		    }
		    catch (Exception e) {
		    	// Keep default (0)
		    }
      }
      catch (ProfileException pe) {
      	// Should never happen
      	pe.printStackTrace();
      }
    } 

    // IMPLEMENTATION OF INTERFACE MobilityHandler

    /**
       @see jade.core.MobilityManager#createAgent()
     */
    public void createAgent(AID agentID, byte[] serializedInstance, 
                            AgentContainer classSite, boolean startIt) throws Exception {
        log("Incoming agent "+agentID, 1);         	
        // Reconstruct the serialized agent
        ObjectInputStream in = new Deserializer(new ByteArrayInputStream(serializedInstance), classSite);
        Agent             instance = (Agent) in.readObject();
        log("Agent "+agentID+" reconstructed", 2);         	

		// check for security permissions - see also: RealMobilityManager.createAgent()
        // agent is about to be created on the destination Container, 
        // let's check for permissions before

		// does the agent come from a MOVE or a CLONE ?
		switch (instance.getState()) {
		case Agent.AP_TRANSIT:  // MOVED
		  // checking CONTAINER_MOVE_TO...
		  myContainer.getAuthority().checkAction(
		  		Authority.CONTAINER_MOVE_TO, 
				myContainer.getContainerPrincipal(), 
		  		instance.getCertificateFolder()  );
		break;
		case Agent.AP_COPY:  // CLONED
		  // checking CONTAINER_CLONE_TO...
		  myContainer.getAuthority().checkAction(
		  		Authority.CONTAINER_CLONE_TO, 
		  		myContainer.getContainerPrincipal(), 
		  		instance.getCertificateFolder()  );
		break;
		} // end switch
        log("Permissions for agent "+agentID+" OK", 2);         	
    

        // Store the container where the classes for this agent can be
        // retrieved
        sites.put(instance, classSite);

        // Make the container initialize the reconstructed agent
        myContainer.initAgent(agentID, instance, startIt);
        log("Agent "+agentID+" inserted into LADT", 1);         	
    } 

    /**
       @see jade.core.MobilityManager#fetchClassFile()
     */
    public byte[] fetchClassFile(String name) throws ClassNotFoundException {
        name = name.replace('.', '/') + ".class";

        InputStream classStream = ClassLoader.getSystemResourceAsStream(name);

        if (classStream == null) {
            throw new ClassNotFoundException();
        } 

        try {
            byte[] bytes = new byte[classStream.available()];

            classStream.read(bytes);

            return (bytes);
        } 
        catch (IOException ioe) {
            throw new ClassNotFoundException();
        } 
    } 

    /**
       @see jade.core.MobilityManager#moveAgent()
     */
    public void moveAgent(AID agentID, 
                          Location where) throws NotFoundException {
                          	
        Agent agent = localAgents.acquire(agentID);

        if (agent == null) {
            throw new NotFoundException("MoveAgent failed to find " 
                                        + agentID);
        } 

        agent.doMove(where);
	localAgents.release(agentID);
    } 

    /**
       @see jade.core.MobilityManager#copyAgent()
     */
    public void copyAgent(AID agentID, Location where, 
                          String newName) throws NotFoundException {
        Agent agent = localAgents.acquire(agentID);

        if (agent == null) {
            throw new NotFoundException("CopyAgent failed to find " 
                                        + agentID);
        } 

        agent.doClone(where, newName);
	localAgents.release(agentID);
    }

    /**
       @see jade.core.MobilityManager#handleTransferResult()
     */
    public void handleTransferResult(AID agentID, boolean result, 
                                     List messages) throws NotFoundException {
        log("Activating incoming agent "+agentID, 1);                             	
        try {
	  Agent agent = localAgents.acquire(agentID);

	  if ((agent == null) || (agent.getState() != Agent.AP_TRANSIT)) {

	      throw new NotFoundException("handleTransferResult() unable to find a suitable agent.");
	  } 

	  if (result == TRANSFER_ABORT) {
	      localAgents.remove(agentID);
	  } 
	  else {

	      // Insert received messages at the start of the queue
	      for (int i = messages.size(); i > 0; i--) {
		  agent.putBack((ACLMessage) messages.get(i - 1));
	      }
        Thread t = myResourceManager.getThread(ResourceManager.USER_AGENTS, agentID.getLocalName(), agent);
	      agent.powerUp(agentID, t);
        log("Incoming agent "+agentID+" activated", 1);                             	
	  }
        }
        finally {
            localAgents.release(agentID);   
        }
    }

    /**
       @see jade.core.MobilityManager#handleMove()
     */
    public void handleMove(AID agentID, Location where) {
    	log("Moving agent "+agentID+" on container "+where.getName(), 1);
	    Agent a = localAgents.acquire(agentID);
	    if (a == null) {
				System.out.println("Internal error: handleMove() called with a wrong name (" + agentID + ") !!!");
				return;
	    } 
	  	String proto = where.getProtocol();
	  	if (!CaseInsensitiveString.equalsIgnoreCase(proto, ContainerID.DEFAULT_IMTP)) {
				System.out.println("Mobility protocol not supported. Abort transfer");
				a.doExecute();
				return;
	  	}

      int transferState = 0;
	    List    messages = new ArrayList();
	    AgentContainer dest = null;
      try {
        // Check for security permissions
      	// Note that CONTAINER_MOVE_TO will be checked on the destination container
				myContainer.getAuthority().checkAction(Authority.AGENT_MOVE, myContainer.getAgentPrincipal(agentID), a.getCertificateFolder() );
				myContainer.getAuthority().checkAction(Authority.CONTAINER_MOVE_FROM, myContainer.getContainerPrincipal(), a.getCertificateFolder() );
    		log("Permissions for agent "+agentID+" OK", 2);

	    	dest = myPlatform.lookup((ContainerID)where);
    		log("Destination container for agent "+agentID+" found", 2);
	    	transferState = 1;
	    	// If the destination container is the same as this one, there is nothing to do
	    	if (CaseInsensitiveString.equalsIgnoreCase(where.getName(), myContainer.here().getName())) {
					a.doExecute();
					return;
	    	} 

	    	// Serialize the agent
	    	ByteArrayOutputStream out = new ByteArrayOutputStream();
				ObjectOutputStream encoder = new ObjectOutputStream(out);
				encoder.writeObject(a);
	    	byte[] bytes = out.toByteArray();
    		log("Agent "+agentID+" correctly serialized", 2);

	    	// Gets the container where the agent classes can be retrieved
	    	AgentContainer classSite = (AgentContainer) sites.get(a);
	    	if (classSite == null) {    
					// The agent was born on this container
					classSite = myContainer;
	    	} 

	    	// Create the agent on the destination container
	    	dest.createAgent(agentID, bytes, classSite, AgentContainer.NOSTART);
	    	transferState = 2;
    		log("Agent "+agentID+" correctly created on destination container", 1);

	    	// Perform an atomic transaction for agent identity transfer
				// From now on, messages for the moving agent will be routed to the 
	    	// destination container
	    	boolean transferResult = myPlatform.transferIdentity(agentID, (ContainerID) myContainer.here(), (ContainerID) where);
	    	transferState = 3;
    		log("Identity of agent "+agentID+" correctly transferred", 1);
                        
	    	if (transferResult == TRANSFER_COMMIT) {
					// Send received messages to the destination container. Note that 
	  			// there is no synchronization problem as the agent is locked in the LADT
					Iterator i = a.getMessageQueue().iterator();
					while (i.hasNext()) {
						messages.add(i.next());
					} 
					dest.postTransferResult(agentID, transferResult, messages);
					a.doGone();
					localAgents.remove(agentID);
					sites.remove(a);
	    	} 
	    	else {
					a.doExecute();
					dest.postTransferResult(agentID, transferResult, messages);
	    	}
    		log("Agent "+agentID+" correctly activated on destination container", 1);
			}
    	catch (IOException ioe) {
    		// Error in agent serialization
    		System.out.println("Error in agent serialization. Abort transfer. "+ioe.getMessage());
				a.doExecute();
   	 	}
    	catch (AuthException ae) {
    		// Permission to move not owned
    		System.out.println("Permission to move not owned. Abort transfer. "+ae.getMessage());
				a.doExecute();
   	 	}
			catch(NotFoundException nfe) {
				if (transferState == 0) {
    			System.out.println("Destination container does not exist. Abort transfer. "+nfe.getMessage());
					a.doExecute();	
				}
				else if (transferState == 2) {
    			System.out.println("Transferring agent does not seem to be part of the platform. Abort transfer. "+nfe.getMessage());
					a.doExecute();	
				}
				else if (transferState == 3) {
    			System.out.println("Transferred agent not found on destination container. Can't roll back. "+nfe.getMessage());
				}
		  }
			catch(IMTPException imtpe) {
    		// Unexpected remote error
				if (transferState == 0) {
    			System.out.println("Can't retrieve destination container. Abort transfer. "+imtpe.getMessage());
					a.doExecute();	
				}
				else if (transferState == 1) {
    			System.out.println("Error creating agent on destination container. Abort transfer. "+imtpe.getMessage());
					a.doExecute();	
				}
				else if (transferState == 2) {
    			System.out.println("Error transferring agent identity. Abort transfer. "+imtpe.getMessage());
					try {
						dest.postTransferResult(agentID, TRANSFER_ABORT, messages);
						a.doExecute();	
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if (transferState == 3) {
    			System.out.println("Error activating transferred agent. Can't roll back!!!. "+imtpe.getMessage());
				}
			}
			finally {
			  localAgents.release(agentID);
			}
    }

    /**
       @see jade.core.MobilityManager#handleClone()
     */
    public void handleClone(AID agentID, Location where, String newName) { 
	    Agent a = localAgents.acquire(agentID);
	    if (a == null) {
				System.out.println("Internal error: handleClone() called with a wrong name (" + agentID + ") !!!");
				return;
	    } 
	  	String proto = where.getProtocol();
	  	if (!CaseInsensitiveString.equalsIgnoreCase(proto, ContainerID.DEFAULT_IMTP)) {
				System.out.println("Mobility protocol not supported. Abort cloning");
				return;
	  	}

      try {
        AgentContainer dest = myPlatform.lookup((ContainerID) where);
        // Check for security permissions
      	// Note that CONTAINER_CLONE_TO will be checked on the destination container
				myContainer.getAuthority().checkAction(Authority.AGENT_CLONE, myContainer.getAgentPrincipal(agentID), a.getCertificateFolder() );
				myContainer.getAuthority().checkAction(Authority.CONTAINER_CLONE_FROM, myContainer.getContainerPrincipal(), a.getCertificateFolder() );

        // Serialize the agent
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream encoder = new ObjectOutputStream(out);
        encoder.writeObject(a);
  			byte[] bytes = out.toByteArray();

        // Gets the container where the agent classes can be retrieved
        AgentContainer classSite = (AgentContainer) sites.get(a);
        if (classSite == null) {    
        	// The agent was born on this container
          classSite = myContainer;
        }

        // Create the agent on the destination container with the new AID
  			AID newID = new AID(newName, AID.ISLOCALNAME);
        dest.createAgent(newID, bytes, classSite, AgentContainer.START);
      } 
    	catch (IOException ioe) {
    		// Error in agent serialization
    		System.out.println("Error in agent serialization. Abort cloning. "+ioe.getMessage());
   	 	}
    	catch (AuthException ae) {
    		// Permission to move not owned
    		System.out.println("Permission to clone not owned. Abort cloning. "+ae.getMessage());
   	 	}
  		catch(NotFoundException nfe) {
    		System.out.println("Destination container does not exist. Abort cloning. "+nfe.getMessage());
  		}
  		catch(IMTPException imtpe) {
    		System.out.println("Unexpected remote error. Abort cloning. "+imtpe.getMessage());
  		}
			finally {
		    localAgents.release(agentID);
			}
    } 

  private void log(String s, int level) {
  	if (verbosity >= level) {
	  	System.out.println("RMM-log: "+s);
  	}
  }  
}

