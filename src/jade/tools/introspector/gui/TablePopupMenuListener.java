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
import java.awt.event.*;
import java.awt.EventQueue;

//jade import
import jade.lang.acl.ACLMessage;
import jade.gui.AclGui;

/**
   Listener class for the popup menu associated ti the message
   table. It allows to insert, remove and show a message in the
   message table. Presentely, only the 'view' option is implemented,
   because the 'add' and 'remove' option would need a modifications to
   the agent message queue class to be implemented.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
*/
public class TablePopupMenuListener implements ActionListener,Runnable {

  private boolean addMessage;
  private boolean viewMessage;
  private boolean removeMessage;
  private MessageTableModel model;
  private int selectedRow;


  public void actionPerformed(ActionEvent evt){
    JMenuItem mi=(JMenuItem)evt.getSource();
    TablePopupMenu tpm=(TablePopupMenu)mi.getParent();
    model=(MessageTableModel)(tpm.getTable().getModel());
    selectedRow=tpm.getTable().getSelectedRow();
    String name=((JMenuItem)evt.getSource()).getName();
    if (name.equals("add")) addMessage=true;
    else if(name.equals("view")) viewMessage=true;
    else if(name.equals("remove")) removeMessage=true;
    Thread td= new Thread(this);
    td.start();
  }



  public void run() {
    if (viewMessage) {
      if( (selectedRow >= 0) && (selectedRow < model.getRowCount()) ) {
	ACLMessage m = (ACLMessage)model.getValueAt(selectedRow,0);
        if(m != null) AclGui.showMsgInDialog(m, null);
      }
      viewMessage = false;
    }
    else if(addMessage) {
      ACLMessage m = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
      m = AclGui.editMsgInDialog(m,null);
      if(m != null) {
        EventQueue.invokeLater(new TableModifier(model, selectedRow, m));
      }
      addMessage = false;
    }
    else if(removeMessage) {
      EventQueue.invokeLater(new TableModifier(model, selectedRow, null));
      removeMessage = false;
    }
  }
}
