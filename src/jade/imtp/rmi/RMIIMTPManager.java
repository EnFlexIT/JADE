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

import java.util.List;
import java.util.ArrayList;
import java.rmi.*;
import java.rmi.registry.*;
import java.net.InetAddress;

import jade.core.*;
import jade.mtp.TransportAddress;

/**
 * @author Giovanni Caire - Telecom Italia Lab
 */
public class RMIIMTPManager implements IMTPManager {
	private Profile myProfile;
	String mainHost;
	int mainPort;
	String platformRMI;

	public RMIIMTPManager() {
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
    }

    /**
     */
    public void remotize(MainContainer mc) throws IMTPException {
    	try {
      	MainContainerRMI mcRMI = new MainContainerRMIImpl(mc);
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
      	throw new IMTPException("", ce);
    	}
    	catch(RemoteException re) {
      	System.err.println("Communication failure while starting JADE Runtime System.");
      	throw new IMTPException("", re);
    	}
    	catch(Exception e) {
      	System.err.println("Problem starting JADE Runtime System.");
      	throw new IMTPException("", e);
    	}
    }

    /**
     */
    public MainContainer getMain() throws IMTPException {
    	// Look the remote Main Container up into the
	  	// RMI Registry.
    	try {
	    	MainContainerRMI remoteMCRMI = (MainContainerRMI)Naming.lookup(platformRMI);
  	  	MainContainer remoteMC = new MainContainerAdapter(remoteMCRMI);
    		return remoteMC;
    	}
    	catch (Exception e) {
      	throw new IMTPException("", e);
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
    		throw new IMTPException("", e);
    	}
    }
}

