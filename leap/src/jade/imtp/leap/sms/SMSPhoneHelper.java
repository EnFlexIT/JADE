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

package jade.imtp.leap.sms;

//#J2SE_EXCLUDE_FILE
//#PJAVA_EXCLUDE_FILE

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.io.*;
import javax.wireless.messaging.*;

import jade.util.Logger;
import jade.imtp.leap.JICP.JICPConnection;
import jade.imtp.leap.JICP.Connection;
import jade.imtp.leap.JICP.JICPPacket;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.imtp.leap.JICP.JICPAddress;
import jade.mtp.TransportAddress;

/**
   @author Giovanni Caire - TILAB
 */
public class SMSPhoneHelper extends MIDlet implements Runnable {
	public static final byte BINARY = 0;
	public static final byte TEXT = 1;
	
	public static final int MAX_ITEMS = 10;
	
	public static final int IDLE = 0;
	public static final int CONNECTING = 1;
	public static final int CONNECTED = 2;
	public static final int STOPPED = 3;
	
	private int status = IDLE;
	
	private TransportAddress address;
	private Connection myConnection;
	
	private Form mainForm;
	
  public void startApp() throws MIDletStateChangeException {
  	if (status == IDLE) {
  		status = CONNECTING;
  		mainForm = new Form("SMS Phone Helper");
  		Display.getDisplay(this).setCurrent(mainForm);
	    try {
	    	String host = getAppProperty("host");
	    	if (host == null) {
	    		host = "localhost";
	    	}
	    	String port = getAppProperty("port");;
	    	if (port == null) {
	    		port = "1100";
	    	}
	    	address = new JICPAddress(host, port, null, null);
	    	Thread t = new Thread(this);
	    	t.start();
	    } 
	    catch (Exception e) {
	      append("Error reading configuration properties: "+e);
	    }
  	}
  } 

  public void pauseApp() {
  } 

  public void destroyApp(boolean unconditional) {
  	// When the MIDlet is closed stop the embedded Thread
  	stop();
  } 
 
  public void run() {
  	try {
	  	append("Connecting to "+address.getHost()+":"+address.getPort()+"...");
	  	myConnection = new JICPConnection(address);
	  	status = CONNECTED;
	  	append("Connection OK");
	  	while (status == CONNECTED) {
	  		JICPPacket pkt = myConnection.readPacket();
  			String url = "sms://"+pkt.getRecipientID();
	  		append("Request received. Destination = "+url);
	  		byte type = pkt.getInfo();
	  		byte[] data = pkt.getData();
	  		if (data == null) {
	  			data = (" ").getBytes();
	  		}
	  		MessageConnection conn = null;
	  		try {
	  			conn = (MessageConnection) Connector.open(url, Connector.WRITE, false);
	  			Message msg = null;
	  			if (type == (byte) 1) {
	  				msg = (TextMessage) conn.newMessage(MessageConnection.TEXT_MESSAGE);
	  				((TextMessage) msg).setPayloadText(new String(data));
	  			}
	  			else {
	  				msg = (BinaryMessage) conn.newMessage(MessageConnection.BINARY_MESSAGE);
  					((BinaryMessage) msg).setPayloadData(data);
	  			}
	  			conn.send(msg);
	  			append("Message sent.");
	  		}
	  		catch (Exception e1) {
	  			append("SMS error: "+e1);
	  		}
	  		finally {
	  			try {
	  				conn.close();
	  			}
	  			catch (Exception e) {}
	  		}
	  	}
  	}
  	catch (Exception e) {
  		synchronized (SMSPhoneHelper.this) {
  			if (status == CONNECTING) {
  				append("Connection error: "+e);
  				status = STOPPED;
  			}
  			else if (status == CONNECTED) {
  				append("Error reading request: "+e);
  				myConnection = null;
  				status = STOPPED;
  			}
  		}
  	}
  }
  
  private synchronized void stop() {
  	if (myConnection != null) {
  		status = STOPPED;
  		try {
	  		myConnection.close();
  		}
  		catch (Exception e) {
  		}
  	}
  }
  
  private void append(String s) {
  	if (mainForm.size() >= MAX_ITEMS) {
  		mainForm.delete(0);
  	}
  	mainForm.append(s);
  }
}