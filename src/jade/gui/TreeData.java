/*
  $Log$
  Revision 1.6  1998/11/05 23:45:59  rimassa
  Changed Agent default state in the GUI from 'suspended' to 'active'.

  Revision 1.5  1998/11/03 00:43:25  rimassa
  Added automatic GUI tree updating in response to AMS 'inform' messages to
  Remote Management Agent.

  Revision 1.4  1998/11/01 14:56:03  rimassa
  Changed code indentation.
  Removed dummy insertion into Agent Platform Tree representation.

  Revision 1.3  1998/10/10 19:37:29  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import com.sun.java.swing.*;
import com.sun.java.swing.tree.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;


/**
 * Class TreeData represents Agents, Containers and Agent-platforms 
 * in the Gui. The Object "TreeData" is a local representation of
 * all the knowledge we have about an agent, and it exposes a lot of methods
 * because in the Gui it is shown in three different ways :
 * AMSTable, white pages, yellow pages,  and in the String representation
 * under the tree.
 * TreeData are FSM with only two states (RUNNING and SUSPENDED) and with four 
 * different levels (SUPER_NODE, AGENT_PLATFORM, CONTAINER, AGENT). The graphic 
 * representation of the state is given only for Agents, but Platforms and containers
 * have their own state, too.
 * The method loadChildren() must be rewritten to work properly 
 */

public class TreeData extends DefaultMutableTreeNode {
  public static final int SUSPENDED = 0;
  public static final int RUNNING   = 1;

  public static final String AddSeparator = "";

  public static final int SUPER_NODE = 0;
  public static final int AGENT_PLATFORM = 1;
  public static final int CONTAINER = 2;
  public static final int AGENT = 3;

  protected static String ContainerToolTip = "Agents Container";
  protected static String SuperNodeToolTip = "JADE Remote Management Agent GUI";

  protected static String[] statesNames;
  protected static String[] LevelsNames;
  protected static Icon[] icons;
	
  protected String name	= "Name";
  protected String type	= "Type";
	
  protected int currentState = RUNNING;
  protected int Level = AGENT; 
  protected String[] addresses ;
  protected boolean hasLoaded = false;	

  static {
    statesNames = new String[2];
    LevelsNames = new String [4];
    icons = new Icon[3];

    statesNames[SUSPENDED]="Suspended";
    statesNames[RUNNING]="Running";

    LevelsNames[SUPER_NODE] ="JADE";
    LevelsNames[AGENT_PLATFORM]="Agent Platform";
    LevelsNames[CONTAINER]="Container";
    LevelsNames[AGENT]="Agent";

    icons[SUSPENDED]=GuiProperties.getIcon("TreeData.SuspendedIcon");
    icons[RUNNING]=GuiProperties.getIcon("TreeData.RunningIcon");
    icons[2]=GuiProperties.getIcon("TreeData.FolderIcon");
  }

  /** 
   * Defines a Node with specific name, addresses, type and Level 
   */
  public TreeData (String nameP, String[] addressesP,String typeP,int LevelP) {

    name = nameP;
    if (addressesP == null) {
      addresses = new String[1];
      addresses[0]=" - ";
    }
    else addresses = addressesP;
    type = typeP;
    Level = LevelP;
  }

  /** 
   * Defines a Agent with specific name, addresses and type  
   */
  public TreeData (String nameP, String[] addressesP,String typeP) {
    this(nameP,addressesP,typeP,AGENT);
  }

  /** 
   * Defines a Node with specific name and Level  
   */
  public TreeData(String t,int LevelP) {
    this(t,null," - ",LevelP);
  }

  /** 
   * Defines a Agent with specific name   
   */
  public TreeData(String t) {
    this(t,null," - ",AGENT);
  }
    
  /**
   * Returns an icon represententing the Level or the state if this is 
   * an Agent
   */
  public Icon getIcon() {
    if (Level==AGENT)
      return icons[currentState];
    else return icons[2];
  }

  /**
   * @return Name of the agent
   */
  public String getName () {
    return name;
  }

  public void setName(String nameP) {
    name = nameP;
  }

  public void setAddresses (String[] aP) {
    addresses = aP;
  }
	
  public String getAddressesAsString() {
    String s=" ";
    for (int i=0;i<addresses.length;i++)
      s = s + addresses[i]+AddSeparator;
    return s;
  }

  public String getType() {
    return type;
  }

  public void setType (String r) {
    type= r;
  }

  public boolean setState (int state) {
    if (state == SUSPENDED || state == RUNNING) {
      currentState = state;
      return true;
    }
    else return false;
  }

  /** 
   * forces the Agent to go from current state to the other state
   */
  public void transState() {
    /* we have just two states, so we can work in base 2... */
    ++currentState;
    currentState %= 2;
  }

  public String getStateName () {
    return statesNames[currentState];
  }

  public int getLevel () {
    return Level;
  }

  public boolean setLevel (int LevelP) {
    if (LevelP >= AGENT_PLATFORM && LevelP<=AGENT) {
      Level = LevelP;
      return true;
    }
    else
      return false;
  }

  public String toString() {
    return  name+AddSeparator+type+AddSeparator+LevelsNames[Level];
  }

  public String getToolTipText () {
    if (Level == AGENT || Level == AGENT_PLATFORM) {
      return statesNames[currentState];
    }
    else if (Level == CONTAINER) {
      return ContainerToolTip;
    }
    else
      return SuperNodeToolTip;
  }

  public void addAddress(String newAdd) {
    String s[] = new String [addresses.length+1];
    int i = 0;

    for (;i<addresses.length;i++)
      s[i]=addresses[i];
    s[i]=newAdd;
  }

  /** 
   * A node is a Leaf only if it is an agent
   */
  public boolean isLeaf() {
    return (Level == AGENT);
  }

  /**
   * If the children have not yet been loaded, 
   * loadChildren is messaged and super is messaged for
   * the return value.
   */
  public int getChildCount() {
    if(!hasLoaded) {
      loadChildren();
    }
    return super.getChildCount();
  }

  /**
   * Messaged the first time getChildCount is messaged.  Creates
   * children asking to the agent platform. This method loads
   * agents on the tree.
   */
  protected void loadChildren() {

    // FIXME:  Chiedere allo AMS informazioni circa gli agenti

    hasLoaded = true;
  }
}
