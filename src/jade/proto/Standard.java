/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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


package jade.proto;

import java.util.Hashtable;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
/**************************************************************

  Name: Standard

  Responsibility and Collaborations:

  + Gathers all FIPA standard protocols in a single class, providing a
    single acces point to them.
    (Protocol)

****************************************************************/
class Standard {

  // Private default constructor, since this is an utility class with
  // only static members and must not be instantiated.
  private Standard() {
  }

  private static Hashtable stdProtocols = null;

  // Used as a lock.
  private static Object mutex = new Object();

  // Fills the static Hashtable with FIPA standard protocols.
  private static void initProtocols() {

    stdProtocols = new Hashtable(10);

    // Build fipa-request protocol and put it in stdProtocols.

    // Build fipa-query protocol and put it in stdProtocols.

    // Build fipa-request-when protocol and put it in stdProtocols.

    // Build fipa-contract-net protocol and put it in stdProtocols.

    // Build fipa-iterated-contract-net protocol and put it in stdProtocols.

    // Build fipa-auction-english protocol and put it in stdProtocols.

    // Build fipa-auction-dutch protocol and put it in stdProtocols.

  }

  // Gets a protocol by name; uses Double Checked Locking pattern to
  // protect Singleton initialization in a multithreaded environment.
  public static Protocol getProtocol(String name) {
    if(stdProtocols == null)
      synchronized(mutex) {
	if(stdProtocols == null)
	  initProtocols();
      }
    return (Protocol)stdProtocols.get(name);
  }

}
