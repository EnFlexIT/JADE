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
   The <code>Sink</code> interface has to be implemented by all
   the components that process JADE kernel-level commands. Some of
   these components are pass-trough filters, whereas others may be the
   final destination for a command.

   @author Giovanni Rimassa - FRAMeTech s.r.l.

   @see jade.core.Filter
*/
public interface Sink {

    /**
       Receive a command object for processing.

       @param cmd A <code>Command</code> describing what operation has
       been requested from previous layers (that can be the actual
       prime source of the command or previous filters in the chain.
    */
    void accept(VerticalCommand cmd);

}
