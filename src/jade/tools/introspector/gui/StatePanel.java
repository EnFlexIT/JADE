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
import java.awt.*;
import java.util.Vector;


/**
   This is a split pane, with a left-side part containing state
   buttons and a right-side part containing some action buttons. It
   allows to manage the state of an agent.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma

*/
public class StatePanel extends JSplitPane{
  private int state;
  private GridBagConstraints cons;
  private JPanel viewPanel;
  private JPanel setPanel;
  private JScrollPane scrollPane1;
  private JScrollPane scrollPane2;
  private JButton waitingLed;
  private JButton activeLed;
  private JButton suspendedLed;
  private JButton deletedLed;
  private JButton moveLed;
  private JButton initiatedLed;
  private JLabel waitingLabel;
  private JLabel activeLabel;
  private JLabel suspendedLabel;
  private JLabel deletedLabel;
  private JLabel moveLabel;
  private JLabel initiatedLabel;
  private JButton suspendAction;
  private JButton waitAction;
  private JButton wakeUpAction;
  private JButton killAction;
  private ImageIcon active;

  private MainBarListener listener;
  private Vector leds;
  private Vector labels;


  public StatePanel(MainBarListener list){
    super();
    leds=new Vector();
    labels=new Vector();
    cons =new GridBagConstraints();
    listener = list;
    build();
  }

  public void build(){
    viewPanel= new JPanel();
    setPanel= new JPanel();
    scrollPane1=new JScrollPane();
    scrollPane2=new JScrollPane();


    waitingLed=new JButton();
    activeLed=new JButton();
    suspendedLed=new JButton();
    deletedLed=new JButton();
    initiatedLed=new JButton();
    moveLed=new JButton();

    waitingLabel=new JLabel("waiting");
    activeLabel=new JLabel("active");
    suspendedLabel=new JLabel("suspended");
    deletedLabel=new JLabel("deleted");
    initiatedLabel=new JLabel("initiated");
    moveLabel=new JLabel("move");

    leds.add(initiatedLed);
    leds.add(activeLed);
    leds.add(suspendedLed);
    leds.add(waitingLed);
    leds.add(deletedLed);
    leds.add(moveLed);


    for(int i=0;i<leds.size();i++){
      JButton b= (JButton)leds.elementAt(i);
      b.setPreferredSize(new Dimension(30,30));
      b.setMaximumSize(new Dimension(30,30));

    }

    Font f = new Font("Monospaced",0,8);
    for(int i=0;i<labels.size();i++){
      JLabel b= (JLabel)labels.elementAt(i);
      //b.setPreferredSize(new Dimension(5,20));
      //b.setMaximumSize(new Dimension(5,20));
      b.setFont(f);
    }

    suspendAction=new JButton("Suspend");
    suspendAction.setFont(f);
    suspendAction.addActionListener(listener);
    suspendAction.setMnemonic(5);


    waitAction=new JButton("Wait");
    waitAction.setFont(f);
    waitAction.addActionListener(listener);
    waitAction.setMnemonic(7);


    wakeUpAction=new JButton("WakeUp");
    wakeUpAction.setFont(f);
    wakeUpAction.addActionListener(listener);
    wakeUpAction.setMnemonic(6);


    killAction=new JButton("Kill");
    killAction.setFont(f);
    killAction.addActionListener(listener);
    killAction.setMnemonic(4);


    viewPanel.setLayout(new GridBagLayout());

    cons.gridx=0;
    cons.gridy=0;
    viewPanel.add(initiatedLed,cons);

    cons.gridx=1;
    viewPanel.add(initiatedLabel,cons);

    cons.gridx=2;
    viewPanel.add(activeLed,cons);
    cons.gridx=3;
    viewPanel.add(activeLabel,cons);

    cons.gridy =1;
    cons.gridx =0;

    viewPanel.add(waitingLed,cons);

    cons.gridx=1;
    viewPanel.add(waitingLabel,cons);

    cons.gridx=2;
    viewPanel.add(suspendedLed,cons);

    cons.gridx=3;
    viewPanel.add(suspendedLabel,cons);

    cons.gridy =2;
    cons.gridx =0;


    viewPanel.add(moveLed,cons);

    cons.gridx=1;
    viewPanel.add(moveLabel,cons);

    cons.gridx=2;
    viewPanel.add(deletedLed,cons);

    cons.gridx=3;
    viewPanel.add(deletedLabel,cons);

    setPanel.setLayout(new GridLayout(2,2,3,3));
    setPanel.add(suspendAction,null);
    setPanel.add(waitAction,null);
    setPanel.add(wakeUpAction,null);
    setPanel.add(killAction,null);

    scrollPane1.getViewport().add(viewPanel);
    scrollPane2.getViewport().add(setPanel);

    this.setContinuousLayout(true);
    this.add(scrollPane1,JSplitPane.LEFT);
    this.add(scrollPane2,JSplitPane.RIGHT);

  }
  public Vector getStateLeds(){
    return leds;
  }

}
