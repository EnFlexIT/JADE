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

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
   This panel contains the behaviour tree for a given agent. It adds a
   TreeMouseListener component to the tree.

   @author Andrea Squeri, -  Universita` di Parma
*/
public class BehaviourPanel extends JSplitPane{
  private JTree behaviourTree;
  private JTextArea text;
  private JScrollPane behaviourScroll;
  private JScrollPane textScroll;
  private JPanel treePanel;
  private TreeMouseListener treeListener;

  public BehaviourPanel(DefaultTreeModel model ){
    super();
    behaviourTree=new JTree(model);
    treeListener = new TreeMouseListener(this);
    behaviourTree.addMouseListener(treeListener);
    this.build();
  }

  public void build(){

    text=new JTextArea();
    behaviourScroll=new JScrollPane();
    textScroll=new JScrollPane();
    treePanel=new JPanel();

    treePanel.setLayout(new BorderLayout());

    //behaviorTree.addMouseListener(new TreeListener);
    behaviourTree.putClientProperty("JTree.lineStyle","Angled");
    behaviourTree.setShowsRootHandles(true);

    treePanel.add(behaviourTree,BorderLayout.CENTER);

    behaviourScroll.getViewport().add(treePanel,null);
    textScroll.getViewport().add(text,null);

   // behaviourScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    //textScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    this.add(behaviourScroll,JSplitPane.LEFT);
    this.add(textScroll,JSplitPane.RIGHT);

    this.setContinuousLayout(true);
    this.setDividerLocation(200);

  }

  public JTree getBehaviourTree(){
    return behaviourTree;
  }

  public JTextArea getBehaviourText(){
    return text;
  }

}
