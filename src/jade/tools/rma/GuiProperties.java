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
import java.util.Properties;
import java.io.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
/**
 * This class encapsulates some informations used by the program
 */
public class GuiProperties {
  protected static UIDefaults MyDefaults;
  protected static GuiProperties foo = new GuiProperties();
  public static final String ImagePath = "";
  static {
    Object[] icons = {
      "AMSAbstractAction.AddAgentPlatformActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/connect.gif"),
      "AMSAbstractAction.AddAgentActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/cervello.gif"),
      "AMSAbstractAction.CustomActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/custom.gif"),
      "AMSAbstractAction.ExitActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/kill.gif"),
      "AMSAbstractAction.getPropertiesActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/properties.gif"),
      "AMSAbstractAction.OpenScriptFileActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/open.gif"),
      "AMSAbstractAction.KillActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/kill.gif"),
      "AMSAbstractAction.PingActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/ping.gif"),
      "AMSAbstractAction.ReloadActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/reload.gif"),
      "AMSAbstractAction.RemoveActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/ex.gif"),
      "AMSAbstractAction.ResumeActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/sveglia.gif"),
      "AMSAbstractAction.SnifferActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/sniffer.gif"),
      "AMSAbstractAction.StartActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/start.gif"),
      "AMSAbstractAction.StartNewAgentActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/baby.gif"),
      "AMSAbstractAction.SuspendActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/suspend.gif"),
      "TreeData.SuspendedIcon",LookAndFeel.makeIcon(foo.getClass(), "images/stopTree.gif"),
      "TreeData.RunningIcon",LookAndFeel.makeIcon(foo.getClass(), "images/runtree.gif"),
      "TreeData.FolderIcon",LookAndFeel.makeIcon(foo.getClass(), "images/TreeClosed.gif"),
      "AMSAbstractAction.DummyAgentActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/dummyagent.gif")    
    };

    MyDefaults = new UIDefaults (icons);
  }

  public static final Icon getIcon(String key) {
    Icon i = MyDefaults.getIcon(key);
    if (i == null) {
      System.out.println(key);
      System.exit(-1);
      return null;
    }
    else return MyDefaults.getIcon(key);
  }
}

