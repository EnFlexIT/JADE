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


package jade.tools.sniffer;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import java.awt.Font;
import jade.gui.AgentTree;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.Color;

  /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date$ $Revision$
 */

  /**
   * Sets the tree and the two canvas inside the MainWindow
   * @see jade.tools.sniffer.PanelCanvas
   */

public class MainPanel extends JPanel {
 protected AgentTree treeAgent;
 protected PanelCanvas panelcan;
 private JSplitPane pane;
 private PopupMouser popM;
 public JTextArea textArea;
 private Font font = new Font("Helvetica",Font.ITALIC,12);
 int pos;

 public MainPanel(Sniffer mySniffer,MainWindow mwnd) {
    Font f;
    f = new Font("SanSerif",Font.PLAIN,14);
    setFont(f);
    setLayout(new BorderLayout(10,10));

    treeAgent = new AgentTree(f);

    panelcan = new PanelCanvas(mwnd,this,mySniffer);
    pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,new JScrollPane(treeAgent.tree),new JScrollPane(panelcan));
    pane.setContinuousLayout(true);
    add(pane);
    textArea=new JTextArea();
    textArea.setBackground(Color.lightGray);
    textArea.setFont(font);
    textArea.setRows(1);
    textArea.setText("                                                                 No Message");
    textArea.setEditable(false);
    add(textArea,"South");
    popM=new PopupMouser(treeAgent.tree,treeAgent);
    treeAgent.tree.addMouseListener(popM);

 }

  public void adjustDividerLocation() {
    pane.setDividerLocation(0.3);
  }

} // End of MainPanel
