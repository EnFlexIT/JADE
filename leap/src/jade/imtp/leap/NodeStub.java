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

import jade.core.*;

/**
   This calss implements a stub to a remote LEAP Node.
   @author Giovanni Caire - TILAB
   @author Giovanni Rimassa - FRAMeTech s.r.l
 */
class NodeStub extends Stub implements Node {
	private String name;
	
  public NodeStub() {
		super();
  }

  public NodeStub(int id) {
		super(id);
  }

  public void setName(String name) {
  	this.name = name;
  }
  
  public String getName() {
  	return name;
  }
  
  public boolean hasServiceManager() {
  	return false;
  }

  public void exportSlice(String serviceName, Service.Slice localSlice) {
  	throw new RuntimeException("Trying to export a slice on a node stub");
  }
  
  public void unexportSlice(String serviceName) {
  }

  /**
     Accepts a command to be forwarded to the remote node.
     @param cmd The horizontal command to process.
     @return The object that is the result of processing the command.
     @throws IMTPException If a communication error occurs while
     contacting the remote node.
  */
  public Object accept(HorizontalCommand cmd) throws IMTPException {
		try {
	    Command wrapperCmd = new Command(Command.ACCEPT_COMMAND, remoteID);
	    wrapperCmd.addParam(cmd);
	    Command result = theDispatcher.dispatchCommand(remoteTAs, wrapperCmd);

	    // Check whether an exception occurred in the remote container
	    checkResult(result, new String[] { });

	    return result.getParamAt(0);
		}
		catch (DispatcherException de) {
	    throw new IMTPException(DISP_ERROR_MSG, de);
		} 
		catch (UnreachableException ue) {
	    throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
  }

  /**
     Serves an incoming horizontal command. This should never
     happen since this is a stub --> throw an exception.
  */
  public Object serve(HorizontalCommand cmd) throws ServiceException {
  	throw new ServiceException("Trying to make a node stub serve an horizontal command");
  }
  
  /**
     Serves an incoming vertical command, locally. This should never
     happen since this is a stub --> throw an exception.
  */
  public Object serve(VerticalCommand cmd) throws ServiceException {
  	throw new ServiceException("Trying to make a node stub serve a vertical command");
  }

  /**
     Performs a ping operation on the remote node.
     @param hang If <code>true</code>, the call hangs until the node
     exits or is interrupted.
     @return If the node is currently terminating, <code>true</code>
     is returned, else <code>false</code>
  */
  public boolean ping(boolean hang) throws IMTPException {
		Command cmd;
		if(hang) {
		    cmd = new Command(Command.PING_NODE_BLOCKING, remoteID);
		}
		else {
		    cmd = new Command(Command.PING_NODE_NONBLOCKING, remoteID);
		}
		cmd.addParam(new Boolean(hang));
	
		try {
		    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
		    checkResult(result, new String[] { });
	
		    Boolean b = (Boolean)result.getParamAt(0);
		    return b.booleanValue();
		}
		catch (DispatcherException de) {
		    throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
		    throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
  }

  public void interrupt() throws IMTPException {
		Command cmd = new Command(Command.EXIT_NODE, remoteID);
	
		try {
		    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
		    checkResult(result, new String[] { });
		}
		catch (DispatcherException de) {
		    throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
		    throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
  }
  
  public void exit() throws IMTPException {
		Command cmd = new Command(Command.INTERRUPT_NODE, remoteID);
	
		try {
		    Command result = theDispatcher.dispatchCommand(remoteTAs, cmd);
		    checkResult(result, new String[] { });
		}
		catch (DispatcherException de) {
		    throw new IMTPException(DISP_ERROR_MSG, de);
		}
		catch (UnreachableException ue) {
		    throw new IMTPException(UNRCH_ERROR_MSG, ue);
		}
  }
}
