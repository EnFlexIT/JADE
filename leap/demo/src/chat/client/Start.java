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

import jade.MicroBoot;
import jade.core.MicroRuntime;
import jade.core.Agent;
import jade.util.leap.Properties;

//#MIDP_EXCLUDE_BEGIN
import java.awt.*;
import java.awt.event.*;
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
#MIDP_INCLUDE_END*/

/**
   Startup class for the chat application
   @author Giovanni Caire - TILAB
 */
//#MIDP_EXCLUDE_BEGIN
public class Start extends MicroBoot {
  public static void main(String args[]) {
  	MicroBoot.main(args);
  	NickNameDlg dlg = new NickNameDlg("Chat");
  }
	
  private static class NickNameDlg extends Frame implements ActionListener {
  	private TextField nameTf;
  	private TextArea msgTa;
  	
  	NickNameDlg(String s) {
  		super(s);
  		
  		setSize(getProperSize(256, 320));
			Panel p = new Panel();
			p.setLayout(new BorderLayout());
			nameTf = new TextField();
			p.add(nameTf, BorderLayout.CENTER);
			Button b = new Button("OK");
			b.addActionListener(this);
			p.add(b, BorderLayout.EAST);
			add(p, BorderLayout.NORTH);
			
			msgTa = new TextArea("Enter nickname\n");
			msgTa.setEditable(false);
			msgTa.setBackground(Color.white);
			add(msgTa, BorderLayout.CENTER);
			
			addWindowListener(new	WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					MicroRuntime.stopJADE();
				}
			} );
			
			showCorrect();
  	}
		
		public void actionPerformed(ActionEvent e) {
	  	String name = nameTf.getText();
	  	if (!checkName(name)) {
		  	msgTa.append("Invalid nickname\n");
	  	}
	  	else {
	  		try {
	    		MicroRuntime.startAgent(name, "chat.client.agent.ChatClientAgent", null);
	    		dispose();
    		}
    		catch (Exception ex) {
    			msgTa.append("Nickname already in use\n");
    		}
	  	}
		}
		
		private void showCorrect() {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Dimension frameSize = getSize();
			int centerX = (int)screenSize.width / 2;
			int centerY = (int)screenSize.height / 2;
			setLocation(centerX - frameSize.width / 2, centerY - frameSize.height / 2);
			show();
		}
		
		private Dimension getProperSize(int maxX, int maxY) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			int x = (screenSize.width < maxX ? screenSize.width : maxX);
			int y = (screenSize.height < maxY ? screenSize.height : maxY);
			return new Dimension(x, y);
		}
  }
//#MIDP_EXCLUDE_END
/*#MIDP_INCLUDE_BEGIN
public class Start extends MicroBoot implements CommandListener {
  private final Command okCommand = new Command("OK", Command.OK, 1);
  private final Command cancelCommand = new Command("Cancel", Command.CANCEL, 1);
  private Form form;
  private TextField tf;
  private StringItem si;
  	
  public void startApp() throws MIDletStateChangeException {
  	super.startApp();
  	
    form = new Form("Enter nickname:");
    Display.getDisplay(Agent.midlet).setCurrent(form);    
    tf = new TextField(null, null, 32, TextField.ANY);
    form.append(tf);
    si = new StringItem(null, null);
    form.append(si);
    
    form.addCommand(okCommand);
    form.addCommand(cancelCommand);
    form.setCommandListener(this);
  }

  public void commandAction(Command c, Displayable d) {
    if (c == okCommand) {
    	String name = tf.getString();	
    	if (!checkName(name)) {
    		si.setText("The nickname must be composed of letters and digits only");
    	}
    	else {
    		try {
    			si.setText("Joining chat. Please wait...");
	    		MicroRuntime.startAgent(name, "chat.client.agent.ChatClientAgent", null);
    		}
    		catch (Exception e) {
    			si.setText("Nickname already in use");
    		}
    	}
    }
    else if (c == cancelCommand) {
    	MicroRuntime.stopJADE();
    }
  }
  
  protected void customize(Properties p) {
  	p.setProperty("exitwhenempty", "true");
	}
#MIDP_INCLUDE_END*/
  
  private static boolean checkName(String name) {
  	if (name == null || name.trim().equals("")) {
  		return false;
  	}
  	// FIXME: should also check that name is composed 
  	// of letters and digits only 
  	return true;
  }
}