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
 * This class represents the J2ME version of an &quot;Iterator&quot;
 * to be used in LEAP.
 * 
 * @author  Nicolas Lhuillier
 * @version 1.0, 29/09/00
 * 
 * @see java.util.Iterator
 */
public interface Iterator {

    /**
     * Returns <tt>true</tt> if the iteration has more elements.
     * 
     * @return <tt>true</tt> if the iterator has more elements.
     * @see java.util.Iterator
     */
    boolean hasNext();

    /**
     * Returns the next element in the interation.
     * 
     * @return the next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     * @see java.util.Iterator
     */
    Object next();

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).
     * 
     * @see java.util.Iterator
     */
    void remove();
}

