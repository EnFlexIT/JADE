/*
  $Log$
  Revision 1.6  1998/11/09 00:28:25  rimassa
  Some minor stylistic changes.

  Revision 1.5  1998/11/01 14:57:25  rimassa
  Changed code indentation to comply with JADE style.

  Revision 1.4  1998/10/26 00:09:50  rimassa
  Removed a message printed on standard output.

  Revision 1.3  1998/10/10 19:37:02  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import com.sun.java.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.*;
import java.util.Vector;

/** 
    AMSAbstractAction is the superclass of the actions 
    performed by AMSGui Controls 
    (AMSToolBar, AMSMenu, AMSPopupMenu, AMSTree)
    This class is abstract because it does not define the
    ActionPerformed(ActionEvent evt) method. In every subClass of 
    AMSAbstractAction this method performs a specific action and updates 
    the GUI.

    Subclasses of AMSAbstractAction are: 
    @see  jade.gui.AddAgentPlatformAction 
    @see  jade.gui.CustomActionAction 
    @see  jade.gui.ExitAction 
    @see  jade.gui.getPropertiesAction 
    @see  jade.gui.KillAction 
    @see  jade.gui.OpenScriptFileAction 
    @see  jade.gui.PingAction 
    @see  jade.gui.ReloadAction 
    @see  jade.gui.RemoveAction 
    @see  jade.gui.ResumeAction 
    @see  jade.gui.SnifferAction 
    @see  jade.gui.StartAction 
    @see  jade.gui.StartNewAgentAction 
    @see  jade.gui.SuspendAction 


**/

public abstract class AMSAbstractAction extends AbstractAction {
  /**
     The Icon representing the action in the Toolbar 
  */
  protected Icon img;

  /**
     The Action Name is also the ToolTip of the button in the AMSToolBar
  */

  protected String ActionName = "Action";
  protected Component parent;

  /**
     Listeners of this action. Listeners are ALL the items selected by the user 
     in the AMSTree. When an action is fired from the GUI it is notified
     to ALL the nodes selected in the tree.
  */
  protected static Vector listeners;

  /**
     The tree on which we work
  */
  protected static AMSTree tree;

  public AMSAbstractAction(String IconKey,String ActionName, Vector listeners) {
    this.img = GuiProperties.getIcon("AMSAbstractAction."+IconKey);
    this.ActionName = ActionName;
    this.listeners = listeners;

    putValue(Action.SMALL_ICON,img);
    putValue(Action.DEFAULT,img);
    putValue(Action.NAME,ActionName);
  }

  public AMSAbstractAction (String IconPath,String ActionName) {
    this(IconPath,ActionName,new Vector());
  }

  public String getActionName() {
    return ActionName;
  }

  /**
     Set ALL Listeners
     @param listenersP the new vector of listeners 
  */
  public synchronized static void setListeners(Object[] listenersP) {
    listeners.removeAllElements();
    for (int i=0;i<listenersP.length;i++)
      listeners.addElement(listenersP[i]);
  }

  public synchronized static void removeAllListeners() {
    listeners.removeAllElements();
  }

  /** 
      Add a listener to the action
      @param current the new listener
  */
  public synchronized static void AddListener(Object current) {
    listeners.addElement(current);
  }

  public synchronized static void removeListener (Object current) {
    listeners.removeElement (current);
  }

  /**
     @return ALL listeners for this action
  */
  public synchronized static Vector getAllListeners() {
    return listeners;
  }

  public synchronized static Object getLast() {
    return listeners.lastElement();
  }

  public synchronized void setIcon (Icon i) {
    img = i;
  }


  public synchronized static Object getFirst() {
    return listeners.firstElement();
  }


  /**
     sets the Tree on which we work
     @param treeP the AMSTree 
  */
  public static void setTree (AMSTree treeP) {	
    tree = treeP;
  }

}

