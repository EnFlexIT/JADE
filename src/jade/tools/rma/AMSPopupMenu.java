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

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

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
  protected CustomAction custom;
  protected getPropertiesAction properties = new getPropertiesAction();
  //  protected SnifferAction sniffer = new SnifferAction();
  protected StartNewAgentAction NewAgent;
  protected SuspendAction suspend;
  protected AddAgentPlatformAction NewAgentPlatform = new AddAgentPlatformAction();
  private JMenu ActionsMenu;

  public AMSPopupMenu (PopupMenuListener parent, rma anRMA, Frame mainWnd) {
    super();
    this.parent = parent;

    JMenu ActionsMenu = new JMenu ("Actions");
    JMenuItem tmp;

    tmp = add(NewAgentPlatform);
    tmp.setIcon(null);

    tmp = add(properties);
    tmp.setEnabled(false);
    tmp.setIcon(null);

    NewAgent = new StartNewAgentAction(anRMA, mainWnd);
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
    tmp.setEnabled(false);
    tmp.setIcon(null);

    custom = new CustomAction(anRMA, mainWnd);
    tmp = ActionsMenu.add(custom);
    tmp.setIcon(null);

    //    tmp = ActionsMenu.add(sniffer);
    //    tmp.setIcon(null);
				
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
      //      sniffer.setEnabled(true);
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
      //      sniffer.setEnabled(false);
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
      //      sniffer.setEnabled(true);
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
      //      sniffer.setEnabled(false);
      suspend.setEnabled(false);
      start.setEnabled(false);
      break;
    }
    return flag;
  }

}

