/*
  $Log$
  Revision 1.2  1998/10/04 18:02:19  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.proto;

import java.util.Hashtable;

/**************************************************************

  Name: Standard

  Responsibility and Collaborations:

  + Gathers all FIPA standard protocols in a single class, providing a
    single acces point to them.
    (Protocol)

****************************************************************/
public class Standard {

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
