/*
  $Log$
  Revision 1.7  1999/05/19 18:31:25  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.6  1999/02/04 14:47:25  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.5  1998/11/15 23:13:59  rimassa
  Minor changes to comply with JADE code indentation style and
  KillAction() new constructor.

  Revision 1.4  1998/10/26 00:10:31  rimassa
  Fixed a wrong indentation.

  Revision 1.3  1998/10/10 19:37:09  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import jade.domain.rma;

/**
 * The ToolBar 
 */
public final class AMSToolBar extends JToolBar implements ActionListener {
  /** 
   * The ComboBox to choice between Yellow and White pages
   */
  protected JComboBox ShowChoice = new JComboBox ();
  protected AMSTree tree;

  public AMSToolBar (AMSTree treeP, rma anRMA) {
    super();
    tree = treeP;
    setBorderPainted(true);
		
    addAction(new OpenScriptFileAction());

    addSeparator();
	
    addAction(new StartNewAgentAction(anRMA));
    addAction(new StartAction());
    addAction(new KillAction("Kill Selected Items", anRMA));
    addAction(new SuspendAction(anRMA));
    addAction(new ResumeAction(anRMA));
    addAction(new PingAction());
		
    addSeparator();

    addAction(new SnifferAction());
    addAction(new CustomAction());
		
    addSeparator();

    ShowChoice.setToolTipText("Show Agent as...");
    ShowChoice.addItem("White Pages");
    ShowChoice.addItem("Yellow Pages");
    ShowChoice.addActionListener(this);
    add(ShowChoice);
  }

  private void addAction(AMSAbstractAction act) {
    JButton b = add(act);
    b.setToolTipText(act.getActionName());
    b.setText("");
    b.setRequestFocusEnabled(false);
    b.setMargin(new Insets(1,1,1,1));
  }
	
  /**
   * This method is messaged when the user modifies the choice 
   * in the ComboBox (White pages|Yellow Pages)
   * When others actions are fired the method actionPerformed of
   * the corresponding action is messaged.
   */
  public void actionPerformed (ActionEvent evt) {
    TreeIconRenderer.setShowType(ShowChoice.getSelectedIndex());
    tree.repaint();
  }

}
