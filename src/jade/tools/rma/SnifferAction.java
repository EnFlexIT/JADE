/*
  $Log$
  Revision 1.2  1999/11/08 16:33:59  rimassaJade
  Actually implemented the action to start a message sniffer.

  Revision 1.1  1999/05/20 15:42:11  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.4  1999/02/04 14:47:30  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:23  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

/**
 * SnifferAction spawns an external application passing as parameters a 
 * String containing ALL agents selected in the Tree
 * @see jade.gui.AMSAbstractAction
 */
public class SnifferAction extends AMSAbstractAction {

  private static int progressiveNumber = 0;
  private rma myRMA;

  public SnifferAction(rma anRMA) {
    super ("SnifferActionIcon","Start Sniffer");
    myRMA = anRMA;		
  }

  public void actionPerformed(ActionEvent e) {
    myRMA.newAgent("sniffer"+progressiveNumber, "jade.tools.sniffer.Sniffer", new String());
    progressiveNumber++;
  }

}
	
