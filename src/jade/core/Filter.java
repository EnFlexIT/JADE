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

   This interface is the base type for command filters, that allow to
   set up an open-ended chain of platform services to process commands
   coming from the upper JADE layers. Filter process commands when
   their <code>accept()</code> method is invoked; a filter can also
   modify the command object that is passed to its
   <code>accept()</code> method.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

   @see jade.core.CommandProcessor
*/
public interface Filter {

    /**
       A constant indicating a filter for incoming commands
    */
    static final boolean INCOMING = false;

    /**
       A constant indicating a filter for outgoing commands
    */
    static final boolean OUTGOING = true;

    /**
       Sets the blocking state of this filter. A blocked filter does
       not process commands, and also prevents subsequent filters to
       process them.

       @param newState The boolean value to set the blocking state to.
    */
    void setBlocking(boolean newState);

    /**
       Inquires the blocking state of this filter. A blocked fliter
       does not process commands, and also prevents subsequent filters
       to process them.

       @return The current blocking state of this filter.
    */
    boolean isBlocking();

    /**
       Sets the skipping state of this filter. A skipped filter does
       not process commands, but passes them directly to subsequent
       filters.

       @param blocked The boolean value to set the skipping state to.
    */
    void setSkipping(boolean newState);

    /**
       Inquires the skipping state of this filter. A skipped filter
       does not process commands, but passes them directly to
       subsequent filters.

       @return The current skipping state of this filter.
    */
    boolean isSkipping();

    /**
       Receive a command object for processing.

       @param cmd A <code>Command</code> describing what operation has
       been requested from previous layers (that can be the actual
       prime source of the command or previous filters in the chain.
    */
    void accept(VerticalCommand cmd);


}
