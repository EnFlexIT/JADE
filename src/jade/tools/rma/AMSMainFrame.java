/*
  $Log$
  Revision 1.4  1999/06/09 12:59:46  rimassa
  Improved screen presentation adding support for main window and dialog
  centering.

  Revision 1.3  1999/06/06 21:54:20  rimassa
  Removed useless main() function.
  Added an error dialog to be shown when some 'failure' or 'refuse'
  message is received from the AMS.

  Revision 1.2  1999/06/06 17:49:29  rimassa
  Made this class package scoped instead of public.
  Added a call to AMSTree to adjust initial separator position for
  three-pane window.

  Revision 1.1  1999/05/20 15:42:07  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.12  1999/05/19 18:31:22  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.11  1999/04/13 16:01:10  rimassa
  Added a method to perform asynchronously GUI disposal.

  Revision 1.10  1999/03/07 22:54:22  rimassa
  Changed class name prefix string in setUI() method to enable multiple
  Look & Feel.

  Revision 1.9  1999/03/03 16:00:51  rimassa
  Added a getModel() method to access underlying TreeModel for GUI
  updates.

  Revision 1.8  1999/02/04 14:47:24  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.7  1998/11/09 00:29:28  rimassa
  Changed preferred window size from (400, 400) to (600, 400).
  Removed older, commented out code.

  Revision 1.6  1998/11/05 23:39:47  rimassa
  Some minor changes. Added a static method getRMA(), returning a static
  reference to RMA agent. Probably a better solution would be useful.

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

package jade.tools.rma;

import java.awt.*;
import java.awt.event.*;

import java.util.Enumeration;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import jade.lang.acl.ACLMessage;

/**
 * A class representing the main window of the Management System
 * command line parameters are:
 *				<p>-wi	 enables Windows Look & Feel (default)</P>
 *				<p>-mo			 Motif Look & Feel</P>
 *				<p>-me			 Metal Look & Feel</P>
 *				<p>-mu			 Multi Look & Feel</P>
 *                              <p>-ba                   Basic Look & Feel</P>
 * <pre>
 *    java AMSMainFrame -mo
 * </pre>
 *
 * @author  Gianstefano Monni
 * @version %I%, %G%
 * @see     javax.swing.JFrame
 */
class AMSMainFrame extends JFrame {
  // FIXME: Static Vector 'listeners' prevents two or more rma within the same JVM 
  private AMSTree tree;

  public AMSMainFrame (rma anRMA) {
    super("JADE Remote Agent Management GUI");
 
    setJMenuBar(new AMSMenu(anRMA, this));

    tree = new AMSTree(anRMA, this);
    setForeground(Color.black);
    setBackground(Color.lightGray);
    addWindowListener(new WindowCloser(anRMA));
    getContentPane().add(new AMSToolBar(tree, anRMA, this),"North");

    getContentPane().add(tree,"Center");

  }

  /**
     show the AMSMainFrame packing and setting its size correctly
  */
  public void ShowCorrect() {
    pack();
    setSize(600,400);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int centerX = (int)screenSize.getWidth() / 2;
    int centerY = (int)screenSize.getHeight() / 2;
    setLocation(centerX - 300, centerY - 200);
    tree.adjustDividerLocation();
    setVisible(true);
    toFront();
  }

  // Perform asynchronous disposal to avoid nasty InterruptedException
  // printout.
  public void disposeAsync() {

    class disposeIt implements Runnable {
      private Window toDispose;

      public disposeIt(Window w) {
	toDispose = w;
      }

      public void run() {
	toDispose.dispose();
      }

    }

    // Make AWT Event Dispatcher thread dispose RMA window for us.
    EventQueue.invokeLater(new disposeIt(this));

  }

  public AMSTreeModel getModel() {
    return tree.getModel();
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

  public void showErrorDialog(String text, ACLMessage msg) {
    String messages[] = new String[3];
    messages[0] = text;
    messages[1] = "";
    messages[2] = "Do you want to view the ACL message ?";
    int answer = JOptionPane.showConfirmDialog(this, messages, "RMA Error !!!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
    switch(answer) {
    case JOptionPane.YES_OPTION:
      jade.tools.DummyAgent.AclGui.showMsgInDialog(msg, this);
      break;
    default:
      break;
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

}
