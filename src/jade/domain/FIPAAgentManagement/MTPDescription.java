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
import java.io.Writer;
import java.io.IOException;

import jade.content.*;

/**
   Description of a message transport protocol.
   @see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
   @author Fabio Bellifemine - CSELT
   @version $Date$ $Revision$

 */
public class MTPDescription implements Concept {

  private String profile;
  private String name;
  private List addresses = new ArrayList();

  public void setProfile(String p) {
    profile = p;
  }

  public void setMtpName(String n) {
    name = n;
  }

  public String getProfile() {
    return profile;
  }

  public String getMtpName() {
    return name;
  }

  public void addAddresses(String a) {
    addresses.add(a);
  }

  public boolean removeAddresses(String a) {
    return addresses.remove(a);
  }

  public void clearAllAddresses() {
    addresses.clear();
  }

  public Iterator getAllAddresses() {
    return addresses.iterator();
  }
 


    public String toString() {
	StringBuffer str = new StringBuffer("( mtp-description ");
        if((profile != null) && (profile.length()>0))
	    str.append(" :profile "+ profile);
	if ((name!=null)&&(name.length()>0))
	    str.append(" :mtp-name " + name);
	if (addresses.size()>0) {
	    str.append(" :addresses (sequence ");
	    for (int i=0; i<addresses.size(); i++)
		try {
		    str.append((String)addresses.get(i) + " ");
		} catch (IndexOutOfBoundsException e) {e.printStackTrace();}
	    str.append(")");
	}
	str.append(")");
	return str.toString();
    }
    
	/**
     * @deprecated the <code>toString</code> method should be used instead
     * of this method.
     **/  
  	public void toText(Writer w) {
      try {
		  w.write(toString());
	  	w.flush();
      } catch(IOException ioe) {
	  	ioe.printStackTrace();
      }
	}

}
