/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

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

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
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
 * 
 * @author LEAP
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
    } 

    // IMPLEMENTATION OF INTERFACE MobilityHandler

    /**
       @see jade.core.MobilityManager#createAgent()
     */
    public void createAgent(AID agentID, byte[] serializedInstance, 
                            AgentContainer classSite, boolean startIt) {
        try {

            // Reconstruct the serialized agent
            ObjectInputStream in = new Deserializer(new ByteArrayInputStream(serializedInstance), classSite);
            Agent             instance = (Agent) in.readObject();

            // Store the container where the classes for this agent can be
            // retrieved
            sites.put(instance, classSite);

            // Make the container initialize the reconstructed agent
            myContainer.initAgent(agentID, instance, startIt);
        } 
        catch (IOException ioe) {
            ioe.printStackTrace();
        } 
        catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } 
        catch (ClassCastException cce) {
            cce.printStackTrace();
        } 
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
        Agent agent = localAgents.get(agentID);

        if (agent == null) {
            throw new NotFoundException("MoveAgent failed to find " 
                                        + agentID);
        } 

        agent.doMove(where);
    } 

    /**
       @see jade.core.MobilityManager#copyAgent()
     */
    public void copyAgent(AID agentID, Location where, 
                          String newName) throws NotFoundException {
        Agent agent = localAgents.get(agentID);

        if (agent == null) {
            throw new NotFoundException("CopyAgent failed to find " 
                                        + agentID);
        } 

        agent.doClone(where, newName);
    } 

    /**
       @see jade.core.MobilityManager#handleTransferResult()
     */
    public void handleTransferResult(AID agentID, boolean result, 
                                     List messages) throws NotFoundException {
        synchronized (localAgents) {
            Agent agent = localAgents.get(agentID);

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

                agent.powerUp(agentID, ResourceManager.USER_AGENTS);
            } 
        } 
    } 

    /**
       @see jade.core.MobilityManager#handleMove()
     */
    public void handleMove(AID agentID, Location where) {
        // Mutual exclusion with AgentContainerImpl.dispatch() method
        synchronized (localAgents) {
            try {
                String proto = where.getProtocol();

								if(!proto.equalsIgnoreCase(ContainerID.DEFAULT_IMTP)) {
                    throw new NotFoundException("Internal error: Mobility protocol not supported !!!");
                } 

                AgentContainer dest = myProfile.getMain().lookup((ContainerID)where);
                Agent          a = localAgents.get(agentID);

                if (a == null) {
                    throw new NotFoundException("Internal error: handleMove() called with a wrong name !!!");
                } 

                // Handle special 'running to stand still' case
                if (where.getName().equalsIgnoreCase(myContainer.here().getName())) {
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
                boolean transferResult = myProfile.getMain().transferIdentity(agentID, 
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
							Agent a = localAgents.get(agentID);
							if(a != null){
	  						a.doDelete();
							}
      			}
      			catch(NotFoundException nfe) {
							nfe.printStackTrace();
							// FIXME: Complete undo on exception
							Agent a = localAgents.get(agentID);
							if(a != null) {
	  						a.doDelete();
							}
      			}
      			catch(ProfileException pe) {
							pe.printStackTrace();
							// FIXME: Complete undo on exception
							Agent a = localAgents.get(agentID);
							if(a != null) {
	  						a.doDelete();
							}
      			}
        }  // END of synchronized
    } 

    /**
       @see jade.core.MobilityManager#handleClone()
     */
    public void handleClone(AID agentID, Location where, String newName) { 
        try {
            String proto = where.getProtocol();

      			if(!proto.equalsIgnoreCase(ContainerID.DEFAULT_IMTP)) {
                throw new NotFoundException("Internal error: Mobility protocol not supported !!!");
            } 

            AgentContainer dest = myProfile.getMain().lookup((ContainerID) where);
            Agent          a = localAgents.get(agentID);

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
    } 

}

