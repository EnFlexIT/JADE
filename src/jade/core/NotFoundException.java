/*
  $Log$
  Revision 1.4  1998/10/11 19:39:17  rimassa
  Changed NotFoundException to simply extend Exception and *not*
  RemoteException (this was really a bug).

  Revision 1.3  1998/10/04 18:01:11  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.core;

class NotFoundException extends Exception {

  NotFoundException(String msg) {
    super(msg);
  }
}
