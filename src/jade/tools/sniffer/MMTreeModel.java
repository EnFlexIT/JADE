package jade.tools.sniffer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * The model of the MMTree
 */
public class MMTreeModel extends DefaultTreeModel{

	/**
	 * the Root of the Tree
	 */
	protected static TreeData root = new TreeData ("Sniffer GUI ",TreeData.SUPER_NODE);

	public MMTreeModel (){
		super(root);
	}

  /* TreeModel methods */
	public Object getRoot(){
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