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

package jade.onto;

/**
  Abstract interface to create user defined ontological entities.
  Implementations of this interface must be able to create an object and to
  provide its class (or a superclass) on demand.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public interface RoleEntityFactory {

  /**
    Creates an object, starting from a given frame. This method can just create
    the object ignoring its argument, or it can use the frame to select the
    concrete class to instantiate.
    @param f A frame containing initialization data for the object.
    @return A Java object, instance of the proper class (either the class
    returned by <code>getClassForRole()</code>, or one of its subclasses).
  */
  Object create(Frame f);

  /**
    Provides the Java class associated with this ontological role. This class is
    usually the class used by the <code>create()</code> method to instantiate
    objects. A useful technique is returning an interface or an abstract class,
    while using concrete subclasses to create objects.
  */
  Class getClassForRole();

}
