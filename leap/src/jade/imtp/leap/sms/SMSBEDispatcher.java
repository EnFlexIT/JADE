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

//#J2ME_EXCLUDE_FILE

import jade.imtp.leap.JICP.*;
import jade.imtp.leap.ICPException;
import jade.util.leap.Properties;

import java.net.*;

import java.io.*;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class SMSBEDispatcher extends BIBEDispatcher implements Constants {

	private int smsPort = SMS_PORT_DEFAULT;
	private boolean connectionDropped = false;
	private SMSManager theSMSManager; 
	private String msisdn;
	
  public void init(JICPServer srv, String id, Properties props) throws ICPException {
  	// Get the msisdn
  	msisdn = props.getProperty("msisdn");
  	if (msisdn == null) {
  		throw new ICPException("Missing MSISDN");
  	}
  	// Get the singleton SMSManager
		theSMSManager = SMSManager.getInstance(props);
		if (theSMSManager == null) {
			throw new ICPException("Cannot attach to the SMSManager");
		}
		super.init(srv, id, props);
  }
  
  /**
   */
  public JICPPacket handleIncomingConnection(Connection c, JICPPacket pkt, InetAddress addr, int port) {  	
  	connectionDropped = false;
  	return super.handleIncomingConnection(c, pkt, addr, port);
  } 

  /**
   */
  public void tick(long currentTime) {
  	if (!connectionDropped) {
  		super.tick(currentTime);
  	}
  }
  
  /**
   */
  public synchronized byte[] dispatch(byte[] payload, boolean flush) throws ICPException {
		System.out.println("dispatch() called. connectionDropped is "+connectionDropped+" flush is "+flush);
  	if (connectionDropped) {
	  	// Move from DROPPED state to DISCONNECTED state to avoid 
	  	// sending more SMS if other calls to dispatch() occurs 
	  	connectionDropped = false;
  		System.out.println("Activating connection refresh");
	  	requestRefresh();
  		throw new ICPException("Connection dropped");
  	}
  	else {
  		return super.dispatch(payload, flush);
  	}  	
  }

  /**
   */
  protected JICPPacket handlePacket(JICPPacket pkt) {
  	if (pkt.getType() == DROP_DOWN_TYPE) {
  		System.out.println("DROP_DOWN request received");
  		// Read the SMS port
  		try {
	  		smsPort = Integer.parseInt(new String(pkt.getData()));
  		}
  		catch (NumberFormatException nfe) {
  			nfe.printStackTrace();
  		}
  		inpHolder.resetConnection(true);
  		outHolder.resetConnection();
  		connectionDropped = true;
  		return null;
  	}
  	else {
  		return super.handlePacket(pkt);
  	}
  }
  			
  private void requestRefresh() {
  	if (msisdn.startsWith("39")) {
  		msisdn = "+"+msisdn;
  	}
  	theSMSManager.sendTextMessage(msisdn, smsPort, null);
  }
}

