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

package jade.tools.introspector.gui;
import javax.swing.*;

/**
   The main menu bar.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
   @version $Date$ $Revision$
*/
public class MainBar extends JMenuBar {
  private JMenu fileMenu;
  private JMenu actionMenu;
  private JMenu viewMenu;

  private JCheckBoxMenuItem viewMessageItem;
  private JCheckBoxMenuItem vewBehaviourItem;
  private JMenuItem exitItem;
  private JMenuItem killItem;
  private JMenuItem suspendItem;
  private JMenuItem wakeUpItem;
  private JMenuItem waitItem;

  private MainBarListener listener;


  public MainBar(MainBarListener list) {
    super();
    listener=list;
    build();
  }

  public void build() {

    viewMenu=new JMenu("View");
    actionMenu= new JMenu("Action");

    viewMessageItem=new JCheckBoxMenuItem("View Messages");
    vewBehaviourItem=new JCheckBoxMenuItem("View Behaviours");
    viewMessageItem.setSelected(true);
    vewBehaviourItem.setSelected(true);


    killItem=new JMenuItem("Kill");
    suspendItem=new JMenuItem("suspend");
    wakeUpItem=new JMenuItem("WakeUp");
    waitItem=new JMenuItem("Wait");


    viewMessageItem.setMnemonic(2);
    vewBehaviourItem.setMnemonic(3);
    killItem.setMnemonic(4);
    suspendItem.setMnemonic(5);
    wakeUpItem.setMnemonic(6);
    waitItem.setMnemonic(7);



    viewMessageItem.addActionListener(listener);
    vewBehaviourItem.addActionListener(listener);
    killItem.addActionListener(listener);
    suspendItem.addActionListener(listener);
    wakeUpItem.addActionListener(listener);
    waitItem.addActionListener(listener);



    viewMenu.add(viewMessageItem);
    viewMenu.add(vewBehaviourItem);
    actionMenu.add(killItem);
    actionMenu.add(suspendItem);
    actionMenu.add(waitItem);
    actionMenu.add(wakeUpItem);


    this.add(viewMenu);
    this.add(actionMenu);

  }

}
