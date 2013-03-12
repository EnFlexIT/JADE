/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
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
package jade.android;

import jade.core.Profile;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author Federico Bergenti - Universita' di Parma
 */
public class AndroidHelper {
	public static final String LOOPBACK = "127.0.0.1";
	

	public static boolean isEmulator() {
		return android.os.Build.MODEL.toLowerCase().contains("sdk");
	}

	public static String getLocalIPAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						int useIPVersion = Integer.parseInt(System.getProperty(Profile.IP_VERSION, Profile.DEFAULT_IPV));
						if (!inetAddress.isLoopbackAddress()) {
							switch (useIPVersion) {
							case Profile.IPV4:
								if (inetAddress instanceof Inet4Address) {
									return  inetAddress.getHostAddress().toString();
								}
								break;
							case Profile.IPV6:
								if (inetAddress instanceof Inet6Address) {
									return  inetAddress.getHostAddress().toString();
								}
								break;
							default:
								return  inetAddress.getHostAddress().toString();
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			// Blank
		}

		return LOOPBACK;
	}
}
