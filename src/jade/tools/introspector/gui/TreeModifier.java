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


package jade.tools.introspection.gui;


import javax.swing.tree.*;
import javax.swing.JTree;

import jade.core.BehaviourID;

/**
   This class adds or removes behaviours from the agent behaviour
   tree.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
*/
public class TreeModifier implements Runnable {
  private JTree tree;
  private DefaultMutableTreeNode node;
  private boolean add;
  public TreeModifier(JTree tre, DefaultMutableTreeNode n, boolean add ) {
    tree=tre;
    node=n;
    this.add=add;
  }

  public void run(){
    boolean root=true;
    boolean complex=false;
    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
    if(add){
      DefaultMutableTreeNode parent=(DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
      if(!parent.isRoot()){
        BehaviourRapp rapp=(BehaviourRapp)parent.getUserObject();
        if(!rapp.getSimple().booleanValue()){
          complex =true;
        }
        root=false;
      }
      if(root || complex){
        int index=parent.getChildCount();
        model.insertNodeInto(node,parent,index);
      }
      /* code to remove it from the agent */
    }
    else{
      model.removeNodeFromParent(node);
    }
  }

}
