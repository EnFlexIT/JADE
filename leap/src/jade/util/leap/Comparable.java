/*****************************************************************
The LEAP libraries, when combined with certain JADE platform components, 
provide a run-time environment for enabling FIPA agents to execute on 
lightweight devices running Java. LEAP and JADE teams have jointly 
designed the API for ease of integration and hence to take advantage 
of these dual developments and extensions so that users only see 
one development platform and a
single homogeneous set of APIs. Enabling deployment to a wide range of
devices whilst still having access to the full development
environment and functionalities that JADE provides. 

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

package jade.util.leap;

/**
 * J2ME version of the &qote;Comparable&qote; interface, used to sort elements in SortedSet.
 * 
 * @see java.lang.Comparable (J2SE)
 */
public interface Comparable {

  /**
   * Compares this object with the specified object for order.
   * Returns a negative integer, zero, or a positive integer as this object
   * is less than, equal to, or greater than the specified object.
   * This relation must be anti-symmetrical and transitive.
   * 
   * It is strongly recommended, but not strictly required that
   * (x.compareTo(y)==0) == (x.equals(y)).
   * Generally speaking, any class that implements the Comparable interface
   * and violates this condition should clearly indicate this fact.
   * The recommended language is
   * "Note: this class has a natural ordering that is inconsistent with equals."
   * @param o the Object to be compared.
   * @return a negative integer, zero, or a positive integer as this object is less than,
   * equal to, or greater than the specified object.
   */
  int compareTo(Object o);
}






