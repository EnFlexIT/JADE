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


package jade.tools.rma;

import java.awt.Frame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Icon;
import javax.swing.JMenuBar;
import javax.swing.JFrame;

import jade.gui.AboutJadeAction;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */
  class MainMenu extends JMenuBar {

  private ActionProcessor actPro;
  private RMAAction obj;
  private JMenu menu;
  private JMenuItem tmp;

  void paintM(boolean enable,Icon icon,RMAAction obj){
    tmp = menu.add(obj);
    tmp.setEnabled(enable);
    tmp.setIcon(icon);
  }
  public MainMenu (Frame mainWnd,ActionProcessor actPro) {

    super();
    this.actPro=actPro;

    menu = new JMenu ("File");
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.CLOSE_ACTION));
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.EXIT_ACTION));
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.SHUTDOWN_ACTION));
    add(menu);

    menu = new JMenu ("Actions");
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.START_ACTION));
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.KILL_ACTION));
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.SUSPEND_ACTION));
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.RESUME_ACTION));
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.CUSTOM_ACTION));
    add(menu);

    menu = new JMenu ("Tools");
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.SNIFFER_ACTION));
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.DUMMYAG_ACTION));
    paintM(true,null,(RMAAction)actPro.actions.get(actPro.SHOWDF_ACTION));
    add(menu);
    
    menu = new JMenu("Help");
    tmp = menu.add(new AboutJadeAction((JFrame)mainWnd));
    add(menu);

    // builds the popupmenu

  } // End Builder
}




