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


/**
   Base class for command filters, that allow to
   set up an open-ended chain of platform services to process commands
   coming from the upper JADE layers. Filters process commands when
   their <code>filter()</code> method is invoked; a filter can also
   modify the command object that is passed to its
   <code>filter()</code> method.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
   @author Giovanni Caire - TILAB

   @see jade.core.CommandProcessor
*/
public abstract class Filter {

		/**
		   The constant indicating the first position in the filter chain
		 */
		public static final int FIRST = 0;
		
		/**
		   The constant indicating the last position in the filter chain
		 */
		public static final int LAST = 100;
	
    /**
       A constant indicating a filter for incoming commands
    */
    public static final boolean INCOMING = false;

    /**
       A constant indicating a filter for outgoing commands
    */
    public static final boolean OUTGOING = true;

    private boolean blocking = false;
    private boolean skipping = false;
    
    // The next filter in the filter chain
    private Filter next;

    // The preferred position in the filter chain. Package scoped since
    // it can be directly accessed by the CommandProcessor
    int preferredPosition = LAST;
    
    /**
       Receive a command object for processing.

       @param cmd A <code>VerticalCommand</code> describing what operation has
       been requested from previous layers (that can be the actual
       prime source of the command or previous filters in the chain).
    */
    final void filter(VerticalCommand cmd) {
 			// FIXME: Should manage the blocking and skipping states
    	//System.out.println("Filter "+this+" processing command "+cmd.getName());
   		if (accept(cmd)) {
    		next.filter(cmd);
    		postProcess(cmd);
    	}
    }

    /**
       Receive a command object for processing.

       @param cmd A <code>VerticalCommand</code> describing what operation has
       been requested from previous layers (that can be the actual
       prime source of the command or previous filters in the chain).
       @return A boolean value, telling whether the filtered command has
       been accepted or not. A filter can veto a command by
       returning <code>false</code> from this method. A vetoed command
       is not propagated in the filter chain.
    */
    protected boolean accept(VerticalCommand cmd) {
    	return true;
    }

    /**
       Post-process a command object after it has been processed by the 
       successive filters in the filter chain.

       @param cmd A <code>VerticalCommand</code> describing what operation has
       been requested from previous layers (that can be the actual
       prime source of the command or previous filters in the chain).
    */
    protected void postProcess(VerticalCommand cmd) {
    }

    /**
       Retrieve the preferred position for this filter in the filter chain
     */
    public final int getPreferredPosition() {
    	return preferredPosition;
    }
    
    /**
       Sets the preferred position for this filter in the filter chain
     */
    public final void setPreferredPosition(int pos) {
    	preferredPosition = pos;
    	if (preferredPosition > LAST) {
    		preferredPosition = LAST;
    	}
    	else if (preferredPosition < FIRST) {
    		preferredPosition = FIRST;
    	}
    }
    
    /**
       Sets the blocking state of this filter. A blocked filter does
       not process commands, and also prevents subsequent filters to
       process them.

       @param newState The boolean value to set the blocking state to.
    */
    public void setBlocking(boolean newState) {
    	blocking = newState;
    }

    /**
       Inquires the blocking state of this filter. A blocked fliter
       does not process commands, and also prevents subsequent filters
       to process them.

       @return The current blocking state of this filter.
    */
    public boolean isBlocking() {
    	return blocking;
    }

    /**
       Sets the skipping state of this filter. A skipped filter does
       not process commands, but passes them directly to subsequent
       filters.

       @param blocked The boolean value to set the skipping state to.
    */
    public void setSkipping(boolean newState) {
    	skipping = newState;
    }

    /**
       Inquires the skipping state of this filter. A skipped filter
       does not process commands, but passes them directly to
       subsequent filters.

       @return The current skipping state of this filter.
    */
    public boolean isSkipping() {
    	return skipping;
    }
    
    ////////////////////////////////////////////////////
    // These methods are called by the CommandProcessor 
    // when installing the filter
    ////////////////////////////////////////////////////
    final void setNext(Filter f) {
    	next = f;
    }
    
    final Filter getNext() {
    	return next;
    }
}
