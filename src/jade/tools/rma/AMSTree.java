/*
  $Log$
  Revision 1.1  1999/05/20 15:42:08  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.8  1999/05/19 18:31:26  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.7  1999/02/04 14:47:26  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.6  1998/11/09 00:30:39  rimassa
  Removed older, commented out code and unused variables.

  Revision 1.5  1998/11/03 00:43:23  rimassa
  Added automatic GUI tree updating in response to AMS 'inform' messages to
  Remote Management Agent.

  Revision 1.4  1998/11/01 14:57:27  rimassa
  Changed code indentation to comply with JADE style.

  Revision 1.3  1998/10/10 19:37:10  rimassa
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

  public AMSTree(rma anRMA) {
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
    pane.setContinuousLayout(false);
    createSplit(table.createTable());		

    /* Enable tool tips for the tree, without this tool tips will not
       be picked up. */
    ToolTipManager.sharedInstance().registerComponent(tree);

    AddAgentPlatformAction.setTree(this);
    popup = new AMSPopupMenu (this, anRMA);
    tree.addTreeSelectionListener(this);
    tree.addMouseListener(new PopupMouser(popup));
    popup.addPopupMenuListener(this);
    TreeIconRenderer treeR = new TreeIconRenderer();
    tree.setCellRenderer(treeR);
    tree.setRowHeight(0);
  }

  private void createSplit (JScrollPane scroll) {
    pan = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,pane,scroll);
    add(pan);
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





