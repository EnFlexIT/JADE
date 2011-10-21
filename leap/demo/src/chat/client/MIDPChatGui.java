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

package chat.client;

//#J2SE_EXCLUDE_FILE
//#PJAVA_EXCLUDE_FILE
//#ANDROID_EXCLUDE_FILE

import jade.core.Agent;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import chat.client.agent.ChatClientAgent;

/**
   @author Giovanni Caire - TILAB
 */
public class MIDPChatGui implements ChatGui, CommandListener {
	private static final int MAX_SIZE = 5;
	
	private ChatClientAgent myAgent;
	private Form main;
	private Form others;
	private TextBox writeTb;
  private final Command writeCmd = new Command("Write", Command.SCREEN, 1);
  private final Command listCmd = new Command("List", Command.SCREEN, 2);
  private final Command clearCmd = new Command("Clear", Command.SCREEN, 3);
  private final Command exitCmd = new Command("Exit", Command.SCREEN, 4);
  private final Command okCmd = new Command("OK", Command.OK, 1);
  private final Command cancelCmd = new Command("Cancel", Command.CANCEL, 1);
	
	public MIDPChatGui(ChatClientAgent a) {
		myAgent = a;
		
		// Main chat form
		main = new Form("Chat: "+myAgent.getLocalName());
		main.addCommand(writeCmd);
		main.addCommand(listCmd);
		main.addCommand(clearCmd);
		main.addCommand(exitCmd);
		main.setCommandListener(this);
    Display.getDisplay(Agent.midlet).setCurrent(main); 
    
    // Write text box
    writeTb = new TextBox("Write", null, 128, TextField.ANY);
    writeTb.addCommand(okCmd);
    writeTb.addCommand(cancelCmd);
		writeTb.setCommandListener(this);
		
		// Others form
		others = new Form("Participants");
		others.addCommand(okCmd);
		others.setCommandListener(this);
		others.append(new StringItem(myAgent.getLocalName(), null));
	}
	
	///////////////////////////////////
	// ChatGui interface implementation
	///////////////////////////////////
	public void notifyParticipantsChanged(String[] names) {
		for (int i = 1; i < others.size(); ++i) {
			others.delete(i);
		}
		for (int i = 0; i < names.length; ++i) {
			others.append(new StringItem(names[i], null));
		}
	}
	
	public synchronized void notifySpoken(String speaker, String sentence) {
		if (main.size() == MAX_SIZE) {
			main.delete(0);
		}
		main.append(new StringItem(speaker+": ", sentence));
	}
	
	public void dispose() {
		// Nothing else to do
	}
	
	////////////////////////////////////////////
	// CommandListener interface implementation
	////////////////////////////////////////////
  public void commandAction(Command c, Displayable d) {
  	if (d == main) {
	    if (c == writeCmd) {
    		Display.getDisplay(Agent.midlet).setCurrent(writeTb); 
	    }
	    else if (c == listCmd) {
    		Display.getDisplay(Agent.midlet).setCurrent(others); 
	    }
	    else if (c == clearCmd) {
    		clearMain(); 
	    }
	    else if (c == exitCmd) {
	    	myAgent.doDelete();
	    	showExiting();
	    }
  	}
  	else if (d == writeTb) {
	    if (c == okCmd) {
	    	String s = writeTb.getString();
	    	myAgent.handleSpoken(s);
	    	writeTb.setString("");
	    }
	    else if (c == cancelCmd) {
	    	// Nothing specific
	    }
    	Display.getDisplay(Agent.midlet).setCurrent(main); 
  	}
  	else if (d == others) {
	    if (c == okCmd) {
    		Display.getDisplay(Agent.midlet).setCurrent(main); 
	    }
  	}
  }  
  
  private void showExiting() {
  	clearMain();
		main.append(new StringItem(null, "Exiting. Please wait..."));
  }
  
  private synchronized void clearMain() {
  	int size = main.size();
		for (int i = 0; i < size; ++i) {
			main.delete(0);
		}
    Display.getDisplay(Agent.midlet).setCurrent(main); 
  }
}



