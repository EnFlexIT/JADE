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

package jade.gui;

import  javax.swing.*;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import java.awt.Font;
import javax.swing.tree.DefaultMutableTreeNode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.tree.MutableTreeNode;
import java.util.Enumeration;
import java.awt.event.MouseListener;
import java.awt.event.*;
import javax.swing.ImageIcon;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.Image;
import java.util.Map;
import java.util.HashMap;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */
public class AgentTree extends JPanel {
	/**
	@serial
	*/
  public JTree tree;
  static protected  Icon[] icons;
  private Map mapDescriptor;


   // This class is abstract and represents the general node

  public abstract class  Node extends DefaultMutableTreeNode {

   /**
   @serial
   */
   protected Icon img;
   /**
   @serial
   */
   protected String name;
   protected boolean chgeIcon=false;

   public Node(String name) {
     this.name = name;
    }

   public Icon getIcon(String typeAgent) {
      Image image = getToolkit().getImage(getClass().getResource(getIconAgent(typeAgent)));
    if (chgeIcon) {
        ImageFilter colorfilter = new MyFilterImage();
        Image imageFiltered=createImage(new FilteredImageSource(image.getSource(),colorfilter));
        return new ImageIcon(imageFiltered);
    }
   else return new ImageIcon(image);

   }

   public String getName(){
    return name;
   }

   public void changeIcon(boolean chI) {
      chgeIcon=chI;
   }

   public abstract String getType();
   public abstract void setType(String type);
   public abstract String getToolTipText();

} // End of class Node

  public class AgentNode extends Node {
   /**
   @serial
   */
   private String typeAgent;
   /**
   @serial
   */
   private String stateAgent;
   /**
   @serial
   */
   private String addressAgent;

   public AgentNode(String name) {
     super(name);
    }

    public void address(String address) {
     addressAgent=address;
    }

    public void setType(String type) {
     typeAgent=type;
    }

    public String getType() {
     return typeAgent;
    }

    public void setState(String state){
     stateAgent=state;
    }

    public String getAddress() {
     return addressAgent;
    }

    public String getToolTipText() {
     return stateAgent;
    }

}  // End of AgentNode


  // class that represents the ContainerNode

 public class ContainerNode extends Node {
 	/**
 	@serial
 	*/
  InetAddress addressmachine;
  /**
  @serial
  */
  String typeContainer;

  public ContainerNode(String name) {
     super(name);
   }

  public void setAddress(InetAddress addr) {
   addressmachine = addr;
  }

  public void setType(String type) {
     typeContainer = type;
  }

  public String getType() {
     return typeContainer;
  }

  public String getToolTipText() {
    if(addressmachine != null)
      return name + ":" + addressmachine.getHostName() + "[" + addressmachine.getHostAddress() + "]";
    else
      return name + ":<Unknown Host> [???:???:???:???]";
  }

 } // End of ContainerNode

 public class SuperContainer extends Node {

  public SuperContainer(String name) {
   super(name);
   register("SUPERCONTAINER",new JPopupMenu(),"images/TreeRoot.gif");
  }

  public String getToolTipText() {
   return ("Java Agent DEvelopment Framework");
  }

  public String getType(){
   return "SUPERCONTAINER";
  }

  public void setType(String noType) {}
}

 public AgentTree(Font f) {

  TreeSelectionModel selModel;
  TreeIconRenderer treeR;

  mapDescriptor=new HashMap();
  tree=new JTree();
  tree.setFont(f);
  tree.setModel(new AgentTreeModel(new SuperContainer("JADE")));
  tree.setLargeModel(false);
  selModel = tree.getSelectionModel();
  selModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
  ToolTipManager.sharedInstance().registerComponent(tree);
  tree.setShowsRootHandles(true);
  treeR = new TreeIconRenderer();
  tree.setCellRenderer(treeR);
  tree.setRowHeight(0);

  }

  public void listenerTree(TreeSelectionListener  panel) {
   tree.addTreeSelectionListener(panel);
  }

  public AgentTree.Node createNewNode(String name,int i) {
    switch(i) {
     case 0: return new AgentTree.ContainerNode(name);
     case 1: return new AgentTree.AgentNode(name);
    }
   return null;
 }

  public void addContainerNode(ContainerNode node,String typeContainer, InetAddress addr) {
    AgentTreeModel model = getModel();
    MutableTreeNode root = (MutableTreeNode)model.getRoot();
    node.setType(typeContainer);
    model.insertNodeInto(node, root, root.getChildCount());
    node.setAddress(addr);
    return;
  }

  public void removeContainerNode(String nameNode) {
    AgentTreeModel model = getModel();
    MutableTreeNode root = (MutableTreeNode)model.getRoot();
    Enumeration containers = root.children();
    while(containers.hasMoreElements()) {
      AgentTree.Node node = (AgentTree.Node)containers.nextElement();
      String nodeName = node.getName();
      if(nodeName.equalsIgnoreCase(nameNode)) {
	model.removeNodeFromParent(node);
	return;
      }
    }
  }

  public void addAgentNode(AgentNode node,String containerName,String agentName,String agentAddress,String agentType) {
      AgentTreeModel model = getModel();
      MutableTreeNode root = (MutableTreeNode)model.getRoot();
      node.setType(agentType);
      AgentTree.AgentNode nod=(AgentTree.AgentNode) node;
       nod.address(agentAddress);
       nod.setState("Running");
       	// Search for the agent container 'containerName'
	      Enumeration containers = root.children();
	        while(containers.hasMoreElements()) {
	          AgentTree.Node container = (AgentTree.Node)containers.nextElement();
	          String contName = container.getName();
	            if(contName.equalsIgnoreCase(containerName)) {
                // Add this new agent to this container and return
	              model.insertNodeInto(node, container, container.getChildCount());
                return;
             }
          }
  }

  public void removeAgentNode(String containerName, String agentName) {
   	AgentTreeModel model = getModel();
	  MutableTreeNode root = (MutableTreeNode)model.getRoot();

     // Search for the agent container 'containerName'
    Enumeration containers = root.children();
    while(containers.hasMoreElements()) {
	    AgentTree.Node container = (AgentTree.Node)containers.nextElement();
	    String contName = container.getName();
	     if(contName.equalsIgnoreCase(containerName)) {
        // Search for the agent 'agentName' in this agent container
	      Enumeration agents = container.children();
	      while(agents.hasMoreElements()) {
	        AgentTree.Node agent = (AgentTree.Node)agents.nextElement();
	        String agName = agent.getName();
	         if(agName.equalsIgnoreCase(agentName)){
             model.removeNodeFromParent(agent);
             return;
           }
        }
      }
    }
  }

  public AgentTreeModel getModel()
  {
      if (tree.getModel() instanceof AgentTreeModel)
	return (AgentTreeModel)tree.getModel();
      else {
	System.out.println(tree.getModel());
	return null;
      }
  }

  public void register(String key, JPopupMenu popmenu, String pathImage) {
   if (!mapDescriptor.containsKey(key)){
            NodeDescriptor nDescriptor=new NodeDescriptor(popmenu,pathImage);
            mapDescriptor.put(key,nDescriptor);
   }
  }

 public JPopupMenu getPopupMenu(String key) {
   NodeDescriptor nDescriptor=(NodeDescriptor) mapDescriptor.get(key);
   return nDescriptor.getPopupMenu();
   }

 protected String getIconAgent(String key) {
   NodeDescriptor nDescriptor=(NodeDescriptor) mapDescriptor.get(key);
   return nDescriptor.getPathImage();
 }

} // End Of AgentTree;
