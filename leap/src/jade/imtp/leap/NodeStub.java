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
import jade.core.IMTPException;
import jade.core.UnreachableException;


/**

   The <code>NodeStub</code> class is the remote proxy of a JADE
   platform <i>Node</i> component, running over LEAP transport layer.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
class NodeStub extends Stub implements NodeLEAP {

    public NodeStub() {
	super();
    }

    public NodeStub(int id) {
	super(id);
    }

    public Object accept(HorizontalCommand cmd, String itf, String[] svcInterfaces) throws IMTPException {
	try {

	    Command wrapperCmd = new Command(Command.ACCEPT_COMMAND, remoteID);
	    wrapperCmd.addParam(cmd);
	    wrapperCmd.addParam(itf);
	    wrapperCmd.addParam(svcInterfaces);

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

    public void ping(boolean hang) throws IMTPException {
	Command cmd = new Command(Command.PING_NODE, remoteID);
	cmd.addParam(new Boolean(hang));

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


}
