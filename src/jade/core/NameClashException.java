/*
  $Log$
  Revision 1.2  1998/10/11 19:40:49  rimassa
  Changed NameClashException to simply extend Exception and *not*
  RemoteException (this was really a bug).

  Revision 1.1  1998/10/11 15:13:50  rimassa
  New exception to detect duplicates in agent names.

*/

package jade.core;

class NameClashException extends Exception {

  NameClashException(String msg) {
    super(msg);
  }
}
