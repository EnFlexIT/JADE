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
   This interface represents a configuration profile for an agent
   container. Various configuration options can be stored within a
   profile object and then used when creating a new agent container.

   @author Giovanni Rimassa - Universita` di Parma
 */
public interface Profile {


  /**
     Access a named parameter of this profile object.
     @param name The name of the desired parameter.
     @return The <code>String</code> value, associated to the given
     name in the current profile object.
   */
  String getParameter(String name);

}
