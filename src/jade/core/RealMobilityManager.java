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
    
    // The ResourceManager of the container including this MobilityManager
    private ResourceManager    myResourceManager;

    /**
     * Inner class Deserializer
     */
    class Deserializer extends ObjectInputStream {
        private AgentContainer ac;

        /**
         * Constructor declaration
         *
         * @param inner
         * @param classSite
         *
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
                cl = new JADEClassLoader(ac);

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
	    	myResourceManager = myProfile.getResourceManager();
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
        // Reconstruct the serialized agent
        ObjectInputStream in = new Deserializer(new ByteArrayInputStream(serializedInstance), classSite);
        Agent             instance = (Agent) in.readObject();

        // Store the container where the classes for this agent can be
        // retrieved
        sites.put(instance, classSite);

        // Make the container initialize the reconstructed agent
        myContainer.initAgent(agentID, instance, startIt);
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

	      agent.powerUp(agentID, myResourceManager);
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
        Agent a = null;
        try {
	    String proto = where.getProtocol();

	    if(!CaseInsensitiveString.equalsIgnoreCase(proto, ContainerID.DEFAULT_IMTP)) {
		throw new NotFoundException("Internal error: Mobility protocol not supported !!!");
	    }

	    a = localAgents.acquire(agentID);
	    if (a == null) {
		throw new NotFoundException("Internal error: handleMove() called with a wrong name (" + agentID + ") !!!");
	    } 

	    AgentContainer dest = myProfile.getPlatform().lookup((ContainerID)where);

	    // Handle special 'running to stand still' case
	    if (CaseInsensitiveString.equalsIgnoreCase(where.getName(), myContainer.here().getName())) {
		a.doExecute();
		return;
	    } 

	    // Serialize the agent
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try {
		ObjectOutputStream encoder = new ObjectOutputStream(out);
		encoder.writeObject(a);
	    } 
	    catch (IOException ioe) {
		ioe.printStackTrace();
	    } 
	    byte[]         bytes = out.toByteArray();

	    // Gets the container where the agent classes can be retrieved
	    AgentContainer classSite = (AgentContainer) sites.get(a);
	    if (classSite == null) {    
		// The agent was born on this container
		classSite = myContainer;
	    } 

	    // Create the agent on the destination container
	    dest.createAgent(agentID, bytes, classSite, AgentContainer.NOSTART);

	    // Perform an atomic transaction for agent identity transfer
	    boolean transferResult = myProfile.getPlatform().transferIdentity(agentID, 
               (ContainerID) myContainer.here(), (ContainerID) where);
                        
	    List    messages = new ArrayList();
	    if (transferResult == TRANSFER_COMMIT) {
		// Send received messages to the destination container
		Iterator i = a.messages();
		while (i.hasNext()) {
		    messages.add(i.next());
		} 

		dest.postTransferResult(agentID, transferResult, messages);

		// From now on, messages will be routed to the new agent
		a.doGone();

		localAgents.remove(agentID);
		sites.remove(a);

	    } 
	    else {
		a.doExecute();
		dest.postTransferResult(agentID, transferResult, messages);
	    }
	}
	catch(IMTPException imtpe) {
	    imtpe.printStackTrace();
	    // FIXME: Complete undo on exception
            if(a != null)
                a.doExecute();
	}
	catch(NotFoundException nfe) {
	    nfe.printStackTrace();
	    // FIXME: Complete undo on exception
            if(a != null)
                a.doExecute();
        }
	catch(ProfileException pe) {
	    pe.printStackTrace();
	    // FIXME: Complete undo on exception
            if(a != null)
                a.doExecute();
	}
	finally {
	  localAgents.release(agentID);
	}
    }

    /**
       @see jade.core.MobilityManager#handleClone()
     */
    public void handleClone(AID agentID, Location where, String newName) { 
        try {
            String proto = where.getProtocol();

      			if(!CaseInsensitiveString.equalsIgnoreCase(proto, ContainerID.DEFAULT_IMTP)) {
                throw new NotFoundException("Internal error: Mobility protocol not supported !!!");
            } 

            AgentContainer dest = myProfile.getPlatform().lookup((ContainerID) where);
            Agent          a = localAgents.acquire(agentID);

            if (a == null) {
                throw new NotFoundException("Internal error: handleClone() called with a wrong name !!!");
            } 

            // Serialize the agent
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                ObjectOutputStream encoder = new ObjectOutputStream(out);
                encoder.writeObject(a);
            } 
            catch (IOException ioe) {
                ioe.printStackTrace();
            } 
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
    		catch(IMTPException imtpe) {
      		imtpe.printStackTrace();
    		}
    		catch(NotFoundException nfe) {
      		nfe.printStackTrace();
    		}
    		catch(ProfileException pe) {
      		pe.printStackTrace();
    		}
	finally {
	    localAgents.release(agentID);
	}
    } 

}

