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


import java.awt.*;
import javax.swing.*;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Vector;

import jade.tools.introspector.Introspector;

/**
   Main Window class.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma

*/
public class MainWindow extends JInternalFrame implements InternalFrameListener
{

  private Introspector debugger;
  private JSplitPane splitPanel;
  private JTabbedPane tabPanel;
  private MainBar mainBar;
  private MessagePanel messagePanel;
  private StatePanel statePanel;
  private BehaviourPanel behaviourPanel;
  private MainBarListener list;

  public MainWindow(Introspector da,String title){
    super(title);
    debugger=da;
    MessageTableModel mi=new MessageTableModel(new Vector(),"MESSAGE IN");
    MessageTableModel mo=new MessageTableModel(new Vector(),"MESSAGE OUT");
    DefaultTreeModel r=new DefaultTreeModel(new DefaultMutableTreeNode("Behaviours"));
    int s=1;

    list=new MainBarListener(this);
    mainBar=new MainBar(list);
    messagePanel=new MessagePanel(mi,mo);
    statePanel=new StatePanel(list);
    behaviourPanel=new BehaviourPanel(r);
    splitPanel=new JSplitPane();
    tabPanel=new JTabbedPane();

    build();


  }

  public void build(){

    /*layout*/
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(splitPanel,BorderLayout.CENTER);

    splitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
    splitPanel.setContinuousLayout(true);

    this.setBehaviourPanelVisible(true);
    this.setStatePanelVisible(true);
    splitPanel.setDividerLocation(90);


    tabPanel.add(messagePanel,"Messages");
    tabPanel.add(statePanel,"State");


    this.setClosable(false);
    this.setIconifiable(true);
    this.setMaximizable(true);
    this.setResizable(true);
    this.setJMenuBar(mainBar);
    this.setBounds(35, 35, 450, 300);
  }

  public void setStatePanelVisible(boolean b){
    if(!b) splitPanel.remove(tabPanel);
    else splitPanel.add(tabPanel,JSplitPane.TOP);
  }

  public void setBehaviourPanelVisible(boolean b){
    if(!b) splitPanel.remove(behaviourPanel);
    else{
      splitPanel.add(behaviourPanel,JSplitPane.BOTTOM);
      splitPanel.setDividerLocation(
          splitPanel.getLastDividerLocation());
    }
  }

  public MessagePanel getMessagePanel(){
    return messagePanel;
  }
  public StatePanel getStatePanel(){
    return statePanel;
  }
  public BehaviourPanel getBehaviourPanel(){
    return behaviourPanel;
  }

  //inerface InternalFrameListener

  public void internalFrameActivated(InternalFrameEvent e){
    this.moveToFront();

  }
  public void internalFrameDeactivated(InternalFrameEvent e){}
  public void internalFrameClosed(InternalFrameEvent e){}
  public void internalFrameClosing(InternalFrameEvent e){}
  public void internalFrameIconified(InternalFrameEvent e){}
  public void internalFrameDeiconified(InternalFrameEvent e){}
  public void internalFrameOpened(InternalFrameEvent e){}
}

