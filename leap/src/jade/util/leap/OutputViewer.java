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

/**
   Utility MIDlet that visualize the output previously written using 
   <code>Logger.println()</code>
   @author Giovanni Caire - TILAB
 */
public class OutputViewer extends MIDlet implements CommandListener {
	private static final String OUTPUT = "OUTPUT";
  
  private static final Command exitCommand = new Command("Exit", Command.EXIT, 1);
  private static final Command clearCommand = new Command("Clear", Command.SCREEN, 1);
  private static final Command okCommand = new Command("OK", Command.OK, 1);
  private Display                       display;
  private String                        recordStoreName;
  private Form                          form, error;

  /**
   */
  public OutputViewer() {
    display = Display.getDisplay(this);
  }

  /**
   */
  public void startApp() {
    form = new Form("Output:");
    form.addCommand(exitCommand);
    form.addCommand(clearCommand);
    form.setCommandListener(this);
    display.setCurrent(form);    
    readOutput();
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
    if (c == exitCommand) {
    	notifyDestroyed();
    }
    if (c == clearCommand) {
    	try {
	    	Class.forName("jade.util.Logger");    	
		  	int size = form.size();
				for (int i = 0; i < size; ++i) {
					form.delete(0);
				}
		    display.setCurrent(form);
    	}
    	catch (Exception e) {
    		showError("Cannot clear output. "+e.getMessage());
    	}
    }
    if (c == okCommand) {
    	display.setCurrent(form);
    }
  }
  
  private void readOutput() {
  	try {
  		RecordStore rs = RecordStore.openRecordStore(OUTPUT, true);
  		int linesCnt = rs.getNumRecords();
   		for (int i=0; i < linesCnt; ++i) {
   			byte[] bb = rs.getRecord(i+1);
   			StringItem line = new StringItem(null, new String(bb));
   			form.append(line);
   		}
    	rs.closeRecordStore();
  	}
  	catch (Exception e) {
  		showError("Cannot open "+OUTPUT+" record store. "+e.getMessage());
  	}
  }
  
  private void showError(String msg) {
		error = new Form("ERROR");
  	form.append(new StringItem(null, msg));
		error.addCommand(okCommand);
		error.setCommandListener(this);
		display.setCurrent(error);
  }
}

