/*
  $Log$
  Revision 1.3  1998/10/04 18:01:11  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

import java.rmi.RemoteException;

class NotFoundException extends RemoteException {

  NotFoundException(String msg) {
    super(msg);
  }
}
