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

import java.util.Map;
import java.util.HashMap;

import javax.swing.*;

import jade.core.AgentState;

/**
   This is a split pane, with a left-side part containing state
   buttons and a right-side part containing some action buttons. It
   allows to manage the state of an agent.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma

*/
public class StatePanel extends JSplitPane {
  private int state;
  private GridBagConstraints cons;
  private JPanel viewPanel;
  private JPanel setPanel;
  private JScrollPane scrollPane1;
  private JScrollPane scrollPane2;
  private ButtonGroup leds;
  private JRadioButton waitingLed;
  private JRadioButton activeLed;
  private JRadioButton suspendedLed;
  private JRadioButton deletedLed;
  private JRadioButton moveLed;
  private JRadioButton initiatedLed;
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

  private Map ledMap = new HashMap();

  public StatePanel(MainBarListener list){
    super();
    leds = new ButtonGroup();
    cons =new GridBagConstraints();
    listener = list;
    build();
  }

  public void build(){
    viewPanel= new JPanel();
    setPanel= new JPanel();
    scrollPane1=new JScrollPane();
    scrollPane2=new JScrollPane();

    Icon ledOff = new ImageIcon(getClass().getResource("images/rbrs.gif"));
    Icon ledOn = new ImageIcon(getClass().getResource("images/rbs.gif"));

    waitingLed = new JRadioButton(ledOff);
    waitingLed.setDisabledSelectedIcon(ledOn);
    waitingLed.setDisabledIcon(ledOff);
    waitingLed.setEnabled(false);
    leds.add(waitingLed);
    ledMap.put(new AgentState("Waiting"), waitingLed);

    activeLed = new JRadioButton(ledOff);
    activeLed.setDisabledSelectedIcon(ledOn);
    activeLed.setDisabledIcon(ledOff);
    activeLed.setEnabled(false);
    leds.add(activeLed);
    ledMap.put(new AgentState("Active"), activeLed);

    suspendedLed = new JRadioButton(ledOff);
    suspendedLed.setDisabledSelectedIcon(ledOn);
    suspendedLed.setDisabledIcon(ledOff);
    suspendedLed.setEnabled(false);
    leds.add(suspendedLed);
    ledMap.put(new AgentState("Suspended"), suspendedLed);

    deletedLed = new JRadioButton(ledOff);
    deletedLed.setDisabledSelectedIcon(ledOn);
    deletedLed.setDisabledIcon(ledOff);
    deletedLed.setEnabled(false);
    leds.add(deletedLed);
    ledMap.put(new AgentState("Deleted"), deletedLed);

    initiatedLed = new JRadioButton(ledOff);
    initiatedLed.setDisabledSelectedIcon(ledOn);
    initiatedLed.setDisabledIcon(ledOff);
    initiatedLed.setEnabled(false);
    leds.add(initiatedLed);
    ledMap.put(new AgentState("Initiated"), initiatedLed);

    moveLed = new JRadioButton(ledOff);
    moveLed.setDisabledSelectedIcon(ledOn);
    moveLed.setDisabledIcon(ledOff);
    moveLed.setEnabled(false);
    leds.add(moveLed);
    ledMap.put(new AgentState("Transit"), moveLed);

    Font f = new Font("Monospaced",0,8);

    waitingLabel=new JLabel("waiting");
    waitingLabel.setFont(f);
    activeLabel=new JLabel("active");
    activeLabel.setFont(f);
    suspendedLabel=new JLabel("suspended");
    suspendedLabel.setFont(f);
    deletedLabel=new JLabel("deleted");
    deletedLabel.setFont(f);
    initiatedLabel=new JLabel("initiated");
    initiatedLabel.setFont(f);
    moveLabel=new JLabel("move");
    moveLabel.setFont(f);


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

    viewPanel.setMinimumSize(viewPanel.getPreferredSize());
    viewPanel.setMaximumSize(viewPanel.getPreferredSize());

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

  public void switchTo(AgentState as) {
    JRadioButton led = (JRadioButton)ledMap.get(as);
    if(led != null)
      led.setSelected(true);
  }

}
