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


package jade.domain.FIPAAgentManagement;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.content.Concept;

/**
   Description of the  services available on an agent
   platform.

   @author Fabio Bellifemine - CSELT
   @version $Date$ $Revision$

 */
public class APService implements Concept {

  private List addresses = new ArrayList(); 
  private String name;
  private String type;

  public APService() {
  }
  
  /**
   * Constructor. Create a new APService where name and type get the same value (i.e.
   * the passed type parameter).
   **/
  public APService(String type, String[] addresses) {
      name=type;
      this.type=type;
      for (int i=0; i<addresses.length; i++)
          this.addresses.add(addresses[i]);
  }
  
  public void setName(String n) {
    name = n;
  }
  public String getName() {
      return name;
  }

  public void setType(String t) {
    type = t;
  }
  public String getType() {
      return type;
  }

  public void addAddresses(String address) {
    addresses.add(address);
  }

  public boolean removeAddresses(String address) {
    return addresses.remove(address);
  }

  public void clearAllAddresses() {
    addresses.clear();
  }

  public Iterator getAllAddresses() {
    return addresses.iterator();
  
  }

    public String toString() {
	StringBuffer str = new StringBuffer("( ap-service ");
        if ((name!=null)&&(name.length()>0))
	    str.append(" :name " + name);
        if ((type!=null)&&(type.length()>0))
	    str.append(" :type " + type);
        String s;
        str.append(" :addresses (sequence");
        for (Iterator i=addresses.iterator(); i.hasNext(); ) {
            s=(String)(i.next());
            str.append(" "+s.toString());
        }
	str.append("))");
	return str.toString();
    }


}
