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

package  jade.tools.introspector.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


/**
   @author Andrea Squeri, -  Universita` di Parma
*/
public class AboutBox extends JDialog {
  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();
  JButton button1 = new JButton();
  Border border1;
  JPanel jPanel1 = new JPanel();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  GridLayout gridLayout1 = new GridLayout();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JLabel title = new JLabel();
  JLabel version = new JLabel();
  JLabel author = new JLabel();
  JLabel nome = new JLabel();
  JLabel mail = new JLabel();
  JLabel indirizzo = new JLabel();

  public AboutBox(Frame frame, String title, boolean modal) {
    super(frame, title, modal);
    try {
      jbInit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    pack();
  }

  public AboutBox(Frame frame, String title) {
    this(frame, title, false);
  }

  public AboutBox(Frame frame) {
    this(frame, "", false);
  }

  private void jbInit() throws Exception {
    border1 = BorderFactory.createRaisedBevelBorder();
    jPanel1.setLayout(gridLayout1);
    panel2.setBorder(border1);
    panel2.setLayout(gridBagLayout2);
    button1.setText("OK");
    button1.addActionListener(new AboutBox_button1_actionAdapter(this));
    gridLayout1.setHgap(4);
    this.addWindowListener(new AboutBox_this_windowAdapter(this));
    panel1.setLayout(gridBagLayout1);
    title.setFont(new java.awt.Font("Dialog", 1, 20));
    title.setText("JADE DEBUGGER");
    version.setFont(new java.awt.Font("Dialog", 0, 15));
    version.setText("VERSION 1.0");
    author.setText("Author :");
    nome.setText("Squeri Andrea");
    mail.setText("e-mail :");
    indirizzo.setText("squeri@CE.UniPr.it");
    panel1.add(panel2, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    panel2.add(title, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel2.add(version, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel2.add(author, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel2.add(nome, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel2.add(mail, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel2.add(indirizzo, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    panel1.add(jPanel1, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(4, 8, 4, 8), 0, 0));
    jPanel1.add(button1, null);
    getContentPane().add(panel1);
  }

  // OK
  void button1_actionPerformed(ActionEvent e) {
    dispose();
  }


  void this_windowClosing(WindowEvent e) {
    dispose();
  }

  void showCorrect(){
    Rectangle r=this.getParent().getBounds();
    Point p=r.getLocation();
    double width=r.getWidth();
    double height=r.getHeight();
    double myX= this.getSize().getWidth();
    double myY=this.getSize().getHeight();
    int x=(int)(((width/2)-(myX/2)));
    int y=(int)(((height/2)-(myY/2)));
    this.setLocation(x,y);
    this.setVisible(true);

  }
}

class AboutBox_button1_actionAdapter implements ActionListener {
  AboutBox adaptee;

  AboutBox_button1_actionAdapter(AboutBox adaptee) {
    this.adaptee = adaptee;
  }

  public void actionPerformed(ActionEvent e) {
    adaptee.button1_actionPerformed(e);
  }
}


class AboutBox_this_windowAdapter extends WindowAdapter {
  AboutBox adaptee;

  AboutBox_this_windowAdapter(AboutBox adaptee) {
    this.adaptee = adaptee;
  }

  public void windowClosing(WindowEvent e) {
    adaptee.this_windowClosing(e);
  }
}

