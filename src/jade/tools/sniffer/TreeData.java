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
import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.tree.TreePath;
import java.io.Serializable;


/**
 * Class TreeData represents Agents, Containers and Agent-platforms 
 * in the Gui. The Object "TreeData" is a local representation of
 * all the knowledge we have about an agent
 * TreeData are FSM with only two states (RUNNING and SUSPENDED) and with four 
 * different levels (SUPER_NODE, AGENT_PLATFORM, CONTAINER, AGENT). The graphic 
 * representation of the state is given only for Agents, but Platforms and containers
 * have their own state, too.
 * The method loadChildren() must be rewritten to work properly 
 * 
 * @author Gianluca Tanca
 * @version $Date$ $Revision$
 */

public class TreeData extends DefaultMutableTreeNode implements Serializable{
	
	public static final int SUSPENDED = 0;
	public static final int RUNNING   = 1;

	public static final String AddSeparator = "";

	public static final int SUPER_NODE     = 0;
	public static final int AGENT_PLATFORM = 1;
	public static final int CONTAINER      = 2;
	public static final int	AGENT          = 3;

	protected static String ContainerToolTip = "Agents Container";
	protected static String SuperNodeToolTip = "Agent Management System GUI";

	protected static String[] statesNames;
	protected static String[] LevelsNames;
	protected static Icon[] icons;
	
  protected String name	   = "Name";
	protected String type	   = "Type";
	
	protected int currentState = RUNNING;
	protected int Level		   = AGENT; 
	protected String[] addresses ;
	protected boolean hasLoaded = false;	

	static 
	{
		statesNames = new String[2];
		LevelsNames = new String [4];
		icons = new Icon[3];

		statesNames[SUSPENDED]="Suspended";
		statesNames[RUNNING]="Running";

		LevelsNames[SUPER_NODE] ="AMS Level";
		LevelsNames[AGENT_PLATFORM]="Agent Platform";
		LevelsNames[CONTAINER]="Container";
		LevelsNames[AGENT]="Agent";

		
		icons[SUSPENDED]=GuiProperties.getIcon("TreeData.SuspendedIcon");
		icons[RUNNING]=GuiProperties.getIcon("TreeData.RunningIcon");
		icons[2]=GuiProperties.getIcon("TreeData.FolderIcon");
	}
	
/** 
 * Defines a Node with specific name, addresses, type and Level
 *
 * @param nameP name of the node
 * @param addressesP array of addresses
 * @param typeP type of the node
 * @param LevelP level of the node 
 */
public TreeData (String nameP, String[] addressesP,String typeP,int LevelP){

	name = nameP;
	if (addressesP == null){
		addresses = new String[1];
		addresses[0]="Address 1 : 200";
	}
	else 
		addresses = addressesP;
	type = typeP;
	Level = LevelP;
}
	
/** 
 * Defines a Node with specific name, addresses, type and Level
 *
 * @param nameP name of the node
 * @param addressesP array of addresses
 * @param typeP type of the node
 */
public TreeData (String nameP, String[] addressesP,String typeP){
	
  this(nameP,addressesP,typeP,AGENT);
}

/** 
 * Defines a Node with specific name, addresses, type and Level
 *
 * @param t name of the node
 * @param LevelP level of the node 
 */
public TreeData(String t,int LevelP){
  this(t,null,"",LevelP);
}
  
/** 
 * Defines a Agent with specific name
 *
 * @param t name of the node   
 */
public TreeData(String t){
  this(t,null,"",AGENT); 
}
    
/**
 * Returns an icon represententing the Level or the state if this is an Agent
 *
 * @return icon of the node
 */
public Icon getIcon(){
	if (Level==AGENT)
		return icons[currentState];
	else 
		return icons[2];
}
    
/**
 * Returns the name of the agent
 *
 * @return Name of the agent
 */
public String getName (){
	return name;
}

/**
 * Sets a name of a node
 *
 * @param nameP name of the node
 */
public void setName(String nameP){
	name = nameP;
}

/**
 * Sets the address of a node
 *
 * @param aP array of addresses for the node
 */
public void setAddresses (String[] aP){
	addresses = aP;
}

/**
 * Returns the addresses of a node as a string
 *
 * @return string of addresses of a node
 */	
public String getAddressesAsString(){
	String s=" ";
	for (int i=0;i<addresses.length;i++)
		s = s + addresses[i]+AddSeparator;
	return s;
}

/**
 * Returns the type of a node of a node
 *
 * @return type of the node
 */
public String getType(){
	return type;
}

/**
 * Sets the type of a node
 *
 * @param r type of the node
 */
public void setType (String r){
	type= r;
}

/**
 * Sets the state of an agent in a node
 *
 * @param state state of the agent
 */
public boolean setState (int state){
	if (state == SUSPENDED || state == RUNNING){
		currentState = state;
		return true;
	}
	else 
		return false;
}

/** 
 * forces the Agent to go from current state to the other state
 */
public void transState(){
	
	/* we have just two states, so we can work in base 2... */
	++currentState;
	currentState %= 2;
}

/**
 * Returns the state name of a node
 *
 * @return state name of the node
 */
public String getStateName (){
	return statesNames[currentState];
}

/**
 * Returns the level of a node
 *
 * @return level of the node
 */
public int getLevel (){
	return Level;
}

/**
 * Sets the level of a node
 *
 * @param levelP level of the node
 */
public boolean setLevel (int LevelP){
	if (LevelP >= AGENT_PLATFORM && LevelP<=AGENT){
		Level = LevelP;
		return true;
	}
	else 
		return false;
}


public String toString(){
		return  name+AddSeparator+type+AddSeparator;
}

/**
 * Returns the ToolTipText of a node
 *
 * @return ToolTipText of a node
 */
public String getToolTipText (){
	if (Level == AGENT || Level == AGENT_PLATFORM){
		return statesNames[currentState];
	}
	else if (Level == CONTAINER){
		return ContainerToolTip;
	}
	else 
		return SuperNodeToolTip;
}

/**
 * Adds an address to a node
 *
 * @param newAdd new address to add to a node
 */
public void addAddress(String newAdd){
	String s[] = new String [addresses.length+1];
	int i = 0;

	for (;i<addresses.length;i++)
		s[i]=addresses[i];
	s[i]=newAdd;
}

/** 
 * A node is a Leaf only if it is an agent
 */
public boolean isLeaf(){
	return (Level == AGENT);
}

/**
 * If the children have not yet been loaded, 
 * loadChildren is messaged and super is messaged for
 * the return value.
 */
public int getChildCount(){
	return super.getChildCount();
}

/**
 * Messaged the first time getChildCount is messaged.  Creates
 * children asking to the agent platform. This method loads
 * agents on the tree.
 */
protected void loadChildren(){
	for (int i=0;i<5;i++){
		/* this call of insert is just to fill the Tree in the 
		   example application. In the real application we have
			 to message the ams and asks it its own entities, and THEN
			 to create new nodes.*/
			 
		insert(new TreeData(LevelsNames[Level+1]+" Num. "+i,Level+1),i);
	}
	// insert e' un metodo della classe DefaultMutableTreeNode
	hasLoaded = true;
}

}