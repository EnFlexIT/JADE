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
import javax.swing.*;
import javax.swing.border.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/**
 * The Menu of the GUI
 */
public class AMSMenu extends JMenuBar {

  public AMSMenu (rma anRMA, Frame mainWnd) {
    super();

    AMSAbstractAction act;

    JMenu menu = new JMenu ("File");
    JMenuItem tmp;
		
    act = new AddAgentPlatformAction();
    tmp = menu.add(act);
    tmp.setEnabled(false);
    tmp.setIcon(null);
		
    act = new OpenScriptFileAction();
    tmp = menu.add(act);
    tmp.setEnabled(false);
    tmp.setIcon(null);
    add(menu);

    tmp = new JMenuItem ("Execute Current Script");
    tmp.setEnabled(false);
    menu.add(tmp);

    act = new CloseRMAAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new ExitAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new ShutDownAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    add(menu);


    menu = new JMenu ("Actions");

    act = new StartNewAgentAction(anRMA, mainWnd);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new StartAction();
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new KillAction("Kill Selected Items", anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new SuspendAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new ResumeAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new PingAction();
    tmp = menu.add(act);
    tmp.setEnabled(false);
    tmp.setIcon(null);

    act = new CustomAction(anRMA, mainWnd);
    tmp = menu.add(act);
    tmp.setIcon(null);

    add(menu);

    // TOOLS MENU
    menu = new JMenu ("Tools");

    act = new SnifferAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new DummyAgentAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new ShowDFGuiAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    add(menu);

  }
}
