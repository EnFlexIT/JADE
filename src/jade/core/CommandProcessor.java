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


import jade.util.leap.List;
import jade.util.leap.LinkedList;
import jade.util.leap.Map;
import jade.util.leap.HashMap;

/**

   Processes JADE kernel-level commands, managing a filter/sink system
   to support dynamically configurable platform services.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public class CommandProcessor {

    public CommandProcessor() {
	inFilters = new LinkedList();
	outFilters = new LinkedList();
	sourceSinks = new HashMap();
	targetSinks = new HashMap();
	updateFiltersArray();
    }


    /**
       Add a new filter to the filter chain.

       @param f The new filter to add.
       @param direction Whether to add this filter to the outgoing or
       incoming filter chain.
    */
    public synchronized void addFilter(Filter f, boolean direction) {

	// FIXME: How do we know the right position for this filter in
	// the filter chain?

	if(direction == Filter.INCOMING) {
	    inFilters.add(f);
	}
	else {
	    outFilters.add(f);
	}

	updateFiltersArray();
    }

    /**
       Remove a filter from the filter chain.

       @param f The filter to remove.
    */
    public synchronized void removeFilter(Filter f, boolean direction) {
	if(direction == Filter.INCOMING) {
	    inFilters.remove(f);
	}
	else {
	    outFilters.remove(f);
	}

	updateFiltersArray();
    }

    /**
       Register a command sink object to handle the given vertical commmand set.

       @param snk A service-specific implementation of the
       <code>Sink</code> interface, managing a set of vertical
       commands.
       @param side One of the two constants
       <code>Sink.COMMAND_SOURCE</code> or
       <code>Sink.COMMAND_TARGET</code>, to state whether this sink
       will handle locally issued commands or commands incoming from
       remote nodes.
       @param commandNames An array containing all the names of the
       vertical commands the new sink wants to handle.
       @throws ServiceException If some other sink is already
       registered for a member of the <code>commandNames</code> set.
    */
    public synchronized void registerSink(Sink snk, boolean side, String[] commandNames) throws ServiceException {

	Map sinks;
	if(side == Sink.COMMAND_SOURCE) {
	    sinks = sourceSinks;
	}
	else {
	    sinks = targetSinks;
	}
	for(int i = 0; i < commandNames.length; i++) {
	    Object old = sinks.put(commandNames[i], snk);
	    if(old != null) { // Command name owned by another service
		sinks.put(commandNames[i], old);
		throw new ServiceException("Command <" + commandNames[i] + "> has a sink already.");
	    }
	}
    }

    /**
       Deregister a sink that is currently handling a given set of vertical commands.

       @param side One of the two constants
       <code>Sink.COMMAND_SOURCE</code> or
       <code>Sink.COMMAND_TARGET</code>, to state whether the sink to
       be removed is handling locally issued commands or commands
       incoming from remote nodes.
       @param commandNames An array containing all the names of the
       vertical commands currently handled by the sink to be removed.
       @throws ServiceException If a member of the
       <code>commandNames</code> set has no associated command sink.
    */
    public synchronized void deregisterSink(boolean side, String[] commandNames) throws ServiceException {
	
	Map sinks;
	if(side == Sink.COMMAND_SOURCE) {
	    sinks = sourceSinks;
	}
	else {
	    sinks = targetSinks;
	}
	for(int i = 0; i < commandNames.length; i++) {
	    Object snk = sinks.remove(commandNames[i]);
	    if(snk == null) {
		throw new ServiceException("No sink is registered for command <" + commandNames[i] + ">");
	    }
	}
    }

    /**
       Process an outgoing command object, carrying out the action it
       represents.  This method is not synchronized and it must be
       reentrant and as fast as possible, because it is going to be a
       bottleneck of the kernel call flow.

       @param cmd The <code>VerticalCommand</code> object to process.
    */
    public Object processOutgoing(VerticalCommand cmd) {

	// FIXME: Should manage the blocking and skipping filter states...

	// Pass the command through every outgoing filter
	Filter[] arr = outFiltersArray;
	for(int i = 0; i < arr.length; i++) {
	    Filter f = arr[i];

	    // Give each filter a chance to veto the command
	    boolean accepted = f.accept(cmd);
	    if(!accepted) {
		// FIXME: Should we throw e.g. a VetoedCommandException?
		return null;
	    }
	}

	Sink s = (Sink)sourceSinks.get(cmd.getName());
	if(s != null) {
	    s.consume(cmd);
	}

	return cmd.getReturnValue();

    }

    /**
       Process an incoming command object, carrying out the action it
       represents.  This method is not synchronized and it must be
       reentrant and as fast as possible, because it is going to be a
       bottleneck of the kernel call flow.

       @param cmd The <code>VerticalCommand</code> object to process.
    */
    public Object processIncoming(VerticalCommand cmd) {
	// FIXME: Should manage the blocking and skipping filter states...

	// Pass the command through every outgoing filter
	Filter[] arr = inFiltersArray;
	for(int i = 0; i < arr.length; i++) {
	    Filter f = arr[i];

	    // Give each filter a chance to veto the command
	    boolean accepted = f.accept(cmd);
	    if(!accepted) {
		// FIXME: Should we throw e.g. a VetoedCommandException?
		return null;
	    }
	}

	Sink s = (Sink)targetSinks.get(cmd.getName());
	if(s != null) {
	    s.consume(cmd);
	}

	return cmd.getReturnValue();

    }


    private final List inFilters;
    private final List outFilters;
    private final Map sourceSinks;
    private final Map targetSinks;


    // Array representation of the filter chains, cached to allow concurrent iteration.
    private Filter[] inFiltersArray;
    private Filter[] outFiltersArray;


    private void updateFiltersArray() {
	inFiltersArray = new Filter[inFilters.size()];
	Object[] objs = inFilters.toArray();

	// Copy the elements
	for(int i = 0; i < objs.length; i++) {
	    inFiltersArray[i] = (Filter)objs[i];
	}

	outFiltersArray = new Filter[outFilters.size()];
	objs = outFilters.toArray();

	// Copy the elements
	for(int i = 0; i < objs.length; i++) {
	    outFiltersArray[i] = (Filter)objs[i];
	}

    }

}
