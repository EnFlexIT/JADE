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

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */
class StartDialog extends JDialog implements ActionListener{
  protected static JTextField agentName;
  protected static JTextField className;
  protected static JTextField container;

  protected static JLabel agentNameL= new JLabel("Agent Name");
  protected static JLabel classNameL= new JLabel("Class Name");
  protected static JLabel containerL= new JLabel("Container");

  protected static JButton OKButton = new JButton ("OK");
  protected static JButton CancelButton = new JButton ("Cancel");

  protected static String agentNameToolTip = "Name of the Agent to start";
  protected static String classNameToolTip = "Class Name of the Agent to start";
  protected static String containerToolTip = "Container on which the Agent will start";

  protected static String result  = "";
  protected static int OK_BUTTON = 0;
  protected static int CANCEL_BUTTON = 1;
  protected static int choice = CANCEL_BUTTON;

  static {

    agentName = new JTextField ();
    agentName.setEditable(false);
    agentName.setToolTipText(agentNameToolTip);
    agentNameL.setToolTipText(agentNameToolTip);

    className = new JTextField ("jade.core.Agent");
    className.setEditable(true);
    className.setToolTipText(classNameToolTip);
    classNameL.setToolTipText(classNameToolTip);

    container = new JTextField ("0");
    container.setEditable(true);
    container.setToolTipText(containerToolTip);
    containerL.setToolTipText(containerToolTip);

  }

  protected StartDialog (String agentNameP, Frame frame) {
    super(frame,"Insert Start Parameters",true);

    getContentPane().setLayout(new GridLayout(4,2));
    agentName.setText(agentNameP);

    getContentPane().add(agentNameL);
    getContentPane().add(agentName);

    getContentPane().add(classNameL);
    getContentPane().add(className);

    getContentPane().add(containerL);
    getContentPane().add(container);

    OKButton.addActionListener(this);
    CancelButton.addActionListener(this);

    getContentPane().add(OKButton);
    getContentPane().add(CancelButton);

    setSize(getPreferredSize());
    setLocation(frame.getX() + (frame.getWidth() - getWidth()) / 2, frame.getY() + (frame.getHeight() - getHeight()) / 2);
    setVisible(true);
  }

  public Dimension getPreferredSize () {
    return (new Dimension(450,150));
  }

  public void actionPerformed (ActionEvent evt) {
    choice = CANCEL_BUTTON;
    if (evt.getSource()==OKButton) {
      choice = OK_BUTTON;
    }
    dispose();
  }

  public static int showStartNewDialog(String containerName, Frame owner) {
    choice=CANCEL_BUTTON;
    agentName.setEditable(true);
    container.setEditable(false);
    setContainer(containerName);
    StartDialog panel = new StartDialog("", owner);
    return choice;
  }

  public static String getAgentName() {
    return agentName.getText();
  }

  public static String getClassName() {
    return className.getText();
  }

  public static String getContainer() {
    return container.getText();
  }

  public static void setAgentName(String agentNameP) {
    agentName.setText(agentNameP);
  }

  public static void setClassName(String classNameP) {
    className.setText(classNameP);
  }

  public static void setContainer(String containerP) {
    container.setText(containerP);
  }

} // End of class StartDialog
