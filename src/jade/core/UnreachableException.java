/*
  $Log$
  Revision 1.2  1999/03/24 12:19:48  rimassa
  Removed ^M characters.

  Revision 1.1  1999/03/17 13:15:31  rimassa
  An exception occurring when a remote agent cannot be reached by the platform.

*/

package jade.core;

class UnreachableException extends Exception {

  UnreachableException(String msg) {
    super(msg);
  }

}
