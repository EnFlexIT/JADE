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


/**

   The <code>Service</code> interface represents a centralized view of
   a JADE kernel-level service. Most JADE services are actually
   distributed, and each part of theirs, that is deployed at a given
   network node, is called <em>service slice</em>. The various slices
   of a service work together to carry out that service's task.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public interface Service {

    /**
       The <code>Slice</code> nested interface represents that part of
       a service that is deployed at a given network node.
    */
    public interface Slice {

	/**
	   Access the service object which this slice is a part of.

	   @return A <code>Service</code> object, that has
	   <code>this</code> as one of its slices.

	   @see jade.core.Service#getSlice(String name)
	*/
	Service getService();

	/**
	   Access the node where this slice resides.

	   @returns The node where this service slice is actually
	   running.
	   @throws ServiceException If some problems occur in
	   retrieving the local node.
	*/
	Node getNode() throws ServiceException;

	/**
	   Serves an incoming vertical command. Typically, concrete
	   slices will serve the command by invoking their service
	   interface methods.  If the command execution has a result
	   or it raises an exception, the outcome is stored in the
	   command return value slot.

	   @param cmd The command that is to be served.
	   @see jade.core.Command#setReturnValue()
	*/
	void serve(VerticalCommand cmd);

    }

    /**
       An implementation of the <code>Slice</code> interface,
       supporting routed dispatching of horizontal commands.
    */
    public class SliceProxy implements Slice {

	public SliceProxy() {
	    this(null, null);
	}

	public SliceProxy(Service svc, Node n) {
	    myService = svc;
	    myNode = n;
	}

	public Service getService() {
	    return myService;
	}

	public Node getNode() throws ServiceException {
	    return myNode;
	}

	public void setNode(Node n) {
	    myNode = n;
	}

	/**
	   Try to serve an incoming vertical command. If the command
	   happens to also be an instance of the
	   <code>HorizontalCommand</code> class, this proxy object
	   will route the command to its remote implementation.

	   @param cmd The command to serve, possibly through the network.
	*/
	public void serve(VerticalCommand cmd) {
	    if(cmd instanceof HorizontalCommand) {
		try {
		    HorizontalCommand command = (HorizontalCommand)cmd;
		    cmd.setReturnValue(myNode.accept(command));
		}
		catch(IMTPException imtpe) {
		    cmd.setReturnValue(new ServiceException("An error occurred while routing the command to the remote implementation", imtpe));
		}
	    }
	    else {
		cmd.setReturnValue(new ServiceException("Cannot serve a purely vertical command through a Proxy"));
	    }
	}

	private Node myNode;
	private Service myService;

    }

    /**
       Retrieve the name of this service, that can be used to look up
       its slices in the Service Finder.

       @return The name of this service.
       @see jade.core.ServiceFinder
    */
    String getName();

    /**
       Retrieve by name a slice of this service. For distributed
       services, the returned slice will generally be some kind of
       proxy object to the real, remote slice.
       The actual proxy management policy (caching, reconnection,
       etc.) is decided by concrete services.

       @param name A name for the requested slice. The name must be
       unique within this service.
       @return The <code>Slice<code> object that is a part of this
       service and is identified by the given name, or
       <code>null</code> if no such slice exists.
       @throws ServiceException If some underlying error (e.g. a
       network problem) occurs, that does not allow to decide whether
       the requested slice exists or not.
    */
    Slice getSlice(String name) throws ServiceException;

    /**
       Retrieve the locally installed slice of this service. A service
       without horizontal interfaces can safely return
       <code>null</code> from this method.

       @return The slice of this service that resides on the local
       platform node, or <code>null</code> if no such slice exists.
    */
    Slice getLocalSlice();

    /**
       Retrieve the whole array of slices that compose this service.

       @return An array of <code>Service.Slice</code> objects, whose
       elements are the slices of this service deployed at the
       different platform nodes.
       @throws ServiceException If some underlying error (e.g. a
       network problem) occurs, that does not allow to retrieve the
       full slice list.
    */
    Slice[] getAllSlices() throws ServiceException;

    /**
       Retrieve the interface through which the different service
       slices will communicate, that is, the service <i>Horizontal
       Interface</i>.

       @return A <code>Class</code> object, representing the interface
       that is implemented by the slices of this service.  Let
       <code>s</code> be the <code>Class</code> object corresponding
       to the <code>Service.Slice</code> interface, and let
       <code>c</code> be the returned <code>Class</code> object. Then,
       the two following conditions must hold:
       <ol>
       <li><code>c.isInterface() == true</code></li>
       <li><code>s.isAssignableFrom(c) == true</code></li>
       </ol>
    */
    Class getHorizontalInterface();

    /**
       Query by how many slices this service is composed at present.

       @return The number of slices belonging to this service. An
       active service must have at least one slice.
    */
    int getNumberOfSlices();


    /**
       Access the command filter this service needs to perform its
       tasks. This filter will be installed within the local command
       processing engine.

       @return A <code>Filter</code> object, used by this service to
       intercept and process kernel-level commands.
    */
    Filter getCommandFilter();

}
