/*
  $Log$
  Revision 1.1  1999/02/03 10:18:32  rimassa
  New exception to model errors in converting from/to different CORBA object
  reference representations.

*/

package jade.core;

public class IIOPFormatException extends Exception {

  public IIOPFormatException(String msg) {
    super(msg);
  }
  
}
