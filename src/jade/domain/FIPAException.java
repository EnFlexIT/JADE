/*
  $Log$
  Revision 1.3  1998/11/30 00:22:09  rimassa
  Added a fromText() static factory method to create an exception out of
  a 'refuse' or 'failure' ACL message.

  Revision 1.2  1998/10/04 18:01:26  rimassa
  Added a 'Log:' field to every source file.

*/

package jade.domain;

import java.io.Reader;
import java.io.Writer;
import java.io.IOException;


import jade.domain.AgentManagementOntology;

public class FIPAException extends Exception {

  public FIPAException(String msg) {
    super(msg);
  }

  public static FIPAException fromText(Reader r) {
    AgentManagementOntology o = AgentManagementOntology.instance();
      return o.getException(r);
  }

  public void toText(Writer w) {
    try {
      w.write(getMessage());
      w.flush();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

}
