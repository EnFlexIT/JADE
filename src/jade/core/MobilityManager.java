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

import java.util.List;

/**
 * Interface declaration
 * 
 * @author Giovanni Caire - TILAB
 */
interface MobilityManager {

    /**
     */
    public void initialize(Profile p, AgentContainerImpl ac, LADT la);

    /**
     * This method creates and initializes an incoming agent.
     * @param agentID The AID of the agent to be created.
     * @param serializedInstance The agent instance serialized in a sequence of bytes
     * @param classSite The AgentContainer holding the classes making up the agent
     * @param startIt A boolean flag indicating whether the agent must be started now or later
     */
    public void createAgent(AID agentID, byte[] serializedInstance, 
                            AgentContainer classSite, boolean startIt);

    /**
     * This method accepts the fully qualified class name as parameter and searches
     * the class file in the classpath
     */
    public byte[] fetchClassFile(String name) throws ClassNotFoundException;

    /**
       Force the specified <code>Agent</code> to move to the specified 
       <code>Location</code>
     */
    public void moveAgent(AID agentID, 
                          Location where) throws NotFoundException;

    /**
       Force the specified <code>Agent</code> to clone itself to the  
       specified <code>Location</code>
     */
    public void copyAgent(AID agentID, Location where, 
                          String newName) throws NotFoundException;

    /**
       Complete a move/clone operation (on the destination Location)
       depending on the result of the notification to the Main container 
       that an Agent has been moved/cloned to this Location
     */
    public void handleTransferResult(AID agentID, boolean result, 
                                     List messages) throws NotFoundException;

    /**
       Move the specified Agent to the specified Location
     */
    public void handleMove(AID agentID, Location where); 

    /**
       Clone the specified Agent to the specified Location
     */
    public void handleClone(AID agentID, Location where, String newName); 
                            
}

