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

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/** 
 * The Tree listens TreeSelection and PopupMenu events, because we want 
 * have a context menu sensible to Nodes and we want to update the Table 
 * on the left
 */
public class AMSTree extends JPanel implements TreeSelectionListener, PopupMenuListener {

  private JTextArea selArea;
  private JTree tree;
  private AMSPopupMenu popup;
  private TablePanel table;
  private JScrollPane scroll;	
  private JSplitPane pan;
  private JSplitPane pane;
  private DefaultMutableTreeNode AMSFirstNode;  
  private static int i = 1;

  public AMSTree(rma anRMA, Frame mainWnd) {
    table = new TablePanel();
    TreeSelectionModel selModel;
    Font f;

    f = new Font("SanSerif",Font.PLAIN,14);
    setFont(f);
    setLayout(new BorderLayout(10,10));

    tree = new JTree();

    tree.setFont(f);
    tree.setModel(new AMSTreeModel());
    tree.setLargeModel(false);

    selModel = tree.getSelectionModel();
    selModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    selArea = new JTextArea(5,20);
    selArea.setEditable(true);

    pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,new JScrollPane(tree),new JScrollPane(selArea));
    pane.setContinuousLayout(true);
    createSplit(table.createTable());		

    /* Enable tool tips for the tree, without this tool tips will not
       be picked up. */
    ToolTipManager.sharedInstance().registerComponent(tree);

    AddAgentPlatformAction.setTree(this);
    popup = new AMSPopupMenu (this, anRMA, mainWnd);
    tree.addTreeSelectionListener(this);
    tree.addMouseListener(new PopupMouser(popup));
    popup.addPopupMenuListener(this);
    TreeIconRenderer treeR = new TreeIconRenderer();
    tree.setCellRenderer(treeR);
    tree.setRowHeight(0);
  }

  private void createSplit (JScrollPane scroll) {
    pan = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pane,scroll);
    pan.setContinuousLayout(true);
    add(pan);
  }

  public void adjustDividerLocation() {
    int rootSize = pane.getDividerLocation(); // This is the height of a single tree folder
    pane.setDividerLocation(7*rootSize); // The initial agent tree has 6 elements; one more empty space
  }

  /** 
   * This method is messaged when a SelectionEvent occurs
   * Here we update the AMSTable, the textArea and the vector
   * of listeners in AMSAbstractAction
   */
  public void valueChanged(TreeSelectionEvent e)
    {
      TreePath paths[] = tree.getSelectionPaths();
      int j,numPaths;
      String formattedPath = "";
      AMSAbstractAction.removeAllListeners();
      if (paths != null)
	numPaths = paths.length;
      else
	numPaths = 0;
      TreeData current;

      for(j=0;j<numPaths;j++) {
	if (paths[j].getLastPathComponent() instanceof TreeData) {

	  current = (TreeData) (paths[j].getLastPathComponent());

	  AMSAbstractAction.AddListener(current);
	  formattedPath += getPathAsString(paths[j]);
	}
	table.setData(AMSAbstractAction.getAllListeners());
      }
      selArea.setText(formattedPath);
    }

  public Dimension getPreferredSize() {
    return new Dimension(200, 200);
  }

  /** 
   * Mouse Listener Method 
   */
  public void actionPerformed (ActionEvent e){}

  /**
   * Popup Listener Method: this method is called to enable and
   * disable some actions depending from the context on which the
   * context menu is showing
   */
  public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    TreePath path = tree.getSelectionPath();

    if (path != null && path.getLastPathComponent() instanceof TreeData) {

      TreeData current = (TreeData) path.getLastPathComponent();
      popup.setMenu(current.getLevel());
    }
    // this way popup menu has all actions disabled
    else 
      popup.setMenu( (TreeData.AGENT_PLATFORM-2) );
  }

  private String getPathAsString (TreePath Tpath)
    {
      Object [] path = Tpath.getPath();
      String formattedPath = " ";

      int max = path.length;
	
      for(i=0;i<max;i++) {
	formattedPath+=".";
	if ( path[i] instanceof TreeData) {
	  TreeData current = (TreeData) path[i];
	  formattedPath+=current.getName();
	}
	else formattedPath += path[i].toString();
      }
      formattedPath+="\n";
      return formattedPath;
    }

  /**
   * @return the current selected node
   */
  protected DefaultMutableTreeNode getSelectedNode() {
    TreePath   selPath = tree.getSelectionPath();

    if(selPath != null)
      return (DefaultMutableTreeNode)selPath.getLastPathComponent();
    return null;
  }

  /**
   * @param name The name of new Node
   * @param level The level (PLATFORM, CONTAINER, AGENT) of new node
   * @return a new node with specified level and name
   */
  public TreeData createNewNode(String name,int level) {
    if (level >TreeData.SUPER_NODE && level<=TreeData.AGENT)
      return new TreeData(name,level);
    else return new TreeData(name,TreeData.AGENT);
  }


  /**
   * @return current TreeModel
   */
  public AMSTreeModel getModel()
    {
      if (tree.getModel() instanceof AMSTreeModel)
	return (AMSTreeModel)tree.getModel();
      else {
	System.out.println(tree.getModel());
	return null;
      }	
    }
    
  /**
   * PopupMenuListener method 
   */
  public void popupMenuWillBecomeInvisible(PopupMenuEvent e){}

  /**
   * PopupMenuListener method 
   */
  public void popupMenuCanceled(PopupMenuEvent e){}

}





