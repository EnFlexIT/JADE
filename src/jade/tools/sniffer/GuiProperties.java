package jade.tools.sniffer;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.Icon;
import java.util.Properties;

/**
 * This class loads the icons used in the toolbar, menus and trees
 */
public class GuiProperties{
	
  protected static UIDefaults MyDefaults;
  protected static GuiProperties foo = new GuiProperties();
  public static final String ImagePath = "";
	static{
  	Object[] icons = {
    	"MMAbstractAction.AddRemoveAgentActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/inout.gif"),
			"MMAbstractAction.ClearCanvasActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/litter2.gif"),
			"MMAbstractAction.DisplayLogFileActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/open.gif"),
			"MMAbstractAction.WriteLogFileActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/save1.gif"), //save1
			"MMAbstractAction.MessageFileActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/textfile.gif"),	
			"MMAbstractAction.ExitActionIcon",LookAndFeel.makeIcon(foo.getClass(), "images/exit.gif"),
			"TreeData.SuspendedIcon",LookAndFeel.makeIcon(foo.getClass(),"images/stoptree.gif"),
			"TreeData.RunningIcon",LookAndFeel.makeIcon(foo.getClass(),"images/runtree.gif"),
			"TreeData.FolderIcon",LookAndFeel.makeIcon(foo.getClass(),"images/treeclosed.gif")
		};
    MyDefaults = new UIDefaults (icons);
	}

	public static final Icon getIcon(String key){
		
	  Icon i = MyDefaults.getIcon(key);
	  if (i == null){
	  	System.out.println(key);
		  System.exit(-1);
		  return null;
		}
	  else 
	  	return MyDefaults.getIcon(key);
	}
}