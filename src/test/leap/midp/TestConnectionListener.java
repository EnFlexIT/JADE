/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
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

package test.leap.midp;

import jade.imtp.leap.ConnectionListener;
import jade.core.Agent;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestConnectionListener implements ConnectionListener, CommandListener {
  private final static Command CMD_BACK = new Command("Back", Command.BACK, 1);
	private Form f;
	private Displayable previous;
	
	public TestConnectionListener() {
		f = new Form("Connection events");
    f.addCommand(CMD_BACK);
    f.setCommandListener(this);
	}
		
	public void handleConnectionEvent(int ev) {
		switch (ev) {
		case BEFORE_CONNECTION:
			f.append(new StringItem(null, "Before connection."));
			break;
		case DISCONNECTED:
			f.append(new StringItem(null, "Disconnection. "+System.currentTimeMillis()));
			show();
			break;
		case RECONNECTED:
			f.append(new StringItem(null, "Reconnection. "+System.currentTimeMillis()));
			show();
			break;
		case DROPPED:
			f.append(new StringItem(null, "Connection dropped. "+System.currentTimeMillis()));
			break;
		case RECONNECTION_FAILURE:
			f.append(new StringItem(null, "Impossible to reconnect. "+System.currentTimeMillis()));
			show();
			break;
		case BE_NOT_FOUND:
			f.append(new StringItem(null, "BackEnd not found! "+System.currentTimeMillis()));
			break;
		case NOT_AUTHORIZED:
			f.append(new StringItem(null, "Not authorized! "+System.currentTimeMillis()));
			show();
			break;
		default:
			break;
		}			
	}
	
	
	private void show() {
		Display theDisplay = Display.getDisplay(Agent.midlet);
		Displayable current = theDisplay.getCurrent();
		if (current != f) {
			previous = current;
		}
    theDisplay.setCurrent(f);
	} 
	
  public void commandAction(Command c, Displayable d) {
    if( c == CMD_BACK) {
    	if (previous != null) {
	    	Display.getDisplay(Agent.midlet).setCurrent(previous);
    	}
    }
  }
}

