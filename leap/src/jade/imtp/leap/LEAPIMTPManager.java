/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.imtp.leap;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.mtp.TransportAddress;
import jade.util.leap.*;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.JICP.Connection;
import java.io.IOException;

/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public class LEAPIMTPManager implements IMTPManager {

	/**
	 * Profile keys for LEAP-IMTP specific parameters
	 */
	static final String MAIN_URL = "main-url";
  private static final String LOCAL_PORT = "local-port";
	private static final String ICPS = "icps";
	private static final String ROUTER_URL = "router-url";
	
  /**
   * Local pointer to the singleton command dispatcher in this JVM
   */
  private CommandDispatcher theDispatcher = null;

  /**
   * The Profile holding the configuration for this IMTPManager
   */
  private Profile           theProfile = null;

  /**
   * Default constructor used to dynamically instantiate a new
   * LEAPIMTPManager object
   */
  public LEAPIMTPManager() {
  } 

  // ////////////////////////////////
  // IMTPManager Interface
  // ////////////////////////////////

  /**
   * Initialize the support for intra-platform communication
   */
  public void initialize(Profile p) throws IMTPException {
    theProfile = p;

    if (!CommandDispatcher.create(theProfile)) {
      throw new IMTPException("wrong type of command dispatcher!");
    } 

    // Get the singleton CommandDispatcher
    theDispatcher = CommandDispatcher.getDispatcher();

    // Add to the CommandDispatcher the ICPs specified in the Profile
    try {
    	// Set defaults if not explicitly set. 
    	setDefaults();
    	
      List     l = theProfile.getSpecifiers(ICPS);
      Iterator it = l.iterator();

      while (it.hasNext()) {
        Specifier s = (Specifier) it.next();

        try {
          ICP peer = (ICP) Class.forName(s.getClassName()).newInstance();
          String id = (s.getArgs() != null ? (String) (s.getArgs()[0]) : null);
          theDispatcher.addICP(peer, id, theProfile);
        } 
        catch (Exception e) {
          System.out.println("Error adding ICP. "+e);
        } 
      } 
    } 
    catch (ProfileException pe) {
      // Just print a warning
      System.out.println("Profile error. "+pe.getMessage());
    } 

    // Now check that some ICP is active. Note that, as a CommandDispatcher
    // can serve multiple IMTPManagers, some ICPs could be already active.
    List URLs = theDispatcher.getLocalURLs();

    if (URLs.size() == 0) {
      throw new IMTPException("No ICP active");
    } 
    else {
      Iterator it = URLs.iterator();

      System.out.println("Listening for intra-platform commands on address:");

      while (it.hasNext()) {
        System.out.println((String) it.next());
      } 
    } 

    // Finally, if a URL for the default router is specified in the
    // Profile, set it in the CommandDispatcher
    theDispatcher.setRouterAddress(theProfile.getParameter(ROUTER_URL, null));
  } 

  /**
   */
  public void remotize(AgentContainer ac) throws IMTPException {

    // Create a skeleton for the container and register it to the
    // CommandDispatcher
    Skeleton skel = new AgentContainerSkel(ac);

    theDispatcher.registerSkeleton(skel, ac);
  } 

  /**
   */
  public void remotize(MainContainer mc) throws IMTPException {

    // Create a skeleton for the main container and register it to the
    // CommandDispatcher
    Skeleton skel = new MainContainerSkel(mc);

    theDispatcher.registerSkeleton(skel, mc);
  } 

  /**
   */
  public void unremotize(AgentContainer ac) throws IMTPException {

    // Deregister the agent container from the CommandDispatcher
    theDispatcher.deregisterSkeleton(ac);
  } 

  /**
   */
  public void unremotize(MainContainer mc) throws IMTPException {

    // Deregister the agent container from the CommandDispatcher
    theDispatcher.deregisterSkeleton(mc);
  } 

  /**
   * Creates a proxy for the given agent, on the given container.
   */
  public AgentProxy createAgentProxy(AgentContainer ac, AID id) throws IMTPException {
    return new RemoteContainerProxy(ac, id);
  } 

  /**
   * Return a MainContainerStub to call remote methods on the Main container
   */ 
  public MainContainer getMain(boolean reconnect) throws IMTPException {
  	return theDispatcher.getMain(theProfile);
  } 

  /**
   * Release all resources of this LEAPIMTPManager
   */
  public void shutDown() {
    if (theDispatcher != null) {
      theDispatcher.shutDown();

      theDispatcher = null;
    } 
  } 

  /**
   */
  public List getLocalAddresses() {
    return theDispatcher.getLocalTAs();
  } 

  // /////////////////////////
  // PRIVATE METHODS
  // /////////////////////////

  /**
     In the Profile ICPS can be specified as follows.
     1) If there is only one ICP -->
     icps = <icp-class-name>
     <param-key> = <param-value>
     .......
     2) In general (typically used when there are 2 or more ICPs)
     icps = <icp1-class-name>(<icp1-id>);<icp2-class-name>(<icp2-id>)...
     <icp1-id>-<param-key> = <param-value>
     .......
     <icp2-id>-<param-key> = <param-value>
     .......
     
     If there is no ICP indication set it as follows. 
     a) Peripheral container / J2SE 
        - There is at least 1 ICP already active --> Nothing
        - There are no ICPs already active --> JICPPeer
     b) Peripheral container / PJAVA or MIDP --> JICPBMPeer
     c) Main container / J2SE or PJAVA --> JICPPeer
   */
  private void setDefaults() throws ProfileException {
    if ("false".equals(theProfile.getParameter(Profile.MAIN, null))) {
      // This is a "Peripheral Container"
      // - MAIN URL
      String mainURL = theProfile.getParameter(MAIN_URL, null);
      if (mainURL == null) {
        String mainHost = getMainHost();
        if (mainHost != null) {
          String mainPort = theProfile.getParameter(Profile.MAIN_PORT, null);
          mainPort = (mainPort != null ? new String(":"+mainPort) : "");
          mainURL = new String("jicp://"+mainHost+mainPort);
          theProfile.setParameter(MAIN_URL, mainURL);
        }
        else {
          throw new ProfileException("Main URL not specified");
        }
      }

      // - ICPS
      if (theProfile.getParameter(ICPS, null) == null) {
      	String jvm = theProfile.getParameter(Profile.JVM, null);
      	if (Profile.J2SE.equals(jvm)) {
      		// Set default ICPS for J2SE
    			if (theDispatcher.getLocalURLs().size() == 0) {
        		theProfile.setParameter(ICPS, "jade.imtp.leap.JICP.JICPPeer");
    			}
      	}
      	else {
      		// Set default ICPS for PJAVA and MIDP (same)
      		theProfile.setParameter(ICPS, "jade.imtp.leap.JICP.JICPBMPeer");
      		if (theProfile.getParameter(JICPProtocol.REMOTE_URL_KEY, null) == null) {
	        	theProfile.setParameter(JICPProtocol.REMOTE_URL_KEY, mainURL);
      		}
      	}
      }
    }
    else {
      // This is the Main Container
      if (theProfile.getParameter(ICPS, null) == null) {
        theProfile.setParameter(ICPS, "jade.imtp.leap.JICP.JICPPeer");
      }
    }  
  }
  
  /**
   */
  private String getMainHost() {
  	String host = theProfile.getParameter(Profile.MAIN_HOST, null);
  	if (host == null) {
	    try {
      	host = Connection.getLocalHost();
    	}
    	catch (Exception e) {
    	}
  	}
    return host;
  }
  
}

