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

package jade.core;

import jade.util.leap.Map;
import jade.util.leap.HashMap;
import jade.util.leap.Serializable;


/**
   This class provides a partial implementation of the
   <code>Node</code> interface. Concrete IMTPs will have to provide a
   full implementation of the <code>Node</code> interface, possibly by
   subclassing this class.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

 */
public abstract class BaseNode implements Node, Serializable {

    public BaseNode(String name, boolean hasSM) {
	myName = name;
	hasLocalSM = hasSM;
	localSlices = new HashMap();
    }

    public BaseNode(String name) {
	this(name, false);
    }

    public void setName(String name) {
	myName = name;
    }

    public String getName() {
	return myName;
    }

    public boolean hasServiceManager() {
	return hasLocalSM;
    }

    /***
    public void changeNodePrincipal(CertificateFolders certs) throws IMTPException {
	try {
	    adaptee.changeNodePrincipal(certs);
	}
	catch(RemoteException re) {
	    throw new IMTPException("RMI exception", re);
	}
    }
    ***/

    public void exportSlice(String serviceName, Service.Slice localSlice) {
	localSlices.put(serviceName, localSlice);
    }

    public void unexportSlice(String serviceName) {
	localSlices.remove(serviceName);
    }

    protected Service.Slice getSlice(String serviceName) {
	return (Service.Slice)localSlices.get(serviceName);
    }


    public Object serve(HorizontalCommand cmd) throws ServiceException {

	String serviceName = cmd.getService();
	String commandName = cmd.getName();
	Object[] commandParams = cmd.getParams();

	// Look up in the local slices table and find the slice to dispatch to
	Service.Slice slice = getSlice(serviceName);

	if(slice != null) {
	    VerticalCommand vCmd = slice.serve(cmd);

	    if(vCmd != null) {
		// Hand it to the command processor
		serve(vCmd);
		return vCmd.getReturnValue();
	    }
	    else {
		return cmd.getReturnValue();
	    }
	}
	else {
	    throw new ServiceException("-- Service Unknown --");
	}
    }

    public void setCommandProcessor(CommandProcessor cp) {
	processor = cp;
    }

    public Object serve(VerticalCommand cmd) throws ServiceException {
	if(processor == null) {
	    throw new ServiceException("No command processor for node <" + getName() + ">");
	}

	return processor.processIncoming(cmd);
    }

    private transient CommandProcessor processor;
    // The name of this node
    private String myName;

    // True if a local copy of the Service Manager is deployed at this Node
    private boolean hasLocalSM;

    // A map, indexed by service name, of all the local slices of this
    // node. This map is used to dispatch incoming commands to the
    // service they belong to.
    private transient Map localSlices;


}
