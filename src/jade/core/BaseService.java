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

//#APIDOC_EXCLUDE_FILE


import jade.core.behaviours.Behaviour;

import jade.util.leap.Map;
import jade.util.leap.HashMap;




/**

   The <code>BaseService</code> abstract class partially implements
   the <code>Service</code> interface, providing a simple and uniform
   mechanism for slice management and service discovery.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

*/
public abstract class BaseService implements Service {

    public static final String MAIN_SLICE = ServiceFinder.MAIN_SLICE;
    public static final String THIS_SLICE = ServiceFinder.THIS_SLICE;

    public void init(AgentContainer ac, Profile p) throws ProfileException {
	myFinder = p.getServiceFinder();
	myIMTPManager = p.getIMTPManager();

	slices = new HashMap();
	aliases = new HashMap();
	
	//#MIDP_EXCLUDE_BEGIN
	int verbosity = 0;
	try {
		String className = getClass().getName();
		String tmp = className.replace('.', '_');
		String verbosityKey = tmp+"_verbosity";
		String verbosityFormatKey = tmp+"_verbosity_format";
		verbosity = Integer.parseInt(p.getParameter(verbosityKey, null));
		if (verbosity > 0) {
			int index = className.lastIndexOf('.');
			String name = (index < 0 ? className : className.substring(index+1));
			myLogger = new jade.util.Logger(name, verbosity, null, p.getParameter(verbosityFormatKey, "%t [%i] %m"));
		}
		
		log("Initialized", 1);
	}
	catch (Exception e) {
		// Ignore and keep default: no logs
	}
	//#MIDP_EXCLUDE_END	
    }

    // Package scoped method to receive the Command Processor from the
    // agent container at startup time. The rationale for avoiding
    // passing this to the constructor is to prevent concrete services
    // from accessing the Command Processor (they are less trusted than jade.core).
    // Notice that, for further measure, this method only works the first time it
    // is called.
    void setCommandProcessor(CommandProcessor cp) {
	if(myCommandProcessor == null) {
	    myCommandProcessor = cp;
	}
    }


    /**
       The <code>getSlice()</code> implementation of this class works
       as follows:
       <ol>
       <li><i>First, the name alias table is used to convert the given 
       slice name into another name, if any</i></li>

       <li><i>Then, the new name (which may or may not be different
       from the original one) is used to look up an internal table
       keeping the service slices</i></li>

       <li><i>If no slice was found, the</i>
       <code>ServiceFinder</code> <i>is asked to provide the slice,
       which is then put into the local table.</i></li> 
       </ol>
    */
    public Service.Slice getSlice(String name) throws ServiceException {

	// First look through the name alias table
	String realName = lookupAlias(name);

	// Then look up in the slice table
	Service.Slice s = (Service.Slice)slices.get(realName);

	// if there's not a suitable slice, ask the service finder,
	// then cache the result in the slices table.
	if(s == null) {
	    try {
		s = myFinder.findSlice(getName(), realName);
		slices.put(realName, s);
	    }
	    catch(IMTPException imtpe) {
		throw new ServiceException("IMTP Error while using the Service Finder", imtpe);
	    }
	}

	return s;

    }

    /**
       This method returns the current number of slices known to this
       service <b>on this node</b>. Due to the distributed nature of
       many JADE services, there is no guaranteed that calling this
       method for the same service on different nodes will actually
       result on the same number.

       @return The number of slices of this service that are known to
       this node.
    */
    public int getNumberOfSlices() {
	return slices.size();
    }

    public Node getLocalNode() throws IMTPException {
	return myIMTPManager.getLocalNode();
    }

    /**
       The <code>getAllSlices()</code> implementation of this class
       directly retrieves the current list of slices from the Service
       Manager.
    */
    public Service.Slice[] getAllSlices() throws ServiceException {
	try {
	    return myFinder.findAllSlices(getName());
	}
	catch(IMTPException imtpe) {
	    throw new ServiceException("IMTP Error while using the Service Finder", imtpe);
	}
    }


    /**
       This protected method allows subclasses to define their own
       naming schemes, by adding aliases for existing slice names.

       @param alias The new alias name.
       @param name The real name this alias must be mapped to.
    */
    protected void addAlias(String alias, String name) {
	aliases.put(alias, name);
    }

    /**
       This protected method is used by <code>getSlice()</code> to
       dereference aliases for slice names. Subclasses can override
       this method to build their own service-specific naming schema.

       @param alias The alias name to map to a real slice name.
       @return A mapped name, or the original one if no mapping was
       found.
    */
    protected String lookupAlias(String alias) {
	String result = (String)aliases.get(alias);
	if(result != null) {
	    return result;
	}
	else {
	    return alias;
	}
    }

    // this should be properly implemented 
    // by the services that have helpers
    public ServiceHelper getHelper(Agent a) {
         return null;
    }

    // This will be overridden by services that provide an AMS-level
    // access to (some of) their features
    public Behaviour getAMSBehaviour() {
	return null;
    }

    public void boot(Profile p) throws ServiceException {
	// Empty placeholder method
    }

    public Object submit(VerticalCommand cmd) throws ServiceException {
    	String cmdName = cmd.getName(); 
	String[] ownedCommands = getOwnedCommands();

	for(int i = 0; i < ownedCommands.length; i++) {
	    if(cmdName.equals(ownedCommands[i])) {
		return myCommandProcessor.processOutgoing(cmd);
	    }
	}

	throw new ServiceException("Command <" + cmdName + "> does not belong to service <" + getName() + ">");

    }

    protected Service.Slice getFreshSlice(String name) throws ServiceException {

	// First look through the name alias table
	String realName = lookupAlias(name);

	// Invalidate the cache entry
	slices.remove(realName);

	// Get a newer slice and return it
	return getSlice(name);

    }

    private ServiceFinder myFinder;
    private IMTPManager myIMTPManager;

    private CommandProcessor myCommandProcessor = null;

    private Map slices;
    private Map aliases;

    //#MIDP_EXCLUDE_BEGIN
    protected jade.util.Logger myLogger;
    //#MIDP_EXCLUDE_END
    
    protected void log(String msg, int level) {
    	//#MIDP_EXCLUDE_BEGIN
    	if (myLogger != null) {
    		myLogger.log(msg, level);
    	}
    	//#MIDP_EXCLUDE_END
    }
}
