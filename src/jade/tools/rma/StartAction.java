/*
  $Log$
  Revision 1.1  1999/05/20 15:42:11  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.5  1999/02/04 14:47:30  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.4  1998/11/05 23:42:37  rimassa
  Changed some code indentation.

  Revision 1.3  1998/10/10 19:37:24  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;
import java.util.*;

/**
 * StartAction starts all the selected agents in the tree
 * @see jade.gui.AMSAbstractAction
 */
public class StartAction extends AMSAbstractAction {
  public StartAction() {
    super ("StartActionIcon","Start Selected Agents");
  }

  public void actionPerformed(ActionEvent evt) {
    for (int i=0;(i<listeners.size() && listeners.elementAt(i) instanceof TreeData);i++)
      try {
	TreeData current = (TreeData)listeners.elementAt(i);
	//	StartDialog.setContainer(current.getAddressesAsString());
	//	StringTokenizer st = new StringTokenizer(current.getAddressesAsString(),":");

	//	StartDialog.setHost(st.nextToken());
	//	StartDialog.setPort(st.nextToken());

	int result = StartDialog.showStartDialog(listeners.elementAt(i).toString());

	if (result == StartDialog.OK_BUTTON) {
	  current.setState(TreeData.RUNNING);
	  String [] s = new String[1];
	  s[0] = StartDialog.getAgentName()+" : "+StartDialog.getClassName();
	  //	  current.setAddresses(s);
	  tree.repaint();
	}
      }
    catch (Exception e){
      e.printStackTrace();
    }
  }
}
