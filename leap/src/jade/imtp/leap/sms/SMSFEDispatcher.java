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

import jade.imtp.leap.JICP.*;
import jade.core.Timer;
import jade.core.TimerDispatcher;
import jade.core.FrontEnd;
import jade.core.BackEnd;
import jade.core.IMTPException;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.imtp.leap.ICPException;

import java.io.IOException;

import javax.wireless.messaging.*;
import javax.microedition.io.Connector;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class SMSFEDispatcher extends BIFEDispatcher implements Constants, MessageListener {
  private boolean connectionDropped = false;
  private Timer cdTimer = null;
  private long connectionDropDownTime = DROP_DOWN_TIME_DEFAULT;
  private int smsPort = SMS_PORT_DEFAULT;
  
  private MessageConnection myMessageConnection;

  private Logger myLogger = Logger.getMyLogger(getClass().getName());
  
  /**
     Redefine the getBackEnd() method to get parameters specific 
     to this class
   */
  public BackEnd getBackEnd(FrontEnd fe, Properties props) throws IMTPException {  	
  	// Specify the mediator class to use
  	myMediatorClass = "jade.imtp.leap.sms.SMSBEDispatcher";
  	
  	// Get the connection-drop-down timeout
    try {
			connectionDropDownTime = Long.parseLong(props.getProperty(DROP_DOWN_TIME_KEY));
    }
    catch (NumberFormatException nfe) {
			// Use default
    }
    
    // Get the SMS listening port
    try {
			smsPort = Integer.parseInt(props.getProperty(SMS_PORT_KEY));
    }
    catch (NumberFormatException nfe) {
			// Use default
    }

    try {
	  	myMessageConnection = (MessageConnection) Connector.open("sms://:"+smsPort);
	  	myMessageConnection.setMessageListener(this);
    }
    catch (Exception e) {
    	// Cannot receive sms messages --> exit
    	throw new IMTPException("Cannot receive SMS messages.", e);
    }
    
    return super.getBackEnd(fe, props);
  }
  
  /**
     Redefine the dispatch() method to reopen the connection if it is 
     dropped-down.
   */
  public synchronized byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
  	if (connectionDropped) {
	  	// Unlike the SMSBEDispatcher we don't move from the DROPPED 
  		// state to the DISCONNECTED state so that the handleReconnection()
  		// method can detect whether or not to start refreshing 
  		// the INP connection
  		refreshOut();
  		throw new ICPException("Connection dropped");
  	}
  	else {
	  	return super.dispatch(payload, flush);
  	}
  }
  
  /**
     Redefine the handleReconnection() method so that if this is 
     an OUT reconnection after a drop-down we also reopen the 
     INP connection. 
   */
	protected synchronized void handleReconnection(Connection c, byte type) {
		if (type == OUT && connectionDropped) {
			connectionDropped = false;
			refreshInp();
		}
		super.handleReconnection(c, type);
	}
			
  /**
     Redefine the writePacket() method to take into account 
     connection drop down timer updates
   */
  protected void writePacket(JICPPacket pkt, Connection c) throws IOException {
  	super.writePacket(pkt, c);
		if (pkt.getType() != JICPProtocol.KEEP_ALIVE_TYPE && pkt.getType() != DROP_DOWN_TYPE) {
			updateConnectionDropDown();
		}
  }
  
  /**
     Refresh the connection drop-down timer.
     Mutual exclusion with doTimeOut()
   */
  private synchronized void updateConnectionDropDown() {
  	TimerDispatcher td = TimerDispatcher.getTimerDispatcher();
  	if (cdTimer != null) {
	  	td.remove(cdTimer);
  	}
  	cdTimer = td.add(new Timer(System.currentTimeMillis()+connectionDropDownTime, this));
  }
  
  /**
     Redefine the doTimeOut() method to take into account 
     connection drop-down timer expirations 
   */
  public synchronized void doTimeOut(Timer t) {
  	if (t == cdTimer) {
  		myLogger.log(Logger.FINE, "CD timer expired ["+System.currentTimeMillis()+"]");
  		// We haven't exchanged any application data for a while -->
  		// Drop down the connection 
  		dropDownConnection();
  	}
  	else {
  		super.doTimeOut(t);
  	}
  }  
  
  private synchronized void dropDownConnection() {
  	if (outConnection != null && !refreshingInput && !connectionDropped) {
	  	// Send a DROP_DOWN packet to the BackEnd. It will close the INP connection
	  	myLogger.log(Logger.FINE, "Issuing connection drop-down request");
	  	JICPPacket pkt = new JICPPacket(DROP_DOWN_TYPE, JICPProtocol.DEFAULT_INFO, String.valueOf(smsPort).getBytes());
	  	try {
		  	writePacket(pkt, outConnection);
	  	}
	  	catch (IOException ioe) {
	  		// Can't reach the BackEnd. 
  			myLogger.log(Logger.WARNING, "IOException sending DropDown request. "+ioe);
  			refreshOut();
	  	}
	  	
	  	connectionDropped = true;
	  	
	  	// Now close the outConnection
	  	try {
		  	outConnection.close();
		  	outConnection = null;
	  	}
	  	catch (IOException ioe) {
	  		// Just print a warning
	  		myLogger.log(Logger.WARNING, "Exception in connection drop-down closing the OUT connection. "+ioe);
	  	}
	  	
	  	// No need to start waiting for connection refresh requests 
	  	// since the message connection is always active
  	}
  }
  
  /**
     Redefine the refreshInp() method to avoid refreshing
     if the connection was closed due to a drop-down. 
     Otherwise the InputManager will immediately restore
     the just dropped-down connection.
   */
  protected synchronized void refreshInp() { 
  	if (!connectionDropped) {
  		super.refreshInp();
  	}
  }
  
  /**
     Redefine the sendKeepAlive() method to avoid sending 
     KEEP_ALIVE packets when the connection has been dropped down.
     Otherwise this will restore the connection.
   */
  protected synchronized void sendKeepAlive() {
  	if (!connectionDropped) {
  		super.sendKeepAlive();
  	}
  }  
  
  /**
     This is called whenever an SMS arrives.
     If the connection is dropped, refresh it, otherwise it is 
     a spurious message.
   */
  public synchronized void notifyIncomingMessage(MessageConnection conn) {
  	myLogger.log(Logger.FINE, "Incoming SMS Message. connectionDropped = "+connectionDropped);
  	if (connectionDropped) {
	  	// Asynchronously read the message
	  	Thread t = new Thread() {
	  		public void run() {
  				myLogger.log(Logger.FINE, "Refreshing output after connection drop-down. refreshingOutput = "+refreshingOutput);
		  		refreshOut();
	  			try {
	  				myMessageConnection.receive();
	  			}
	  			catch (Exception e) {
	  				myLogger.log(Logger.WARNING, "Error receiving SMS message. "+e);
	  			}
	  		}
	  	};
	  	t.start();
  	}
  }
}

