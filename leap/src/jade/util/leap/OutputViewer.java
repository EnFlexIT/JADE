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

import java.util.Vector;

/**
   Utility MIDlet that visualize the output previously written using 
   <code>jade.util.Logger</code>
   @author Giovanni Caire - TILAB
 */
public class OutputViewer extends MIDlet implements CommandListener {
	private static final String OUTPUT = "OUTPUT";
  
  private final Command clearCommand = new Command("Clear", Command.SCREEN, 1);
  private final Command tailCommand = new Command("Tail", Command.SCREEN, 1);
  private Display display;
  private Form myForm;
  
  /**
   */
  public OutputViewer() {
    display = Display.getDisplay(this);
  }

  /**
   */
  public void startApp() {
  	refresh();
    readAll();
  }

  private void refresh() {
    myForm = new Form(OUTPUT);
    myForm.addCommand(tailCommand);
    myForm.addCommand(clearCommand);
    myForm.setCommandListener(this);
    display.setCurrent(myForm);    
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
  	if (d == myForm) {
	    if (c == clearCommand) {
	    	try {
	    		// This will delete the OUTPUT RecordStore
		    	Class.forName("jade.util.Logger");    	
		    	refresh();
	    	}
	    	catch (Exception e) {
	    		showError("Cannot clear output. "+e.getMessage());
	    	}
	    }
	    else if (c == tailCommand) {
	    	refresh();
	    	readTail();
	    }
  	}
  }
  
  private void readAll() {
		RecordStore rs = null;
    RecordEnumeration rse = null;
  	try {
  		rs = RecordStore.openRecordStore(OUTPUT, true);
			for (rse=rs.enumerateRecords(null, null, false); rse.hasNextElement(); ) {
				byte[] bb = rse.nextRecord();
   			StringItem line = new StringItem(null, new String(bb));
   			myForm.append(line);
   		}
  	}
  	catch (Exception e) {
  		showError("Cannot open "+OUTPUT+" record store. "+e.getMessage());
  	} 
  	finally {
				try {
            if (rse != null) rse.destroy();
						if (rs != null)  rs.closeRecordStore();
				} catch (Exception any) {
				}
		}
  }
  
  private void readTail() {
		RecordStore rs = null;
    RecordEnumeration rse = null;
    Vector v = new Vector();
  	try {
  		rs = RecordStore.openRecordStore(OUTPUT, true);
			for (rse=rs.enumerateRecords(null, null, false); rse.hasNextElement(); ) {
				byte[] bb = rse.nextRecord();
   			StringItem line = new StringItem(null, new String(bb));
   			v.addElement(line);
   		}
   		for (int i = v.size();i > (v.size() - 10) && i > 0; --i) {
   			myForm.append((StringItem) v.elementAt(i-1));
   		}
  	}
  	catch (Exception e) {
  		showError("Cannot open "+OUTPUT+" record store. "+e.getMessage());
  	} 
  	finally {
				try {
            if (rse != null) rse.destroy();
						if (rs != null)  rs.closeRecordStore();
				} catch (Exception any) {
				}
		}
  }
  
  private void showError(String msg) {
  	Alert alert = new Alert("ERROR", msg, null, AlertType.ERROR);
		display.setCurrent(alert);
  }
}

