/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.util.leap;

//#J2SE_EXCLUDE_FILE
//#PJAVA_EXCLUDE_FILE

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;
import java.util.Enumeration;
import java.io.IOException;

import jade.util.leap.Properties;

/**
   @author Giovanni Caire - TILAB
 */
public class Config extends MIDlet implements CommandListener {
  private static final Command setCommand = new Command("Set", Command.SCREEN, 0);
  private static final Command saveCommand = new Command("Save", Command.SCREEN, 1);
  private static final Command viewCommand = new Command("View", Command.SCREEN, 1);
  private static final Command exitCommand = new Command("Exit", Command.SCREEN, 1);
  private static final Command okCommand = new Command("OK", Command.OK, 0);
  private static final Command cancelCommand = new Command("Cancel", Command.OK, 0);
  private Display   display;
  private Form      main, set, view, first, last, error;
	private TextField keyTxt, valueTxt;
	private Properties props;
	private boolean unsaved = false;
  
  /**
   */
  public Config() {
    display = Display.getDisplay(this);
  }

  /**
   */
  public void startApp() {
    props = new Properties();
    first = new Form("");
    first.append(new StringItem(null, "Reload previous configuration?"));
    first.addCommand(okCommand);
    first.addCommand(cancelCommand);
    first.setCommandListener(this);
    display.setCurrent(first);   
    
    main = new Form("Configuration");
    main.addCommand(setCommand);
    main.addCommand(saveCommand);
    main.addCommand(viewCommand);
    main.addCommand(exitCommand);
    main.setCommandListener(this);
    
    keyTxt = new TextField("key", null, 64, TextField.ANY);
    valueTxt = new TextField("value", null, 64, TextField.ANY);
  }

  /**
   */
  public void pauseApp() {
  } 

  /**
   */
  public void destroyApp(boolean unconditional) {
  }
  
  /**
   */
  public void commandAction(Command c, Displayable d) {
    if (d == main) {
      if (c == setCommand) {
      	set();
      }
      else if (c == saveCommand) {
      	save();
      }
      else if (c == viewCommand) {
        view();
      }
      else if (c == exitCommand) {
        exit();
      }
    }
    else if (d == first) {
    	if (c == okCommand) {
    		reload();
    	}
    	display.setCurrent(main);
    	first = null;
    }
    else if (d == last) {
    	if (c == okCommand) {
    		save();
    	}
    	notifyDestroyed();
    }
    else if (d == set) {
    	if (c == okCommand) {
    		grab(keyTxt.getString(), valueTxt.getString());
    	}
    	display.setCurrent(main);
    	set = null;
    }
    else if (d == view) {
  		display.setCurrent(main);
  		view = null;
    }
    else if (d == error) {
    	notifyDestroyed();
    }
  }
  
  private void set() {
  	set = new Form("Set property");
  	set.append(keyTxt);
  	set.append(valueTxt);
  	set.addCommand(okCommand);
  	set.addCommand(cancelCommand);
    set.setCommandListener(this);
  	display.setCurrent(set);
  }
  
  private void reload() {
  	try {
	  	props.load("CONFIG");
	  	unsaved = false;
  	}
  	catch(IOException ioe) {
  		showError("Loading error: "+ioe.getMessage());
  	}
  }
  
  private void save() {
  	try {
	  	props.store("CONFIG");
	  	unsaved = false;
  	}
  	catch(IOException ioe) {
  		showError("Storing error: "+ioe.getMessage());
  	}
  }
  
  private void view() {
  	view = new Form("Current");
  	Enumeration e = props.keys();
  	while (e.hasMoreElements()) {
  		String key = (String) e.nextElement();
  		view.append(new StringItem(key+"=", props.getProperty(key)));
  	}
  	view.addCommand(okCommand);
    view.setCommandListener(this);
  	display.setCurrent(view);
  }		
  
  private void exit() {
  	if (unsaved) {
  		last = new Form("");
    	last.append(new StringItem(null, "Save current configuration?"));
  		last.addCommand(okCommand);
  		last.addCommand(cancelCommand);
  		last.setCommandListener(this);
  		display.setCurrent(last);
  	}
  	else {
	    notifyDestroyed();
  	}
  }  
  
  private void grab(String key, String value) {
  	if (key != null && !key.trim().equals("")) {
  		if (value != null && !value.trim().equals("")) {
  			props.setProperty(key.trim(), value.trim());
  		}
  		else {
  			props.remove(key.trim());
  		}
  		unsaved = true;
  	}
  }
  
  private void showError(String msg) {
		error = new Form("ERROR");
  	error.append(new StringItem(null, msg));
		error.addCommand(okCommand);
		error.setCommandListener(this);
		display.setCurrent(error);
  }
}

