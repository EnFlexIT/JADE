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

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.awt.Font;
import java.awt.BorderLayout;
import java.util.Enumeration;
import java.util.Vector;

/** 
 * The Tree listens TreeSelection events, because we want 
 * have a context menu sensible to Nodes. Leaves are sensible 
 * to double clicks.
 * 
 * @see javax.swing.JPanel
 * @see javax.swing.tree.TreeSelectionListener
 * @author Gianluca Tanca
 * @version $Date$ $Revision$
 *
 */
public class MMTree extends JPanel implements TreeSelectionListener{
    
  protected JTree tree; //was private
  private JScrollPane pane;	
	private DefaultMutableTreeNode MMFirstNode;  
	private static int i = 1;
   
	public MMTree(){
		
		setDoubleBuffered(false);
		        
    TreeSelectionModel selModel;
    Font f;
		
    f = new Font("SanSerif",Font.PLAIN,14);
    setFont(f);
		setLayout(new BorderLayout());

		tree = new JTree();
    tree.setFont(f);
    tree.setModel(new MMTreeModel());
		tree.setLargeModel(false);
		tree.setDoubleBuffered(false);
		
    selModel = tree.getSelectionModel();
    selModel.setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
       
		pane = new JScrollPane(tree);
		pane.setOpaque(true);
		pane.setDoubleBuffered(false);
		add(pane);

     /* Enable tool tips for the tree, without this tool tips will not
        be picked up. */
		ToolTipManager.sharedInstance().registerComponent(tree);

		tree.addTreeSelectionListener(this);
		
		tree.addMouseListener(new DoubleClicker());
	
		TreeIconRenderer treeR = new TreeIconRenderer();
		tree.setCellRenderer(treeR);
		tree.setRowHeight(0);
		
	}

/**
 * Returns a handler to the main JTree component 
 *
 * @return handler to the tree
 */
protected JTree getTree(){
	return tree;
}

/** 
 * This method is messaged when a SelectionEvent occurs
 * Here we update the vector of listeners in MMAbstractAction
 */
public void valueChanged(TreeSelectionEvent e){
    	    
	TreePath paths[] = tree.getSelectionPaths();
	Object pathObj[];
  int i,max;
  int j,numPaths;
  String formattedPath = "";
  MMAbstractAction.removeAllListeners();
  // numPaths = paths.length;
  TreeData current;

	if (paths != null) { 
		numPaths = paths.length;	             
	  // ad ogni elemento dell'albero che viene scoperto
		//viene aggiunto un listener
		for(j=0;j<numPaths;j++){
			if (paths[j].getLastPathComponent() instanceof TreeData){
				current = (TreeData) (paths[j].getLastPathComponent());
				MMAbstractAction.AddListener(current);
				formattedPath += getPathAsString(paths[j]);
			}
		}
	}
}

/**
 * Converts a TreePath object in a String object
 *
 * @param Tpath TreePath object to be converted
 * @return TreePath object converted in string
 */
private String getPathAsString (TreePath Tpath){
	Object [] path = Tpath.getPath();
	String formattedPath = " ";

	int max = path.length;
	
	for(i=0;i<max;i++){
		formattedPath+=".";
		if ( path[i] instanceof TreeData){
			TreeData current = (TreeData) path[i];
			formattedPath+=current.getName();
		}
		else formattedPath += path[i].toString();
		}
		formattedPath+="\n";
		return formattedPath;
}

/**
 * Returns the currently selected node
 *
 * @return the current selected node
 */
protected DefaultMutableTreeNode getSelectedNode(){
	TreePath   selPath = tree.getSelectionPath();

	if(selPath != null)
		return (DefaultMutableTreeNode)selPath.getLastPathComponent();
	return null;
}

/**
 * Creates a new node in the selection tree
 *
 * @param name The name of new Node
 * @param level The level (PLATFORM, CONTAINER, AGENT) of new node
 * @return a new node with specified level and name
 */
public DefaultMutableTreeNode createNewNode(String name,int level){
	if (level >TreeData.SUPER_NODE && level<=TreeData.AGENT)
		return new TreeData(name,level);
	else 
		return new TreeData(name,TreeData.AGENT);
}


/**
 * Returns the currently used TreeModel
 *
 * @return current TreeModel
 */
public MMTreeModel getModel(){
	
	if (tree.getModel() instanceof MMTreeModel)
		return (MMTreeModel)tree.getModel();
	else 
		System.out.println(tree.getModel());
	System.exit(-1);
	return null;
}
	

/**
 * Adds a new container to the selection tree
 *
 * @param name name of the container to add
 */	
public void addContainer(String name){ 
	DefaultMutableTreeNode node = createNewNode(name, TreeData.CONTAINER);
  MMTreeModel model = (MMTreeModel)tree.getModel();	
  DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
  model.insertNodeInto(node, root, root.getChildCount());        	
}
	
/**
 * Adds a new agent to the selection tree
 *
 * @param containerName name of the container to put the new agent in
 * @param agentName name of the new agent
 * @param agentAddress address of the new agent
 * @param agentType type of the new agent
 */	
public void addAgent(String containerName, String agentName, String agentAddress, String agentType){ //by BENNY

	// Add an agent to the specified container

  MMTreeModel model = (MMTreeModel)tree.getModel();
  DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

  // Create a suitable new node
  TreeData node = (TreeData)createNewNode(agentName, TreeData.AGENT);
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
			// return;
  	}
	}
} // End of addAgent method
	

/**
 * Remove an agent from the selection tree
 *
 * @param containerName name of the container that contains the agent to be removed
 * @param agentName name of the agent to be removed
 */	
public void removeAgent(String containerName, String agentName) { 

	// Remove an agent from the specified container

  MMTreeModel model = (MMTreeModel)tree.getModel();
  DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

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


/**
 * Removes a container from the selection tree
 *
 * @param name name of the container to be removed
 */
public void removeContainer(String name) { 

	// Remove a container from the tree model
  MMTreeModel model = (MMTreeModel)tree.getModel();
  DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
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


/**
 * Inner class that handled mouse events for the selection tree
 *
 * @see java.awt.event.MouseAdapter  
 */
public class DoubleClicker extends MouseAdapter{  
  public void mouseClicked (MouseEvent e){
    	
    boolean leftButtonPressed;
    
    /* La seguente riga setta a 1 leftButtonPressed solo se e' stato premuto il tasto 
       sinistro del mouse */
    leftButtonPressed = e.getModifiers() == InputEvent.BUTTON1_MASK; //by BENNY
    
    if (e.isPopupTrigger())
      System.out.println("PopUp Go!");
    
    if ((!e.isPopupTrigger()) && leftButtonPressed) // by BENNY
      
      if(e.getClickCount() == 2){
	
	JTree tree;
	Object item;
	TreePath path;
	TreeData node = null;
	Agent agent;
	
	tree = (JTree) e.getSource();
	path = tree.getPathForLocation(e.getX(), e.getY());
	
	if (path != null) { //by BENNY
	  item = path.getLastPathComponent();    
	  node = (TreeData) item;
	  //}
	  //aggiunge sul canvas solo gli agenti
	  if(node.isLeaf()==true) 
	    {   
	      int atPos = node.getName().indexOf("@"); //by BENNY
	      if ((atPos == -1)&&(MMAbstractAction.canvasAgent.isPresent(node.getName())==false)
		  ||
		  (atPos != -1)&&(MMAbstractAction.canvasAgent.isPresent(node.getName().substring(0,atPos))==false))
		{ 
		  
		  if ( atPos == -1 )
		    agent = new Agent(node.name);
		  else
		    agent = new Agent(node.name.substring(0,atPos));							
		  MMAbstractAction.canvasAgent.addAgent(agent);
		  Vector v = new Vector(1); //just one element in the vector. sniffMsg method requires a Vector as a parameter
		  v.add(node);
		  SnifferGUI.sniffHandler.sniffMsg(v,Sniffer.SNIFF_ON);        
		}
	      else
		{ 
		  if ( atPos == -1 )					
		    MMAbstractAction.canvasAgent.removeAgent(node.getName());
		  else
		    MMAbstractAction.canvasAgent.removeAgent(node.getName().substring(0,atPos));
		  Vector v = new Vector(1); //just one element in the vector. sniffMsg method requires a Vector as a parameter
		  v.add(node);
		  SnifferGUI.sniffHandler.sniffMsg(v,Sniffer.SNIFF_OFF);       
		}
	    }
	}
	tree.scrollPathToVisible(path);
      }
    
		
		
	} //end of mouseClicked
} // end of inner class


	
} // end of MMTree class
