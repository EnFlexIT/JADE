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

import jade.tools.introspector.Introspector;
import jade.util.Sensor;
import jade.util.Event;

/**
   This class listens to the events fired by the main menu bar.

   @author Andrea Squeri,Corti Denis,Ballestracci Paolo -  Universita` di Parma
   @version $Date$ $Revision$
*/
public class MainBarListener implements ActionListener{
  private MainWindow mainWnd;
  private Sensor introspectorSensor;

  public MainBarListener(MainWindow main, Sensor s){
    mainWnd=main;
    introspectorSensor = s;
  }

  public void actionPerformed(ActionEvent e){
    AbstractButton source=(AbstractButton) e.getSource();
    int ID=source.getMnemonic();

    switch(ID){

      case 2: //view message+state
        JCheckBoxMenuItem item=(JCheckBoxMenuItem) source;
        if (item.isSelected()) mainWnd.setMessagePanelVisible(true);
        else mainWnd.setMessagePanelVisible(false);
        break;
      case 3://view Behaviour
        JCheckBoxMenuItem item1=(JCheckBoxMenuItem) source;
        if (item1.isSelected()) mainWnd.setBehaviourPanelVisible(true);
        else mainWnd.setBehaviourPanelVisible(false);
        break;
      case 4://kill
        System.out.println("kill agent: Not yet implemented");
        break;
      case 5://suspend
        System.out.println("suspend agent: Not yet implemented");
        break;
      case 6://wakeup
        System.out.println("WakeUp agent: Not yet implemented");
        break;
      case 7://wait
        System.out.println("wait agent: Not yet implemented");
        break;
      case 8://Step
      	introspectorSensor.post(new Event(Introspector.STEP_EVENT, mainWnd));
      	break;
      case 9://Break
      	introspectorSensor.post(new Event(Introspector.BREAK_EVENT, mainWnd));
      	break;
      case 10://Slow
      	introspectorSensor.post(new Event(Introspector.SLOW_EVENT, mainWnd));
      	break;
      case 11://Go
      	introspectorSensor.post(new Event(Introspector.GO_EVENT, mainWnd));
      	break;
    }
  }
}
