/*
  $Log$
  Revision 1.2  1999/06/04 11:34:27  rimassa
  Added icon for DummyAgent tool.

  Revision 1.1  1999/05/20 15:42:09  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.6  1999/02/04 14:47:27  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.5  1998/11/14 14:11:20  rimassa
  Fixed a major bug: the static Vector of all GUI icons was removed by
  mistake.

  Revision 1.3  1998/10/10 19:37:15  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import javax.swing.*;
import java.util.Properties;
import java.io.*;
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
      "TreeData.RunningIcon",LookAndFeel.makeIcon(foo.getClass(), "images/fg.gif"),
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

