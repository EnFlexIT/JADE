/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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

package jade.util;

//__CLDC_UNSUPPORTED__BEGIN
import java.io.PrintStream;
import java.io.PrintWriter;
//__CLDC_UNSUPPORTED__END

/**

  This class acts as a base class for all the exceptions that wrap
  another (nested) exception. The typical usage for descendents of
  this class is to throw them within the <code>catch</code> block for
  their nested exception.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

 */
public abstract class WrapperException extends Exception {

  private String message;
  private Throwable nested;

  protected WrapperException(String m, Throwable t) {
    message = m;
    nested = t;
  }

  protected WrapperException(String m) {
    message = m;
  }

  public String getMessage() {
    if((nested != null)) {
      return message + " [nested message is: " + nested.getMessage() + "]";
    }
    return message;
  }

//__CLDC_UNSUPPORTED__BEGIN
  public void printStackTrace() {
    printStackTrace(System.err);
  }

  public void printStackTrace(PrintStream s) {
    PrintWriter pw = new PrintWriter(s);
    printStackTrace(pw);
  }

  public void printStackTrace(PrintWriter s) {
    super.printStackTrace(s);
    if(nested != null) {
      s.println("Nested Exception is:");
      nested.printStackTrace(s);
    }
  }
//__CLDC_UNSUPPORTED__END

  /**
     Reads the exception wrapped by this object.
     @return the <code>Throwable</code> object that is the exception thrown by
     the concrete MTP subsystem.
  */
  public Throwable getNested() {
    return nested;
  }

}
