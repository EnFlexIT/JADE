/*
  $Log$
  Revision 1.5  1999/06/25 12:56:43  rimassa
  Changed code to reflect new jade.gui utility package.

  Revision 1.4  1999/06/16 00:21:21  rimassa
  Commented out a debugging printout.

  Revision 1.3  1999/06/09 13:01:56  rimassa
  Added support for dialog centering with respect to RMA main window.

  Revision 1.2  1999/06/04 11:33:55  rimassa
  Actually implemented the action body.

  Revision 1.1  1999/05/20 15:42:09  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.4  1999/02/04 14:47:26  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:13  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;

import jade.lang.acl.ACLMessage;
import jade.gui.*;

/** 
 * Send Custom message Action
 * @see jade.tools.rma.AMSAbstractAction
 */
public class CustomAction extends AMSAbstractAction
{
    private rma myRMA;
    private Frame mainWnd;
    public CustomAction(rma anRMA, Frame f)
    {
	super ("CustomActionIcon","Send Custom Message to Selected Agents");
	myRMA = anRMA;
	mainWnd = f;
    }

    public void actionPerformed(ActionEvent e) 
    {
	ACLMessage msg2 = new ACLMessage("not-understood");
	for (int i=0;i<listeners.size();i++) {
	  TreeData current = (TreeData)listeners.elementAt(i);
	  if (current.getLevel() == TreeData.AGENT) {
	    // System.err.println("AddDest "+ current.getName());
	    msg2.addDest(current.getName());
	  }
	}
	ACLMessage msg = jade.gui.AclGui.editMsgInDialog(msg2, mainWnd);
	if (msg != null)
	  myRMA.send(msg);

    }
}
