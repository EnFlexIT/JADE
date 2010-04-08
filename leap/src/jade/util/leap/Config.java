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
	private static final String CONFIG_RS = "conf";
	
  private static final Command setCommand = new Command("Set", Command.SCREEN, 0);
  private static final Command saveCommand = new Command("Save", Command.SCREEN, 1);
  private static final Command clearCommand = new Command("Clear", Command.SCREEN, 1);
  private static final Command exitCommand = new Command("Exit", Command.SCREEN, 1);
  private static final Command okCommand = new Command("OK", Command.OK, 0);
  private static final Command cancelCommand = new Command("Cancel", Command.CANCEL, 0);
  private static final Command yesCommand = new Command("Yes", Command.OK, 0);
  private static final Command noCommand = new Command("No", Command.CANCEL, 0);
  private Display   display;
  private Form      main, set, last, error;
	private TextField keyTxt, valueTxt;
	private boolean modified = false;
  
  /**
   */
  public Config() {
    display = Display.getDisplay(this);
  }

  /**
   */
  public void startApp() {
    main = new Form("Configuration");
    main.addCommand(setCommand);
    main.addCommand(clearCommand);
    main.addCommand(saveCommand);
    main.addCommand(exitCommand);
    main.setCommandListener(this);
    
    set = new Form("Set property");
    set.addCommand(okCommand);
    set.addCommand(cancelCommand);
    keyTxt = new TextField("key", null, 64, TextField.ANY);
    valueTxt = new TextField("value", null, 64, TextField.ANY);
    set.append(keyTxt);
    set.append(valueTxt);
    set.setCommandListener(this);
    
    display.setCurrent(main);
    load();
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
      	display.setCurrent(set);
      }
      else if (c == clearCommand) {
        clear();
      }
      else if (c == saveCommand) {
      	save();
      }
      else if (c == exitCommand) {
        exit();
      }
    }
    else if (d == set) {
    	if (c == okCommand) {
    		grab(keyTxt.getString(), valueTxt.getString());
    	}
    	keyTxt.setString(null);
    	valueTxt.setString(null);
    	display.setCurrent(main);
    }
    else if (d == last) {
    	if (c == yesCommand) {
    		save();
    	}
    	notifyDestroyed();
    }
    else if (d == error) {
    	notifyDestroyed();
    }
  }
  
  private void load() {
  	Properties props = new Properties();
  	try {
	  	props.load(CONFIG_RS);
  	}
  	catch (IOException ioe) {
  		// The recordstore does not exist yet
  	}
  	
  	modified = false;
  	Enumeration keys = props.keys();
  	while (keys.hasMoreElements()) {
  		String k = (String) keys.nextElement();
  		String v = props.getProperty(k);
  		main.append(new StringItem(k, "="+v));
  	}
  }
  
  private void save() {
  	try {
	  	Properties props = new Properties();
	  	int size = main.size();
	  	for (int i = 0; i < size; ++i) {
	  		StringItem si = (StringItem) main.get(i);
	  		props.setProperty(si.getLabel(), si.getText().substring(1));
	  	}
	  	props.store(CONFIG_RS);
	  	modified = false;
  	}
  	catch(IOException ioe) {
  		showError("Storing error: "+ioe.getMessage());
  	}
  }
  
  private void clear() {
  	while (main.size() > 0) {
  		main.delete(0);
  	}
  	modified = true;
  }
  
  private void exit() {
  	if (modified) {
  		last = new Form("");
    	last.append(new StringItem(null, "Save before exiting?"));
  		last.addCommand(yesCommand);
  		last.addCommand(noCommand);
  		last.setCommandListener(this);
  		display.setCurrent(last);
  	}
  	else {
	    notifyDestroyed();
  	}
  }  
  
  private void grab(String key, String value) {	
  	if (key != null) {
  		key = key.trim();
  		if (!key.equals("")) {
  			// Valid key. Search if it already exists
  			int i = search(key);
  			if (i >= 0) {
  				// A property with the same key already exists. Substitute/remove it
  				if (value != null && !value.trim().equals("")) {
  					StringItem si = (StringItem) main.get(i);
  					si.setText("="+value.trim());
  				}
  				else {
  					main.delete(i);
  				}
  			}
  			else {
  				// New property
  				if (value != null) {
  					value = value.trim();
  					if (!value.equals("")) {
		  				StringItem si = new StringItem(key, "="+value);
		  				main.append(si);
  					}
  				}
  			}
  			modified = true;
  		}
  	}
  }
  
  private int search(String key) {
  	int size = main.size();
  	for (int i = 0; i < size; i++) {
  		StringItem si = (StringItem) main.get(i);
  		if (si.getLabel().equals(key)) {
  			return i;
  		}
  	}
  	return -1;
  }
  	
  private void showError(String msg) {
		error = new Form("ERROR");
  	error.append(new StringItem(null, msg));
		error.addCommand(okCommand);
		error.setCommandListener(this);
		display.setCurrent(error);
  }
}

