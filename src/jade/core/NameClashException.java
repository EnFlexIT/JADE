/*
  $Log$
  Revision 1.1  1998/10/11 15:13:50  rimassa
  New exception to detect duplicates in agent names.

*/

package jade.core;

import java.rmi.RemoteException;

class NameClashException extends RemoteException {

  NameClashException(String msg) {
    super(msg);
  }
}
