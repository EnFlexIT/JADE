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

//#APIDOC_EXCLUDE_FILE
//#J2ME_EXCLUDE_FILE

import java.awt.Font;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.Image;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import jade.domain.FIPAAgentManagement.APDescription;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;

import System.Windows.Forms.*;
import System.Drawing.*;
import System.Collections.*;
/**
   
   @author Francisco Regi, Andrea Soracchi - Universita' di Parma
   @version $Date: 2003/12/23 13:25:19 $ $Revision: 2.9 $
 */
public class AgentTree /* extends Panel */
{
	
	private static String localPlatformName = "ThisPlatform";
	/**
	 @serial
	 */
	public TreeView tree;
	private Panel myPanel;
	static protected  Icon[] icons;
	/**
	 @serial
	 */
	private Map mapDescriptor;

	/** @delegate */
	delegate void AddAgentNodeDelegate(AgentNode node, String containerName, String agentName, String agentAddress, String agentType);
	/** @delegate */
	delegate void AddContainerNodeDelegate(ContainerNode node, String typeContainer, java.net.InetAddress addr);
	/** @delegate */
	delegate void AddRemoteAgentNodeDelegate(AMSAgentDescription agent, String HAP);
	/** @delegate */
	delegate void AddRemotePlatformNodeDelegate(AID ams, APDescription desc);
	/** @delegate */
	delegate void AddRemotePlatformsFolderNodeDelegate();

	/** @delegate */
	delegate void ModifyAgentNodeDelegate(String containerName, String agentName, String address, String state, String ownership);

	/** @delegate */
	delegate void RemoveAgentNodeDelegate(String containerName, String agentName);
	/** @delegate */
	delegate void RemoveContainerNodeDelegate(String nameNode);
	/** @delegate */
	delegate void RemoveRemotePlatformNodeDelegate(String name);

	/** @delegate */
	delegate void ThawAgentNodeDelegate(String oldContainerName, String newContainerName, String agentName);
 

	// This class is abstract and represents the general node
	public abstract class  Node extends TreeNode 
	{

		/**
		 @serial
		 */
		protected Icon img;
		/**
		 @serial
		 */
		protected String name;
		/**
		 /**
		 @serial
		 */
		protected String state;
		/**
		 /**
		 @serial
		 */
		protected String ownership;
		/**
		 @serial
		 */
		protected boolean greyOut=false;

		public Node(String name) 
		{
			super(name);
			this.name = name;
		}

		public Icon getIcon(String typeAgent) 
		{
			/*
			Image image = getToolkit().getImage(getClass().getResource(getIconAgent(typeAgent)));
			if (greyOut) 
			{
				ImageFilter colorfilter = new MyFilterImage();
				Image imageFiltered=createImage(new FilteredImageSource(image.getSource(),colorfilter));
				return new ImageIcon(imageFiltered);
			}
			else return new ImageIcon(image);
			*/
			return null;

		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}
   
		public String getState()
		{
			return state != null ? state : "";
		}

		public void setState(String state)
		{
			this.state = state;
		}
   
		public String getOwnership()
		{
			return ownership != null ? ownership : "";
		}

		public void setOwnership(String ownership)
		{
			this.ownership = ownership;
		}

		public void changeIcon(String agentState) 
		{
			if(agentState.equalsIgnoreCase("suspended")) 
			{
				greyOut = true;
				setType("FIPAAGENT");
			}
			else if(agentState.equalsIgnoreCase("active")) 
			{
				greyOut = false;
				setType("FIPAAGENT");
			}
			else if(agentState.equalsIgnoreCase("frozen")) 
			{
				greyOut = false;
				setType("FROZENAGENT");
			}
		}

		public abstract String getType();
		public abstract void setType(String type);
		public abstract String getToolTipText();

	} // End of class Node

	public class AgentNode extends Node 
	{
		/**
		 @serial
		 */
		private String typeAgent;
		/**
		 @serial
		 */
		private String addressAgent;

		public AgentNode(String name) 
		{
			super(name);
		}

		public void address(String address) 
		{
			addressAgent=address;
		}

		public void setType(String type) 
		{
			typeAgent=type;
		}

		public String getType() 
		{
			return typeAgent;
		}

		public String getAddress() 
		{
			return addressAgent;
		}

		public String getToolTipText() 
		{
			//return stateAgent;
			return ("Local Agent");
		}

	}  // End of AgentNode


	// class that represents the ContainerNode

	public class ContainerNode extends Node 
	{
		/**
		 @serial
		 */
		InetAddress addressmachine;
		/**
		 @serial
		 */
		String typeContainer;

		public ContainerNode(String name) 
		{
			super(name);
		}

		public void setAddress(InetAddress addr) 
		{
			addressmachine = addr;
		}

		public void setType(String type) 
		{
			typeContainer = type;
		}

		public String getType() 
		{
			return typeContainer;
		}

		public String getToolTipText() 
		{
			if(addressmachine != null)
				return name + ":" + addressmachine.getHostName() + "[" + addressmachine.getHostAddress() + "]";
			else
				return name + ":<Unknown Host> [???:???:???:???]";
		}

	} // End of ContainerNode

	public class SuperContainer extends Node 
	{

		public SuperContainer(String name) 
		{
			super(name);
			register("SUPERCONTAINER",new MenuItem(),"images/folderyellow.gif");
		}

		public String getToolTipText() 
		{
			return ("Java Agent DEvelopment Framework");
		}

		public String getType()
		{
			return "SUPERCONTAINER";
		}

		public void setType(String noType) {}
	}

	public class RemotePlatformsFolderNode extends Node
	{
	
		public RemotePlatformsFolderNode(String name)
		{
			super(name);
		}
	
		public String getToolTipText()
		{
			return ("List of RemotePlatforms");
		}
	
		public void setType(String noType)
		{
	
		}
	
		public String getType()
		{
			return("REMOTEPLATFORMS");
		}
	
	}

	public class localPlatformFolderNode extends Node
	{

		public localPlatformFolderNode(String name)
		{
			super(name);
			register("LOCALPLATFORM",new MenuItem(), "images/folderyellow.gif");
		}
	
		public String getToolTipText()
		{
			return("Local JADE Platform");
		}
	
		public void setType(String noType){}
	
		public String getType()
		{
			return("LOCALPLATFORM");
		}
	}

	//remote PlatformNode
	public class RemotePlatformNode extends Node
	{
	
		private APDescription AP_Profile;
		private AID amsAID;
	
		public RemotePlatformNode(String name)
		{
			super(name);
		}
	
		public String getToolTipText()
		{
			return ("Remote Platform");
		}
	
		public void setType(String noType)
		{
	
		}
	
		public String getType()
		{
			return("REMOTEPLATFORM");
		}
	
		public void setAPDescription(APDescription desc)
		{
	
			AP_Profile = desc;
		}
	
		public APDescription getAPDescription()
		{
			return AP_Profile;
		}

		public void setAmsAID(AID id)
		{
			amsAID = id;
		}
	
		public AID getAmsAID()
		{
			return amsAID;
		}
	}

	//public class RemoteAgentNode extends Node{
	public class RemoteAgentNode extends AgentNode
	{

		private AMSAgentDescription amsd;
	
		public RemoteAgentNode(String name)
		{
			super(name);
		}
	
		public String getToolTipText()
		{
			return ("Remote Agent");
		}
	
		public void setType(String noType)
		{
	
		}
	
		public String getType()
		{
			return("REMOTEAGENT");
		}
	
		public void setAMSDescription(AMSAgentDescription id)
		{
			amsd = id;
		}
	
		public AMSAgentDescription getAMSDescription()
		{
			return amsd;
		}
	}

	private static final String FROZEN_AGENTS = "Frozen Agents";
	private System.Drawing.Font myFont = null;

	public AgentTree(System.Drawing.Font f) 
	{
		super();
		/*
		TreeSelectionModel selModel;
		TreeIconRenderer treeR;
		mapDescriptor=new HashMap();
		tree=new JTree();
		tree.setFont(f);
		tree.setModel(new AgentTreeModel(new SuperContainer("AgentPlatforms")));
		tree.setLargeModel(false);
		selModel = tree.getSelectionModel();
		selModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
  
		//added folder for localPlatform.
		AgentTreeModel model = getModel();
		MutableTreeNode root = (MutableTreeNode)model.getRoot();
		localPlatformFolderNode localAP = new localPlatformFolderNode(localPlatformName);
		model.insertNodeInto(localAP, root, root.getChildCount());
  
		ToolTipManager.sharedInstance().registerComponent(tree);
		tree.setShowsRootHandles(true);
		treeR = new TreeIconRenderer();
		tree.setCellRenderer(treeR);
		tree.setRowHeight(0);
		*/
		
		mapDescriptor	=new HashMap();
		myFont = f;
	}

	public void setPanel(Panel p)
	{
		myPanel = p;
	}

	public Panel getPanel()
	{
		return myPanel;
	}

	public void InitializeTreeView()
	{
		
		//added folder for localPlatform.
		TreeNode root = new TreeNode("AgentPlatforms");
		localPlatformFolderNode localAP = new localPlatformFolderNode(localPlatformName);
		root.get_Nodes().Add( localAP );
		tree.get_Nodes().Add( root );

		//Customization of TreeView
		tree.set_ImageIndex(-1);
		tree.set_Location(new System.Drawing.Point(0, 0));
		tree.set_Name("treeView1");
		tree.set_SelectedImageIndex(-1);
		//tree.set_Size(new System.Drawing.Size(200, 272));
		tree.set_TabIndex(0);
		tree.set_Scrollable(true);
		tree.set_Font(myFont);

		myPanel.set_BorderStyle( BorderStyle.None );
		myPanel.set_AutoScroll(true);
	}

	/*
	public void listenerTree(TreeSelectionListener  panel) 
	{
		tree.addTreeSelectionListener(panel);
	}
	*/

	public AgentTree.Node createNewNode(String name,int i) 
	{
		switch(i) 
		{
			case 0: return new AgentTree.ContainerNode(name);    
			case 1: return new AgentTree.AgentNode(name);
		}
		return null;
	}

 
	public void refreshLocalPlatformName(String newName)
	{
 
		String oldName = localPlatformName;
		localPlatformName = newName;
		/*
		AgentTreeModel model = getModel();
		MutableTreeNode root = (MutableTreeNode)model.getRoot();
		Enumeration children = root.children();
		while(children.hasMoreElements())
		*/
		TreeNode root = tree.get_Nodes().get_Item(0);
		IEnumerator children = root.get_Nodes().GetEnumerator();
		while ( children.MoveNext() )
		{
			//AgentTree.Node node = (AgentTree.Node)children.nextElement();
			AgentTree.Node node = (AgentTree.Node) children.get_Current();
			String name = node.getName();
			if(name.equalsIgnoreCase(oldName))
			{
				node.setName(newName);
				return;
			}
		}
	}

	public void clearLocalPlatform() 
	{
		/*
		AgentTreeModel model = getModel();
		MutableTreeNode root = (MutableTreeNode)model.getRoot();
		Enumeration folders = root.children();
		while(folders.hasMoreElements())
		*/
		TreeNode root = tree.get_Nodes().get_Item(0);
		IEnumerator folders = root.get_Nodes().GetEnumerator();
		while ( folders.MoveNext() )
		{
			//AgentTree.Node folderNode =(AgentTree.Node)folders.nextElement();
			AgentTree.Node folderNode =(AgentTree.Node)folders.get_Current();
			String folderName = folderNode.getName();
			if(folderName.equalsIgnoreCase(localPlatformName)) 
			{
				/*
				Enumeration containers = folderNode.children();
				List toRemove = new LinkedList();
				while(containers.hasMoreElements()) 
				*/
				List toRemove = new LinkedList();
				IEnumerator containers = folderNode.get_Nodes().GetEnumerator();
				while (containers.MoveNext() )
				{
					//AgentTree.Node container = (AgentTree.Node)containers.nextElement();
					AgentTree.Node container = (AgentTree.Node) containers.get_Current();
					toRemove.add(container);
				}

				Iterator it = toRemove.iterator();
				while(it.hasNext()) 
				{
					/*
					MutableTreeNode node = (MutableTreeNode)it.next();
					model.removeNodeFromParent(node);
					*/
					TreeNode node = (TreeNode) it.next();
					tree.get_Nodes().Remove(node);
				}
			}
		}
	}
	
	public void addContainerNode(ContainerNode node, String typeContainer, InetAddress addr) 
	{
		if ( myPanel.get_InvokeRequired() )
		{
			AddContainerNodeDelegate acnd = new AddContainerNodeDelegate( addContainerNode );
			myPanel.BeginInvoke( acnd, new Object[] {node, typeContainer, addr} );
		}
		else
		{
			/*
			 AgentTreeModel model = getModel();
			 MutableTreeNode root = (MutableTreeNode)model.getRoot();
			 */
			node.setType(typeContainer);
			/*
			 Enumeration folders = root.children();
			 while(folders.hasMoreElements())
			 */
			TreeNode root = tree.get_Nodes().get_Item(0);
			IEnumerator folders = root.get_Nodes().GetEnumerator();
			while ( folders.MoveNext() )
			{
				//AgentTree.Node folderNode =(AgentTree.Node)folders.nextElement();
				AgentTree.Node folderNode =(AgentTree.Node) folders.get_Current();
				String folderName = folderNode.getName();
				if(folderName.equalsIgnoreCase(localPlatformName))
					//model.insertNodeInto(node, folderNode, folderNode.getChildCount());
					folderNode.get_Nodes().Add( node );
			}
			node.setAddress(addr);
			return;
		}
	}

	public void removeContainerNode(String nameNode) 
	{
		if ( myPanel.get_InvokeRequired() )
		{
			RemoveContainerNodeDelegate rcnd = new RemoveContainerNodeDelegate( removeContainerNode );
			myPanel.BeginInvoke( rcnd, new Object[] {nameNode} );
		}
		else
		{
			/*
			 AgentTreeModel model = getModel();
			 MutableTreeNode root = (MutableTreeNode)model.getRoot();
			 Enumeration folders = root.children();
			 while(folders.hasMoreElements())
			 */
			TreeNode root = tree.get_Nodes().get_Item(0);
			IEnumerator folders = root.get_Nodes().GetEnumerator();
			while ( folders.MoveNext() )
			{
				//AgentTree.Node folderNode =(AgentTree.Node)folders.nextElement();
				AgentTree.Node folderNode =(AgentTree.Node) folders.get_Current();
				String folderName = folderNode.getName();
				if(folderName.equalsIgnoreCase(localPlatformName))
				{//found the localplatform folder	
					/*
					 Enumeration containers = folderNode.children();
					 while(containers.hasMoreElements()) 
					 */
					IEnumerator containers = folderNode.get_Nodes().GetEnumerator();
					boolean found = false;
					AgentTree.Node node = null;
					while (containers.MoveNext() && !found )
					{
						//AgentTree.Node node = (AgentTree.Node)containers.nextElement();
						node = (AgentTree.Node) containers.get_Current();
						String nodeName = node.getName();
						if(nodeName.equalsIgnoreCase(nameNode)) 
						{
							/*
							 model.removeNodeFromParent(node);
							 return;
							 */
							found = true;
						}
					}

					if (found)
						folderNode.get_Nodes().Remove( node );
				}
			}
		}
	}



	public void addRemotePlatformsFolderNode()
	{
		if ( myPanel.get_InvokeRequired() )
		{
			AddRemotePlatformsFolderNodeDelegate arpfnd = new AddRemotePlatformsFolderNodeDelegate( addRemotePlatformsFolderNode );
			myPanel.BeginInvoke(arpfnd);
		}
		else
		{
			/*
			 AgentTreeModel model = getModel();
			 MutableTreeNode root = (MutableTreeNode)model.getRoot();
			 Enumeration children = root.children();
			 */
			TreeNode root = tree.get_Nodes().get_Item(0);
			IEnumerator children = root.get_Nodes().GetEnumerator();
		  	
			boolean existing = false;
  	
			//while(children.hasMoreElements() & (!existing))
			while( children.MoveNext() & (!existing) )
			{
				//AgentTree.Node node = (AgentTree.Node)children.nextElement();
				AgentTree.Node node = (AgentTree.Node) children.get_Current();
				String nodeName = node.getName();
				if(nodeName.equalsIgnoreCase("REMOTEPLATFORMS"))
					existing = true;
			}
  	
			if (!existing)
			{
				RemotePlatformsFolderNode rpn = new RemotePlatformsFolderNode("RemotePlatforms");
				//model.insertNodeInto(rpn, root, root.getChildCount());
				tree.get_Nodes().Add(rpn);
			}
  	
			return;
		}
	}
  
	public void addAgentNode(AgentNode node, String containerName, String agentName, String agentAddress, String agentType) 
	{
		if ( myPanel.get_InvokeRequired() )
		{
			AddAgentNodeDelegate aand = new AddAgentNodeDelegate( addAgentNode );
			myPanel.BeginInvoke( aand, new Object[] {node, containerName, agentName, agentAddress, agentType} );
		}
		else
		{
			/*
			 AgentTreeModel model = getModel();
			 MutableTreeNode root = (MutableTreeNode)model.getRoot();
			 */
			TreeNode root = tree.get_Nodes().get_Item(0);
			node.setType(agentType);
			AgentTree.AgentNode nod=(AgentTree.AgentNode) node;
			nod.address(agentAddress);
			nod.setState("Running");
			//search for the folder of the local Platform
			/*
			 Enumeration folders = root.children();
			 while(folders.hasMoreElements())
			 */
			IEnumerator folders = root.get_Nodes().GetEnumerator();
			while( folders.MoveNext() )
			{
				//AgentTree.Node folderNode = (AgentTree.Node)folders.nextElement();
				AgentTree.Node folderNode = (AgentTree.Node) folders.get_Current();
				String folderName = folderNode.getName();
				if(folderName.equalsIgnoreCase(localPlatformName))
				{
					// Search for the agent container 'containerName'
					/*
					 Enumeration containers = folderNode.children();
					 while(containers.hasMoreElements()) 
					 */
					IEnumerator containers = folderNode.get_Nodes().GetEnumerator();
					boolean found = false;
					AgentTree.Node container = null;
					while( containers.MoveNext() && !found )
					{
						//AgentTree.Node container = (AgentTree.Node)containers.nextElement();
						container = (AgentTree.Node) containers.get_Current();
						String contName = container.getName();
						if(contName.equalsIgnoreCase(containerName)) 
						{
							// Add this new agent to this container and return
							/*
							 model.insertNodeInto(node, container, container.getChildCount());
							 return;
							 */
							found = true;
						}
					}
					
					if (container == null)
						return;

					IEnumerator agents = container.get_Nodes().GetEnumerator();
					boolean foundAgent = false;
					while (agents.MoveNext() && !foundAgent)
					{
						AgentTree.Node aNode = (AgentTree.Node) agents.get_Current();
						if (aNode.getName().equalsIgnoreCase( node.getName() ) )
							foundAgent = true;
					}
					if (found && !foundAgent )
					{

						
						container.get_Nodes().Add( node );
					}
				}
			}
		}
	}

	public void modifyAgentNode(String containerName, String agentName, String address, String state, String ownership) 
	{
		if ( myPanel.get_InvokeRequired() )
		{
			ModifyAgentNodeDelegate mand = new ModifyAgentNodeDelegate( modifyAgentNode );
			myPanel.BeginInvoke( mand, new Object[] {containerName, agentName, address, state, ownership} );
		}
		else
		{
			/*
			 AgentTreeModel model = getModel();
			 MutableTreeNode root = (MutableTreeNode)model.getRoot();
			 //search for the folder of the local Platform
			 Enumeration folders = root.children();
			 while (folders.hasMoreElements()) 
			 */
			TreeNode root = tree.get_Nodes().get_Item(0);
			IEnumerator folders = root.get_Nodes().GetEnumerator();
			while( folders.MoveNext() )
			{
				//AgentTree.Node folderNode = (AgentTree.Node)folders.nextElement();
				AgentTree.Node folderNode = (AgentTree.Node) folders.get_Current();
				String folderName = folderNode.getName();
				if (folderName.equalsIgnoreCase(localPlatformName)) 
				{
					// Search for the agent container 'containerName'
					/*
					 Enumeration containers = folderNode.children();
					 while (containers.hasMoreElements()) 
					 */
					IEnumerator containers = folderNode.get_Nodes().GetEnumerator();
					while( containers.MoveNext() )
					{
						//AgentTree.Node container = (AgentTree.Node)containers.nextElement();
						AgentTree.Node container = (AgentTree.Node) containers.get_Current();
						String contName = container.getName();
						if (contName.equalsIgnoreCase(containerName)) 
						{
							/*
							 Enumeration agents = container.children();
							 while(agents.hasMoreElements()) 
							 */
							IEnumerator agents = container.get_Nodes().GetEnumerator();
							boolean found = false;
							AgentTree.Node agentOld = null;
							AgentTree.Node agent	= null;
							while( agents.MoveNext() && !found )
							{
								//AgentTree.Node agent = (AgentTree.Node)agents.nextElement();
								agent = (AgentTree.Node) agents.get_Current();
								agentOld = (AgentTree.Node) agents.get_Current();

								if (agent.getName().equalsIgnoreCase(agentName)) 
								{
									if (state != null) 
										agent.setState(state);
									if (ownership != null) 
										agent.setOwnership(ownership);
									agent.changeIcon(state);
									/*
									 model.nodeChanged(agent);
									 return;
									 */
									found = true;
								}
							}
							container.get_Nodes().Remove(agentOld);
							container.get_Nodes().Add(agent);
						}
					}
				}
			}
		}
	}

	public void moveAgentNode(String fromContainerName, String toContainerName, String agentName) 
	{
		/*
		AgentTreeModel model = getModel();
		*/

		AgentTree.Node fromContainer = findContainerNode(fromContainerName);
		AgentTree.Node toContainer = findContainerNode(toContainerName);

		// If there is a frozen agent already, do nothing, else move the agent node
		AgentTree.Node frozenAgents = findFrozenAgentsFolder(toContainer, FROZEN_AGENTS);
		if(frozenAgents != null) 
		{
			AgentTree.Node agent = findAgentNode(frozenAgents, agentName);
			if(agent == null) 
			{
				// Move the agent node
				agent = findAgentNode(fromContainer, agentName);
				/*
				model.removeNodeFromParent(agent);
				model.insertNodeInto(agent, toContainer, toContainer.getChildCount());
				*/
				frozenAgents.get_Nodes().Remove(agent);
				toContainer.get_Nodes().Add(agent);
			}
		}
		else 
		{
			// Move the agent node
			AgentTree.Node agent = findAgentNode(fromContainer, agentName);
			/*
			model.removeNodeFromParent(agent);
			model.insertNodeInto(agent, toContainer, toContainer.getChildCount());
			*/
			fromContainer.get_Nodes().Remove(agent);
			toContainer.get_Nodes().Add(agent);
		}
	}

	public void freezeAgentNode(String oldContainerName, String newContainerName, String agentName) 
	{
		/*
		AgentTreeModel model = getModel();
		*/
		AgentTree.Node oldContainer = findContainerNode(oldContainerName);
		AgentTree.Node agent = findAgentNode(oldContainer, agentName);
		/*
		model.removeNodeFromParent(agent);
		*/
		oldContainer.get_Nodes().Remove(agent);

		agent.setState("frozen");
		agent.changeIcon("frozen");

		AgentTree.Node newContainer = findContainerNode(newContainerName);
		AgentTree.Node frozenAgents = findFrozenAgentsFolder(newContainer, FROZEN_AGENTS);
		if(frozenAgents == null) 
		{
			frozenAgents = createNewNode(FROZEN_AGENTS, 0);
			frozenAgents.setType("FROZENCONTAINER");
			/*
			model.insertNodeInto(frozenAgents, newContainer, 0);
			*/
			newContainer.get_Nodes().Add(frozenAgents);
		}
		/*
		model.insertNodeInto(agent, frozenAgents, frozenAgents.getChildCount());
		*/
		frozenAgents.get_Nodes().Add(agent);

	}

	public void thawAgentNode(String oldContainerName, String newContainerName, String agentName) 
	{
		if ( myPanel.get_InvokeRequired() )
		{
			ThawAgentNodeDelegate tand = new ThawAgentNodeDelegate( thawAgentNode );
			myPanel.BeginInvoke( tand, new Object[] {oldContainerName, newContainerName, agentName} );
		}
		else
		{
			/*
			 AgentTreeModel model = getModel();
			 */
			AgentTree.Node oldContainer = findContainerNode(oldContainerName);
			AgentTree.Node frozenAgents = findFrozenAgentsFolder(oldContainer, FROZEN_AGENTS);
			AgentTree.Node agent = findAgentNode(frozenAgents, agentName);
			/*
			 model.removeNodeFromParent(agent);
			 if(frozenAgents.isLeaf()) 
			 {
			 model.removeNodeFromParent(frozenAgents);
			 }
			 */
			frozenAgents.get_Nodes().Remove(agent);
			if (frozenAgents.get_Nodes().get_Count() == 0)
				oldContainer.get_Nodes().Remove(frozenAgents);

			agent.setState("active");
			agent.changeIcon("active");

			AgentTree.Node newContainer = findContainerNode(newContainerName);
			/*
			 model.insertNodeInto(agent, newContainer, newContainer.getChildCount());
			 */
			newContainer.get_Nodes().Add(agent);
		}

	}

	public void removeAgentNode(String containerName, String agentName) 
	{
		if ( myPanel.get_InvokeRequired() )
		{
			RemoveAgentNodeDelegate rand = new RemoveAgentNodeDelegate( removeAgentNode );
			myPanel.BeginInvoke( rand, new Object[] {containerName, agentName} );
		}
		else
		{
			/*
			 AgentTreeModel model = getModel();
			 */
			AgentTree.Node container = findContainerNode(containerName);
			AgentTree.Node agent = findAgentNode(container, agentName);

			if(agent != null) 
			{
				/*
				 model.removeNodeFromParent(agent);
				 */
				container.get_Nodes().Remove(agent);
			}
			else 
			{
				// It can be a frozen agent
				AgentTree.Node frozenAgents = findFrozenAgentsFolder(container, FROZEN_AGENTS);
				if(frozenAgents != null) 
				{
					agent = findAgentNode(frozenAgents, agentName);
				
					/*
					 model.removeNodeFromParent(agent);
					 if(frozenAgents.isLeaf()) 
					 {
					 model.removeNodeFromParent(frozenAgents);
					 }
					 */
					frozenAgents.get_Nodes().Remove(agent);
					if (frozenAgents.get_Nodes().get_Count() == 0)
						container.get_Nodes().Remove(frozenAgents);
				}
			}
		}
	}

	public void addRemotePlatformNode(AID ams,APDescription desc)
	{
		if ( myPanel.get_InvokeRequired() )
		{
			AddRemotePlatformNodeDelegate arpnd = new AddRemotePlatformNodeDelegate( addRemotePlatformNode );
			myPanel.BeginInvoke( arpnd, new Object[] {ams, desc} );
		}
		else
		{		
			/*
			 AgentTreeModel model = getModel();
			 MutableTreeNode root = (MutableTreeNode)model.getRoot();
    
			 // Search for the folder REMOTEPLATFORM
			 Enumeration containers = root.children();
			 while(containers.hasMoreElements()) 
			 */
			TreeNode root = tree.get_Nodes().get_Item(0);
			IEnumerator containers = root.get_Nodes().GetEnumerator();
			while ( containers.MoveNext() )
			{//1
				//AgentTree.Node container = (AgentTree.Node)containers.nextElement();
				AgentTree.Node container = (AgentTree.Node) containers.get_Current();
				String contName = container.getName();
				if(contName.equalsIgnoreCase("REMOTEPLATFORMS")) 
				{//2
					boolean found = false;
					/*
					 Enumeration agents = container.children();
					 while(agents.hasMoreElements() && !found)
					 */
					IEnumerator agents = container.get_Nodes().GetEnumerator();
					while ( agents.MoveNext() )
					{//3
						//AgentTree.RemotePlatformNode platform = (AgentTree.RemotePlatformNode)agents.nextElement();
						AgentTree.RemotePlatformNode platform = (AgentTree.RemotePlatformNode) agents.get_Current();
						String APName = platform.getName();
						if(APName.equalsIgnoreCase(desc.getName()))
						{//update the APDescription of this node
							platform.setAPDescription(desc);
							found = true;
						}
					}//3
					if(!found)
					{
						// Add this new platform to this container and return
						RemotePlatformNode node = new RemotePlatformNode(desc.getName());
						node.setAPDescription(desc);
						node.setAmsAID(ams);
						/*
						 model.insertNodeInto(node, container, container.getChildCount());}
						 */
						container.get_Nodes().Add( node );
						return;		
					}
				}//2
			}//1
		}

	}

  
	public void removeRemotePlatformNode(String name)
	{
		if ( myPanel.get_InvokeRequired() )
		{
			RemoveRemotePlatformNodeDelegate rrpnd = new RemoveRemotePlatformNodeDelegate( removeRemotePlatformNode );
			myPanel.BeginInvoke( rrpnd, new Object[] {name} );
		}
		else
		{
			/*
			 AgentTreeModel model = getModel();
			 MutableTreeNode root = (MutableTreeNode)model.getRoot();

			 // Search for the  RemotePlatforms node
			 Enumeration containers = root.children();
			 while(containers.hasMoreElements()) 
			 */
			TreeNode root = tree.get_Nodes().get_Item(0);
			IEnumerator containers = root.get_Nodes().GetEnumerator();
			while ( containers.MoveNext() )
			{
				//AgentTree.Node container = (AgentTree.Node)containers.nextElement();
				AgentTree.Node container = (AgentTree.Node) containers.get_Current();
				String contName = container.getName();
				if(contName.equalsIgnoreCase("REMOTEPLATFORMS")) 
				{
					// Search for the ams  
					/*
					 Enumeration agents = container.children();
					 while(agents.hasMoreElements()) 
					 */
					IEnumerator agents = container.get_Nodes().GetEnumerator();
					while( agents.MoveNext() )
					{
						//AgentTree.Node agent = (AgentTree.Node)agents.nextElement();
						AgentTree.Node agent = (AgentTree.Node) agents.get_Current();
						String agName = agent.getName();
						if(agName.equalsIgnoreCase(name))
						{
							/*
							 model.removeNodeFromParent(agent);
							 //if it's the last child remove the folder REMOTEPLATFORMS
							 if (container.getChildCount() == 0)
							 model.removeNodeFromParent(container);
							 */
							container.get_Nodes().Remove( agent );
							if ( container.get_Nodes().get_Count() == 0 )
								root.get_Nodes().Remove(container);
							return;
						}
					}
				}
			}
		}
	}
  
	public void addRemoteAgentNode(AMSAgentDescription agent, String HAP)
	{//0
  		
		if ( myPanel.get_InvokeRequired() )
		{
			AddRemoteAgentNodeDelegate arand = new AddRemoteAgentNodeDelegate( addRemoteAgentNode );
			myPanel.BeginInvoke( arand, new Object[] {agent, HAP} );
		}
		else
		{
			/*
			AgentTreeModel model = getModel();
			MutableTreeNode root = (MutableTreeNode)model.getRoot();
	  	
			//Search for the REMOTEPLATFORMS node
			Enumeration containers = root.children();
	    
			while(containers.hasMoreElements()) 
			*/
			TreeNode root = tree.get_Nodes().get_Item(0);
			IEnumerator containers = root.get_Nodes().GetEnumerator();
			while ( containers.MoveNext() )
			{ //1
	    
				//AgentTree.Node container = (AgentTree.Node)containers.nextElement();
				AgentTree.Node container = (AgentTree.Node) containers.get_Current();
				String contName = container.getName();
	    
				if(contName.equalsIgnoreCase("REMOTEPLATFORMS")) 
				{ //2
					//search the remotePlatform
					/*
					 Enumeration plat_Enum = container.children();
	     	 
					 while(plat_Enum.hasMoreElements())
					 */
					IEnumerator plat_Enum = container.get_Nodes().GetEnumerator();
					while ( plat_Enum.MoveNext() )
					{//3
						//AgentTree.Node platformNode = (AgentTree.Node)plat_Enum.nextElement();
						AgentTree.Node platformNode = (AgentTree.Node) plat_Enum.get_Current();
						String platformNodeName = platformNode.getName();
						if(platformNodeName.equalsIgnoreCase(HAP))
						{//4
							//now add remote agent registered with that ams...
							/*
							 Enumeration remote_agents = platformNode.children();
							 boolean found = false;
							 while(remote_agents.hasMoreElements() && ! found)
							 */
							IEnumerator remote_agents = platformNode.get_Nodes().GetEnumerator();
							boolean found = false;
							while ( remote_agents.MoveNext() && !found)
							{ //5
	     	 
								//AgentTree.RemoteAgentNode node = (AgentTree.RemoteAgentNode)remote_agents.nextElement();
								AgentTree.RemoteAgentNode node = (AgentTree.RemoteAgentNode) remote_agents.get_Current();
								String remoteName = node.getName();
								if(remoteName.equalsIgnoreCase(agent.getName().getName()))
								{//6
									node.setAMSDescription(agent); //update the AMSDescription
									found = true;
								}//6
							}//5
							if(!found)
							{//7
								AgentTree.RemoteAgentNode newNode = new AgentTree.RemoteAgentNode(agent.getName().getName());
								newNode.setAMSDescription(agent);
								/*
								 model.insertNodeInto(newNode,platformNode,platformNode.getChildCount());
								 */
								platformNode.get_Nodes().Add( newNode );
							}//7
						}//4
					}//3
				}//2
			}//1
		}
	}//0
  
	/*
	public AgentTreeModel getModel()
	{
		if (tree.getModel() instanceof AgentTreeModel)
			return (AgentTreeModel)tree.getModel();
		else 
		{
			System.out.println(tree.getModel());
			return null;
		}
	}
	*/

	public void register(String key, MenuItem popmenu, String pathImage) 
	{
		if (!mapDescriptor.containsKey(key))
		{
			NodeDescriptor nDescriptor=new NodeDescriptor(popmenu,pathImage);
			mapDescriptor.put(key,nDescriptor);
		}
	}

	public MenuItem getPopupMenu(String key) 
	{
		NodeDescriptor nDescriptor=(NodeDescriptor) mapDescriptor.get(key);
		return nDescriptor.getPopupMenu();
	}

	public void setNewPopupMenu(String key,MenuItem pop)
	{
		if (mapDescriptor.containsKey(key))
		{
			NodeDescriptor nDescriptor = (NodeDescriptor)mapDescriptor.get(key);
			nDescriptor.setNewPopupMenu(pop);
		}
	}
	protected String getIconAgent(String key) 
	{
		NodeDescriptor nDescriptor=(NodeDescriptor) mapDescriptor.get(key);
		return nDescriptor.getPathImage();
	}

	private AgentTree.Node findAgentNode(AgentTree.Node container, String name) 
	{
		/*
		Enumeration agents = container.children();
		while(agents.hasMoreElements()) 
		*/
		IEnumerator agents = container.get_Nodes().GetEnumerator();
		while( agents.MoveNext() )
		{
			//AgentTree.Node agent = (AgentTree.Node)agents.nextElement();
			AgentTree.Node agent = (AgentTree.Node) agents.get_Current();
			if (agent.getName().equalsIgnoreCase(name)) 
			{
				return agent;
			}
		}

		return null;
	}

	private AgentTree.Node findContainerNode(String name) 
	{
		/*
		AgentTreeModel model = getModel();
		MutableTreeNode root = (MutableTreeNode)model.getRoot();
		//search for the folder of the local Platform
		Enumeration folders = root.children();
		while (folders.hasMoreElements()) 
		*/
		TreeNode root = tree.get_Nodes().get_Item(0);
		IEnumerator folders = root.get_Nodes().GetEnumerator();
		while( folders.MoveNext() )
		{
			//AgentTree.Node folderNode = (AgentTree.Node)folders.nextElement();
			AgentTree.Node folderNode = (AgentTree.Node) folders.get_Current();
			String folderName = folderNode.getName();
			if(folderName.equalsIgnoreCase(localPlatformName)) 
			{
				// Search for the agent container 'name'
				/*
				Enumeration containers = folderNode.children();
				while (containers.hasMoreElements()) 
				*/
				IEnumerator containers = folderNode.get_Nodes().GetEnumerator();
				while( containers.MoveNext() )
				{
					//AgentTree.Node container = (AgentTree.Node)containers.nextElement();
					AgentTree.Node container = (AgentTree.Node) containers.get_Current();
					String contName = container.getName();
					if(contName.equalsIgnoreCase(name)) 
					{
						return container;
					}
				}
			}
		}

		return null;

	}

	private AgentTree.Node findFrozenAgentsFolder(AgentTree.Node container, String name) 
	{
		/*
		Enumeration agents = container.children();
		while(agents.hasMoreElements()) 
		*/
		IEnumerator agents = container.get_Nodes().GetEnumerator();
		while( agents.MoveNext() ) 
		{
			//AgentTree.Node child = (AgentTree.Node)agents.nextElement();
			AgentTree.Node child = (AgentTree.Node) agents.get_Current();
			if(child.getName().equalsIgnoreCase(name) && child.getType().equalsIgnoreCase("FROZENCONTAINER")) 
			{
				return child;
			}
		}

		return null;

	}

}
