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

import jade.mtp.TransportAddress;

public class ContainerID implements Location {

  public static final String DEFAULT_IMTP ="JADE-IMTP"; 

  private String name;
  private String address;


  // Default constructor, used by the ontology engine
  public ContainerID() {
  }

  public ContainerID(String n, TransportAddress a) {
    name = n;
    if(a != null)
      address = a.getHost();
    else
      address = "<Unknown Host>";
  }

  public void setName(String n) {
    name = n;
  }

  public String getName() {
    return name;
  }

  public void setProtocol(String p) {
    // Ignore it
  }

  public String getProtocol() {
    return DEFAULT_IMTP;
  }

  public void setAddress(String a) {
    address = a;
  }

  public String getAddress() {
    return address;
  }

  public String getID() {
    return name + '@' + DEFAULT_IMTP + "://" + address;
  }

  public String toString() {
    return getID();
  }
  
  public boolean equals(Object obj) {
  	try {
  		ContainerID cid = (ContainerID) obj;
  		return CaseInsensitiveString.equalsIgnoreCase(name, cid.getName());
  	}
  	catch (ClassCastException cce) {
  		return false;
  	}
  }
  
  public int hashCode() {
  	return name.toLowerCase().hashCode();
  }
  		
}
