/*
  $Log$
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

/** 
 * Send Custom message Action
 * @see jade.tools.rma.AMSAbstractAction
 */
public class CustomAction extends AMSAbstractAction
{
    private rma myRMA;
    public CustomAction(rma anRMA)
    {
	super ("CustomActionIcon","Send Custom Message to Selected Agents");
	myRMA = anRMA;
    }

    public void actionPerformed(ActionEvent e) 
    {
	ACLMessage msg2 = new ACLMessage("not-understood");
	for (int i=0;i<listeners.size();i++) {
	  TreeData current = (TreeData)listeners.elementAt(i);
	  if (current.getLevel() == TreeData.AGENT) {
	    System.err.println("AddDest "+ current.getName());
	    msg2.addDest(current.getName());
	  }
	}
	ACLMessage msg = jade.tools.DummyAgent.AclGui.editMsgInDialog(msg2, null);
	if (msg != null)
	  myRMA.send(msg);

    }
}
