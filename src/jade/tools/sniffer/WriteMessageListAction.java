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

import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import java.util.Iterator;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Serializable;

   /**
   Javadoc documentation for the file
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   <Br>
   <a href="mailto:a_soracchi@libero.it"> Andrea Soracchi(e-mail) </a>
   @version $Date$ $Revision$
 */

 /**
 * Writes a text file with all sniffed messages showed on the Message Canvas. A dialog box
 * asks the user the name of the file.
 * @see jade.Sniffer.FixedAction
 * @see jade.Sniffer.AgentList
 * @see jade.Sniffer.MessageList
 */

 public class WriteMessageListAction extends FixedAction implements Serializable {

private PrintWriter out;
private MainPanel mainPanel;

 public WriteMessageListAction(ActionProcessor actPro,MainPanel mainPanel){
  	super ("MessageFileActionIcon","Write Message List File",actPro);
        this.mainPanel=mainPanel;
	}

  public void doAction(){
   try{
     JFileChooser fileDialog = new JFileChooser();
     int returnVal = fileDialog.showSaveDialog(null);
     if(returnVal == JFileChooser.APPROVE_OPTION){
       String fileName = fileDialog.getSelectedFile().getAbsolutePath();
       out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
       MessageList msgList = mainPanel.panelcan.canvMess.getMessageList();
       Iterator it = msgList.getMessages();
       while(it.hasNext()) {
	 Message curMsg = (Message)it.next();
	 out.println(curMsg.toString());
       }
      out.close();
      System.out.println("Message List File Written.");
   }
   } catch (Exception e){
        System.out.println("Error Writing List File:" + e);
     }
 }

} // End of class writeMessageListAction
