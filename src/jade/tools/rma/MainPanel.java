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
import javax.swing.tree.TreePath;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import jade.gui.AgentTree;
import jade.gui.APDescriptionPanel;	
import jade.domain.FIPAAgentManagement.APDescription;

/**
   
   @author Francisco Regi, Andrea Soracchi - Universita` di Parma
   @version $Date$ $Revision$
 */
class MainPanel extends JPanel implements  TreeSelectionListener
{

  private APDescriptionPanel APDescription_panel;
  AgentTree treeAgent;       // FIXME: It should be private
  private TablePanel table;
  private JScrollPane scroll;
  private JSplitPane pan;
  private JSplitPane pane;
  private MainWindow mainWnd;
  private PopupMouser popM;

  
  	
  public MainPanel(rma anRMA, MainWindow mainWnd) {
   
  	table = new TablePanel();
    this.mainWnd = mainWnd;
    Font f;
    f = new Font("SanSerif",Font.PLAIN,14);
    setFont(f);
    setLayout(new BorderLayout(10,10));

    treeAgent = new AgentTree(f);
    
    //To allow single selection on the tree.
    //  treeAgent.tree.getSelectionModel().setSelectionMode
    //  (javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
   
    pan = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,treeAgent.tree,table.createTable());
    add(pan);
   
    treeAgent.listenerTree(this);
    popM=new PopupMouser(treeAgent.tree,treeAgent);
    treeAgent.tree.addMouseListener(popM);

  }

 public void  valueChanged(TreeSelectionEvent e) {
   String wholePath;
   String typeNode;
   TreePath paths[] = treeAgent.tree.getSelectionPaths();
   AgentTree.Node current;
   AgentTree.ContainerNode currentC;
   Object[] relCur;
   
   if (paths!=null) {
    current=(AgentTree.Node)paths[0].getLastPathComponent();
     int numPaths=paths.length;
     //selArea.setText(" ");
      
     /*OLd version to show the path of the agent.
     for(int i=0;i<numPaths;i++) {
       relCur= paths[i].getPath();
        wholePath="";
        for (int j=0;j<relCur.length;j++) {
         if (relCur[j] instanceof AgentTree.Node) {
           current=(AgentTree.Node)relCur[j];
           wholePath=wholePath.concat(current.getName()+".");
         }
        }
       selArea.append(wholePath+" \n");
     }*/ 	
     
     java.util.ArrayList agentPaths = new java.util.ArrayList();
     for(int i=0; i<numPaths;i++)
     {
     	relCur = paths[i].getPath();
     	for(int j=0;j<relCur.length;j++)
     	{
     		if (relCur[j] instanceof AgentTree.AgentNode)
     	  	//to display in the table only the agent.
     			agentPaths.add(paths[i]);	
     	  else
     	  if (relCur[j] instanceof AgentTree.SuperContainer)
     	  	{//show the APDescription in the TextArea
     	  	}
     	}
     }
     
     //table.setData(paths);
     TreePath[] agents = new TreePath [agentPaths.size()];
     for(int i= 0;i<agentPaths.size(); i++)
     	agents[i]=(TreePath)agentPaths.get(i);
     table.setData(agents);
   }
 }


  public void adjustDividersLocation() {
    //int rootSize = pane.getDividerLocation(); // This is the height of a single tree folder
    //pane.setDividerLocation(7*rootSize); // The initial agent tree has 6 elements; one more empty space
    pan.setDividerLocation(300);
   
    
  }

  public Dimension getPreferredSize() {
    return new Dimension(200, 200);
  }
  
  
  
  
} 