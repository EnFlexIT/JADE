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
import jade.core.VerticalCommand;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Filter;
import jade.core.Node;

import jade.core.Profile;
import jade.core.Agent;
import jade.core.AgentState;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.AgentContainerImpl;
import jade.core.MainContainerImpl;

import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;

import jade.security.Authority;
import jade.security.CertificateFolder;
import jade.security.AgentPrincipal;
import jade.security.IdentityCertificate;
import jade.security.AuthException;

/**

   The JADE service to manage the basic agent life cycle: creation,
   destruction, suspension and resumption.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class AgentManagementService extends BaseService {

    /**
       The name of this service.
    */
    public static final String NAME = "jade.core.management.AgentManagement";

    /**
       This command name represents the <code>create-agent</code>
       action. The target agent identifier in this command is set to
       <code>null</code>, because no agent exists yet.
       This command object represents only the <i>first half</i> of
       the complete agent creation process. Even if this command is
       accepted by the kernel, there is no guarantee that the
       requested creation will ever happen. Only when the
       <code>InformCreated</code> command is issued can one assume
       that the agent creation has taken place.
    */
    public static final String REQUEST_CREATE = "Request-Create";

    /**
       This command name represents the <code>start-agent</code>
       action. The target agent identifier in this command has already
       been created, but its internal thread was not started at
       creation time.
    */
    public static final String REQUEST_START = "Request-Start";

    /**
       This command name represents the <code>kill-agent</code>
       action.
       This command object represents only the <i>first half</i> of
       the complete agent destruction process. Even if this command is
       accepted by the kernel, there is no guarantee that the
       requested destruction will ever happen. Only when the
       <code>InformKilled</code> command is issued can one assume that
       the agent destruction has taken place.
    */
    public static final String REQUEST_KILL = "Request-Kill";

    /**
       This command name represents all agent management actions requesting
       a change in the life cycle state of their target agent
       (suspend, resume, etc.).
       This command object represents only the <i>first half</i> of
       the complete agent state change process. Even if this command
       is accepted by the kernel, there is no guarantee that the
       requested state change will ever happen. Only when the
       <code>InformStateChanged</code> command is issued can one
       assume that the state change has taken place.
    */
    public static final String REQUEST_STATE_CHANGE = "Request-State-Change";


    // private AgentState myNewState;

    /**
       This command is issued by an agent that has just been created,
       and causes JADE runtime to actually start up the agent thread.
       The agent creation can be the outcome of a previously issued
       <code>RequestCreate</code> command. In that case, this command
       represents only the <i>second half</i> of the complete agent
       creation process.
    */
    public static final String INFORM_CREATED = "Inform-Created";



    /**
       This command is issued by an agent that has just been destroyed
       and whose thread is terminating.
       The agent destruction can either be an autonomous move of the
       agent or the outcome of a previously issued
       <code>RequestKill</code> command. In the second case, this
       command represents only the <i>second half</i> of the complete
       agent destruction process.

    */
    public static final String INFORM_KILLED = "Inform-Killed";

    /**
       This command is issued by an agent that has just changed its
       life-cycle state.
       The agent state change can either be an autonomous move of the
       agent or the outcome of a previously issued
       <code>RequestStateChange</code> command. In that case, this
       command represents only the <i>second half</i> of the complete
       agent state tansition process.
    */
    public static final String INFORM_STATE_CHANGED = "Inform-State-Changed";


    /**
       This command name represents the <code>kill-container</code>
       action.
    */
    public static final String KILL_CONTAINER = "Kill-Container";


    //    public static final String MAIN_SLICE = "Main-Slice";
    public static final String MAIN_SLICE = "Main-Container";

    public static final boolean CREATE_AND_START = true;
    public static final boolean CREATE_ONLY = false;

    public AgentManagementService(AgentContainerImpl ac, Profile p) throws ProfileException {
	super(p);

	myContainer = ac;

	// Create a local slice
	localSlice = new ServiceComponent();

    }


    public String getName() {
	return NAME;
    }

    public Class getHorizontalInterface() {
	return AgentManagementSlice.class;
    }

    public Slice getLocalSlice() {
	return localSlice;
    }

    public Filter getCommandFilter() {
	return localSlice;
    }

    /**
       Inner mix-in class for this service: this class receives
       commands through its <code>Filter</code> interface and serves
       them, coordinating with remote parts of this service through
       the <code>Slice</code> interface (that extends the
       <code>Service.Slice</code> interface).
    */
    private class ServiceComponent implements Filter, AgentManagementSlice {


	// Implementation of the Filter interface

	public void accept(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();
		if(name.equals(REQUEST_CREATE)) {
		    handleRequestCreate(cmd);
		}
		else if(name.equals(REQUEST_START)) {
		    handleRequestStart(cmd);
		}
		else if(name.equals(REQUEST_KILL)) {
		    handleRequestKill(cmd);
		}
		else if(name.equals(REQUEST_STATE_CHANGE)) {
		    handleRequestStateChange(cmd);
		}
		else if(name.equals(INFORM_KILLED)) {
		    handleInformKilled(cmd);
		}
		else if(name.equals(INFORM_STATE_CHANGED)) {
		    handleInformStateChanged(cmd);
		}
		else if(name.equals(INFORM_CREATED)) {
		    handleInformCreated(cmd);
		}
		else if(name.equals(KILL_CONTAINER)) {
		    handleKillContainer(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(new UnreachableException("Remote container is unreachable", imtpe));
	    }
	    catch(NotFoundException nfe) {
		cmd.setReturnValue(nfe);
	    }
	    catch(NameClashException nce) {
		cmd.setReturnValue(nce);
	    }
	    catch(AuthException ae) {
		cmd.setReturnValue(ae);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(new UnreachableException("A Service Exception occurred", se));
	    }
	}

	public void setBlocking(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

    	public boolean isBlocking() {
	    return false; // Blocking and Skipping not implemented
	}

	public void setSkipping(boolean newState) {
	    // Do nothing. Blocking and Skipping not supported
	}

	public boolean isSkipping() {
	    return false; // Blocking and Skipping not implemented
	}


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

	public void serve(VerticalCommand cmd) {
	    try {
		String cmdName = cmd.getName();
		Object[] params = cmd.getParams();

		if(cmdName.equals(H_CREATEAGENT)) {
		    AID agentID = (AID)params[0];
		    String className = (String)params[1];
		    Object[] arguments = (Object[])params[2];
		    String ownership = (String)params[3];
		    CertificateFolder certs = (CertificateFolder)params[4];
		    boolean startIt = ((Boolean)params[5]).booleanValue();

		    createAgent(agentID, className, arguments, ownership, certs, startIt);
		}
		else if(cmdName.equals(H_KILLAGENT)) {
		    AID agentID = (AID)params[0];

		    killAgent(agentID);
		}
		else if(cmdName.equals(H_CHANGEAGENTSTATE)) {
		    AID agentID = (AID)params[0];
		    int newState = ((Integer)params[1]).intValue();

		    changeAgentState(agentID, newState);
		}
		else if(cmdName.equals(H_BORNAGENT)) {
		    AID agentID = (AID)params[0];
		    ContainerID cid = (ContainerID)params[1];
		    CertificateFolder certs = (CertificateFolder)params[2];

		    bornAgent(agentID, cid, certs);
		}
		else if(cmdName.equals(H_DEADAGENT)) {
		    AID agentID = (AID)params[0];

		    deadAgent(agentID);
		}
		else if(cmdName.equals(H_SUSPENDEDAGENT)) {
		    AID agentID = (AID)params[0];

		    suspendedAgent(agentID);
		}
		else if(cmdName.equals(H_RESUMEDAGENT)) {
		    AID agentID = (AID)params[0];

		    resumedAgent(agentID);
		}
		else if(cmdName.equals(H_EXITCONTAINER)) {
		    exitContainer();
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
	    }
	}


	// Implementation of the service-specific horizontal interface AgentManagementSlice 


	public void createAgent(AID agentID, String className, Object arguments[], String ownership, CertificateFolder certs, boolean startIt) throws IMTPException, NotFoundException, NameClashException, AuthException {
	    Agent agent = null;
	    try {
		agent = (Agent)Class.forName(new String(className)).newInstance();
		agent.setArguments(arguments);
		//#MIDP_EXCLUDE_BEGIN
		// Set agent principal and certificates
		if(certs != null) {
		    agent.setPrincipal(certs);
		}
		// Set agent ownership
		if(ownership != null)
		    agent.setOwnership(ownership);
		else if(certs.getIdentityCertificate() != null)
		    agent.setOwnership(((AgentPrincipal)certs.getIdentityCertificate().getSubject()).getOwnership());
		//#MIDP_EXCLUDE_END

		// Execute the second half of the creation process
		initAgent(agentID, agent, startIt);
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
	    catch(ServiceException se) {
		throw new IMTPException("Service exception in createAgent()", se);
	    }
	}

	public void killAgent(AID agentID) throws IMTPException, NotFoundException {

	    Agent a = myContainer.acquireLocalAgent(agentID);

	    if(a == null)
		throw new NotFoundException("Kill-Agent failed to find " + agentID);
	    a.doDelete();

	    myContainer.releaseLocalAgent(agentID);
	}

	public void changeAgentState(AID agentID, int newState) throws IMTPException, NotFoundException {
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

	public void bornAgent(AID name, ContainerID cid, CertificateFolder certs) throws IMTPException, NameClashException, NotFoundException, AuthException {
	    MainContainerImpl impl = myContainer.getMain();
	    if(impl != null) {
		try {
		    // If the name is already in the GADT, throws NameClashException
		    impl.bornAgent(name, cid, certs, false); 
		}
		catch(NameClashException nce) {
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
			impl.bornAgent(name, cid, certs, true);
		    }
		}
	    }
	}

	public void deadAgent(AID name) throws IMTPException, NotFoundException {
	    MainContainerImpl impl = myContainer.getMain();
	    if(impl != null) {
		impl.deadAgent(name);
	    }
	}

	public void suspendedAgent(AID name) throws IMTPException, NotFoundException {
	    MainContainerImpl impl = myContainer.getMain();
	    if(impl != null) {
		impl.suspendedAgent(name);
	    }
	}

	public void resumedAgent(AID name) throws IMTPException, NotFoundException {
	    MainContainerImpl impl = myContainer.getMain();
	    if(impl != null) {
		impl.resumedAgent(name);
	    }
	}

	public void exitContainer() throws IMTPException, NotFoundException {
	    myContainer.shutDown();
	}

    } // End of AgentManagementSlice class



    // Vertical command handler methods

    private void handleRequestCreate(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, NameClashException, ServiceException {

	Object[] params = cmd.getParams();
	String name = (String)params[0];
	String className = (String)params[1];
	String[]args = (String[])params[2];
	ContainerID cid = (ContainerID)params[3];
	String ownership = (String)params[4];
	CertificateFolder certs = (CertificateFolder)params[5];

	MainContainerImpl impl = myContainer.getMain();
	if(impl != null) {
	    AID agentID = new AID(name, AID.ISLOCALNAME);
	    AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
	    targetSlice.createAgent(agentID, className, args, ownership, certs, CREATE_AND_START);
	}
	else {
	    // Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	}
    }

    private void handleRequestStart(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, NameClashException, ServiceException {

	Object[] params = cmd.getParams();
	AID target = (AID)params[0];

	Agent instance = myContainer.acquireLocalAgent(target);

	if(instance == null)
	    throw new NotFoundException("Start-Agent failed to find " + target);


	//#MIDP_EXCLUDE_BEGIN
	CertificateFolder agentCerts = instance.getCertificateFolder();
	//#MIDP_EXCLUDE_END

	/*#MIDP_INCLUDE_BEGIN
	  CertificateFolder agentCerts = new CertificateFolder();
	  #MIDP_INCLUDE_END*/

	// Notify the main container through its slice
	AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);
	mainSlice.bornAgent(target, myContainer.getID(), agentCerts);

	// Actually start the agent thread
	myContainer.powerUpLocalAgent(target, instance);

	myContainer.releaseLocalAgent(target);
    }

    private void handleRequestKill(VerticalCommand cmd) throws IMTPException, AuthException, NotFoundException, ServiceException {

	Object[] params = cmd.getParams();
	AID agentID = (AID)params[0];

	MainContainerImpl impl = myContainer.getMain();
	if(impl != null) {
	    ContainerID cid = impl.getContainerID(agentID);
	    AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
	    targetSlice.killAgent(agentID);
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

	MainContainerImpl impl = myContainer.getMain();
	if(impl != null) {
	    ContainerID cid = impl.getContainerID(agentID);
	    AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
	    targetSlice.changeAgentState(agentID, newState);
	}
	else {
	    // Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	}
    }

    private void handleInformCreated(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, AuthException, ServiceException {

	Object[] params = cmd.getParams();
	AID target = (AID)params[0];
	Agent instance = (Agent)params[1];
	boolean startIt = ((Boolean)params[2]).booleanValue();

	initAgent(target, instance, startIt);
    }

    private void handleInformKilled(VerticalCommand cmd) throws IMTPException, NotFoundException, ServiceException {
	Object[] params = cmd.getParams();
	AID target = (AID)params[0];

	// Remove the dead agent from the LADT of the container
	myContainer.removeLocalAgent(target);

	// Notify the main container through its slice
	AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);
	mainSlice.deadAgent(target);
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
		mainSlice.suspendedAgent(target);
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
		mainSlice.resumedAgent(target);
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

	// Forward to the correct slice
	AgentManagementSlice targetSlice = (AgentManagementSlice)getSlice(cid.getName());
	targetSlice.exitContainer();

    }


    private void initAgent(AID target, Agent instance, boolean startIt) throws IMTPException, AuthException, NameClashException, NotFoundException, ServiceException {
	// Connect the new instance to the local container
	Agent old = myContainer.addLocalAgent(target, instance);

	try {

	    //#MIDP_EXCLUDE_BEGIN
	    CertificateFolder agentCerts = instance.getCertificateFolder();
	    //#MIDP_EXCLUDE_END

	    /*#MIDP_INCLUDE_BEGIN
	    CertificateFolder agentCerts = new CertificateFolder();
	    #MIDP_INCLUDE_END*/

	    if(startIt) {

		// Notify the main container through its slice
		AgentManagementSlice mainSlice = (AgentManagementSlice)getSlice(MAIN_SLICE);
		mainSlice.bornAgent(target, myContainer.getID(), agentCerts);

		// Actually start the agent thread
		myContainer.powerUpLocalAgent(target, instance);
	    }

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
    private AgentContainerImpl myContainer;

    // The local slice for this service
    private ServiceComponent localSlice;

}

