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

import jade.util.leap.List;
import jade.util.leap.LinkedList;


/**

   Processes JADE kernel-level commands, managing a filter chain to
   support dynamically configurable platform services.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public class CommandProcessor {

    public CommandProcessor() {
	filters = new LinkedList();
	updateFiltersArray();
    }


    /**
       Add a new filter to the filter chain.

       @param f The new filter to add.
    */
    public synchronized void addFilter(Filter f) {

	// FIXME: How do we know the right position for this filter in
	// the filter chain?

	filters.add(f);
	updateFiltersArray();
    }

    /**
       Remove a filter from the filter chain.

       @param f The filter to remove.
    */
    public synchronized void removeFilter(Filter f) {
	filters.remove(f);
	updateFiltersArray();
    }

    /**
       Process a command object, carrying out the action it
       represents.  This method is not synchronized and it must be
       reentrant and as fast as possible, because it is going to be a
       bottleneck of the kernel call flow.

       @param cmd The <code>VerticalCommand</code> object to process.
    */
    public Object process(VerticalCommand cmd) {

	// FIXME: Should manage the blocking and skipping filter states...

	// Pass the command through every filter
	Filter[] arr = filtersArray;
	for(int i = 0; i < arr.length; i++) {
	    Filter f = arr[i];
	    f.accept(cmd);
	}

	return cmd.getReturnValue();

    }


    private final List filters;


    // Array representation of the filter chain, cached to allow concurrent iteration.
    private Filter[] filtersArray;


    private void updateFiltersArray() {
	filtersArray = new Filter[filters.size()];
	Object[] objs = filters.toArray();

	// Copy the elements
	for(int i = 0; i < objs.length; i++) {
	    filtersArray[i] = (Filter)objs[i];
	}
    }

}
