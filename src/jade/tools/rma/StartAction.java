/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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
import java.awt.event.*;
import java.awt.*;
import java.lang.*;
import java.util.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/**
 * StartAction starts all the selected agents in the tree
 * @see jade.gui.AMSAbstractAction
 */
public class StartAction extends AMSAbstractAction {
  public StartAction() {
    super ("StartActionIcon","Start Selected Agents");
  }

  public void actionPerformed(ActionEvent evt) {
    for (int i=0;(i<listeners.size() && listeners.elementAt(i) instanceof TreeData);i++)
      try {
	TreeData current = (TreeData)listeners.elementAt(i);
	//	StartDialog.setContainer(current.getAddressesAsString());
	//	StringTokenizer st = new StringTokenizer(current.getAddressesAsString(),":");

	//	StartDialog.setHost(st.nextToken());
	//	StartDialog.setPort(st.nextToken());

	int result = StartDialog.showStartDialog(listeners.elementAt(i).toString());

	if (result == StartDialog.OK_BUTTON) {
	  current.setState(TreeData.RUNNING);
	  String [] s = new String[1];
	  s[0] = StartDialog.getAgentName()+" : "+StartDialog.getClassName();
	  //	  current.setAddresses(s);
	  tree.repaint();
	}
      }
    catch (Exception e){
      e.printStackTrace();
    }
  }
}
