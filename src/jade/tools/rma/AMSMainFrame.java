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

import java.util.Enumeration;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import jade.lang.acl.ACLMessage;
import jade.gui.*;

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
 * Javadoc documentation for the file
 * @author  Gianstefano Monni
 * @version $Date$ $Revision$
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

  public void addContainer(final String name) {

    // Add a container node to the tree model
    Runnable addIt = new Runnable() {
      public void run() {
	MutableTreeNode node = tree.createNewNode(name, TreeData.CONTAINER);
	AMSTreeModel model = tree.getModel();
	MutableTreeNode root = (MutableTreeNode)model.getRoot();
	model.insertNodeInto(node, root, root.getChildCount());
      }
    };
    SwingUtilities.invokeLater(addIt);
  }

  public void removeContainer(final String name) {

    // Remove a container from the tree model
    Runnable removeIt = new Runnable() {
      public void run() {

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
    };
    SwingUtilities.invokeLater(removeIt);
  }

  public void addAgent(final String containerName, final String agentName, final String agentAddress, final String agentType) {

    // Add an agent to the specified container
    Runnable addIt = new Runnable() {
      public void run() {
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
    };
    SwingUtilities.invokeLater(addIt);
  }

  public void removeAgent(final String containerName, final String agentName) {

    // Remove an agent from the specified container
    Runnable removeIt = new Runnable() {
      public void run() {
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
    };
    SwingUtilities.invokeLater(removeIt);
  }

  public void showErrorDialog(String text, ACLMessage msg) {
    String messages[] = new String[3];
    messages[0] = text;
    messages[1] = "";
    messages[2] = "Do you want to view the ACL message ?";
    int answer = JOptionPane.showConfirmDialog(this, messages, "RMA Error !!!", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
    switch(answer) {
    case JOptionPane.YES_OPTION:
      jade.gui.AclGui.showMsgInDialog(msg, this);
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
