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
   @author Giovanni Caire - TILAB
 */
public class OutputViewer extends MIDlet implements CommandListener {
  private static final Command exitCommand = new Command("Exit", Command.EXIT, 1);
  private static final Command downCommand = new Command("Down", Command.SCREEN, 1);
  private static final Command upCommand = new Command("Up", Command.SCREEN, 0);
  private Display                       display;
  private String                        recordStoreName;
  private Form                          form;
  private StringItem                    si;
  private int                           offset = 0;

  /**
   */
  public OutputViewer() {
    display = Display.getDisplay(this);
  }

  /**
   */
  public void startApp() {
    form = new Form("Output:");
    si = new StringItem(null, null);
    form.append(si);
    form.addCommand(exitCommand);
    form.addCommand(downCommand);
    form.addCommand(upCommand);
    form.setCommandListener(this);
    display.setCurrent(form);    
    si.setText(readOutput());
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
    if (d == form) {
      if (c == exitCommand) {
        exit();
      }
      else if (c == downCommand) {
      	offset++;
    		si.setText(readOutput());
      }
      else if (c == upCommand) {
      	offset--;
    		si.setText(readOutput());
      }
    }
  }
  
  private void exit() {
    notifyDestroyed();
  }
  
  private String readOutput() {
  	StringBuffer sb = new StringBuffer();
  	try {
  		RecordStore rs = RecordStore.openRecordStore("OUTPUT", false);
  		int linesCnt = rs.getNumRecords();
   		for (int i = offset; i < linesCnt; ++i) {
   			byte[] bb = rs.getRecord(i+1);
   			String line = new String(bb);
   			sb.append(line);
   			sb.append('\n');
   		}
    	rs.closeRecordStore();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  	return sb.toString();
  }		
}

