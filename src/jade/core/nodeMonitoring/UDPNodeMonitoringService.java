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

package jade.core.nodeMonitoring;

//#J2ME_EXCLUDE_FILE
//#APIDOC_EXCLUDE_FILE

import jade.core.AgentContainer;
import jade.core.Profile;
import jade.core.ProfileException;
import jade.core.NodeFailureMonitor;

import jade.util.Logger;

/**
   UDP based implementation of the NodeMonitoringService. 
 */
public class UDPNodeMonitoringService extends NodeMonitoringService {
  private static final String PREFIX = "jade_core_nodeMonitoring_UDPNodeMonitoringService_";
  
  /**
     The name of this service
   */
  public static final String NAME = "jade.core.nodeMonitoring.UDPNodeMonitoring";
  
  /**
   * This constant is the name of the property whose value contains an
   * integer representing the port number where the Main Container is
   * listening for UDP pings. 
   */
  public static final String PORT = PREFIX + "port"; 
  
  /**
   * This constant is the name of the property whose value contains an
   * integer representing the time interval (in milliseconds) in which a peripheral
   * container sends UDP ping messages to the Main Container.<br>
   * This property is only meaningful on a peripheral container.
   */
  public static final String PING_DELAY = PREFIX + "pingdelay";
 
  /**
   * This constant is the name of the property whose value contains an
   * integer representing the maximum time (in milliseconds) the main container 
   * waits for a ping message before considering the peripheral container
   * unreachable.<br>
   * This property is only meaningful on a main container.
   */
  public static final String PING_DELAY_LIMIT = PREFIX + "pingdelaylimit";

  /**
   * This constant is the name of the property whose value contains an
   * integer representing the maximum time a node can stay unreachable after it gets removed
   * from the platform.<br>
   * This property is only meaningful on a main container.
   */
  public static final String UNREACHABLE_LIMIT = PREFIX + "unreachablelimit";

  
  /**
   * Default port on which the server is waiting for ping messages
   */
  public static final int DEFAULT_PORT = 28000;
  
  /**
   * Default time between two outgoing pings
   */
  public static final int DEFAULT_PING_DELAY = 1000;
  
  /**
   * Default maximum time the server waits for a ping
   */
  public static final int DEFAULT_PING_DELAY_LIMIT = 3000;
  
  /**
   * Default maximum time a node can stay unreachable
   */
  public static final int DEFAULT_UNREACHABLE_LIMIT = 10000;
  

	private UDPMonitorServer myServer;
	private UDPMonitorClient myClient;
	
	public String getName() {
		return NAME;
	}
	
	public void init(AgentContainer ac, Profile p) throws ProfileException {
		super.init(ac, p);
		
	  String host =  p.getParameter(Profile.MAIN_HOST, Profile.getDefaultNetworkName());
	  
    int port = getPosIntValue(p, PORT, DEFAULT_PORT);
    int pingDelay = getPosIntValue(p, PING_DELAY, DEFAULT_PING_DELAY);
    int pingDelayLimit = getPosIntValue(p, PING_DELAY_LIMIT, DEFAULT_PING_DELAY_LIMIT);
    int unreachLimit= getPosIntValue(p, UNREACHABLE_LIMIT, DEFAULT_UNREACHABLE_LIMIT);
	  		  
		if (ac.getMain() != null) {
			// We are on the main container --> launch a UDPMonitorServer
			try {
				// Note that the server will be started as soon as a NodeFailureMonitor will register.
				myServer = new UDPMonitorServer(port, pingDelayLimit, unreachLimit);
				myLogger.log(Logger.INFO, "UDPMonitorServer successfully created. Port = "+port+" pingdelaylimit = "+pingDelayLimit+" unreachablelimit = "+unreachLimit);
			}
			catch (Exception e)  {		  
				String s = "Error creating UDP monitoring server";
				myLogger.log(Logger.SEVERE, s);
				throw new ProfileException(s, e);
			}
		}
		else {
			// We are on a peripheral container --> launch a UDPMonitorClient
			try {
			  myClient = new UDPMonitorClient(ac.getNodeDescriptor().getNode(), host, port, pingDelay);
			  myClient.start();
				myLogger.log(Logger.INFO, "UDPMonitorClient successfully started. Host = "+host+" port = "+port+" pingdelay = "+pingDelay);
			}
			catch (Exception e)  {		  
				String s = "Error starting UDP monitoring client";
				myLogger.log(Logger.SEVERE, s);
				throw new ProfileException(s, e);
			}
		}          
	}
	
	public NodeFailureMonitor getFailureMonitor() {
		if (myServer != null) {
			return new UDPNodeFailureMonitor(myServer);
		}
		else {
			return null;
		}
	}
	
	public void shutdown() {
		// No need to stop the server since it has been stopped when there were
		// no more NodeFailureMonitor-s registered with it.
		if (myClient != null) {
      myClient.stop();
		}
	}

  /**
   * Extracts an integer value from a given profile. If the value
   * is less than zero it returns the specified default value
   * @param p profile
   * @param paramName name of the parameter in the profile
   * @param defaultValue default value
   */
  private static int getPosIntValue(Profile p, String paramName, int defaultValue) {
    int value = Integer.valueOf(p.getParameter(paramName, "-1")).intValue();
    if (value >= 0) {
      return value;   
    } else {
     return defaultValue; 
    }
  }
}