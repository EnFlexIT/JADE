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

package jade.core.management;

import jade.core.ServiceFinder;
import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;

import jade.core.Profile;
import jade.core.Agent;
import jade.core.AgentState;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.AgentContainer;
import jade.core.MainContainer;

import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;

import jade.security.Authority;
import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.AuthException;

/**

   The JADE service to manage the basic agent life cycle: creation,
   destruction, suspension and resumption.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class AgentManagementService extends BaseService {

    static final String NAME = "jade.core.management.AgentManagement";



    private static final String[] OWNED_COMMANDS = new String[] {
        AgentManagementSlice.REQUEST_CREATE,
	AgentManagementSlice.REQUEST_START,
	AgentManagementSlice.REQUEST_KILL,
	AgentManagementSlice.REQUEST_STATE_CHANGE,
	AgentManagementSlice.INFORM_CREATED,
	AgentManagementSlice.INFORM_KILLED,
	AgentManagementSlice.INFORM_STATE_CHANGED,
	AgentManagementSlice.KILL_CONTAINER,
	AgentManagementSlice.ADD_TOOL,
	AgentManagementSlice.REMOVE_TOOL
    };


    public void init(AgentContainer ac, Profile p) throws ProfileException {
	super.init(ac, p);

	myContainer = ac;

    }


    public String getName() {
	return AgentManagementSlice.NAME;
    }

    public Class getHorizontalInterface() {
	try {
	    return Class.forName(AgentManagementSlice.NAME + "Slice");
	}
	catch(ClassNotFoundException cnfe) {
	    return null;
	}
    }

    public Service.Slice getLocalSlice() {
	return localSlice;
    }

    public Filter getCommandFilter(boolean direction) {
	return null;
    }


    public Sink getCommandSink(boolean side) {
	if(side == Sink.COMMAND_SOURCE) {
	    return senderSink;
	}
	else {
	    return receiverSink;
	}
    }

    public String[] getOwnedCommands() {
	return OWNED_COMMANDS;
    }


    // This inner class handles the messaging commands on the command
    // issuer side, turning them into horizontal commands and
    // forwarding them to remote slices when necessary.
    private class CommandSourceSink implements Sink {

	public void consume(VerticalCommand cmd) {
		
	    try {
		String name = cmd.getName();
		if(name.equals(AgentManagementSlice.REQUEST_CREATE)) {
		    handleRequestCreate(cmd);
		}
		else if(name.equals(AgentManagementSlice.REQUEST_START)) {
		    handleRequestStart(cmd);
		}
		else if(name.equals(AgentManagementSlice.REQUEST_KILL)) {
		    handleRequestKill(cmd);
		}
		else if(name.equals(AgentManagementSlice.REQUEST_STATE_CHANGE)) {
		    handleRequestStateChange(cmd);
		}
		else if(name.equals(AgentManagementSlice.INFORM_KILLED)) {
		    handleInformKilled(cmd);
		}
		else if(name.equals(AgentManagementSlice.INFORM_STATE_CHANGED)) {
		    handleInformStateChanged(cmd);
		}
		else if(name.equals(AgentManagementSlice.INFORM_CREATED)) {
		    handleInformCreated(cmd);
		}
		else if(name.equals(AgentManagementSlice.KILL_CONTAINER)) {
		    handleKillContainer(cmd);
		}
		else if(name.equals(AgentManagementSlice.ADD_TOOL)) {
		    handleAddTool(cmd);
		}
		else if(name.equals(AgentManagementSlice.REMOVE_TOOL)) {
		    handleRemoveTool(cmd);
		}
	    }
	    catch (Throwable t) {
	    	cmd.setReturnValue(t);
	    }
	}


	// Vertical command handler methods

	private void handleRequestCreate(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, NameClashException, ServiceException {

	    Object[] params = cmd.getParams();
	    String name = (String)params[0];
	    String className = (String)params[1];
	    String[]args = (String[])params[2];
	    ContainerID cid = (ContainerID)params[3];
	    JADEPrincipal owner = (JADEPrincipal) params[4];
	    Credentials initialCredentials = (Credentials) params[5];

	    log("Source Sink consuming command REQUEST_CREATE. Name is "+name, 3);
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
				AID agentID = new AID(name, AID.ISLOCALNAME);
				AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
				if (targetSlice != null) {
					try {
					    targetSlice.createAgent(agentID, className, args, owner, initialCredentials, cmd);
					}
					catch(IMTPException imtpe) {
					    // Try to get a newer slice and repeat...
					    targetSlice = (AgentManagementSlice)getFreshSlice(cid.getName());
					    targetSlice.createAgent(agentID, className, args, owner, initialCredentials, cmd);
					}
				}
				else {
					throw new NotFoundException("Container "+cid.getName()+" not found");
				}
	    }
	    else {
				// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}

	private void handleRequestStart(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, NameClashException, ServiceException {
		/*

	    Object[] params = cmd.getParams();
	    AID target = (AID)params[0];

	    log("Source Sink consuming command REQUEST_START. Name is "+target.getName(), 3);
	    Agent instance = myContainer.acquireLocalAgent(target);

	    if(instance == null)
		throw new NotFoundException("Start-Agent failed to find " + target);


			Credentials agentCerts = null;

	    // Notify the main container through its slice
	    AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

	    try {
		mainSlice.bornAgent(target, myContainer.getID(), agentCerts);
	    }
	    catch(IMTPException imtpe) {
		// Try to get a newer slice and repeat...
		mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
		mainSlice.bornAgent(target, myContainer.getID(), agentCerts);
	    }

	    // Actually start the agent thread
	    myContainer.powerUpLocalAgent(target, instance);

	    myContainer.releaseLocalAgent(target);
	    */
	}

	private void handleRequestKill(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, ServiceException {

	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];

	    log("Source Sink consuming command REQUEST_KILL. Name is "+agentID.getName(), 3);
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		ContainerID cid = impl.getContainerID(agentID);
		// Note that since getContainerID() succeeded, targetSlice can't be null
		AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
		try {
		    targetSlice.killAgent(agentID);
		}
		catch(IMTPException imtpe) {
		    // Try to get a newer slice and repeat...
		    targetSlice = (AgentManagementSlice)getFreshSlice(cid.getName());
		    targetSlice.killAgent(agentID);
		}
	    }
	    else {
		// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}

	private void handleRequestStateChange(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, ServiceException {

	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    AgentState as = (AgentState)params[1];

	    int newState = Agent.AP_MIN;
	    if(as.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
		newState = Agent.AP_SUSPENDED;
	    }
	    else if(as.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.WAITING)) {
		newState = Agent.AP_WAITING;
	    }
	    else if(as.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE)) {
		newState = Agent.AP_ACTIVE;
	    }

	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		ContainerID cid = impl.getContainerID(agentID);
		// Note that since getContainerID() succeeded, targetSlice can't be null
		AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
		try {
		    targetSlice.changeAgentState(agentID, newState);
		}
		catch(IMTPException imtpe) {
		    // Try to get a newer slice and repeat...
		    targetSlice = (AgentManagementSlice)getFreshSlice(cid.getName());
		    targetSlice.changeAgentState(agentID, newState);
		}
	    }
	    else {
		// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}

	private void handleInformCreated(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, AuthException, ServiceException {

	    Object[] params = cmd.getParams();
	    AID target = (AID)params[0];
	    Agent instance = (Agent)params[1];

    	log("Source Sink consuming command INFORM_CREATED. Name is "+target.getName(), 3);
	    initAgent(target, instance, cmd);
	}

	private void handleInformKilled(VerticalCommand cmd) throws IMTPException, NotFoundException, ServiceException {
	    Object[] params = cmd.getParams();
	    AID target = (AID)params[0];

    	log("Source Sink consuming command INFORM_KILLED. Name is "+target.getName(), 3);
	    // Remove the dead agent from the LADT of the container
	    myContainer.removeLocalAgent(target);

	    // Notify the main container through its slice
	    AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

	    try {
		mainSlice.deadAgent(target);
	    }
	    catch(IMTPException imtpe) {
		// Try to get a newer slice and repeat...
		mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
		mainSlice.deadAgent(target);
	    }
	}

	private void handleInformStateChanged(VerticalCommand cmd) {

	    Object[] params = cmd.getParams();
	    AID target = (AID)params[0];
	    AgentState from = (AgentState)params[1];
	    AgentState to = (AgentState)params[2];

	    if (to.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
		try {
		    // Notify the main container through its slice
		    AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

		    try {
			mainSlice.suspendedAgent(target);
		    }
		    catch(IMTPException imtpe) {
			// Try to get a newer slice and repeat...
			mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
			mainSlice.suspendedAgent(target);
		    }
		}
		catch(IMTPException re) {
		    re.printStackTrace();
		}
		catch(NotFoundException nfe) {
		    nfe.printStackTrace();
		}
		catch(ServiceException se) {
		    se.printStackTrace();
		}
	    }
	    else if (from.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
		try {
		    // Notify the main container through its slice
		    AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

		    try {
			mainSlice.resumedAgent(target);
		    }
		    catch(IMTPException imtpe) {
			// Try to get a newer slice and repeat...
			mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
			mainSlice.resumedAgent(target);
		    }
		}
		catch(IMTPException re) {
		    re.printStackTrace();
		}
		catch(NotFoundException nfe) {
		    nfe.printStackTrace();
		}
		catch(ServiceException se) {
		    se.printStackTrace();
		}
	    }
	}

	private void handleKillContainer(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {

	    Object[] params = cmd.getParams();
	    ContainerID cid = (ContainerID)params[0];

    	log("Source Sink consuming command KILL_CONTAINER. Container is "+cid.getName(), 3);
	    // Forward to the correct slice
	    AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
	    try {
	    	if (targetSlice != null) {
	    		// If target slice is null the container has already exited in the meanwhile
					targetSlice.exitContainer();
	    	}
	    }
	    catch(IMTPException imtpe) {
				// Try to get a newer slice and repeat...
	    	targetSlice = (AgentManagementSlice)getFreshSlice(cid.getName());
	    	if (targetSlice != null) {
	    		// If target slice is null the container has already exited in the meanwhile
					targetSlice.exitContainer();
	    	}
	    }

	}

	private void handleAddTool(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    AID tool = (AID)params[0];

	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		impl.toolAdded(tool);
	    }
	    else {
		// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}

	private void handleRemoveTool(VerticalCommand cmd) {
	    Object[] params = cmd.getParams();
	    AID tool = (AID)params[0];

	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		impl.toolRemoved(tool);
	    }
	    else {
		// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}


    } // End of CommandSourceSink class


    private class CommandTargetSink implements Sink {

	public void consume(VerticalCommand cmd) {
		
	    try {
		String name = cmd.getName();
		if(name.equals(AgentManagementSlice.REQUEST_CREATE)) { 
		    handleRequestCreate(cmd);
		}
		else if(name.equals(AgentManagementSlice.REQUEST_KILL)) {
		    handleRequestKill(cmd);
		}
		else if(name.equals(AgentManagementSlice.REQUEST_STATE_CHANGE)) {
		    handleRequestStateChange(cmd);
		}
		else if(name.equals(AgentManagementSlice.INFORM_KILLED)) {
		    handleInformKilled(cmd);
		}
		else if(name.equals(AgentManagementSlice.INFORM_STATE_CHANGED)) {
		    handleInformStateChanged(cmd);
		}
		else if(name.equals(AgentManagementSlice.INFORM_CREATED)) {
		    handleInformCreated(cmd);
		}
		else if(name.equals(AgentManagementSlice.KILL_CONTAINER)) {
		    handleKillContainer(cmd);
		}
	    }
	    catch (Throwable t) {
	    	cmd.setReturnValue(t);
	    }
	}


	// Vertical command handler methods

	private void handleRequestCreate(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, NameClashException, ServiceException {

	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    String className = (String)params[1];
	    Object[] arguments = (Object[])params[2];
	    JADEPrincipal owner = (JADEPrincipal)params[3];
	    Credentials initialCredentials = (Credentials)params[4];
	    
			log("Target sink consuming command REQUEST_CREATE: Name is "+agentID.getName(), 2);
	    createAgent(agentID, className, arguments, owner, initialCredentials);
	}

	private void handleRequestKill(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, ServiceException {

	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];

			log("Target sink consuming command REQUEST_KILL: Name is "+agentID.getName(), 2);
	    killAgent(agentID);
	}

	private void handleRequestStateChange(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, ServiceException {

	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    int newState = ((Integer)params[1]).intValue();

	    changeAgentState(agentID, newState);
	}

	private void handleInformCreated(VerticalCommand cmd) throws NotFoundException, NameClashException, AuthException, ServiceException {

	    Object[] params = cmd.getParams();

	    AID agentID = (AID)params[0];
	    ContainerID cid = (ContainerID)params[1];
	    Credentials certs = (Credentials)params[2];

			log("Target sink consuming command INFORM_CREATED: Name is "+agentID.getName(), 2);
	    bornAgent(agentID, cid, certs);
	}

	private void handleInformKilled(VerticalCommand cmd) throws NotFoundException, ServiceException {

	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];

			log("Target sink consuming command INFORM_KILLED: Name is "+agentID.getName(), 2);
	    deadAgent(agentID);
	}

	private void handleInformStateChanged(VerticalCommand cmd) throws NotFoundException {

	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    String newState = (String)params[1];
	    String oldState = (String)params[2];

	    if (newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED)) {
		suspendedAgent(agentID);
	    }
	    else if(newState.equals(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE)) {
		resumedAgent(agentID);
	    }
	}

	private void handleKillContainer(VerticalCommand cmd) {
			log("Target sink consuming command KILL_CONTAINER", 2);
	    exitContainer();
	}

	private void createAgent(AID agentID, String className, Object arguments[], JADEPrincipal owner, Credentials initialCredentials) throws IMTPException, NotFoundException, NameClashException, AuthException {
	    Agent agent = null;
	    try {
		agent = (Agent)Class.forName(new String(className)).newInstance();
		agent.setArguments(arguments);

		myContainer.initAgent(agentID, agent, owner, initialCredentials);
		myContainer.powerUpLocalAgent(agentID);
	    }
	    catch(ClassNotFoundException cnfe) {
		throw new IMTPException("Class " + className + " for agent " + agentID + " not found in createAgent()", cnfe);
	    }
	    catch(InstantiationException ie) {
		throw new IMTPException("Instantiation exception in createAgent()", ie);
	    }
	    catch(IllegalAccessException iae) {
		throw new IMTPException("Illegal access exception in createAgent()", iae);
	    }
	}

	private void killAgent(AID agentID) throws IMTPException, NotFoundException {

	    Agent a = myContainer.acquireLocalAgent(agentID);

	    if(a == null)
		throw new NotFoundException("Kill-Agent failed to find " + agentID);
	    a.doDelete();

	    myContainer.releaseLocalAgent(agentID);
	}

	private void changeAgentState(AID agentID, int newState) throws IMTPException, NotFoundException {
	    Agent a = myContainer.acquireLocalAgent(agentID);

	    if(a == null)
		throw new NotFoundException("Change-Agent-State failed to find " + agentID);

	    if(newState == Agent.AP_SUSPENDED) {
		a.doSuspend();
	    }
	    else if(newState == Agent.AP_WAITING) {
		a.doWait();
	    }
	    else if(newState == Agent.AP_ACTIVE) {
		int oldState = a.getState();
		if(oldState == Agent.AP_SUSPENDED) {
		    a.doActivate();
		}
		else {
		    a.doWake();
		}
	    }

	    myContainer.releaseLocalAgent(agentID);
	}

	private void bornAgent(AID name, ContainerID cid, Credentials creds) throws NameClashException, NotFoundException, AuthException {
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		try {
		    // If the name is already in the GADT, throws NameClashException
		    impl.bornAgent(name, cid, creds, false); 
		}
		catch(NameClashException nce) {
			//#CUSTOMJ2SE_EXCLUDE_BEGIN
		    try {
			ContainerID oldCid = impl.getContainerID(name);
			Node n = impl.getContainerNode(oldCid);

			// Perform a non-blocking ping to check...
			n.ping(false);

			// Ping succeeded: rethrow the NameClashException
			throw nce;
		    }
		    catch(NameClashException nce2) {
			throw nce2; // Let this one through...
		    }
		    catch(Exception e) {
			// Ping failed: forcibly replace the dead agent...
			impl.bornAgent(name, cid, creds, true);
		    }
			//#CUSTOMJ2SE_EXCLUDE_END
			/*#CUSTOMJ2SE_INCLUDE_BEGIN
			try {
				System.out.println("Replacing old agent "+name.getName());
				dyingAgents.add(name);
				((jade.core.AgentManager) impl).kill(name);
				waitUntilDead(name);
		    impl.bornAgent(name, cid, certs, false); 
			}
			catch (Exception e) {
				dyingAgents.remove(name);
				impl.bornAgent(name, cid, certs, true);
			}				
			#CUSTOMJ2SE_INCLUDE_END*/
		}
	    }
	}

	/*#CUSTOMJ2SE_INCLUDE_BEGIN
	private jade.util.leap.List dyingAgents = new jade.util.leap.ArrayList();
	
	private void waitUntilDead(AID id) {
		synchronized (dyingAgents) {
			while (dyingAgents.contains(id)) {
				try {
					dyingAgents.wait();
    		}
    		catch (Exception e) {}
    	}
    }
  }
  
	private void notifyDead(AID id) {
		synchronized (dyingAgents) {
			dyingAgents.remove(id);
			dyingAgents.notifyAll();
    }
  }
	#CUSTOMJ2SE_INCLUDE_END*/
    
	private void deadAgent(AID name) throws NotFoundException {
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		impl.deadAgent(name);
		/*#CUSTOMJ2SE_INCLUDE_BEGIN
		notifyDead(name);
		#CUSTOMJ2SE_INCLUDE_END*/
	    }
	}

	private void suspendedAgent(AID name) throws NotFoundException {
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		impl.suspendedAgent(name);
	    }
	}

	private void resumedAgent(AID name) throws NotFoundException {
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		impl.resumedAgent(name);
	    }
	}

	private void exitContainer() {
	    myContainer.shutDown();
	}


    } // End of CommandTargetSink class



    /**
       Inner mix-in class for this service: this class receives
       commands from the service <code>Sink</code> and serves them,
       coordinating with remote parts of this service through the
       <code>Service.Slice</code> interface.
    */
    private class ServiceComponent implements Service.Slice {

	// Implementation of the Service.Slice interface

	public Service getService() {
	    return AgentManagementService.this;
	}

	public Node getNode() throws ServiceException {
	    try {
		return AgentManagementService.this.getLocalNode();
	    }
	    catch(IMTPException imtpe) {
		throw new ServiceException("Problem in contacting the IMTP Manager", imtpe);
	    }
	}

	public VerticalCommand serve(HorizontalCommand cmd) {
	    VerticalCommand result = null;
	    try {
		String cmdName = cmd.getName();
		Object[] params = cmd.getParams();

		if(cmdName.equals(AgentManagementSlice.H_CREATEAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentManagementSlice.REQUEST_CREATE, AgentManagementSlice.NAME, null);
		    AID agentID = (AID)params[0];
		    String className = (String)params[1];
		    Object[] arguments = (Object[])params[2];
		    String ownership = (String)params[3];
		    Credentials certs = (Credentials)params[4];
		    gCmd.addParam(agentID);
		    gCmd.addParam(className);
		    gCmd.addParam(arguments);
		    gCmd.addParam(ownership);
		    gCmd.addParam(certs);

		    result = gCmd;
		}
		else if(cmdName.equals(AgentManagementSlice.H_KILLAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentManagementSlice.REQUEST_KILL, AgentManagementSlice.NAME, null);
		    AID agentID = (AID)params[0];
		    gCmd.addParam(agentID);

		    result = gCmd;
		}
		else if(cmdName.equals(AgentManagementSlice.H_CHANGEAGENTSTATE)) {
		    GenericCommand gCmd = new GenericCommand(AgentManagementSlice.REQUEST_STATE_CHANGE, AgentManagementSlice.NAME, null);
		    AID agentID = (AID)params[0];
		    Integer newState = (Integer)params[1];
		    gCmd.addParam(agentID);
		    gCmd.addParam(newState);

		    result = gCmd;
		}
		else if(cmdName.equals(AgentManagementSlice.H_BORNAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_CREATED, AgentManagementSlice.NAME, null);
		    AID agentID = (AID)params[0];
		    ContainerID cid = (ContainerID)params[1];
		    Credentials certs = (Credentials)params[2];
		    gCmd.addParam(agentID);
		    gCmd.addParam(cid);
		    gCmd.addParam(certs);

		    result = gCmd;
		}
		else if(cmdName.equals(AgentManagementSlice.H_DEADAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_KILLED, AgentManagementSlice.NAME, null);
		    AID agentID = (AID)params[0];
		    gCmd.addParam(agentID);

		    result = gCmd;
		}
		else if(cmdName.equals(AgentManagementSlice.H_SUSPENDEDAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_STATE_CHANGED, AgentManagementSlice.NAME, null);
		    AID agentID = (AID)params[0];
		    gCmd.addParam(agentID);
		    gCmd.addParam(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED);
		    gCmd.addParam("*");

		    result = gCmd;
		}
		else if(cmdName.equals(AgentManagementSlice.H_RESUMEDAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentManagementSlice.INFORM_STATE_CHANGED, AgentManagementSlice.NAME, null);
		    AID agentID = (AID)params[0];
		    gCmd.addParam(agentID);
		    gCmd.addParam(jade.domain.FIPAAgentManagement.AMSAgentDescription.ACTIVE);
		    gCmd.addParam(jade.domain.FIPAAgentManagement.AMSAgentDescription.SUSPENDED);

		    result = gCmd;
		}
		else if(cmdName.equals(AgentManagementSlice.H_EXITCONTAINER)) {
		    GenericCommand gCmd = new GenericCommand(AgentManagementSlice.KILL_CONTAINER, AgentManagementSlice.NAME, null);

		    result = gCmd;
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
	    }
      return result;
	}

    } // End of AgentManagementSlice class



    private void initAgent(AID target, Agent instance, VerticalCommand vCmd) throws IMTPException, AuthException, NameClashException, NotFoundException, ServiceException {
  // Connect the new instance to the local container
	Agent old = myContainer.addLocalAgent(target, instance);

	try {

	    Credentials agentCerts = null;
          //TOFIX:  --- the following needs to be replaced ----
	    //#MIDP_EXCLUDE_BEGIN
	    //CertificateFolder agentCerts = instance.getCertificateFolder();
	    //#MIDP_EXCLUDE_END

	    /*# MIDP_INCLUDE_BEGIN
	    CertificateFolder agentCerts = new CertificateFolder();

	    Authority authority = myContainer.getAuthority();

	    if(agentCerts.getIdentityCertificate() == null) {
	        AgentPrincipal principal = authority.createAgentPrincipal(target, AgentPrincipal.NONE);
		IdentityCertificate identity = authority.createIdentityCertificate();
		identity.setSubject(principal);
		authority.sign(identity, agentCerts);
		agentCerts.setIdentityCertificate(identity);
	    }
	    # MIDP_INCLUDE_END*/

		// Notify the main container through its slice
		AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);

		try {
		    mainSlice.bornAgent(target, myContainer.getID(), null);
		}
		catch(IMTPException imtpe) {
		    // Try to get a newer slice and repeat...
		    mainSlice = (AgentManagementSlice)getFreshSlice(MAIN_SLICE);
		    mainSlice.bornAgent(target, myContainer.getID(), null);
		}

		// Actually start the agent thread
		//myContainer.powerUpLocalAgent(target, instance);

	}
	catch(NameClashException nce) {
	    myContainer.removeLocalAgent(target);
	    if(old != null) {
		myContainer.addLocalAgent(target, old);
	    }
	    throw nce;
	}
	catch(IMTPException imtpe) {
	    myContainer.removeLocalAgent(target);
	    throw imtpe;
	}
	catch(NotFoundException nfe) {
	    myContainer.removeLocalAgent(target);
	    throw nfe;
	}
	catch(AuthException ae) {
	    myContainer.removeLocalAgent(target);
	    throw ae;
	}
    }

    // The concrete agent container, providing access to LADT, etc.
    private AgentContainer myContainer;

    // The local slice for this service
    private final ServiceComponent localSlice = new ServiceComponent();

    // The command sink, source side
    private final CommandSourceSink senderSink = new CommandSourceSink();

    // The command sink, target side
    private final CommandTargetSink receiverSink = new CommandTargetSink();

    // Work-around for PJAVA compilation
    protected Service.Slice getFreshSlice(String name) throws ServiceException {
    	return super.getFreshSlice(name);
    }
}
