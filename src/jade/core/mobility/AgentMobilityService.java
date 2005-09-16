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

package jade.core.mobility;

//#MIDP_EXCLUDE_FILE

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.InterruptedIOException;
import java.util.StringTokenizer;
import java.util.zip.*;

import java.net.URL;

import jade.core.ServiceFinder;
import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.Command;
import jade.core.GenericCommand;
import jade.core.Service;
import jade.core.ServiceHelper;
import jade.core.BaseService;
import jade.core.ServiceException;
import jade.core.Sink;
import jade.core.Filter;
import jade.core.Node;
import jade.core.LifeCycle;

import jade.core.Profile;
import jade.core.Agent;
import jade.core.Agent.Interrupted;
import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.AgentContainer;
import jade.core.MainContainer;
import jade.core.AgentDescriptor;

import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.core.NotFoundException;

import jade.core.management.AgentManagementService;

import jade.lang.acl.ACLMessage;

import jade.security.Credentials;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;
import jade.security.CredentialsHelper;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.Logger;

/**
   The JADE service to manage mobility-related agent life cycle: migration
   and clonation.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
   @author Giovanni Caire - TILAB
*/
public class AgentMobilityService extends BaseService {
	public static final String NAME = AgentMobilitySlice.NAME;

  public static final int AP_TRANSIT = 7;
  public static final int AP_COPY = 8;
  public static final int AP_GONE = 9;


    private static final String[] OWNED_COMMANDS = new String[] {
	AgentMobilityHelper.REQUEST_MOVE,
	AgentMobilityHelper.REQUEST_CLONE,
	AgentMobilityHelper.INFORM_MOVED,
	AgentMobilityHelper.INFORM_CLONED
    };


    static final boolean MIGRATION = false;
    static final boolean CLONING = true;

    static final boolean CREATE_AND_START = true;
    static final boolean CREATE_ONLY = false;

    static final boolean TRANSFER_ABORT = false;
    static final boolean TRANSFER_COMMIT = true;


    public void init(AgentContainer ac, Profile p) throws ProfileException {
	super.init(ac, p);

	myContainer = ac;
    }

    public String getName() {
	return AgentMobilitySlice.NAME;
    }

    public Class getHorizontalInterface() {
	return AgentMobilitySlice.class;
    }

    public Service.Slice getLocalSlice() {
	return localSlice;
    }

    public ServiceHelper getHelper(Agent a) {
	return new AgentMobilityHelperImpl();
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

    /**
     * Retrieve the name of the container where the classes of a given agent can be found
     */
    public String getClassSite(Agent a) {
    	return (String) sites.get(a);
    }
    
    // This inner class handles the messaging commands on the command
    // issuer side, turning them into horizontal commands and
    // forwarding them to remote slices when necessary.
    private class CommandSourceSink implements Sink {

	public void consume(VerticalCommand cmd) {
	    try {
		String name = cmd.getName();
		if(name.equals(AgentMobilityHelper.REQUEST_MOVE)) {
		    handleRequestMove(cmd);
		}
		else if(name.equals(AgentMobilityHelper.REQUEST_CLONE)) {
		    handleRequestClone(cmd);
		}
		else if(name.equals(AgentMobilityHelper.INFORM_MOVED)) {
		    handleInformMoved(cmd);
		}
		else if(name.equals(AgentMobilityHelper.INFORM_CLONED)) {
		    handleInformCloned(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(imtpe);
	    }
	    catch(NotFoundException nfe) {
		cmd.setReturnValue(nfe);
	    }
	    catch(NameClashException nce) {
		cmd.setReturnValue(nce);
	    }
	    catch(JADESecurityException ae) {
		cmd.setReturnValue(ae);
	    }
	    catch(ServiceException se) {
		cmd.setReturnValue(new IMTPException("Service error", se));
	    }
	}


	// Vertical command handler methods

	private void handleRequestMove(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    Location where = (Location)params[1];

	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		ContainerID cid = impl.getContainerID(agentID);
		AgentMobilitySlice targetSlice = (AgentMobilitySlice)getSlice(cid.getName());
		try {
		    targetSlice.moveAgent(agentID, where);
		}
		catch(IMTPException imtpe) {
		    // Try to get a newer slice and repeat...
		    targetSlice = (AgentMobilitySlice)getFreshSlice(cid.getName());
		    targetSlice.moveAgent(agentID, where);
		}
	    }
	    else {
		// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}

	private void handleRequestClone(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    Location where = (Location)params[1];
	    String newName = (String)params[2];

	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
		ContainerID cid = impl.getContainerID(agentID);
		AgentMobilitySlice targetSlice = (AgentMobilitySlice)getSlice(cid.getName());
		try {
		    targetSlice.copyAgent(agentID, where, newName);
		}
		catch(IMTPException imtpe) {
		    // Try to get a newer slice and repeat...
		    targetSlice = (AgentMobilitySlice)getFreshSlice(cid.getName());
		    targetSlice.copyAgent(agentID, where, newName);
		}
	    }
	    else {
		// Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	    }
	}

	private void handleInformMoved(VerticalCommand cmd) throws IMTPException, ServiceException, JADESecurityException, NotFoundException {
		Object[] params = cmd.getParams();
		AID agentID = (AID)params[0];
		Location where = (Location)params[1];
		
    if(myLogger.isLoggable(Logger.INFO))
      myLogger.log(Logger.INFO,"Moving agent " + agentID + " on container " + where.getName());
		
		Agent a = myContainer.acquireLocalAgent(agentID);
		if (a == null) {
			myLogger.log(Logger.SEVERE,"Internal error: handleMove() called with a wrong name (" + agentID + ") !!!");
			return;
		}
		String proto = where.getProtocol();
		if(!CaseInsensitiveString.equalsIgnoreCase(proto, ContainerID.DEFAULT_IMTP)) {
			myLogger.log(Logger.SEVERE,"Mobility protocol not supported. Aborting transfer");
			a.restoreBufferedState();
			return;
		}

		int transferState = 0;
		List messages = new ArrayList();
		AgentMobilitySlice dest = null;
		try {
			// If the destination container is the same as this one, there is nothing to do
			if (CaseInsensitiveString.equalsIgnoreCase(where.getName(), myContainer.here().getName())) {
				return;
			}

			dest = (AgentMobilitySlice) getSlice(where.getName());
			if (dest == null) {
				myLogger.log(Logger.SEVERE,"Destination does not exist or does not support mobility");
				return;
			}
			if(myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE,"Destination container for agent " + agentID + " found");
			}

			transferState = 1;

			// Serialize the agent
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream encoder = new ObjectOutputStream(out);
			encoder.writeObject(a);
			byte[] bytes = out.toByteArray();
			if(myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE,"Agent " + agentID + " correctly serialized");
			}
    
			// Gets the container where the agent classes can be retrieved
			String classSiteName = (String)sites.get(a);			
			if (classSiteName == null) {
			    // The agent was born on this container
			    classSiteName = getLocalNode().getName();
			}
			
			// Create the agent on the destination container
			try {
			    dest.createAgent(agentID, bytes, classSiteName, MIGRATION, CREATE_ONLY);
			}
			catch(IMTPException imtpe) {
			    // Try to get a newer slice and repeat...
			    dest = (AgentMobilitySlice)getFreshSlice(where.getName());
			    dest.createAgent(agentID, bytes, classSiteName, MIGRATION, CREATE_ONLY);
			}
			
			transferState = 2;
			if(myLogger.isLoggable(Logger.FINE)) {
				myLogger.log(Logger.FINE,"Agent " + agentID + " correctly created on destination container");
			}

			AgentMobilitySlice mainSlice = (AgentMobilitySlice)getSlice(MAIN_SLICE);
			
			// Perform an atomic transaction for agent identity transfer
			// From now on, messages for the moving agent will be routed to the
			// destination container
			boolean transferResult = false;
			try {
			    transferResult = mainSlice.transferIdentity(agentID, (ContainerID) myContainer.here(), (ContainerID) where);
			}
			catch(IMTPException imtpe) {
			    // Try to get a newer slice and repeat...
			    mainSlice = (AgentMobilitySlice)getFreshSlice(MAIN_SLICE);
			    transferResult = mainSlice.transferIdentity(agentID, (ContainerID) myContainer.here(), (ContainerID) where);
			}
			
			transferState = 3;
			
			if (transferResult == TRANSFER_COMMIT) {
				if(myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE,"Identity of agent " + agentID + " correctly transferred");
				}

				// Send received messages to the destination container. Note that
				// there is no synchronization problem as the agent is locked in the LADT
				myContainer.fillListFromMessageQueue(messages, a);
				
				dest.handleTransferResult(agentID, transferResult, messages);
				
				try {
				  // Cause the invocation of 'beforeMove()' and the
				  // subsequent termination of the agent thread
					a.changeStateTo(new LifeCycle(AP_GONE) {
						public boolean alive() {
							return false;
						}
					});
					
				  //AgentMobilityHelperImpl mobHelper = (AgentMobilityHelperImpl) a.getHelper(AgentMobilityHelper.NAME);
				  //mobHelper.gone();
				  // Remove the gone agent from the LADT
				  myContainer.removeLocalAgent(a.getAID());
				}
				catch (Exception e) {
					// Should never happen
					e.printStackTrace();
				}
				sites.remove(a);
				if(myLogger.isLoggable(Logger.FINE)) {
					myLogger.log(Logger.FINE,"Agent " + agentID + " correctly gone");
				}
			}
			else {
			    myLogger.log(Logger.WARNING,"Error transferring identity of agent " + agentID);
			
					a.restoreBufferedState();
			    dest.handleTransferResult(agentID, transferResult, messages);
			    myLogger.log(Logger.WARNING,"Migration of agent " + agentID + "aborted");
			}
		}
		//#DOTNET_EXCLUDE_BEGIN
		catch (IOException ioe) {
			// Error in agent serialization
			myLogger.log(Logger.SEVERE,"Error in agent serialization. Abort transfer. " + ioe);
		}
		catch (JADESecurityException ae) {
			// Permission to move not owned
			myLogger.log(Logger.SEVERE,"Permission to move not owned. Abort transfer. " + ae.getMessage());
    }
    catch(NotFoundException nfe) {
			if(transferState == 0) {
				myLogger.log(Logger.SEVERE,"Destination container does not exist. Abort transfer. " + nfe.getMessage());
			}
			else if(transferState == 2) {
				myLogger.log(Logger.SEVERE,"Transferring agent does not seem to be part of the platform. Abort transfer. " + nfe.getMessage());
			}
			else if(transferState == 3) {
				// PANIC !!!
				myLogger.log(Logger.SEVERE,"Transferred agent not found on destination container. Can't roll back. " + nfe.getMessage());
			}
		}
		catch(NameClashException nce) {
			// This should not happen, because the agent is not changing its name but just its location...
			nce.printStackTrace();
		}
		catch(IMTPException imtpe) {
			// Unexpected remote error
			if (transferState == 0) {
				myLogger.log(Logger.SEVERE,"Can't retrieve destination container. Abort transfer. " + imtpe.getMessage());
			}
			else if (transferState == 1) {
				myLogger.log(Logger.SEVERE,"Error creating agent on destination container. Abort transfer. " + imtpe.getMessage());
			}
			else if (transferState == 2) {
				myLogger.log(Logger.SEVERE,"Error transferring agent identity. Abort transfer. " + imtpe.getMessage());

				try {
					dest.handleTransferResult(agentID, TRANSFER_ABORT, messages);
				}
				catch (Exception e) {
					e.printStackTrace();
		    }
			}
			else if (transferState == 3) {
				// PANIC !!!
				myLogger.log(Logger.SEVERE,"Error activating transferred agent. Can't roll back!!!. " + imtpe.getMessage());
			}
		}
		//#DOTNET_EXCLUDE_END
		/*#DOTNET_INCLUDE_BEGIN
		catch(System.Exception exc)
		{
			if(myLogger.isLoggable(Logger.SEVERE))
				myLogger.log(Logger.SEVERE,"Error in agent serialization. Abort transfer. " + exc.get_Message());
		}
		#DOTNET_INCLUDE_END*/
		finally {
			if (transferState <= 2) {
				// Something went wrong --> Roll back.
				a.restoreBufferedState();
			}
			myContainer.releaseLocalAgent(agentID);
		}
	}

	private void handleInformCloned(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, JADESecurityException { // HandleInformCloned start
	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    Location where = (Location)params[1];
	    String newName = (String)params[2];

	    try {

	  //log("Cloning agent " + agentID + " on container " + where.getName(), 1);
          if(myLogger.isLoggable(Logger.INFO))
            myLogger.log(Logger.INFO,"Cloning agent " + agentID + " on container " + where.getName());

		Agent a = myContainer.acquireLocalAgent(agentID);
		if (a == null) {
		    //System.out.println("Internal error: handleClone() called with a wrong name (" + agentID + ") !!!");
                    if(myLogger.isLoggable(Logger.SEVERE))
                      myLogger.log(Logger.SEVERE,"Internal error: handleClone() called with a wrong name (" + agentID + ") !!!");
		    return;
		}
		String proto = where.getProtocol();
		if (!CaseInsensitiveString.equalsIgnoreCase(proto, ContainerID.DEFAULT_IMTP)) {
		    //System.out.println("Mobility protocol not supported. Abort cloning");
                    if(myLogger.isLoggable(Logger.SEVERE))
                      myLogger.log(Logger.SEVERE,"Mobility protocol not supported. Abort cloning");
		    return;
		}

		/* --- This code should go into the Security Service ---

		// Check for security permissions
		// Note that CONTAINER_CLONE_TO will be checked on the destination container
		myContainer.getAuthority().checkAction(Authority.AGENT_CLONE, myContainer.getAgentPrincipal(agentID), a.getCertificateFolder() );
		myContainer.getAuthority().checkAction(Authority.CONTAINER_CLONE_FROM, myContainer.getContainerPrincipal(), a.getCertificateFolder() );

		log("Permissions for agent " + agentID + " OK", 2);
		// --- End of code that should go into the Security Service ---
		*/

		AgentMobilitySlice dest = (AgentMobilitySlice)getSlice(where.getName());
		//log("Destination container for agent " + agentID + " found", 2);
                if(myLogger.isLoggable(Logger.INFO))
                  myLogger.log(Logger.INFO,"Destination container for agent " + agentID + " found");


		// Serialize the agent
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream encoder = new ObjectOutputStream(out);
		encoder.writeObject(a);
		byte[] bytes = out.toByteArray();
		//log("Agent " + agentID + " correctly serialized", 2);
                if(myLogger.isLoggable(Logger.INFO))
                  myLogger.log(Logger.INFO,"Agent " + agentID + " correctly serialized");


		// Gets the container where the agent classes can be retrieved
		String classSiteName = (String)sites.get(a);
		if (classSiteName == null) {
		    // The agent was born on this container
		    classSiteName = getLocalNode().getName();
		}

		// Create the agent on the destination container with the new AID
		AID newID = new AID(newName, AID.ISLOCALNAME);
		try {
		    dest.createAgent(newID, bytes, classSiteName, CLONING, CREATE_AND_START);
		}
		catch(IMTPException imtpe) {
		    // Try to get a newer slice and repeat...
		    dest = (AgentMobilitySlice)getFreshSlice(where.getName());
		    dest.createAgent(newID, bytes, classSiteName, CLONING, CREATE_AND_START);
		}
		//log("Cloned Agent " + newID + " correctly created on destination container", 1);
                if(myLogger.isLoggable(Logger.INFO))
                  myLogger.log(Logger.INFO,"Cloned Agent " + newID + " correctly created on destination container");

	    }
	    catch (IOException ioe) {
		// Error in agent serialization
		throw new IMTPException("I/O serialization error in handleInformCloned()", ioe);
	    }
	    catch(ServiceException se) {
		throw new IMTPException("Destination container not found in handleInformCloned()", se);
	    }
	    finally {
		myContainer.releaseLocalAgent(agentID);
	    }
	}

    } // End of CommandSourceSink class


    // This inner class handles the messaging commands on the command
    // issuer side, turning them into horizontal commands and
    // forwarding them to remote slices when necessary.
    private class CommandTargetSink implements Sink {

	public void consume(VerticalCommand cmd) {

	    try {
		String name = cmd.getName();
		if(name.equals(AgentMobilityHelper.REQUEST_MOVE)) {
		    handleRequestMove(cmd);
		}
		else if(name.equals(AgentMobilityHelper.REQUEST_CLONE)) {
		    handleRequestClone(cmd);
		}
		else if(name.equals(AgentMobilityHelper.INFORM_MOVED)) {
		    handleInformMoved(cmd);
		}
		else if(name.equals(AgentMobilityHelper.INFORM_CLONED)) {
		    handleInformCloned(cmd);
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
	    }
	}

	private void handleRequestMove(VerticalCommand cmd) throws IMTPException, NotFoundException {
	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    Location where = (Location)params[1];

	    moveAgent(agentID, where);
	}

	private void handleRequestClone(VerticalCommand cmd) throws IMTPException, NotFoundException {
	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    Location where = (Location)params[1];
	    String newName = (String)params[2];

	    copyAgent(agentID, where, newName);
	}

	private void handleInformMoved(VerticalCommand cmd) {
	    // Nothing to do here: INFORM_MOVED has no target-side action...
	}

	private void handleInformCloned(VerticalCommand cmd) throws JADESecurityException, NotFoundException, NameClashException {
	    Object[] params = cmd.getParams();
	    AID agentID = (AID)params[0];
	    ContainerID cid = (ContainerID)params[1];
	    Credentials creds = (Credentials)params[2];

	    clonedAgent(agentID, cid, creds);
	}

	private void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException {
		Agent a = myContainer.acquireLocalAgent(agentID);
		
		if(a == null) {
			throw new NotFoundException("Move-Agent failed to find " + agentID);
		}
		a.doMove(where);
		
		myContainer.releaseLocalAgent(agentID);
	}

	private void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
	    Agent a = myContainer.acquireLocalAgent(agentID);

	    if(a == null)
		throw new NotFoundException("Clone-Agent failed to find " + agentID);
	    a.doClone(where, newName);

	    myContainer.releaseLocalAgent(agentID);
	}

	// FIXME: adjust principal
	private void clonedAgent(AID agentID, ContainerID cid, Credentials credentials) throws JADESecurityException, NotFoundException, NameClashException {
	    MainContainer impl = myContainer.getMain();
	    if(impl != null) {
	  // Retrieve the ownership from the credentials
    String ownership = "NONE";
    if (credentials != null) {
    	JADEPrincipal ownerPr = credentials.getOwner();
    	if (ownerPr != null) {
    		ownership = ownerPr.getName();
    	}
    }
		try {
		    // If the name is already in the GADT, throws NameClashException
		    impl.bornAgent(agentID, cid, null, ownership, false);
		}
		catch(NameClashException nce) {
		    try {
			ContainerID oldCid = impl.getContainerID(agentID);
			Node n = impl.getContainerNode(oldCid).getNode();

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
			impl.bornAgent(agentID, cid, null, ownership, true);
		    }
		}
	    }
	}


    } // End of CommandTargetSink class


    /**
       Inner mix-in class for this service: this class receives
       commands through its <code>Filter</code> interface and serves
       them, coordinating with remote parts of this service through
       the <code>Slice</code> interface (that extends the
       <code>Service.Slice</code> interface).
    */
    private class ServiceComponent implements Service.Slice {


	// Implementation of the Service.Slice interface

	public Service getService() {
	    return AgentMobilityService.this;
	}

	public Node getNode() throws ServiceException {
	    try {
		return AgentMobilityService.this.getLocalNode();
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

		if(cmdName.equals(AgentMobilitySlice.H_CREATEAGENT)) {
		    AID agentID = (AID)params[0];
		    byte[] serializedInstance = (byte[])params[1];
		    String classSiteName = (String)params[2];
		    boolean isCloned = ((Boolean)params[3]).booleanValue();
		    boolean startIt = ((Boolean)params[4]).booleanValue();

		    createAgent(agentID, serializedInstance, classSiteName, isCloned, startIt);
		}
		else if(cmdName.equals(AgentMobilitySlice.H_FETCHCLASSFILE)) {
		    String className = (String)params[0];
		    String agentName = (String)params[1];

		    cmd.setReturnValue(fetchClassFile(className, agentName));
		}
		else if(cmdName.equals(AgentMobilitySlice.H_MOVEAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentMobilityHelper.REQUEST_MOVE, AgentMobilitySlice.NAME, null);
		    AID agentID = (AID)params[0];
		    Location where = (Location)params[1];
		    gCmd.addParam(agentID);
		    gCmd.addParam(where);

		    result = gCmd;
		}
		else if(cmdName.equals(AgentMobilitySlice.H_COPYAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentMobilityHelper.REQUEST_CLONE, AgentMobilitySlice.NAME, null);
		    AID agentID = (AID)params[0];
		    Location where = (Location)params[1];
		    String newName = (String)params[2];
		    gCmd.addParam(agentID);
		    gCmd.addParam(where);
		    gCmd.addParam(newName);

		    result = gCmd;
		}
		else if(cmdName.equals(AgentMobilitySlice.H_PREPARE)) {

		    cmd.setReturnValue(new Boolean(prepare()));
		}
		else if(cmdName.equals(AgentMobilitySlice.H_TRANSFERIDENTITY)) {
		    AID agentID = (AID)params[0];
		    Location src = (Location)params[1];
		    Location dest = (Location)params[2];

		    cmd.setReturnValue(new Boolean(transferIdentity(agentID, src, dest)));
		}
		else if(cmdName.equals(AgentMobilitySlice.H_HANDLETRANSFERRESULT)) {
		    AID agentID = (AID)params[0];
		    boolean transferResult = ((Boolean)params[1]).booleanValue();
		    List messages = (List)params[2];

		    handleTransferResult(agentID, transferResult, messages);
		}
		else if(cmdName.equals(AgentMobilitySlice.H_CLONEDAGENT)) {
		    GenericCommand gCmd = new GenericCommand(AgentMobilityHelper.INFORM_CLONED, AgentMobilitySlice.NAME, null);
		    AID agentID = (AID)params[0];
		    ContainerID cid = (ContainerID)params[1];
		    Credentials creds = (Credentials)params[2];
		    gCmd.addParam(agentID);
		    gCmd.addParam(cid);
		    gCmd.addParam(creds);

		    result = gCmd;
		}
	    }
	    catch(Throwable t) {
		cmd.setReturnValue(t);
		if(result != null) {
		    result.setReturnValue(t);
		}
	    }

            return result;
	}


	private void createAgent(AID agentID, byte[] serializedInstance, String classSiteName, boolean isCloned, boolean startIt) throws IMTPException, ServiceException, NotFoundException, NameClashException, JADESecurityException {
	    try {
		//log("Incoming agent " + agentID, 1);
                if(myLogger.isLoggable(Logger.INFO))
                  myLogger.log(Logger.INFO,"Incoming agent " + agentID);


		// Reconstruct the serialized agent
		//#DOTNET_EXCLUDE_BEGIN
		ObjectInputStream in = new Deserializer(new ByteArrayInputStream(serializedInstance), agentID.getName(), classSiteName, myContainer.getServiceFinder());
		Agent instance = (Agent)in.readObject();
		//#DOTNET_EXCLUDE_END
		/*#DOTNET_INCLUDE_BEGIN
		ubyte[] ubyteSerializedInstance = new ubyte[serializedInstance.length];
		System.Buffer.BlockCopy(serializedInstance, 0, ubyteSerializedInstance, 0, serializedInstance.length);
		ByteArrayInputStream in = new ByteArrayInputStream(serializedInstance);
		ObjectInputStream decoder = new ObjectInputStream(in);
		Object obj = decoder.readObject();
		
		Agent instance = (Agent) obj;
		#DOTNET_INCLUDE_END*/

		//log("Agent " + agentID + " reconstructed", 2);
                if(myLogger.isLoggable(Logger.INFO))
                  myLogger.log(Logger.INFO,"Agent " + agentID + " reconstructed");



		/* --- This code should go into the Security Service ---

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

		log("Permissions for agent " + agentID + " OK", 2);

		// --- End of code that should go into the Security Service ---
		*/

	  Credentials agentCerts = null;
		//#MIDP_EXCLUDE_BEGIN
		//CertificateFolder agentCerts = instance.getCertificateFolder();
		//#MIDP_EXCLUDE_END

		/*# MIDP_INCLUDE_BEGIN
		  CertificateFolder agentCerts = new CertificateFolder();
		  # MIDP_INCLUDE_END*/

		if(isCloned) {
		    // Notify the main slice that a new agent is born
		    AgentMobilitySlice mainSlice = (AgentMobilitySlice)getSlice(MAIN_SLICE);

		    try {
			mainSlice.clonedAgent(agentID, myContainer.getID(), agentCerts);
		    }
		    catch(IMTPException imtpe) {
			// Try to get a newer slice and repeat...
			mainSlice = (AgentMobilitySlice)getFreshSlice(MAIN_SLICE);
			mainSlice.clonedAgent(agentID, myContainer.getID(), agentCerts);
		    }
		}

		// Store the container where the classes for this agent can be
		// retrieved
		sites.put(instance, classSiteName);

		// Connect the new instance to the local container
		Agent old = myContainer.addLocalAgent(agentID, instance);

		if(startIt) {
		    // Actually start the agent thread
		    myContainer.powerUpLocalAgent(agentID);
		}

		//log("Agent " + agentID + " inserted into LADT", 1);
                if(myLogger.isLoggable(Logger.INFO))
                  myLogger.log(Logger.INFO,"Agent " + agentID + " inserted into LADT");

	    }
	    catch(IOException ioe) {
		throw new IMTPException("An I/O error occurred during de-serialization", ioe);
	    }
	    catch(ClassNotFoundException cnfe) {
		throw new IMTPException("A class was not found during de-serialization", cnfe);
	    }
	    catch(Throwable t) {
	    	t.printStackTrace();
		throw new IMTPException("Unexpected error.", t);
	    }
	}

	private byte[] fetchClassFile(String className, String agentName) throws IMTPException, ClassNotFoundException {
		if (myLogger.isLoggable(Logger.FINE))
			myLogger.log(Logger.FINE, "Fetching class " + className);

		String fileName = className.replace('.', '/') + ".class";
		int length = -1;
		InputStream classStream = ClassLoader.getSystemResourceAsStream(fileName);
		if (classStream == null) {
			// In PJAVA for some misterious reason getSystemResourceAsStream()
			// does not work --> Try to do it by hand
			if (myLogger.isLoggable(Logger.FINER))
				myLogger.log(Logger.FINER, "Class not found as a system resource. Try manually");

			String currentCp = System.getProperty("java.class.path");
			StringTokenizer st = new StringTokenizer(currentCp, ";");
			while (st.hasMoreTokens()) {
				try {
					String path = st.nextToken();
					if (myLogger.isLoggable(Logger.FINER)) {
						myLogger.log(Logger.FINER, "Searching in path " + path);
					}
					if (path.endsWith(".jar")) {
						if (myLogger.isLoggable(Logger.FINER)) {
							myLogger.log(Logger.FINER, "It's a jar file");
						}

						ClassInfo info = getClassStreamFromJar(fileName, path);
						if (info != null) {
							classStream = info.getClassStream();
							length = info.getLength();
							break;
						}
					} 
					else {
						if (myLogger.isLoggable(Logger.FINER)) {
							myLogger.log(Logger.FINER, "Trying file " + path + "/" + fileName);
						}

						File f = new File(path + "/" + fileName);
						if (f.exists()) {
							if (myLogger.isLoggable(Logger.FINER)) {
								myLogger.log(Logger.FINER, "File exists");
							}
							classStream = new FileInputStream(f);
							break;
						}
					}
				} 
				catch (Exception e) {
					if (myLogger.isLoggable(Logger.WARNING)) {
						myLogger.log(Logger.WARNING, e.toString());
					}
				}
			}
		}
		if (classStream == null) {
			// Maybe the agent was loaded from a separate Jar file
			try {
				AgentManagementService ams = (AgentManagementService) myFinder.findService(AgentManagementService.NAME);
				String jarName = ams.getAgentJarFileName(agentName);
				ClassInfo info = getClassStreamFromJar(fileName, jarName);
				classStream = info.getClassStream();
				length = info.getLength();
			}
			catch (NullPointerException npe) {
				// No jarfile or class not forund in jarfile. Ignore
			}
			catch (Exception e) {
				// Should never happen since findService() never throws exceptions
				e.printStackTrace();
			}
		}
		if (classStream == null) {
			if (myLogger.isLoggable(Logger.WARNING)) {
				myLogger.log(Logger.WARNING, "Class " + className + " not found");
			}
			throw new ClassNotFoundException(className);
		}
		
		try {
			if (length == -1) {
				length = (int) classStream.available();
			}
			byte[] bytes = new byte[length];
			if (myLogger.isLoggable(Logger.FINER)) {
				myLogger.log(Logger.FINER, "Class " + className + " fetched. Length is " + length);
			}

			DataInputStream dis = new DataInputStream(classStream);
			dis.readFully(bytes);
			return (bytes);
		} 
		catch (IOException ioe) {
			throw new ClassNotFoundException("IOException reading class bytes. " + ioe.getMessage());
		}
	}

	private ClassInfo getClassStreamFromJar(String classFileName, String jarName) throws IOException {
		File f = new File(jarName);
		if (f.exists()) {
			if (myLogger.isLoggable(Logger.FINER)) {
				myLogger.log(Logger.FINER, "Jar file exists");
			}
		}
		ZipFile zf = new ZipFile(f);
		ZipEntry e = zf.getEntry(classFileName);
		if (e != null) {
			if (myLogger.isLoggable(Logger.FINER)) {
				myLogger.log(Logger.FINER, "Entry " + classFileName + " found");
			}

			return new ClassInfo(zf.getInputStream(e), (int) e.getSize());
		}
		return null;
	}
	
	
	/**
	 * Inner class ClassInfo
	 * This utility bean class is used only to keey together some pieces of information related to a class
	 */
	private class ClassInfo {
		private InputStream classStream;
		private int length = -1;
		
		public ClassInfo(InputStream is, int l) {
			classStream = is;
			length = l;
		}
		
		public InputStream getClassStream() {
			return classStream;
		}
		
		public int getLength() {
			return length;
		}
	} // END of inner class ClassInfo
	
	
	private void handleTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException {
	    //log("Activating incoming agent "+agentID, 1);
            if(myLogger.isLoggable(Logger.FINER))
               myLogger.log(Logger.FINER,"Activating incoming agent "+agentID);

	    try {
		Agent agent = myContainer.acquireLocalAgent(agentID);

		if ((agent == null) || (agent.getState() != AP_TRANSIT)) {
		    throw new NotFoundException("handleTransferResult() unable to find a suitable agent.");
		}

		if (result == TRANSFER_ABORT) {
		    myContainer.removeLocalAgent(agentID);
		}
		else {
		    // Insert received messages at the start of the queue
		    for (int i = messages.size(); i > 0; i--) {
			agent.putBack((ACLMessage)messages.get(i - 1));
		    }

		    myContainer.powerUpLocalAgent(agentID);
		    //log("Incoming agent " + agentID + " activated", 1);
                    if(myLogger.isLoggable(Logger.INFO))
                       myLogger.log(Logger.INFO,"Incoming agent " + agentID + " activated");

		}
	    }
	    finally {
		myContainer.releaseLocalAgent(agentID);
	    }
	}

	private boolean prepare() {
	    // Just return 'true', because this method is simply used as a 'ping', for now...
	    return true;
	}

	private boolean transferIdentity(AID agentID, Location src, Location dest) throws IMTPException, NotFoundException {
		//log("Transferring identity of agent "+agentID+" from "+src.getName()+" to "+dest.getName(), 2);
                if(myLogger.isLoggable(Logger.INFO))
                   myLogger.log(Logger.INFO,"Transferring identity of agent "+agentID+" from "+src.getName()+" to "+dest.getName());


    MainContainer impl = myContainer.getMain();
    if(impl != null) {
			AgentDescriptor ad = impl.acquireAgentDescriptor(agentID);
			if (ad != null) {
				try {
			    AgentMobilitySlice srcSlice = (AgentMobilitySlice)getSlice(src.getName());
			    AgentMobilitySlice destSlice = (AgentMobilitySlice)getSlice(dest.getName());
			    boolean srcReady = false;
			    boolean destReady = false;

			    try {
						srcReady = srcSlice.prepare();
			    }
			    catch(IMTPException imtpe) {
						srcSlice = (AgentMobilitySlice)getFreshSlice(src.getName());
						srcReady = srcSlice.prepare();
			    }
					//log("Source "+src.getName()+" "+srcReady, 2);
                                        if(myLogger.isLoggable(Logger.INFO))
                                           myLogger.log(Logger.INFO,"Source "+src.getName()+" "+srcReady);


			    try {
						destReady = destSlice.prepare();
			    }
			    catch(IMTPException imtpe) {
						destSlice = (AgentMobilitySlice)getFreshSlice(dest.getName());
						destReady = destSlice.prepare();
			    }
					//log("Destination "+dest.getName()+" "+destReady, 2);
                                        if(myLogger.isLoggable(Logger.INFO))
                                           myLogger.log(Logger.INFO,"Destination "+dest.getName()+" "+destReady);


			    if(srcReady && destReady) {
						// Commit transaction
						impl.movedAgent(agentID, (ContainerID)src, (ContainerID)dest);
						return true;
			    }
			    else {
						// Problems on a participant slice: abort transaction
						return false;
			    }
				}
				catch(Exception e) {
			    // Link failure: abort transaction
					//log("Link failure!", 2);
                                        if(myLogger.isLoggable(Logger.WARNING))
                                           myLogger.log(Logger.WARNING,"Link failure!");

			    return false;
				}
				finally {
			    impl.releaseAgentDescriptor(agentID);
				}
			}
			else {
				throw new NotFoundException("Agent agentID not found");
			}
    }
    else {
			// Do nothing for now, but could also use another slice as transaction coordinator...
			//log("Not a main!", 2);
                        if(myLogger.isLoggable(Logger.WARNING))
                           myLogger.log(Logger.WARNING,"Not a main!");

			return false;
    }
	}

    } // End of ServiceComponent class




    /**
     * Inner class Deserializer
     */
    private class Deserializer extends ObjectInputStream {
    	private String agentName;
	private String classSiteName;
	private ServiceFinder finder;

        /**
         */
        public Deserializer(InputStream inner, String an, String sliceName, ServiceFinder sf) throws IOException {
            super(inner);
            agentName = an;
	    classSiteName = sliceName;
            finder = sf;
        }

        /**
         */
        protected Class resolveClass(ObjectStreamClass v)
        	throws IOException, ClassNotFoundException {
            MobileAgentClassLoader cl = (MobileAgentClassLoader)loaders.get(classSiteName);
            if (cl == null) {
              cl = new MobileAgentClassLoader(agentName, classSiteName, finder);
              loaders.put(classSiteName, cl);
            }
            Class c = cl.loadClass(v.getName());
            return c;
        }
    }    // END of inner class Deserializer

    // This Map holds the mapping between a container and the class loader
    // that can retrieve agent classes from this container.
    private final Map loaders = new HashMap();

    // This Map holds the mapping between an agent that arrived on this
    // container and the service slice where its classes can be found
    private final Map sites = new HashMap();

    // The concrete agent container, providing access to LADT, etc.
    private AgentContainer myContainer;

    // The local slice for this service
    private final ServiceComponent localSlice = new ServiceComponent();


    /**
       Inner class AgentMobilityHelperImpl.
       The actual implementation of the AgentMobilityHelper interface.
     */
    private class AgentMobilityHelperImpl implements AgentMobilityHelper {
		  private Agent myAgent;
		  private Movable myMovable;

		  public void init(Agent a) {
		  	myAgent = a;
			}

			public void registerMovable(Movable m) {
				myMovable = m;
			}

			public void move(Location destination) {
				myAgent.changeStateTo(new TransitLifeCycle(destination, myMovable, AgentMobilityService.this));
			}

			public void clone(Location destination, String newName) {
				myAgent.changeStateTo(new CopyLifeCycle(destination, newName, myMovable, AgentMobilityService.this));
			}

			/*void gone() {
				myAgent.changeStateTo(new GoneLifeCycle(myMovable));
			}*/
    }  // END of inner class AgentMobilityHelperImpl


  /**
     Inner class TransitLifeCycle
   */
  private static class TransitLifeCycle extends LifeCycle {
  	private Location myDestination;
  	private Movable myMovable;
  	private transient AgentMobilityService myService;
  	private Logger myLogger;
  	private boolean firstTime = true;

  	private TransitLifeCycle(Location l, Movable m, AgentMobilityService s) {
  		super(AP_TRANSIT);
  		myDestination = l;
  		myMovable = m;
  		myService = s;
  		myLogger = Logger.getMyLogger(myService.getName());
  	}

  	public void init() {
			myAgent.restoreBufferedState();
			if (myMovable != null) {
				myMovable.afterMove();
			}
		}

		public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
		  try {
		  	// Issue a single INFORM_MOVED vertical command
			  if (firstTime) {
				  firstTime = false;
				informMoved(myAgent.getAID(), myDestination);
			  }
		  }
		  catch (Exception e) {
		  	if (myAgent.getState() == myState) {
			  	// Something went wrong during the transfer. Rollback
			  	myAgent.restoreBufferedState();
					myDestination = null;
					if (e instanceof JADESecurityException) {
						// Will be caught together with all other JADESecurityException-s
						throw (JADESecurityException) e;
			  	}
			  	else {
			  		e.printStackTrace();
			  	}
		  	}
		  	else {
		  		throw new Interrupted();
		  	}
		  }
		}

		public void end() {
    	//System.err.println("***  Agent " + myAgent.getName() + " moved in a forbidden situation ***");
            if(myLogger.isLoggable(Logger.SEVERE))
              myLogger.log(Logger.SEVERE,"***  Agent " + myAgent.getName() + " moved in a forbidden situation ***");

			myAgent.clean(true);
		}

		public boolean transitionTo(LifeCycle newLF) {
			int s = newLF.getState();
			switch (s) {
			case AP_GONE:
				if (myMovable != null) {
					myMovable.beforeMove();
				}
				// No break statement --> Fall through
			case Agent.AP_ACTIVE:
			case Agent.AP_DELETED:
				return true;				
			}
			return false;
		}

		public void informMoved(AID agentID, Location where) throws ServiceException, JADESecurityException, NotFoundException, IMTPException {
	    GenericCommand cmd = new GenericCommand(AgentMobilityHelper.INFORM_MOVED, AgentMobilitySlice.NAME, null);
	    cmd.addParam(agentID);
	    cmd.addParam(where);
	    // Set the credentials of the moving agent
	    myService.initCredentials(cmd, agentID);

	    Object lastException = myService.submit(cmd);
	    if(lastException != null) {
				if(lastException instanceof JADESecurityException) {
				    throw (JADESecurityException)lastException;
				}
				if(lastException instanceof NotFoundException) {
				    throw (NotFoundException)lastException;
				}
				if(lastException instanceof IMTPException) {
				    throw (IMTPException)lastException;
				}
	    }
		}
  } // END of inner class TransitLifeCycle


  /**
     Inner class CopyLifeCycle
   */
  private static class CopyLifeCycle extends LifeCycle {
  	private Location myDestination;
  	private String myNewName;
  	private Movable myMovable;
  	private transient AgentMobilityService myService;
  	private Logger myLogger;

  	private CopyLifeCycle(Location l, String newName, Movable m, AgentMobilityService s) {
  		super(AP_COPY);
  		myDestination = l;
  		myNewName = newName;
  		myMovable = m;
  		myService = s;
  		myLogger = Logger.getMyLogger(myService.getName());
  	}

  	public void init() {
			myAgent.restoreBufferedState();
			if (myMovable != null) {
				myMovable.afterClone();
			}
		}

		public void execute() throws JADESecurityException, InterruptedException, InterruptedIOException {
			if (myMovable != null) {
		  	myMovable.beforeClone();
			}
		  try {
			  informCloned(myAgent.getAID(), myDestination, myNewName);
		  }
		  catch (Exception e) {
		  	if (myAgent.getState() == myState) {
			  	// Something went wrong during the clonation. Rollback
					myDestination = null;
					myNewName = null;
					myAgent.restoreBufferedState();
					if (e instanceof JADESecurityException) {
						// Will be catched together with all other JADESecurityException-s
						throw (JADESecurityException) e;
			  	}
			  	else {
			  		e.printStackTrace();
			  		return;
			  	}
		  	}
		  	else {
		  		throw new Interrupted();
		  	}
		  }
		  // Once cloned go back to the previous state
		  myAgent.restoreBufferedState();
		}

		public boolean transitionTo(LifeCycle newLF) {
			int s = newLF.getState();
			return (s == Agent.AP_ACTIVE || s == Agent.AP_DELETED);
		}

		public void end() {
    	//System.err.println("***  Agent " + myAgent.getName() + " cloned in a forbidden situation ***");
            if(myLogger.isLoggable(Logger.SEVERE))
              myLogger.log(Logger.SEVERE,"***  Agent " + myAgent.getName() + " cloned in a forbidden situation ***");
			myAgent.clean(true);
		}

		public void informCloned(AID agentID, Location where, String newName) throws ServiceException, JADESecurityException, IMTPException, NotFoundException, NameClashException {
	    GenericCommand cmd = new GenericCommand(AgentMobilityHelper.INFORM_CLONED, AgentMobilitySlice.NAME, null);
	    cmd.addParam(agentID);
	    cmd.addParam(where);
	    cmd.addParam(newName);
	    // Set the credentials of the cloning agent
	    myService.initCredentials(cmd, agentID);

	    Object lastException = myService.submit(cmd);
	    if(lastException != null) {
				if(lastException instanceof JADESecurityException) {
				    throw (JADESecurityException)lastException;
				}
				if(lastException instanceof NotFoundException) {
				    throw (NotFoundException)lastException;
				}
				if(lastException instanceof IMTPException) {
				    throw (IMTPException)lastException;
				}
				if(lastException instanceof NameClashException) {
				    throw (NameClashException)lastException;
				}
	    }
		}
  } // END of inner class CopyLifeCycle



    // The command sink, source side
    private final CommandSourceSink senderSink = new CommandSourceSink();

    // The command sink, target side
    private final CommandTargetSink receiverSink = new CommandTargetSink();

    // Work-around for PJAVA compilation
    protected Service.Slice getFreshSlice(String name) throws ServiceException {
    	return super.getFreshSlice(name);
    }

  private void initCredentials(Command cmd, AID id) {
  	Agent agent = myContainer.acquireLocalAgent(id);
  	if (agent != null) {
  		try {
		  	CredentialsHelper ch = (CredentialsHelper) agent.getHelper("jade.core.security.Security");
	  		cmd.setPrincipal(ch.getPrincipal());
	  		cmd.setCredentials(ch.getCredentials());
	  	}
	  	catch (ServiceException se) {
	  		// The security plug-in is not there. Just ignore it
	  	}
  	}
  	myContainer.releaseLocalAgent(id);
  }
}

