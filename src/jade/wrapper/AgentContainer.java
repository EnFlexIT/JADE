/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be usefubut
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.wrapper;

import jade.wrapper.PlatformController.Listener;
import jade.core.AID;
import jade.core.AgentContainerImpl;
import jade.core.NotFoundException;
import jade.core.IMTPException;
import jade.core.AgentManager;

import jade.core.event.*;

import jade.mtp.MTPException;

import java.util.Vector;
import java.util.Enumeration;
import jade.security.JADEPrincipal;
import jade.security.Credentials;

/**
   This class is a Proxy class, allowing access to a JADE agent
   container. Invoking methods on instances of this class, it is
   possible to request services from <it>in-process</it> agent
   containers.
   This class must not be instantiated by applications. Instead, use
   the <code>createContainer()</code> method in class
   <code>Runtime</code>.
   <br>
   <b>NOT available in MIDP</b>
   <br>
   @see jade.core.Runtime#createAgentContainer(Profile)

   @author Giovanni Rimassa - Universita' di Parma

 */
public class AgentContainer implements PlatformController {

  private AgentContainerImpl myImpl;
  private String myPlatformName;
  private State platformState = PlatformState.PLATFORM_STATE_VOID;
	private ListenerManager myListenerManager = new ListenerManager();
	
  /**
     Public constructor. This constructor requires a concrete
     implementation of a JADE agent container, which cannot be
     instantiated by applications, so it cannot be meaningfully called
     from application code. The proper way to create an agent
     container from an application is to call the
     <code>Runtime.createContainer()</code> method.
     @see jade.core.Runtime#createAgentContainer(Profile)
     @param impl A concrete implementation of a JADE agent container.
     @param platformName the name of the platform
   */
  public AgentContainer(AgentContainerImpl impl, String platformName) {
    myImpl = impl;
    myPlatformName = platformName;
    platformState = PlatformState.PLATFORM_STATE_READY;
  }


  /**
   * Get agent proxy to local agent given its name.
   * @param localAgentName The short local name of the desired agent.
   * @throws ControllerException If any probelms occur obtaining this proxy.
   */
  public AgentController getAgent(String localAgentName) throws ControllerException {
      // FIXME. To check for security permissions
    if(myImpl == null) {
      throw new ControllerException("Stale proxy.");
    }
    AID agentID = new AID(localAgentName, AID.ISLOCALNAME);
    AgentController a = myImpl.getAgent(agentID);
    if (a == null) {
      throw new ControllerException("Agent " + localAgentName + " not found.");
    } 
    return a; 
  }




  /**
     Creates a new JADE agent, running within this container, 
     @param nickname A platform-unique nickname for the newly created
     agent. The agent will be given a FIPA compliant agent identifier
     using the nickname and the ID of the platform it is running on.
     @param className The fully qualified name of the class that
     implements the agent.
     @param args An object array, containing initialization parameters
     to pass to the new agent. 
     @return A proxy object, allowing to call state-transition forcing
     methods on the real agent instance.*/
  public AgentController createNewAgent(String nickname, String className, Object[] args) throws StaleProxyException {
    if(myImpl == null)
      throw new StaleProxyException();
    try {
      jade.core.Agent a = (jade.core.Agent)Class.forName(new String(className)).newInstance();
      a.setArguments(args);
      AID agentID = new AID(nickname, AID.ISLOCALNAME);
      myImpl.initAgent(agentID, a, false, (JADEPrincipal)null, (Credentials)null);

      Agent result = new Agent(agentID, a);
      return result;
    }
    catch(Exception e) {
      throw new StaleProxyException(e); // it would have been better throwing a ControllerException but that would have broken backward-compatibilityfor 
    }

  }

    // HP Patch begin ----------------------------------------------------------------------------------
    /**
     * Add an Agent to this container. Typically Agent would be some class extending
     * Agent which was instantiated and configured.
     * @param nickname A platform-unique nickname for the newly created agent.
     * The agent will be given a FIPA compliant agent identifier using the nickname and
     * the ID of the platform it is running on.
     * @param anAgent The agent to be added to this agent container.
     * @return An AgentController, allowing to call state-transition forcing methods on the real agent instance.
     */
    public Agent acceptNewAgent(String nickname, jade.core.Agent anAgent)
                                          throws StaleProxyException {
        if (myImpl == null) {
            throw new StaleProxyException();
        }
        AID agentID = new AID(nickname, AID.ISLOCALNAME);
        try {
            myImpl.initAgent(agentID, anAgent, false, (JADEPrincipal)null, (Credentials)null);
        }
        catch(Exception e) {
            throw new StaleProxyException(e);
        }
        return new Agent(agentID, anAgent);
    }

    /**
     * Kill a particular agent.
     * @param nickname A platform-unique nickname of the agent to kill.

    public void killAgent(String nickname)
            throws StaleProxyException, IMTPException, NotFoundException {
        if (myImpl == null) {
            throw new StaleProxyException();
        }
        AID agentID = new AID(nickname, AID.ISLOCALNAME);

        myImpl.killAgent(agentID);
    }
    */
    // HP Patch end ------------------------------------------------------------------------------------


  /**
     Shuts down this container, terminating all the agents running within it.
   */
  public void kill() throws StaleProxyException {
    if(myImpl == null)
      throw new StaleProxyException();
    myImpl.shutDown();
    // release resources of this object
    myImpl = null;
    myPlatformName = null;
    platformState = PlatformState.PLATFORM_STATE_KILLED;
    myListenerManager = null;
  }



  /**
     Installs a new message transport protocol, that will run within
     this container.

     @param address The transport address exported by the new MTP, in
     string format.
     @param className The fully qualified name of the Java class that
     implements the transport protocol.
     @exception MTPException If something goes wrong during transport
     protocol activation.
   */
  public void installMTP(String address, String className) throws MTPException, StaleProxyException {
    if(myImpl == null)
      throw new StaleProxyException();
    try {
	throw new IMTPException("Temporary Hack");
	//      myImpl.installMTP(address, className);
    }
    catch(IMTPException imtpe) { // It should never happen...
      throw new InternalError("Remote exception on a local call.");
    }
  }

  /**
     Removes a message transport protocol, previously running within this
     container.

     @param address The transport address exported by the new MTP, in
     string format.
     @exception MTPException If something goes wrong during transport
     protocol activation.
     @exception NotFoundException If no protocol with the given
     address is currently installed on this container.
   */
  public void uninstallMTP(String address) throws MTPException, NotFoundException, StaleProxyException {
    if(myImpl == null)
      throw new StaleProxyException();
    try {
	throw new IMTPException("Temporary Hack");
	//      myImpl.uninstallMTP(address);
    }
    catch(IMTPException imtpe) { // It should never happen...
      throw new InternalError("Remote exception on a local call.");
    }
  }

  /**
   * return the name (i.e. the HAP) of this platform
   * @deprecated Use getPlatfromName instead.
   **/
  public String getName() { return myPlatformName; }
    
  /**
   * Retrieve the name of the wrapped platform.
   * @return the name (i.e. the HAP) of this platform.
   * @see jade.wrapper.AgentContainer#getContainerName()
   **/
  public String getPlatformName() {
    return myPlatformName;
  }

  /**
   * Retrieve the name of the wrapped container.
   * @return the name of this platform container.
   * @see jade.wrapper.AgentContainer#getPlatformName()
   **/
  public String getContainerName() throws ControllerException {
    if(myImpl == null) {
      throw new ControllerException("Stale proxy.");
    }
    return myImpl.here().getName();
  }

  public void start() throws ControllerException { 
  }

  public void suspend() throws ControllerException {
		throw new ControllerException("Not_Yet_Implemented");
  }

  public void resume() throws ControllerException {
		throw new ControllerException("Not_Yet_Implemented");
  }

  public State getState() { return platformState; }
  
  public synchronized void addPlatformListener(Listener aListener) throws ControllerException {
  	//#ALL_EXCLUDE_BEGIN
  	if (myListenerManager.addListener(aListener) == 1) {
  		myImpl.addPlatformListener(myListenerManager);
  	}
  	//#ALL_EXCLUDE_END
  }
  public synchronized void removePlatformListener(Listener aListener) throws ControllerException {
  	//#ALL_EXCLUDE_BEGIN
  	if (myListenerManager.removeListener(aListener) == 0) {
  		myImpl.removePlatformListener(myListenerManager);
  	}
  	//#ALL_EXCLUDE_END
  }
    
  class ListenerManager implements AgentManager.Listener {
    private Vector listeners = new Vector();
    
  	public int addListener(Listener l) {
  		listeners.addElement(l);
  		return listeners.size();
  	}
  	
  	public int removeListener(Listener l) {
  		listeners.removeElement(l);
  		return listeners.size();
  	}
  	
 		public void addedMTP(MTPEvent ev) {
 			System.out.println("Added MTP");
  	} 
  	public void removedMTP(MTPEvent ev) {
 			System.out.println("Removed MTP");
  	} 
  	public void messageIn(MTPEvent ev) {
 			System.out.println("Message IN");
  	} 
  	public void messageOut(MTPEvent ev) {
 			System.out.println("Message OUT");
  	} 
  	public void addedContainer(jade.core.event.PlatformEvent ev) {
 			System.out.println("Added container");
  	} 
  	public void removedContainer(jade.core.event.PlatformEvent ev) {
 			System.out.println("Removed container");
  	} 
  	public void bornAgent(jade.core.event.PlatformEvent ev) {
  		Enumeration e = listeners.elements();
  		while (e.hasMoreElements()) {
  			Listener l = (Listener) e.nextElement();
  			ev.setSource(AgentContainer.this);
  			l.bornAgent(ev);
  		}
  	} 
  	public void deadAgent(jade.core.event.PlatformEvent ev) {
  		Enumeration e = listeners.elements();
  		while (e.hasMoreElements()) {
  			Listener l = (Listener) e.nextElement();
  			ev.setSource(AgentContainer.this);
  			l.deadAgent(ev);
  		}
  	}
  	public void movedAgent(jade.core.event.PlatformEvent ev) {
 			System.out.println("Moved agent");
  	} 
  	public void suspendedAgent(jade.core.event.PlatformEvent ev) {
 			System.out.println("Suspended agent");
  	} 
  	public void resumedAgent(jade.core.event.PlatformEvent ev) {
 			System.out.println("Resumed agent");
  	} 
        public void frozenAgent(jade.core.event.PlatformEvent ev) {
	                System.out.println("Frozen agent");
	}
        public void thawedAgent(jade.core.event.PlatformEvent ev) {
	                System.out.println("Thawed agent");
	}
//__SECURITY__BEGIN  
  	public void changedAgentPrincipal(jade.core.event.PlatformEvent ev) {
 			System.out.println("Changed agent principal");
  	} 
  	public void changedContainerPrincipal(jade.core.event.PlatformEvent ev) {
 			System.out.println("Changed container principal");
  	} 
//__SECURITY__END 
  }
}
