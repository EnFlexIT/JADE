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

import jade.core.BaseNode;
import jade.core.Service;
import jade.core.CommandProcessor;
import jade.core.HorizontalCommand;
import jade.core.VerticalCommand;
import jade.core.IMTPException;
import jade.core.ServiceException;


/**
   This class wraps a JICP endpoint representing the local platform
   node. When this node is sent over the network, the JICP enpoint
   information is transferred, too.

   @author Giovanni Rimassa - FRAMeTech s.r.l

 */
class NodeAdapter extends BaseNode {

    public NodeAdapter(String name, CommandDispatcher disp) {
	super(name);
	NodeSkel skel = new NodeSkel(this);
	adaptee = skel;
	disp.registerSkeleton(skel, this);
    }

    public NodeAdapter(String name, NodeLEAP node) {
	super(name);
	adaptee = node;
    }

    public void setCommandProcessor(CommandProcessor cp) {
	processor = cp;
    }

    public Object accept(HorizontalCommand cmd) throws IMTPException {
	return adaptee.accept(cmd, null, null);
    }

    public Object serve(VerticalCommand cmd) throws ServiceException {
	if(processor == null) {
	    throw new ServiceException("No command processor for node <" + getName() + ">");
	}

	return processor.processIncoming(cmd);
    }

    public Service.Slice getSlice(String serviceName) {
	return super.getSlice(serviceName);
    }

    public boolean ping(boolean hang) throws IMTPException {
	return adaptee.ping(hang);
    }

    public void exit() throws IMTPException {
	adaptee.exit();
    }

    public void interrupt() throws IMTPException {
	adaptee.interrupt();
    }


    // Package scoped, used only for node serialization
    NodeLEAP getAdaptee() {
	return adaptee;
    }


    private NodeLEAP adaptee;
    private transient CommandProcessor processor;

}
