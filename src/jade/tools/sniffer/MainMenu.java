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


package jade.tools.sniffer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.Icon;
import javax.swing.JMenuBar;
import javax.swing.JFrame;
import jade.gui.AboutJadeAction;

    /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date$ $Revision$
   */

   /**
    * Sets up the menu bar and the relative menus
    */

public class MainMenu extends JMenuBar{

 private JMenuItem tmp;
 private ActionProcessor actPro;
 private JMenu menu;

 void paintM(boolean enable,SnifferAction obj){
    tmp = menu.add(obj);
    tmp.setEnabled(enable);
 }

 public MainMenu(MainWindow mainWnd,ActionProcessor actPro) {
   super();
   this.actPro=actPro;

   menu = new JMenu ("Actions");
   paintM(true,(SnifferAction)actPro.actions.get(actPro.DO_SNIFFER_ACTION));
   paintM(true,(SnifferAction)actPro.actions.get(actPro.DO_NOT_SNIFFER_ACTION));
   paintM(true,(SnifferAction)actPro.actions.get(actPro.SWOW_ONLY_ACTION));
   menu.addSeparator();

   paintM(true,(SnifferAction)actPro.actions.get(actPro. CLEARCANVAS_ACTION));

   menu.addSeparator();

   paintM(true,(SnifferAction)actPro.actions.get(actPro.DISPLAYLOGFILE_ACTION));
   paintM(true,(SnifferAction)actPro.actions.get(actPro.WRITELOGFILE_ACTION));
   paintM(true,(SnifferAction)actPro.actions.get(actPro.WRITEMESSAGELIST_ACTION));

   menu.addSeparator();

   paintM(true,(SnifferAction)actPro.actions.get(actPro. EXIT_SNIFFER_ACTION));
   add(menu);

   menu = new JMenu ("About");
   menu.add(new AboutJadeAction((JFrame)mainWnd));
   menu.add(new AboutBoxAction((JFrame)mainWnd));
   add(menu);
  }

} 