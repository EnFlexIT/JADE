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

package jade.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import java.util.Properties;

/**
   This class is concrete implementation of the <code>Profile</code>
   interface, to be used only in the J2SE environment.
   It provides accessor methods to fill in the various configuration
   options.

   @author Giovanni Rimassa - Universita` di Parma
 */
public class ProfileImpl implements Profile {

  private static final String HOST = "host";
  private static final String PORT = "port";
  private static final String PLATFORM_ID = "name";

  private Properties props;

  public String getMainContainerHost() {
    return props.getProperty(HOST);
  }

  public String getMainContainerPort() {
    return props.getProperty(PORT);
  }

  public String getPlatformID() {
    return props.getProperty(PLATFORM_ID);
  }

  /**
     Creates a new profile implementation, with default host and port
     for the main container and the default platform ID.
   */
  public ProfileImpl() {
    Properties def = new Properties();

    try {
      String host = InetAddress.getLocalHost().getHostName();
      def.setProperty(HOST, host);
      def.setProperty(PORT, "1099");
      def.setProperty(PLATFORM_ID, host + ":1099/JADE");

      props = new Properties(def);
    }
    catch(UnknownHostException uhe) {
      uhe.printStackTrace();
    }
  }


  /**
     Creates a new profile implementation, with the given host, port
     and name. To keep the default value for some of them, just pass
     <code>null</code> as the corresponding argument.
     @param host The host name where the Main Container is running.
     @param port The port where the Main Container is listening for
     container registrations.
     @param platformID The unique ID of the platform.
  */
  public ProfileImpl(String host, String port, String platformID) {
    this(); // Call default constructor
    if(host != null)
      props.setProperty(HOST, host);
    if(port != null)
      props.setProperty(PORT, port);
    if(platformID != null)
      props.setProperty(PLATFORM_ID, platformID);
  }

}
