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

package jade.wrapper;

import jade.core.AgentContainerImpl;

/**
   This class is a Proxy class, allowing access to a JADE main
   container. Invoking methods on instances of this class, it is
   possible to request services from <it>in-process</it> main
   containers.
   This class must not be instantiated by applications. Instead, use
   the <code>createMainContainer()</code> method in class
   <code>Runtime</code>.
   @see jade.core.Runtime#createMainContainer(Profile p)

   @author Giovanni Rimassa - Universita` di Parma

 */
public class MainContainer extends AgentContainer {

  /**
     Public constructor. This constructor requires a concrete
     implementation of a JADE main container, which cannot be
     instantiated bt applications, so it cannot be meaningfully called
     from application code. The proper way to create an agent
     container from an application is to call the
     <code>Runtime.createMainContainer()</code> method.
     @see jade.core.Runtime#createMainContainer(Profile p)
     @param impl A concrete implementation of a JADE main container.
   */
  public MainContainer(AgentContainerImpl impl) {
    super(impl);
  }

}
