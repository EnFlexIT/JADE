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
import java.util.StringTokenizer;
import java.util.zip.*;

import java.net.URL;

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
import jade.core.AID;
import jade.core.CaseInsensitiveString;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.AgentContainerImpl;
import jade.core.MainContainerImpl;

import jade.core.ProfileException;
import jade.core.IMTPException;
import jade.core.NameClashException;
import jade.core.NotFoundException;
import jade.core.UnreachableException;

import jade.lang.acl.ACLMessage;

import jade.security.Authority;
import jade.security.CertificateFolder;
import jade.security.AgentPrincipal;
import jade.security.IdentityCertificate;
import jade.security.AuthException;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

/**

   The JADE service to manage mobility-related agent life cycle: migration
   and clonation.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public class AgentMobilityService extends BaseService {

    /**
       The name of this service.
    */
    public static final String NAME = "Agent-Mobility";

    /**
       This command name represents the <code>move-agent</code>
       action.
       This command object represents only the <i>first half</i> of
       the complete agent migration process. Even if this command is
       accepted by the kernel, there is no guarantee that the
       requested mogration will ever happen. Only when the
       <code>InformMoved</code> command is issued can one assume that
       the agent migration has taken place.
    */
    public static final String REQUEST_MOVE = "Request-Move";

    /**
       This command name represents the <code>clone-agent</code>
       action.
       This command object represents only the <i>first half</i> of
       the complete agent clonation process. Even if this command is
       accepted by the kernel, there is no guarantee that the
       requested clonation will ever happen. Only when the
       <code>InformCloned</code> command is issued can one assume that
       the agent clonation has taken place.
    */
    public static final String REQUEST_CLONE = "Request-Clone";

    /**
       This command is issued by an agent that has just migrated.
       The agent migration can either be an autonomous move of the
       agent or the outcome of a previously issued
       <code>RequestMove</code> command. In the second case, this
       command represents only the <i>second half</i> of the complete
       agent migration process.
    */
    public static final String INFORM_MOVED = "Inform-Moved";

    /**
       This command is issued by an agent that has just cloned itself.
       The agent clonation can either be an autonomous move of the
       agent or the outcome of a previously issued
       <code>RequestClone</code> command. In the second case, this
       command represents only the <i>second half</i> of the complete
       agent clonation process.
    */
    public static final String INFORM_CLONED = "Inform-Cloned";


    public static final String MAIN_SLICE = "Main-Container";


    static final boolean MIGRATION = false;
    static final boolean CLONING = true;

    static final boolean CREATE_AND_START = true;
    static final boolean CREATE_ONLY = false;

    static final boolean       TRANSFER_ABORT = false;
    static final boolean       TRANSFER_COMMIT = true;


    public AgentMobilityService(AgentContainerImpl ac, Profile p) throws ProfileException {
	super(p);

	myContainer = ac;

	// Create a local slice
	localSlice = new ServiceComponent();

	// Initialize internal tables
	loaders = new HashMap();
	sites = new HashMap();
    }

    public String getName() {
	return NAME;
    }

    public Class getHorizontalInterface() {
	return AgentMobilitySlice.class;
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
    private class ServiceComponent implements Filter, AgentMobilitySlice {


	// Implementation of the Filter interface

	public void accept(VerticalCommand cmd) { // FIXME: Should set the exception somehow...

	    try {
		String name = cmd.getName();
		if(name.equals(REQUEST_MOVE)) {
		    handleRequestMove(cmd);
		}
		else if(name.equals(REQUEST_CLONE)) {
		    handleRequestClone(cmd);
		}
		else if(name.equals(INFORM_MOVED)) {
		    handleInformMoved(cmd);
		}
		else if(name.equals(INFORM_CLONED)) {
		    handleInformCloned(cmd);
		}
	    }
	    catch(IMTPException imtpe) {
		cmd.setReturnValue(new UnreachableException("A remote container was unreachable during agent cloning", imtpe));
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
		cmd.setReturnValue(new UnreachableException("A service slice was not found during agent cloning", se));
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


	// Implementation of the service-specific horizontal interface AgentMobilitySlice 

	public void createAgent(AID agentID, byte[] serializedInstance, String classSiteName, boolean isCloned, boolean startIt) throws IMTPException, ServiceException, NotFoundException, NameClashException, AuthException {
	    try {
		log("Incoming agent " + agentID, 1);

		AgentMobilitySlice classSite = (AgentMobilitySlice)getSlice(classSiteName);

		// Reconstruct the serialized agent
		ObjectInputStream in = new Deserializer(new ByteArrayInputStream(serializedInstance), classSiteName, classSite);
		Agent instance = (Agent)in.readObject();

		log("Agent " + agentID + " reconstructed", 2);         	


		// --- This code should go into the Security Service ---

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


		// Store the container where the classes for this agent can be
		// retrieved
		sites.put(instance, classSite);

		// Connect the new instance to the local container
		Agent old = myContainer.addLocalAgent(agentID, instance);

		//#MIDP_EXCLUDE_BEGIN
		CertificateFolder agentCerts = instance.getCertificateFolder();
		//#MIDP_EXCLUDE_END

		/*#MIDP_INCLUDE_BEGIN
		  CertificateFolder agentCerts = new CertificateFolder();
		  #MIDP_INCLUDE_END*/


		if(isCloned) {
		    // Notify the main slice that a new agent is born
		    AgentMobilitySlice mainSlice = (AgentMobilitySlice)getSlice(MAIN_SLICE);
		    mainSlice.clonedAgent(agentID, myContainer.getID(), agentCerts);
		}

		if(startIt) {
		    // Actually start the agent thread
		    myContainer.powerUpLocalAgent(agentID, instance);
		}

		log("Agent " + agentID + " inserted into LADT", 1);
	    }
	    catch(IOException ioe) {
		throw new IMTPException("An I/O error occurred during de-serialization", ioe);
	    }
	    catch(ClassNotFoundException cnfe) {
		throw new IMTPException("A class was not found during de-serialization", cnfe);
	    }
	}

	public byte[] fetchClassFile(String name) throws IMTPException, ClassNotFoundException {

	    log("Fetching class " + name, 4);
	    String fileName = name.replace('.', '/') + ".class";
	    int length = -1;
	    InputStream classStream = ClassLoader.getSystemResourceAsStream(fileName);
	    if (classStream == null) {
		// In PJAVA for some misterious reason getSystemResourceAsStream() 
		// does not work --> Try to do it by hand
		log("Class not found as a system resource. Try manually", 5);
		String currentCp = System.getProperty("java.class.path");
		StringTokenizer st = new StringTokenizer(currentCp, ";");
		while (st.hasMoreTokens()) {
		    try {
			String path = st.nextToken();
			log("Searching in path "+path, 5);
			if (path.endsWith(".jar")) {
			    log("It's a jar file", 5);
			    File f = new File(path);
			    if (f.exists()) {
				log("Jar file exists", 5);
			    }
			    ZipFile zf = new ZipFile(f);
			    ZipEntry e = zf.getEntry(fileName);
			    if (e != null) {
				log("Entry "+fileName+" found", 5);
				length = (int) e.getSize();
				classStream = zf.getInputStream(e);
				break;
			    }
			}
			else {
			    log("Trying file "+path+"/"+fileName, 5);
			    File f = new File(path+"/"+fileName);
			    if (f.exists()) {
				log("File exists", 5);
				classStream = new FileInputStream(f);
				break;
			    }
			}
		    }
		    catch (Exception e) {
			log(e.toString(), 5);
		    }
		}
	    }

	    if (classStream == null) {
        	log("Class " + name + " not found", 4);
	        throw new ClassNotFoundException(name);
	    } 
	    try {
		if (length == -1) {
		    length = (int) classStream.available();
		}
		byte[] bytes = new byte[length];
		log("Class " + name + " fetched. Length is " + length, 4);
		DataInputStream dis = new DataInputStream(classStream);
		dis.readFully(bytes);
		return (bytes);
	    } 
	    catch (IOException ioe) {
		throw new ClassNotFoundException("IOException reading class bytes. "+ioe.getMessage());
	    }

	}

	public void moveAgent(AID agentID, Location where) throws IMTPException, NotFoundException {
	    Agent a = myContainer.acquireLocalAgent(agentID);

	    if(a == null)
		throw new NotFoundException("Move-Agent failed to find " + agentID);
	    a.doMove(where);

	    myContainer.releaseLocalAgent(agentID);
	}

	public void copyAgent(AID agentID, Location where, String newName) throws IMTPException, NotFoundException {
	    Agent a = myContainer.acquireLocalAgent(agentID);

	    if(a == null)
		throw new NotFoundException("Clone-Agent failed to find " + agentID);
	    a.doClone(where, newName);

	    myContainer.releaseLocalAgent(agentID);
	}

	public void handleTransferResult(AID agentID, boolean result, List messages) throws IMTPException, NotFoundException {
	    log("Activating incoming agent "+agentID, 1);                             	
	    try {
		Agent agent = myContainer.acquireLocalAgent(agentID);

		if ((agent == null) || (agent.getState() != Agent.AP_TRANSIT)) {
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

		    myContainer.powerUpLocalAgent(agentID, agent);
		    log("Incoming agent " + agentID + " activated", 1);                             	
		}
	    }
	    finally {
		myContainer.releaseLocalAgent(agentID);   
	    }
	}

	public boolean prepare() {
	    // Just return 'true', because this method is simply used as a 'ping', for now...
	    return true;
	}

	public boolean transferIdentity(AID agentID, Location src, Location dest) throws IMTPException, NotFoundException {

	    MainContainerImpl impl = myContainer.getMain();
	    if(impl != null) {

		impl.lockEntryForAgent(agentID);

		try {
		    AgentMobilitySlice srcSlice = (AgentMobilitySlice)getSlice(src.getName());
		    AgentMobilitySlice destSlice = (AgentMobilitySlice)getSlice(dest.getName());

		    if(!srcSlice.prepare() || (!destSlice.prepare())) {
			// Problems on a participant slice: abort transaction
			return false;
		    }
		}
		catch(Exception e) {
		    // Link failure: abort transaction
		    return false;
		}
		finally {
		    impl.unlockEntryForAgent(agentID);
		}

		// Commit transaction
		impl.updateEntryForAgent(agentID, src, dest);
		impl.unlockEntryForAgent(agentID);
		return true;
	    }
	    else {
		// Do nothing for now, but could also use another slice as transaction coordinator...
		return false;
	    }
	}


	public void clonedAgent(AID agentID, ContainerID cid, CertificateFolder certs) throws IMTPException, AuthException, NotFoundException, NameClashException {
	    MainContainerImpl impl = myContainer.getMain();
	    if(impl != null) {
		try {
		    // If the name is already in the GADT, throws NameClashException
		    impl.bornAgent(agentID, cid, certs, false); 
		}
		catch(NameClashException nce) {
		    try {
			ContainerID oldCid = impl.getContainerID(agentID);
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
			impl.bornAgent(agentID, cid, certs, true);
		    }
		}
	    }
	}


    } // End of ServiceComponent class



    // Vertical command handler methods

    private void handleRequestMove(VerticalCommand cmd) throws IMTPException, ServiceException, NotFoundException {
	Object[] params = cmd.getParams();
	AID agentID = (AID)params[0];
	Location where = (Location)params[1];

	MainContainerImpl impl = myContainer.getMain();
	if(impl != null) {
	    ContainerID cid = impl.getContainerID(agentID);
	    AgentMobilitySlice targetSlice = (AgentMobilitySlice)getSlice(cid.getName());
	    targetSlice.moveAgent(agentID, where);
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

	MainContainerImpl impl = myContainer.getMain();
	if(impl != null) {
	    ContainerID cid = impl.getContainerID(agentID);
	    AgentMobilitySlice targetSlice = (AgentMobilitySlice)getSlice(cid.getName());
	    targetSlice.copyAgent(agentID, where, newName);
	}
	else {
	    // Do nothing for now, but could also route the command to the main slice, thus enabling e.g. AMS replication
	}
    }

    private void handleInformMoved(VerticalCommand cmd) throws IMTPException, ServiceException, AuthException, NotFoundException {
	Object[] params = cmd.getParams();
	AID agentID = (AID)params[0];
	Location where = (Location)params[1];

    	log("Moving agent " + agentID + " on container " + where.getName(), 1);
	Agent a = myContainer.acquireLocalAgent(agentID);
	if (a == null) {
	    System.out.println("Internal error: handleMove() called with a wrong name (" + agentID + ") !!!");
	    return;
	}
	String proto = where.getProtocol();
	if(!CaseInsensitiveString.equalsIgnoreCase(proto, ContainerID.DEFAULT_IMTP)) {
	    System.out.println("Mobility protocol not supported. Aborting transfer");
	    myContainer.abortMigration(a);
	    return;
	}

      int transferState = 0;
      List messages = new ArrayList();
      AgentMobilitySlice dest = null;
      try {

	  // --- This code should go into the Security Service ---

	  // Check for security permissions
	  // Note that CONTAINER_MOVE_TO will be checked on the destination container
	  myContainer.getAuthority().checkAction(Authority.AGENT_MOVE, myContainer.getAgentPrincipal(agentID), a.getCertificateFolder());
	  myContainer.getAuthority().checkAction(Authority.CONTAINER_MOVE_FROM, myContainer.getContainerPrincipal(), a.getCertificateFolder());

	  log("Permissions for agent " + agentID + " OK", 2);

	  // --- End of code that should go into the Security Service ---

	  dest = (AgentMobilitySlice)getSlice(where.getName());

	  log("Destination container for agent " + agentID + " found", 2);
	  transferState = 1;
	  // If the destination container is the same as this one, there is nothing to do
	  if (CaseInsensitiveString.equalsIgnoreCase(where.getName(), myContainer.here().getName())) {
	      myContainer.abortMigration(a);
	      return;
	  }

	  // Serialize the agent
	  ByteArrayOutputStream out = new ByteArrayOutputStream();
	  ObjectOutputStream encoder = new ObjectOutputStream(out);
	  encoder.writeObject(a);
	  byte[] bytes = out.toByteArray();
	  log("Agent " + agentID + " correctly serialized", 2);

	  // Gets the container where the agent classes can be retrieved
	  AgentMobilitySlice classSite = (AgentMobilitySlice)sites.get(a);
	  if (classSite == null) {
	      // The agent was born on this container
	      classSite = localSlice;
	  }

	  // Create the agent on the destination container
	  dest.createAgent(agentID, bytes, myContainer.here().getName(), MIGRATION, CREATE_ONLY);
	  transferState = 2;
	  log("Agent " + agentID + " correctly created on destination container", 1);

	  AgentMobilitySlice mainSlice = (AgentMobilitySlice)getSlice(MAIN_SLICE);

	  // Perform an atomic transaction for agent identity transfer
	  // From now on, messages for the moving agent will be routed to the 
	  // destination container
	  boolean transferResult = mainSlice.transferIdentity(agentID, (ContainerID) myContainer.here(), (ContainerID) where);
	  transferState = 3;
	  log("Identity of agent " + agentID + " correctly transferred", 1);
                        
	  if (transferResult == TRANSFER_COMMIT) {

	      // Send received messages to the destination container. Note that
	      // there is no synchronization problem as the agent is locked in the LADT
	      myContainer.fillListFromMessageQueue(messages, a);

	      dest.handleTransferResult(agentID, transferResult, messages);

	      // Cause the invocation of 'beforeMove()' and the
	      // subsequent termination of the agent thread, along
	      // with its removal from the LADT
	      myContainer.commitMigration(a);
	      sites.remove(a);
	  }

	  else {
	      myContainer.abortMigration(a);
	      dest.handleTransferResult(agentID, transferResult, messages);
	  }
	  log("Agent " + agentID + " correctly activated on destination container", 1);
			}
    	catch (IOException ioe) {
	    // Error in agent serialization
	    System.out.println("Error in agent serialization. Abort transfer. " + ioe);
	    myContainer.abortMigration(a);
	}
    	catch (AuthException ae) {
	    // Permission to move not owned
	    System.out.println("Permission to move not owned. Abort transfer. " + ae.getMessage());
	    myContainer.abortMigration(a);
	}
      catch(NotFoundException nfe) {
	  if(transferState == 0) {
	      System.out.println("Destination container does not exist. Abort transfer. " + nfe.getMessage());
	      myContainer.abortMigration(a);
	  }
	  else if(transferState == 2) {
	      System.out.println("Transferring agent does not seem to be part of the platform. Abort transfer. " + nfe.getMessage());
	      myContainer.abortMigration(a);
	  }
	  else if(transferState == 3) {
	      System.out.println("Transferred agent not found on destination container. Can't roll back. " + nfe.getMessage());
	  }
      }
      catch(NameClashException nce) {
	  // This should not happen, because the agent is not changing its name but just its location...
      }
      catch(IMTPException imtpe) {
	  // Unexpected remote error
	  if (transferState == 0) {
	      System.out.println("Can't retrieve destination container. Abort transfer. " + imtpe.getMessage());
	      myContainer.abortMigration(a);
	  }
	  else if (transferState == 1) {
	      System.out.println("Error creating agent on destination container. Abort transfer. " + imtpe.getMessage());
	      myContainer.abortMigration(a);
	  }
	  else if (transferState == 2) {
	      System.out.println("Error transferring agent identity. Abort transfer. " + imtpe.getMessage());
	      try {
		  dest.handleTransferResult(agentID, TRANSFER_ABORT, messages);
		  myContainer.abortMigration(a);
	      }
	      catch (Exception e) {
		  e.printStackTrace();
	      }
	  }
	  else if (transferState == 3) {
	      System.out.println("Error activating transferred agent. Can't roll back!!!. " + imtpe.getMessage());
	  }
      }
      finally {
	  myContainer.releaseLocalAgent(agentID);
      }
    }

    private void handleInformCloned(VerticalCommand cmd) throws IMTPException, NotFoundException, NameClashException, AuthException {
	Object[] params = cmd.getParams();
	AID agentID = (AID)params[0];
	Location where = (Location)params[1];
	String newName = (String)params[2];

	try {

	    Agent a = myContainer.acquireLocalAgent(agentID);
	    if (a == null) {
		System.out.println("Internal error: handleClone() called with a wrong name (" + agentID + ") !!!");
		return;
	    } 
	    String proto = where.getProtocol();
	    if (!CaseInsensitiveString.equalsIgnoreCase(proto, ContainerID.DEFAULT_IMTP)) {
		System.out.println("Mobility protocol not supported. Abort cloning");
		return;
	    }

	    AgentMobilitySlice dest = (AgentMobilitySlice)getSlice(where.getName());

	    // --- This code should go into the Security Service ---

	    // Check for security permissions
	    // Note that CONTAINER_CLONE_TO will be checked on the destination container
	    myContainer.getAuthority().checkAction(Authority.AGENT_CLONE, myContainer.getAgentPrincipal(agentID), a.getCertificateFolder() );
	    myContainer.getAuthority().checkAction(Authority.CONTAINER_CLONE_FROM, myContainer.getContainerPrincipal(), a.getCertificateFolder() );

	    // --- End of code that should go into the Security Service ---


	    // Serialize the agent
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    ObjectOutputStream encoder = new ObjectOutputStream(out);
	    encoder.writeObject(a);
	    byte[] bytes = out.toByteArray();

	    // Gets the container where the agent classes can be retrieved
	    AgentMobilitySlice classSite = (AgentMobilitySlice)sites.get(a);
	    if (classSite == null) {
        	// The agent was born on this container
		classSite = localSlice;
	    }

	    // Create the agent on the destination container with the new AID
	    AID newID = new AID(newName, AID.ISLOCALNAME);
	    dest.createAgent(newID, bytes, myContainer.here().getName(), CLONING, CREATE_AND_START);

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




    /**
     * Inner class Deserializer
     */
    private class Deserializer extends ObjectInputStream {
	private String classSiteName;
        private AgentMobilitySlice classSite;

        /**
         */
        public Deserializer(InputStream inner, String sliceName, AgentMobilitySlice slice) throws IOException {
            super(inner);
	    classSiteName = sliceName;
            classSite = slice;
        }

        /**
         */
        protected Class resolveClass(ObjectStreamClass v) 
        	throws IOException, ClassNotFoundException {
            MobileAgentClassLoader cl = (MobileAgentClassLoader)loaders.get(classSiteName);
            if (cl == null) {
                cl = new MobileAgentClassLoader(classSite, verbosity);
                loaders.put(classSiteName, cl);
            } 
            Class c = cl.loadClass(v.getName());
            return c;
        } 
    }    // END of inner class Deserializer


    private void log(String s, int level) {
	if (verbosity >= level) {
	    System.out.println("MobilityService-log: " + s);
	}
    }

    // This Map holds the mapping between a container and the class loader
    // that can retrieve agent classes from this container.
    private Map loaders;

    // This Map holds the mapping between an agent that arrived on this
    // container and the service slice where its classes can be found
    private Map sites;

    // The concrete agent container, providing access to LADT, etc.
    private AgentContainerImpl myContainer;

    // The ResourceManager of the local container
    //private ResourceManager myResourceManager;
    
    private static final String VERBOSITY_KEY = "jade_core_RealMobilityManager_verbosity";
    private int verbosity = 0;

    // The local slice for this service
    private ServiceComponent localSlice;

}

