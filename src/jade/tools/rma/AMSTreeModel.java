/*
  $Log$
  Revision 1.2  1999/07/11 21:34:43  rimassa
  Removed a temporary workaround for a deadlock problem. Now a correct
  use of the SwingUtilities class ensures that all GUI update is done by
  the AWT Event Dispatcher thread.

  Revision 1.1  1999/05/20 15:42:08  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.7  1999/05/19 18:29:43  rimassa
  Overridden fireTreeNodesInserted() method to perform a deferred event
  handling. This is a workaround for a nasty deadlock bug occurring at
  times during RMA GUI startup.

  Revision 1.6  1999/02/04 14:47:26  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.5  1998/11/03 00:43:24  rimassa
  Added automatic GUI tree updating in response to AMS 'inform' messages to
  Remote Management Agent.

  Revision 1.4  1998/11/01 14:57:28  rimassa
  Changed code indentation to comply with JADE style.

  Revision 1.3  1998/10/10 19:37:11  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 * The model of the AMSTree
 */
public class AMSTreeModel extends DefaultTreeModel {

  /**
   * the Root of the Tree
   */
  protected static TreeData root = new TreeData ("JADE ",TreeData.SUPER_NODE);

  public AMSTreeModel () {
    super(root);
  }

  /* TreeModel methods */
  public Object getRoot() {
    return root;
  }

  /**
   * This method must be rewritten if we want
   * to make possible editing the tree
   */
  public void valueForPathChanged(TreePath path, Object newValue) {}

	
  /**
   * This method must be rewritten if we want
   * to make possible editing the tree
   */
  protected void fireValueChanged(TreePath path,int[] ind,Object[] children)  {}

}

