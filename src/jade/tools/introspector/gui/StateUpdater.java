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


import javax.swing.JButton;
import java.util.Vector;
import java.awt.Color;
import javax.swing.ImageIcon;

import jade.domain.introspection.ChangedAgentState;

/**

   Handles a ChangedAgentState event and updates the correct button in
   the interface.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma

*/
public class StateUpdater implements Runnable {
  private int state;
  private StatePanel gui;
  private ImageIcon active;

  public StateUpdater(ChangedAgentState cae, StatePanel gui) {
    state = se.getState().intValue();
    this.gui = gui;
    active = new ImageIcon("sveglia.gif");
  }
  public void run(){

    Vector leds = gui.getStateLeds();
    JButton b = null;
    for (int i=0;i<leds.size();i++){
      b=(JButton)leds.elementAt(i);
      if(b.getIcon()!=null ) b.setIcon(null);
    }
    b=(JButton)leds.elementAt(--state);
    b.setIcon(active);
  }
}
