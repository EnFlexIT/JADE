/*
  $Log$
  Revision 1.3  1999/07/13 19:24:46  rimassa
  Made this exception class public.

  Revision 1.2  1999/03/24 12:19:48  rimassa
  Removed ^M characters.

  Revision 1.1  1999/03/17 13:15:31  rimassa
  An exception occurring when a remote agent cannot be reached by the platform.

*/

package jade.core;

public class UnreachableException extends Exception {

  UnreachableException(String msg) {
    super(msg);
  }

}
