/*
  $Log$
  Revision 1.1  1999/05/20 15:42:07  rimassa
  Moved RMA agent from jade.domain package to jade.tools.rma package.

  Revision 1.7  1999/05/19 18:31:23  rimassa
  Changed various classes to remove static references to RMA agent from GUI
  components and actions.

  Revision 1.6  1999/02/04 14:47:25  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.5  1998/11/15 23:12:56  rimassa
  Minor changes to comply with KillAction() new constructor.

  Revision 1.4  1998/11/09 22:17:28  Giovanni
  Added two items to 'File' JADE menu; one shuts down the Remote
  Management Agent, whereas the othe closes the whole Agent Platform.

  Revision 1.3  1998/10/10 19:37:04  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.tools.rma;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;


/**
 * The Menu of the GUI
 */
public class AMSMenu extends JMenuBar {

  public AMSMenu (rma anRMA) {
    super();

    AMSAbstractAction act;

    JMenu menu = new JMenu ("File");
    JMenuItem tmp;
		
    act = new AddAgentPlatformAction();
    tmp = menu.add(act);
    tmp.setIcon(null);
		
    act = new OpenScriptFileAction();
    tmp = menu.add(act);
    tmp.setIcon(null);
    add(menu);

    tmp = new JMenuItem ("Execute Current Script");
    menu.add(tmp);

    act = new CloseRMAAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new ExitAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new ShutDownAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    add(menu);


    menu = new JMenu ("Actions");

    act = new StartNewAgentAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new StartAction();
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new KillAction("Kill Selected Items", anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new SuspendAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new ResumeAction(anRMA);
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new PingAction();
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new SnifferAction();
    tmp = menu.add(act);
    tmp.setIcon(null);

    act = new CustomAction();
    tmp = menu.add(act);
    tmp.setIcon(null);

    add(menu);

  }
}
