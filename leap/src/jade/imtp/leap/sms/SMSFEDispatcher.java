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
import jade.core.FrontEnd;
import jade.core.BackEnd;
import jade.core.IMTPException;
import jade.util.Logger;
import jade.util.leap.Properties;

import javax.wireless.messaging.*;
import javax.microedition.io.Connector;

/**
   FrontEnd side dispatcher class that extends <code>BIFEDispatcher</code>
   and uses SMS to receive OUT-of-bound notifications from the BackEnd
   when the connection (currently dropped) must be re-established.
   @see SMSBEDispatcher
   @author Giovanni Caire - TILAB
 */
public class SMSFEDispatcher extends BIFEDispatcher implements MessageListener {
	public static final int SMS_PORT_DEFAULT = 9876;
	public static final String SMS_PORT_KEY = "jade_imtp_leap_sms_SMSFEDispatcher_smsport";	
  
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
    
    // Get the SMS listening port
    try {
			smsPort = Integer.parseInt(props.getProperty(SMS_PORT_KEY));
    }
    catch (NumberFormatException nfe) {
			// Use default
    }

    // Initialize the MessageConnection
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
  
  protected JICPPacket prepareDropDownRequest() {
	  return new JICPPacket(JICPProtocol.DROP_DOWN_TYPE, JICPProtocol.DEFAULT_INFO, String.valueOf(smsPort).getBytes());
  }
  
  /**
     This is called whenever an SMS arrives.
     If the connection is dropped, refresh it, otherwise it is 
     a spurious message.
   */
  public synchronized void notifyIncomingMessage(MessageConnection conn) {
  	myLogger.log(Logger.FINE, "Incoming SMS Message. Refresh connection");
		refreshOut();
		
  	// Asynchronously read the message
  	Thread t = new Thread() {
  		public void run() {
  			try {
  				myMessageConnection.receive();
  			}
  			catch (Exception e) {
  				myLogger.log(Logger.WARNING, "Error reading SMS message. "+e);
  			}
  		}
  	};
  	t.start();
  }
}

