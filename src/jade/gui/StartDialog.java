/*
  $Log$
  Revision 1.6  1999/02/04 14:47:31  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.5  1998/11/09 00:33:11  rimassa
  Changed dialog window size and some code indentation.

  Revision 1.4  1998/11/05 23:44:06  rimassa
  Dialog rewritten in order to make it comply with JADE agent
  descriptor; i.e. an Agent Container, an agent name and a class name.

  Revision 1.3  1998/10/10 19:37:25  rimassa
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

/**
 * The Dialog to set command line parameters
 * to start Agents. IT has many static attributes because
 * ONLY one Dialog of this kind can be shown at one moment
 */
public class StartDialog extends JDialog implements ActionListener {
  protected static JTextField agentName;
  protected static JTextField className;
  protected static JTextField container;

  protected static JLabel agentNameL= new JLabel("Agent Name");
  protected static JLabel classNameL= new JLabel("Class Name");
  protected static JLabel containerL= new JLabel("Container");

  protected static JButton OKButton = new JButton ("OK");
  protected static JButton CancelButton = new JButton ("Cancel");

  protected static JFrame frame = new JFrame ("Insert Start Parameters");

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

  protected StartDialog (String agentNameP) {
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

  /**
   * This method shows a modal Dialog
   * useful to set parameters to start
   * agents previously registered 
   */
  public static int showStartDialog(String agentNameP) {
    agentName.setEditable(false);
    StartDialog panel = new StartDialog(agentNameP);
    return choice;
  }

  /**
   * This method shows a modal Dialog
   * useful to set parameters to start
   * new agents  
   */
  public static int showStartNewDialog(String containerName) {
    agentName.setEditable(true);
    if(containerName == null) {
      container.setEditable(true);
      setContainer("");
    }
    else {
      setContainer(containerName);
      container.setEditable(false);
    }

    StartDialog panel = new StartDialog("New Agent");
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


}
