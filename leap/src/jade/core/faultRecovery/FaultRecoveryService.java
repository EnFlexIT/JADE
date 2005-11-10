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

package jade.core.faultRecovery;

//#J2ME_EXCLUDE_FILE

import jade.core.ServiceFinder;
import jade.core.VerticalCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Filter;
import jade.core.Agent;
import jade.core.AID;
import jade.core.Node;
import jade.core.NodeDescriptor;
import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.management.AgentManagementSlice;

import jade.lang.acl.LEAPACLCodec;
import jade.imtp.leap.NodeSerializer;
import jade.util.Logger;

import java.io.*;
import java.util.Map;
import java.util.Iterator;


/**
   The FaultRecovery service allows recovering a platform after a fault
   and a successive restart of the main container.
   
   On a Main container this service keeps track of platform nodes in a 
   persistent storage. When the platform shuts down the persistent storage
   is cleared. At bootstrap time the service gets all nodes from
   the persistent storage (there are nodes only if the main container 
   is restarting after a crash) and notifies them (if still alive) about
   about the recovery.
   
   On peripheral containers, the node, when notified about a main recovery,
   re-adds itself to the recovered main (actually this is done by the
   ServiceManager) and issues a Service.REATTACHED incoming V-Command.
   The FaultRecovery service filter intercepts this command and re-adds
   all agents living in the container.
   
   @author Giovanni Caire - TILAB
 */
public class FaultRecoveryService extends BaseService {
	public static final String NAME = "jade.core.faultRecovery.FaultRecovery";
	
	public static final String CLEAN_STORAGE = "jade_core_faultRecovery_FaultRecoveryService_cleanstorage";
	public static final String PERSISTENT_STORAGE_CLASS = "jade_core_faultRecovery_FaultRecoveryService_persistentstorage";
	public static final String PERSISTENT_STORAGE_CLASS_DEFAULT = "jade.core.faultRecovery.FSPersistentStorage";
	
	private AgentContainer myContainer;
	private MainContainer myMain;
	
	private Filter inpFilter;
	private Filter outFilter;
	
	private PersistentStorage myPS;
	private NodeSerializer nodeSerializer;
	private AIDSerializer aidSerializer;
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		myContainer = ac;
		myMain = myContainer.getMain();
		if (myMain != null) {
			// Create the command filters
			inpFilter = new MainCommandIncomingFilter();
			outFilter = new MainCommandOutgoingFilter();
			
			// Initialize the serializers
			nodeSerializer = new NodeSerializer();
			aidSerializer = new AIDSerializer();
			
			// Initialize the PersistentStorage
			String psClass = p.getParameter(PERSISTENT_STORAGE_CLASS, PERSISTENT_STORAGE_CLASS_DEFAULT);
			try {
				myLogger.log(Logger.CONFIG, "Loading PersistentStorage of class "+psClass);
				myPS = (PersistentStorage) Class.forName(psClass).newInstance();
				myPS.init(p);
				boolean cleanStorage = p.getBooleanProperty(CLEAN_STORAGE, false);
				if (cleanStorage) {
					myLogger.log(Logger.CONFIG, "Clearing PersistentStorage ...");
					myPS.clear();
				}
			}
			catch (Exception e) {
				String msg = "Error initializing PersistentStorage. ";
				myLogger.log(Logger.SEVERE, msg+e);
				if (myPS != null) {
					myPS.close();
				}
				throw new ProfileException(msg, e);
			}
		}
		else {
			// Create the command incoming filter
			inpFilter = new ContainerCommandIncomingFilter();
		}
	}

	public void boot(Profile p) throws ServiceException {
		if (myMain != null) {
			try {
				String oldAddress = myPS.getLocalAddress();
				String newAddress = myContainer.getServiceManager().getLocalAddress();
				myPS.storeLocalAddress(newAddress);
				
				if (oldAddress != null) {
					myLogger.log(Logger.INFO, "Initiating fault recovery procedure...");
					// Recover all non-child nodes first
					Map allNodes = myPS.getAllNodes(false);
					Iterator it = allNodes.keySet().iterator();
					while (it.hasNext()) {
						String name = (String) it.next();
						checkNode(name, (byte[]) allNodes.get(name), oldAddress, newAddress);
					}
					// Then recover all child nodes
					allNodes = myPS.getAllNodes(true);
					it = allNodes.keySet().iterator();
					while (it.hasNext()) {
						String name = (String) it.next();
						checkNode(name, (byte[]) allNodes.get(name), oldAddress, newAddress);
					}
					
					// Finally recover all tools
					Map allTools = myPS.getAllTools();
					it = allTools.keySet().iterator();
					while (it.hasNext()) {
						String name = (String) it.next();
						checkTool(name, (byte[]) allTools.get(name));
					}
					myLogger.log(Logger.INFO, "Fault recovery procedure completed.");
				}
			}
			catch (Exception e) {
				String msg = "Error recovering from previous fault. ";
				myLogger.log(Logger.SEVERE, msg+e);
				throw new ServiceException(msg, e);
			}
		}
	}

	public void shutdown() {
		if (myPS != null) {
			try {
				myPS.clear();
				myPS.close();
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Unexpected error clearing PersistentStorage. "+e);
				e.printStackTrace();
			}
		}
	}

	public String getName() {
		return NAME;
	}

	public Filter getCommandFilter(boolean direction) {
		if(direction == Filter.INCOMING) {
			return inpFilter;
		}
		else {
			return outFilter;
		}
	}


	private void checkNode(String name, byte[] nn, String oldAddress, String currentAddress) {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Recovering node "+name+" ...");
		}
		Node node = null;
		try {
			node = nodeSerializer.deserialize(nn);
			node.platformManagerDead(oldAddress, currentAddress);
			myLogger.log(Logger.INFO, "Node "+name+" recovered.");
		}
		catch (IMTPException imtpe) {
			myLogger.log(Logger.INFO, "Node "+name+" unreachable. It has likely been killed in the meanwhile");
			// Remove the node from the PS
			try {
				myPS.removeNode(node.getName());
			}
			catch (Exception ex) {
				myLogger.log(Logger.WARNING, "Cannot remove node "+node.getName()+" from persistent storage. "+ex);
				ex.printStackTrace();
			}
		}
		catch (Exception e) {
			myLogger.log(Logger.WARNING, "Error recovering node "+name+". "+e);
			e.printStackTrace();
		}
	}
	
	private void checkTool(String name, byte[] tt) {
		if (myLogger.isLoggable(Logger.FINE)) {
			myLogger.log(Logger.FINE, "Recovering subscribed tool "+name+" ...");
		}
		// FIXME: to be implemented
	}
	
	
	/**
	   Inner class MainCommandIncomingFilter.
	   This filter is installed on a Main Container and intercepts 
	   the NEW_NODE and DEAD_NODE V-Commands
	 */
	private class MainCommandIncomingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			try {
				if (name.equals(Service.NEW_NODE)) {
					handleNewNode((NodeDescriptor) cmd.getParams()[0]);
				}
				else if (name.equals(Service.DEAD_NODE)) {
					handleDeadNode((NodeDescriptor) cmd.getParams()[0]);
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error processing command "+name+". "+e);
				e.printStackTrace();
			}
			
	    // Never veto a command
	    return true;
		}
	} // END of inner class MainCommandIncomingFilter
	
	
	/**
	   Inner class MainCommandOutgoingFilter.
	   This filter is installed on a Main Container and intercepts 
	   the ADD_TOOL and REMOVE_TOOL VCommands of the AgentManagementService
	 */
	private class MainCommandOutgoingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			try {
				if (name.equals(AgentManagementSlice.ADD_TOOL)) {
					handleAddTool((AID) cmd.getParams()[0]);
				}
				else if (name.equals(AgentManagementSlice.REMOVE_TOOL)) {
					handleRemoveTool((AID) cmd.getParams()[0]);
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error processing command "+name+". "+e);
				e.printStackTrace();
			}
			
	    // Never veto a command
	    return true;
		}
	} // END of inner class MainCommandOutgoingFilter
	

	/**
	   Inner class ContainerCommandIncomingFilter.
	   This filter is installed on a peripheral Container and intercepts 
	   the REATTACHED V-Commands
	 */
	private class ContainerCommandIncomingFilter extends Filter {
		public boolean accept(VerticalCommand cmd) {
			String name = cmd.getName();
			try {
				if (name.equals(Service.REATTACHED)) {
					handleReattached();
				}
			}
			catch (Exception e) {
				myLogger.log(Logger.WARNING, "Error processing command "+name+". "+e);
				e.printStackTrace();
			}
			
	    // Never veto a command
	    return true;
		}
	} // END of inner class ContainerCommandIncomingFilter
	
	   
	////////////////////////////////////////////
	// Methods called by the filters
	////////////////////////////////////////////
	
	/**
	   Add a newly born node to the persistent storage
	 */
	private void handleNewNode(NodeDescriptor dsc) throws Exception {
		Node node = dsc.getNode();
		if (!node.hasPlatformManager()) {
			byte[] nn = nodeSerializer.serialize(node);
			myPS.storeNode(node.getName(), (dsc.getParentNode() != null), nn);			
			myLogger.log(Logger.FINE, "Node "+node.getName()+" added to persistent storage.");
		}
	}
	
	/**
	   Remove a dead node from the persistent storage
	 */
	private void handleDeadNode(NodeDescriptor dsc) throws Exception {
		Node node = dsc.getNode();
		if (!node.hasPlatformManager()) {
			myPS.removeNode(node.getName());			
			myLogger.log(Logger.FINE, "Node "+node.getName()+" removed from persistent storage.");
		}
	}
	
	/**
	   Add a newly subscribed tool to the persistent storage
	 */
	private void handleAddTool(AID tool) throws Exception {
		byte[] tt = aidSerializer.serialize(tool);
		myPS.storeTool(tool.getName(), tt);			
		myLogger.log(Logger.FINE, "Tool "+tool.getName()+" added to persistent storage.");
	}
	
	/**
	   Remove an unsubscribed node from the persistent storage
	 */
	private void handleRemoveTool(AID tool) throws Exception {
		myPS.removeTool(tool.getName());			
		myLogger.log(Logger.FINE, "Tool "+tool.getName()+" removed from persistent storage.");
	}
	
	/**
	   The container reattached to a recovered Main. Inform the new Main about 
	   all local agents.
	 */
	private void handleReattached() {
		if(myLogger.isLoggable(Logger.INFO)) {
			myLogger.log(Logger.INFO, "Re-adding all local agents to recovered main container...");
		}
		AID[] ids = myContainer.agentNames();
		for (int i = 0; i < ids.length; ++i) {
			AID id = ids[i];
			Agent agent = myContainer.acquireLocalAgent(id);
			if (agent != null) {
				if(myLogger.isLoggable(Logger.CONFIG)) {
					myLogger.log(Logger.CONFIG, "Re-adding agent "+id.getName());
				}
				try {
					// Note that we pass null owner principal and null initial credentials.
					// The Security service will insert the existing ones.
					myContainer.initAgent(id, agent, null, null);
				}
				catch (Exception e) {
					myLogger.log(Logger.SEVERE, "Error reattaching agent "+id.getName()+". "+e);
					e.printStackTrace();
				}
			}
			myContainer.releaseLocalAgent(id);
		}
	}

	
	/**
	   Inner class AIDSerializer
	 */
	private class AIDSerializer {
		public byte[] serialize(AID aid) throws Exception {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			LEAPACLCodec.serializeAID(aid, dos);
			return baos.toByteArray();
		}
		
		public AID deserialize(byte[] bb) throws Exception {
			ByteArrayInputStream bais = new ByteArrayInputStream(bb);
			DataInputStream dis = new DataInputStream(bais);
			return LEAPACLCodec.deserializeAID(dis);
		}
	} // END of inner class AIDSerializer
}
