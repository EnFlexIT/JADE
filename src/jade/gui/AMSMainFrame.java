/*
  $Log$
  Revision 1.5  1998/11/03 00:43:22  rimassa
  Added automatic GUI tree updating in response to AMS 'inform' messages to
  Remote Management Agent.

  Revision 1.4  1998/11/01 14:57:26  rimassa
  Changed code indentation to comply with JADE style.

  Revision 1.3  1998/10/10 19:37:03  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import java.awt.*;
import java.awt.event.*;

import java.util.Enumeration;

import com.sun.java.swing.*;
import com.sun.java.swing.border.*;
import com.sun.java.swing.tree.*;

/**
 * A class representing the main window of the Management System
 * command line parameters are:
 *				<p>-wi	 enables Windows Look & Feel (default)</P>
 *				<p>-mo			 Motif Look & Feel</P>
 *				<p>-me			 Metal Look & Feel</P>
 *				<p>-mu			 Multi Look & Feel</P>
 * <pre>
 *    java AMSMainFrame -mo
 * </pre>
 *
 * @author  Gianstefano Monni
 * @version %I%, %G%
 * @see     com.sun.java.swing.JFrame
 */
public class AMSMainFrame extends JFrame {	

  private AMSTree tree;

  public AMSMainFrame () {
    super("JADE Remote Agent Management GUI");
    setJMenuBar(new AMSMenu());

    tree = new AMSTree();
    setForeground(Color.black);
    setBackground(Color.lightGray);
    addWindowListener(new WindowCloser());
    getContentPane().add(new AMSToolBar(tree),"North");
		
    getContentPane().add(tree,"Center");
  }
  /**
     show the AMSMainfFrame packing and setting its size correctly
  */
  public void ShowCorrect() {
    pack();
    setSize(400,400);
    setVisible(true);
  }

  public void addContainer(String name) {

    // Add a container node to the tree model
    MutableTreeNode node = tree.createNewNode(name, TreeData.CONTAINER);
    AMSTreeModel model = tree.getModel();
    MutableTreeNode root = (MutableTreeNode)model.getRoot();
    model.insertNodeInto(node, root, root.getChildCount());

  }

  public void removeContainer(String name) {

    // Remove a container from the tree model
    AMSTreeModel model = tree.getModel();
    MutableTreeNode root = (MutableTreeNode)model.getRoot();
    Enumeration containers = root.children();
    while(containers.hasMoreElements()) {
      TreeData node = (TreeData)containers.nextElement();
      String nodeName = node.getName();
      if(nodeName.equalsIgnoreCase(name)) {
	model.removeNodeFromParent(node);
	return;
      }
    }

  }

  public void addAgent(String containerName, String agentName, String agentAddress, String agentType) {

    // Add an agent to the specified container

    AMSTreeModel model = tree.getModel();
    MutableTreeNode root = (MutableTreeNode)model.getRoot();

    // Create a suitable new node
    TreeData node = tree.createNewNode(agentName, TreeData.AGENT);
    node.addAddress(agentAddress);
    node.setType(agentType);

    // Search for the agent container 'containerName'
    Enumeration containers = root.children();
    while(containers.hasMoreElements()) {
      TreeData container = (TreeData)containers.nextElement();
      String contName = container.getName();
      if(contName.equalsIgnoreCase(containerName)) {
	// Add this new agent to this container and return
	model.insertNodeInto(node, container, container.getChildCount());
	//	tree.expandPath(new TreePath(container.getPath()));
	//	tree.scrollPathToVisible(new TreePath(node.getPath()));
	return;
      }
    }
  }

  public void removeAgent(String containerName, String agentName) {

    // Remove an agent from the specified container

    AMSTreeModel model = tree.getModel();
    MutableTreeNode root = (MutableTreeNode)model.getRoot();

    // Search for the agent container 'containerName'
    Enumeration containers = root.children();
    while(containers.hasMoreElements()) {
      TreeData container = (TreeData)containers.nextElement();
      String contName = container.getName();
      if(contName.equalsIgnoreCase(containerName)) {

	// Search for the agent 'agentName' in this agent container
	Enumeration agents = container.children();
	while(agents.hasMoreElements()) {
	  TreeData agent = (TreeData)agents.nextElement();
	  String agName = agent.getName();
	  if(agName.equalsIgnoreCase(agentName)) {
	    model.removeNodeFromParent(agent);
	    return;
	  }
	}
      }
    }
  }

  private void setUI(String ui) {
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf."+ui);
      SwingUtilities.updateComponentTreeUI(this);
      pack();
    }
    catch(Exception e) {
	System.out.println(e);
	e.printStackTrace(System.out);
    }
  }
  /**
     enables Motif L&F
  */
  public void setUI2Motif() {
    setUI("motif.MotifLookAndFeel");   
  }
    
  /**
     enables Windows L&F
  */
  public void setUI2Windows() {
    setUI("windows.WindowsLookAndFeel");   
  }

  /**
     enables Multi L&F
  */
  public void setUI2Multi() {
    setUI("multi.MultiLookAndFeel");   
  }

  /**
     enables Metal L&F
  */
  public void setUI2Metal() {
    setUI("metal.MetalLookAndFeel");   
  }

  public static void main (String[] argv) {
    AMSMainFrame jf = new AMSMainFrame();

    if (argv.length >= 1) {
      if (argv[0].equals("-me"))
	jf.setUI2Metal();
      else if (argv[0].equals("-mo"))
	jf.setUI2Motif();
      else if (argv[0].equals("-wi"))
	jf.setUI2Windows();
      else if (argv[0].equals("-mu"))
	jf.setUI2Multi();
      else if (argv[0].equals("-h"))
	System.out.println("Usage : java AMSMainFrame -[wi][mo][mu]");
    }
    jf.ShowCorrect();
  }
}
