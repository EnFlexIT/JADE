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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.table.*;

import jade.lang.acl.ACLMessage;

/**
   The panel containing the message tables.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
*/
public class MessagePanel extends JSplitPane {
  private JScrollPane inScroll;
  private JScrollPane outScroll;
  private JTable inMessage;
  private JTable outMessage;
  private MessageTableModel inModel,outModel;
  private TableMouseListener listener;

  public MessagePanel(MessageTableModel in,MessageTableModel out) {
    super();
    inModel =in;
    outModel=out;
    inMessage=new JTable(inModel);
    outMessage=new JTable(outModel);
    inMessage.setName("IN");
    outMessage.setName("OUT");
    inMessage.setDefaultRenderer(ACLMessage.class, new ACLMessageRenderer());
    outMessage.setDefaultRenderer(ACLMessage.class, new ACLMessageRenderer());
    listener = new TableMouseListener();
    inMessage.addMouseListener(listener);
    outMessage.addMouseListener(listener);
    build();
  }

  public void build() {
    inScroll=new JScrollPane();
    inScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    inScroll.setAutoscrolls(true);

    outScroll=new JScrollPane();
    outScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    outScroll.setAutoscrolls(true);

    inScroll.getViewport().add(inMessage, null);
    outScroll.getViewport().add(outMessage, null);

    this.add(inScroll, JSplitPane.LEFT);
    this.add(outScroll, JSplitPane.RIGHT);
    this.setDividerSize(2);
    this.setDividerLocation(200);
  }

  public MessageTableModel getModelIn() {
    return inModel;
  }

  public MessageTableModel getModelOut() {
    return outModel;
  }

  private static class ACLMessageRenderer extends JLabel implements TableCellRenderer {

    private Icon myIcon;

    public ACLMessageRenderer() {
      setHorizontalAlignment(SwingConstants.LEFT);
      setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
      String path =  "/jade/tools/introspector/gui/images/aclMessage.gif";
      myIcon = new ImageIcon(getClass().getResource(path));
      setIcon(myIcon);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      int height = myIcon.getIconHeight();
      if(table.getRowHeight() != height)
	table.setRowHeight(height);
      ACLMessage msg = (ACLMessage)value;
      if(isSelected)
	setBackground(Color.cyan);
      else
	setBackground(table.getBackground());
      setText(ACLMessage.getPerformative(msg.getPerformative()));
      return this;
    }

  }

}


