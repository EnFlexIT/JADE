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

package jade.imtp.rmi;


import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import jade.core.HorizontalCommand;
import jade.core.Service;

import jade.util.leap.Map;
import jade.util.leap.HashMap;


/**
   @author Giovanni Rimassa - FRAMeTech s.r.l.
 */
public class NodeRMIImpl extends UnicastRemoteObject implements NodeRMI {

    public NodeRMIImpl() throws RemoteException {
	localSlices = new HashMap();
    }

    public Object accept(HorizontalCommand cmd, Class itf, Class[] classes) throws RemoteException {
	System.out.println("--- Command Received ---");
	System.out.println("Name: <" + cmd.getName() + ">");
	System.out.println("Service: <" + cmd.getService() + ">");

	/***
	Object[] args = cmd.getParams();
	for(int i = 0; i < args.length; i++) {
	    System.out.println("param[" + i + "] = " + args[i]);
	}
	***/
	System.out.println("--- ================ ---");

	String serviceName = cmd.getService();
	String commandName = cmd.getName();
	Object[] commandParams = cmd.getParams();

	// Look up in the local slices table and find the slice to dispatch to
	Service.Slice slice = (Service.Slice)localSlices.get(serviceName);

	if(slice != null) {
	    // Reflective dispatching
	    try {
		Method toCall = itf.getMethod(commandName, classes);
		try {
		    return toCall.invoke(slice, commandParams);
		}
		catch(InvocationTargetException ite) {
		    Throwable cause = ite.getCause();

		    // If this is a declared exception of the method, let it through
		    Class[] declaredExceptions = toCall.getExceptionTypes();
		    for(int i = 0; i < declaredExceptions.length; i++) {
			if(declaredExceptions[i].equals(cause.getClass())) {
			    return cause;
			}
		    }

		    // Unknown or runtime exception: just print it for now...
		    cause.printStackTrace();
		}

	    }
	    catch(Exception e) {
		// FIXME: Throw something to mean 'Wrong version', 'Bad parameter' or whatever is needed
		e.printStackTrace();
	    }
	}
	else {
	    // FIXME: Throw something to mean 'Service Unknown'
	    System.out.println("-- Service Unknown --");
	}

	// FIXME: Make it so that the execution never reaches here (i.e. throw in every catch clause)
	return null;

    }

    public void ping(boolean hang) throws RemoteException {
      if(hang) {
	  waitTermination();
      }
    }

    public void exit() throws RemoteException {
      // Unblock threads hung in ping() method (this will deregister the container)
      notifyTermination();
    }

    public void exportSlice(String serviceName, Service.Slice localSlice) {
	localSlices.put(serviceName, localSlice);
    }

    public void unexportSlice(String serviceName) {
	localSlices.remove(serviceName);
    }


    private void waitTermination() {
	synchronized(terminationLock) {
	    try {
		terminationLock.wait();
	    }
	    catch(InterruptedException ie) {
		System.out.println("PING wait interrupted");
		// Do nothing
	    }
	}
    }

    private void notifyTermination() {
      synchronized(terminationLock) {
	  terminationLock.notifyAll();
      }
    }


    // A map, indexed by service name, of all the local slices of this
    // node. This map is used to dispatch incoming commands to the
    // service they belong to.
    private Map localSlices;

    // This monitor is used to hang a remote ping() call in order to
    // detect node failures.
    private Object terminationLock = new Object();

}
