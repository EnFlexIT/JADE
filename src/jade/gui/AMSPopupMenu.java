/*
  $Log$
  Revision 1.6  1999/05/19 18:31:24  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.5  1999/02/04 14:47:25  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.4  1998/11/15 23:16:23  rimassa
  Changed code indentation to comply with JADE style.
  Modified some menu items to use KillAction() new constructor. Besides,
  arranged items enabling and disabling to allow killing both agents and
  agent containers.

  Revision 1.3  1998/10/10 19:37:05  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

import jade.domain.rma;

/**
 * The Context Menu of the GUI
 */

public class AMSPopupMenu extends JPopupMenu {
  PopupMenuListener parent;
  protected AMSAbstractAction act;
  protected PingAction ping = new PingAction();
  protected StartAction start = new StartAction();
  protected KillAction killAgent;
  protected KillAction killContainer;
  protected ResumeAction resume;
  protected CustomAction custom = new CustomAction();
  protected getPropertiesAction properties = new getPropertiesAction();
  protected SnifferAction sniffer = new SnifferAction();
  protected StartNewAgentAction NewAgent;
  protected SuspendAction suspend;
  protected AddAgentPlatformAction NewAgentPlatform = new AddAgentPlatformAction();
  private JMenu ActionsMenu;

  public AMSPopupMenu (PopupMenuListener parent, rma anRMA) {
    super();
    this.parent = parent;

    JMenu ActionsMenu = new JMenu ("Actions");
    JMenuItem tmp;

    tmp = add(NewAgentPlatform);
    tmp.setIcon(null);

    tmp = add(properties);			
    tmp.setIcon(null);

    NewAgent = new StartNewAgentAction(anRMA);
    tmp = ActionsMenu.add(NewAgent);
    tmp.setIcon(null);

    killContainer = new KillAction("Kill Selected Containers", anRMA);
    tmp = ActionsMenu.add(killContainer);
    tmp.setIcon(null);

    tmp = ActionsMenu.add(start);
    tmp.setIcon(null);

    killAgent = new KillAction("Kill Selected Agents", anRMA);		
    tmp = ActionsMenu.add(killAgent);
    tmp.setIcon(null);

    suspend = new SuspendAction (anRMA);
    tmp = ActionsMenu.add(suspend);
    tmp.setIcon(null);

    resume = new ResumeAction(anRMA);		
    tmp = ActionsMenu.add(resume);
    tmp.setIcon(null);
		
    tmp = ActionsMenu.add(ping);
    tmp.setIcon(null);
		
    tmp = ActionsMenu.add(custom);
    tmp.setIcon(null);
		
    tmp = ActionsMenu.add(sniffer);
    tmp.setIcon(null);
				
    addPopupMenuListener(parent);
    add(ActionsMenu);

  }

  /**
   * This method is called to enable and disable some actions 
   * depending from the item selected in the AMSTree
   @param Level is the level of the Node selected
  */ 

  public boolean setMenu(int Level) {		
    boolean flag = true;

    switch(Level) {
    case TreeData.AGENT:
      NewAgentPlatform.setEnabled(false);				
      NewAgent.setEnabled(false);
      killContainer.setEnabled(false);
      custom.setEnabled(true);
      resume.setEnabled(true);
      killAgent.setEnabled(true);
      ping.setEnabled(true);
      properties.setEnabled(true);
      sniffer.setEnabled(true);
      start.setEnabled(true);
      suspend.setEnabled(true);
      break;
    case TreeData.CONTAINER:
      NewAgentPlatform.setEnabled(false);
      NewAgent.setEnabled(true);
      killContainer.setEnabled(true);
      custom.setEnabled(false);
      resume.setEnabled(false);
      killAgent.setEnabled(false);
      ping.setEnabled(false);
      properties.setEnabled(false);
      sniffer.setEnabled(false);
      suspend.setEnabled(false);
      start.setEnabled(false);
      break;
    case TreeData.AGENT_PLATFORM:
      NewAgentPlatform.setEnabled(false);
      NewAgent.setEnabled(true);
      killContainer.setEnabled(false);
      start.setEnabled(false);
      resume.setEnabled(true);
      killAgent.setEnabled(true);
      custom.setEnabled(true);
      ping.setEnabled(true);
      properties.setEnabled(true);
      sniffer.setEnabled(true);
      suspend.setEnabled(true);
      NewAgentPlatform.setEnabled(false);
      break;
    default:
      NewAgentPlatform.setEnabled(true);
      properties.setEnabled(false);
      killContainer.setEnabled(false);
      NewAgent.setEnabled(false);
      custom.setEnabled(false);
      resume.setEnabled(false);
      killAgent.setEnabled(false);
      ping.setEnabled(false);
      properties.setEnabled(false);
      sniffer.setEnabled(false);
      suspend.setEnabled(false);
      start.setEnabled(false);
      break;
    }
    return flag;
  }

}

