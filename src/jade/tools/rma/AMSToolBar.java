/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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


package jade.tools.rma;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
/**
 * The ToolBar 
 */
public final class AMSToolBar extends JToolBar implements ActionListener {
  /** 
   * The ComboBox to choice between Yellow and White pages
   */
  protected JComboBox ShowChoice = new JComboBox ();
  protected AMSTree tree;

  public AMSToolBar (AMSTree treeP, rma anRMA, Frame mainWnd) {
    super();
    tree = treeP;
    setBorderPainted(true);
		
    //    addAction(new OpenScriptFileAction());

    // addSeparator();
	
    addAction(new StartNewAgentAction(anRMA, mainWnd));
    addAction(new StartAction());
    addAction(new KillAction("Kill Selected Items", anRMA));
    addAction(new SuspendAction(anRMA));
    addAction(new ResumeAction(anRMA));
    addAction(new CustomAction(anRMA, mainWnd));

    // addAction(new PingAction());
		
    addSeparator();

    addAction(new SnifferAction(anRMA));
    addAction(new DummyAgentAction(anRMA));

    addSeparator();

    ShowChoice.setToolTipText("Show Agent as...");
    ShowChoice.addItem("White Pages");
    ShowChoice.addItem("Yellow Pages");
    ShowChoice.addActionListener(this);
    ShowChoice.setEnabled(false);
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
