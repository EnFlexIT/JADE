/*
  $Log$
  Revision 1.4  1998/11/09 22:19:09  Giovanni
  Changed code indentation to comply with JADE coding style.

  Revision 1.3  1998/10/10 19:37:15  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import com.sun.java.swing.*;
import java.util.Properties;
import java.io.*;
/**
 * This class encapsulates some informations used by the program
 */
public class GuiProperties {
  protected static UIDefaults MyDefaults;
  protected static GuiProperties foo = new GuiProperties();
  public static final String ImagePath = "";

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

