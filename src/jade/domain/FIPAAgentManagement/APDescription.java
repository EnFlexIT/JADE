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

import java.io.Writer;
import java.io.IOException;
/**
   Agent platform description.
   @see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
   @author Giovanni Rimassa - Universita` di Parma
   @version $Date$ $Revision$
 */
public class APDescription {

  private String name;
  private boolean dynamic;
  private boolean mobility;
  private APTransportDescription transportProfile;

  public void setName(String n) {
    name = n;
  }

  public String getName() {
      return name;
  }

  public void setDynamic(Boolean d) {
    dynamic = d.booleanValue();
  }

  public Boolean getDynamic() {
    return new Boolean(dynamic);
  }

  public void setMobility(Boolean m) {
    mobility = m.booleanValue();
  }

  public Boolean getMobility() {
    return new Boolean(mobility);
  }

  public void setTransportProfile(APTransportDescription aptd) {
    transportProfile = aptd;
  }

  public APTransportDescription getTransportProfile() {
    return transportProfile;
  }
  
  public void toText(Writer w) {
  try {
    w.write("( ap-description ");
    if ((name!=null)&&(name.length()>0))
      w.write(" :name " + name);
  
    w.write(" :dynamic "+ dynamic);
    	
    w.write(" :mobility " + mobility);
    
    if(transportProfile != null)
    	{
    		w.write(" :transport-profile ");
    	  transportProfile.toText(w);
    	}
    		
    	
    w.write(")");
    w.flush();
  } catch(IOException ioe) {
    ioe.printStackTrace();
  }
	}

}