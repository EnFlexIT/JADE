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

package jade.imtp.leap;

// FIXME: Temporary Hack
import java.lang.reflect.*;

import jade.core.HorizontalCommand;
import jade.core.Service;
import jade.core.IMTPException;


/**

   The <code>NodeSkel</code> class is the remote adapter for JADE
   platform <i>Node</i> components, running over LEAP transport layer.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
class NodeSkel extends Skeleton implements NodeLEAP {

    private NodeAdapter impl;

    public NodeSkel(NodeAdapter na) {
	impl = na;
    }

    public Command executeCommand(Command command) throws Throwable {
	Command resp = null;

	switch (command.getCode()) {

	case Command.ACCEPT_COMMAND: {
	    HorizontalCommand cmd = (HorizontalCommand)command.getParamAt(0);
	    String itfName = (String)command.getParamAt(1);
	    String[] formalParameterTypes = (String[])command.getParamAt(2);
	    Object result = accept(cmd, itfName, formalParameterTypes);

	    resp = new Command(Command.OK);
	    resp.addParam(result);

	    break;
	} 

	case Command.PING_NODE: {
	    Boolean hang = (Boolean)command.getParamAt(0);
	    ping(hang.booleanValue());

	    resp = new Command(Command.OK);

	    break;
	} 

	case Command.EXIT_NODE: {

	    exit();

	    resp = new Command(Command.OK);

	    break;
	} 

	}

	return resp;
    }


    public Object accept(HorizontalCommand cmd, String itfName, String[] formalParameterTypes) throws IMTPException {

	try {
	    Class itf = Class.forName(itfName);
	    Class[] classes = new Class[formalParameterTypes.length];
	    for(int i = 0; i < formalParameterTypes.length; i++) {
		String className = formalParameterTypes[i];
		if(className.equals("boolean")) {
		    classes[i] = Boolean.TYPE;
		}
		else if(className.equals("int")) {
		    classes[i] = Integer.TYPE;
		}
		else {
		    classes[i] = Class.forName(className);
		}
	    }

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
	    Service.Slice slice = impl.getSlice(serviceName);

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
	catch(Exception e) {
	    e.printStackTrace();
	    throw new IMTPException("Error during horizontal command dispatching", e);
	}
    }

    public void ping(boolean hang) throws IMTPException {
      if(hang) {
	  waitTermination();
      }
    }

    public void exit() throws IMTPException {
      // Unblock threads hung in ping() method (this will deregister the container)
      notifyTermination();
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


    // This monitor is used to hang a remote ping() call in order to
    // detect node failures.
    private Object terminationLock = new Object();


}
