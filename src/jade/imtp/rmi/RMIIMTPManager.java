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
package jade.imtp.rmi;


import java.net.InetAddress;

import java.rmi.*;
import java.rmi.registry.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;


import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.mtp.TransportAddress;


/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public class RMIIMTPManager implements IMTPManager {

  private static class RemoteProxyRMI implements RemoteProxy {
    private AgentContainerRMI ref;
    private AID               receiver;

    // This needs to be restored whenever a RemoteProxyRMI object is
    // serialized.
    private transient RMIIMTPManager manager;

    public RemoteProxyRMI(AgentContainerRMI ac, AID recv) {
        ref = ac;
        receiver = recv;
    }

    public AID getReceiver() {
      return receiver;
    } 

    public AgentContainer getRef() {
      return manager.getAdapter(ref);
    } 

    public void dispatch(ACLMessage msg) throws NotFoundException {
      try {
        ref.dispatch(msg, receiver);
      } 
      catch (RemoteException re) {
	throw new NotFoundException("IMTP failure: [" + re.getMessage() + "]");
      }
      catch (IMTPException imtpe) {
	throw new NotFoundException("IMTP failure: [" + imtpe.getMessage() + "]");
      }
    }

    public void ping() throws UnreachableException {
      try {
	ref.ping(false);
      } 
      catch (RemoteException re) {
	throw new UnreachableException("Unreachable remote object: [" + re.getMessage() + "]");
      }
      catch (IMTPException imtpe) {
	throw new UnreachableException("Unreachable remote object: [" + imtpe.getMessage() + "]");
      }
    }

    public void setMgr(RMIIMTPManager mgr) {
      manager = mgr;
    }

  } // End of RemoteProxyRMI class


  private Profile myProfile;
  private String mainHost;
  private int mainPort;
  private String platformRMI;
  private MainContainer remoteMC;

  // Maps agent containers into their stubs
  private Map stubs;

  public RMIIMTPManager() {
    stubs = new HashMap();
  }

  /**
   */
  public void initialize(Profile p) throws IMTPException {
    try {
      myProfile = p;
      mainHost = myProfile.getParameter(Profile.MAIN_HOST);
      mainPort = Integer.parseInt(myProfile.getParameter(Profile.MAIN_PORT));
      platformRMI = "rmi://" + mainHost + ":" + mainPort + "/JADE";
    }
    catch (ProfileException pe) {
      throw new IMTPException("Can't get main host and port", pe);
    }
  }

  /**
   */
  public void remotize(AgentContainer ac) throws IMTPException {
    try {
      AgentContainerRMI acRMI = new AgentContainerRMIImpl(ac, this);
      stubs.put(ac, acRMI);
    }
    catch(RemoteException re) {
      throw new IMTPException("Failed to create the RMI container", re);
    }
  }

  /**
   */
  public void remotize(MainContainer mc) throws IMTPException {
    try {
      MainContainerRMI mcRMI = new MainContainerRMIImpl(mc, this);
      Registry theRegistry = LocateRegistry.createRegistry(mainPort);
      Naming.bind(platformRMI, mcRMI);
    }
    catch(ConnectException ce) {
      // This one is thrown when trying to bind in an RMIRegistry that
      // is not on the current host
      System.out.println("ERROR: trying to bind to a remote RMI registry.");
      System.out.println("If you want to start a JADE main container:");
      System.out.println("  Make sure the specified host name or IP address belongs to the local machine.");
      System.out.println("  Please use '-host' and/or '-port' options to setup JADE host and port.");
      System.out.println("If you want to start a JADE non-main container: ");
      System.out.println("  Use the '-container' option, then use '-host' and '-port' to specify the ");
      System.out.println("  location of the main container you want to connect to.");
      throw new IMTPException("RMI Binding error", ce);
    }
    catch(RemoteException re) {
      throw new IMTPException("Communication failure while starting JADE Runtime System.", re);
    }
    catch(Exception e) {
      throw new IMTPException("Problem starting JADE Runtime System.", e);
    }
  }

  /**
     Disconnects the given Agent Container and hides it from remote
     JVMs.
  */
  public void unremotize(AgentContainer ac) throws IMTPException {
    try {
      AgentContainerRMIImpl impl = (AgentContainerRMIImpl)getRMIStub(ac);    
      if(impl == null)
	throw new IMTPException("No RMI object for this agent container");
      impl.unexportObject(impl, true);
    }
    catch(RemoteException re) {
      throw new IMTPException("RMI error during shutdown", re);
    }
    catch(ClassCastException cce) {
      throw new IMTPException("The RMI implementation is not locally available", cce);
    }
  }

  /**
     Disconnects the given Main Container and hides it from remote
     JVMs.
  */
  public void unremotize(MainContainer mc) throws IMTPException {
    // Unbind the main container from RMI Registry
    // Unexport the RMI object
  }

  public RemoteProxy createAgentProxy(AgentContainer ac, AID id) throws IMTPException {
    AgentContainerRMI acRMI = getRMIStub(ac);
    RemoteProxyRMI rp = new RemoteProxyRMI(acRMI, id);
    rp.setMgr(this);
    return rp;
  }

  /**
   */
  public synchronized MainContainer getMain() throws IMTPException {
    // Look the remote Main Container up into the
    // RMI Registry.
    try {
      if(remoteMC == null) {
	MainContainerRMI remoteMCRMI = (MainContainerRMI)Naming.lookup(platformRMI);
	remoteMC = new MainContainerAdapter(remoteMCRMI, this);
      }
      return remoteMC;
    }
    catch (Exception e) {
      throw new IMTPException("Exception in RMI Registry lookup", e);
    }
  }

  /**
   */
  public void shutDown() {
  }

  /**
   */
  public List getLocalAddresses() throws IMTPException {
    try {
      List l = new ArrayList();
      TransportAddress addr = new RMIAddress(InetAddress.getLocalHost().getHostName(), null, null, null);
      l.add(addr);
      return l;
    }
    catch (Exception e) {
      throw new IMTPException("Exception in reading local addresses", e);
    }
  }

  AgentContainerRMI getRMIStub(AgentContainer ac) {
    return (AgentContainerRMI)stubs.get(ac);
  }

  AgentContainer getAdapter(AgentContainerRMI acRMI) {
    Iterator it = stubs.entrySet().iterator();
    while(it.hasNext()) {
      Map.Entry e = (Map.Entry)it.next();
      if(acRMI.equals(e.getValue()))
	return (AgentContainer)e.getKey();
    }
    AgentContainer ac = new AgentContainerAdapter(acRMI, this);
    stubs.put(ac, acRMI);
    return ac;
  }

  void adopt(RemoteProxy rp) throws IMTPException {
    try {
      RemoteProxyRMI rpRMI = (RemoteProxyRMI)rp;
      rpRMI.setMgr(this);
    }
    catch(ClassCastException cce) {
      throw new IMTPException("Cannot adopt this Remote Proxy", cce);
    }
  }


}

