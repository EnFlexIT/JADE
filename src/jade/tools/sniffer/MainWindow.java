/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
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

package jade.tools.sniffer;

import javax.swing.tree.MutableTreeNode;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Image;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import java.net.InetAddress;

import jade.gui.AgentTree;
import jade.core.AID;

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date$ $Revision$
 */

 /**
 * This class performs the <em>Sniffer</em> main-windows GUI setup. Also provides method for
 * asynchronous disposal at takedown.
 *
 * @see	javax.swing.JFrame
 */


public class MainWindow extends JFrame {

  protected MainPanel mainPanel;
  protected ActionProcessor actPro; // Making this public allows us to get directly to the sniff agent action.
  private PopupMenuAgent popA;
  private Sniffer mySniffer;
  private String snifferLogo = "images/sniffer.gif";
  
  public MainWindow(Sniffer mySniffer) {
   super(mySniffer.getName() + " - Sniffer Agent");
     this.mySniffer=mySniffer;
     mainPanel = new MainPanel(mySniffer, this);
     actPro=new ActionProcessor(mySniffer,mainPanel);
     setJMenuBar(new MainMenu(this,actPro));
     popA=new PopupMenuAgent(actPro);
     setForeground(Color.black);
     setBackground(Color.lightGray);
     Image image = getToolkit().getImage(getClass().getResource(snifferLogo));
     setIconImage(image);

     addWindowListener(new ProgramCloser());
     mainPanel.treeAgent.register("FIPAAGENT",popA,"images/runtree.gif");
     mainPanel.treeAgent.register("FIPACONTAINER",null,"images/foldergreen.gif");
     getContentPane().add(new ToolBar(actPro),"North");
     getContentPane().add(mainPanel,"Center");
 }


 public void ShowCorrect() {
  pack();
  setSize(new Dimension(700,500));
  mainPanel.adjustDividerLocation();
  this.setVisible(true);
  toFront();
 }


 /**
 * Tells the Agent Tree to add a container.
 *
 * @param	cont name of the container to be added
 */

 public void addContainer(final String name, final InetAddress addr) {
  Runnable addIt = new Runnable() {
   public void run() {
    MutableTreeNode node = mainPanel.treeAgent.createNewNode(name,0);
    mainPanel.treeAgent.addContainerNode((AgentTree.ContainerNode)node,"FIPACONTAINER",addr);
   }
  };
  SwingUtilities.invokeLater(addIt);
 }

 /**
 * Tells the Agent Tree to remove a specified container.
 *
 * @param	cont name of the container to be removed
 */

 public void removeContainer(final String name) {
  Runnable removeIt = new Runnable() {
   public void run() {
    mainPanel.treeAgent.removeContainerNode(name);
   }
  };
  SwingUtilities.invokeLater(removeIt);
 }

 /**
 * Tells the Agent Tree to add an agent.
 *
 * @param	cont name of the container to contain the new agent
 * @param name name of the agent to be created
 * @param addr address of the agent to be created
 * @param comm comment (usually type of the agent)
 */

 public void addAgent(final String containerName, final AID agentID) {
  Runnable addIt = new Runnable() {
   public void run() {
     String agentName = agentID.getName();
     AgentTree.Node node = mainPanel.treeAgent.createNewNode(agentName, 1);
     mainPanel.treeAgent.addAgentNode((AgentTree.AgentNode)node, containerName, agentName, "agentAddress", "FIPAAGENT");
   }
  };
  SwingUtilities.invokeLater(addIt);
 }

 /**
 * Tells the Agent Tree to remove a specified agent.
 *
 * @param	cont name of the container containing the agent
 * @param name name of the agent to be removed
 */

 public void removeAgent(final String containerName, final AID agentID) {
  Runnable removeIt = new Runnable() {
   public void run() {
     String agentName = agentID.getName();
     mainPanel.treeAgent.removeAgentNode(containerName, agentName);
     mainPanel.panelcan.canvAgent.removeAgent(agentName);
     mainPanel.panelcan.canvAgent.repaintNoSniffedAgent(new Agent(agentID));
   }
  };
  SwingUtilities.invokeLater(removeIt);
 }

 /**
 * Displays a dialog box with the error string.
 *
 * @param	errMsg error message to print
 */

 public void showError(String errMsg) {
  JOptionPane.showMessageDialog(null, errMsg, "Error in " + mySniffer.getName(), JOptionPane.ERROR_MESSAGE);
 }

 public Dimension getPreferredSize() {
  return new Dimension(700,500);
 }

 private void setUI(String ui) {
  try {
   UIManager.setLookAndFeel("javax.swing.plaf."+ui);
   SwingUtilities.updateComponentTreeUI(this);
   pack();
  } catch(Exception e){
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
	public void setUI2Metal()
	{
		setUI("metal.MetalLookAndFeel");
    }

 /**
 * Provides async disposal of the gui to prevent deadlock when not running in
 * awt event dispatcher
 */

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
    SwingUtilities.invokeLater(new disposeIt(this));
  }


} // End of class MainWindow 
