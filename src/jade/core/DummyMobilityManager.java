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

import jade.util.leap.List;

/**
 * Mobility manager used in configurations that do not support mobility.
 * 
 * @author  Federico Bergenti
 * @version 1.0, 22/11/00
 */
public class DummyMobilityManager implements MobilityManager {

    /**
     * Initialization method.
     */
    public void initialize(Profile p, AgentContainerImpl ac, LADT la) {
    	return;
    }

    /**
       @see jade.core.MobilityManager#createAgent(AID agentID, byte[] serializedInstance, 
                            AgentContainer classSite, boolean startIt)
     */
    public void createAgent(AID agentID, byte[] serializedInstance, 
                            AgentContainer classSite, boolean startIt) throws Exception {
    	throw new Exception("Unsupported operation");
    } 

    /**
       @see jade.core.MobilityManager#fetchClassFile(String)
     */
    public byte[] fetchClassFile(String name) throws ClassNotFoundException {
    	throw new ClassNotFoundException("Unsupported operation");
    } 

    /**
       @see jade.core.MobilityManager#moveAgent(AID agentID, 
                          Location where) throws NotFoundException
     */
    public void moveAgent(AID agentID, 
                          Location where) throws NotFoundException {
    	return;
    } 

    /**
       @see jade.core.MobilityManager#copyAgent(AID agentID, Location where, 
                          String newName) throws NotFoundException;
     */
    public void copyAgent(AID agentID, Location where, 
                          String newName) throws NotFoundException {
    	return;
    } 

    /**
       @see jade.core.MobilityManager#handleTransferResult(AID, boolean, List)
     */
    public void handleTransferResult(AID agentID, boolean result, 
                                     List messages) throws NotFoundException {
    	return;
    } 

    /**
       @see jade.core.MobilityManager#handleMove(AID agentID, Location where)
     */
    public void handleMove(AID agentID, Location where) {
    	return;
    } 

    /**
       @see jade.core.MobilityManager#handleClone(AID agentID, Location where, String newName)
     */
    public void handleClone(AID agentID, Location where, String newName) {
    	return;
    } 

}

