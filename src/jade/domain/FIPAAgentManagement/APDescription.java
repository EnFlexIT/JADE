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

import jade.content.Concept;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import jade.util.leap.ArrayList;
 
/**
   Agent platform description.
   @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
   @author Giovanni Rimassa - Universita` di Parma
   @author Alessandro Chiarotto - TILAB
   @version $Date$ $Revision$
 */
 
public class APDescription implements Concept {

  private String name;
  private List services = new ArrayList(1); 

  public void setName(String n) {
    name = n;
  }

  public String getName() {
      return name;
  }

  
   public void addAPServices(APService a) {
      services.add(a);
    }

    public boolean removeAPServices(APService a) {
      return services.remove(a);
    }

	public void clearAllAPServices(){
	  services.clear();
	}
	
	public Iterator getAllAPServices(){
	  return services.iterator();
	}

    /**
     * @return an SL0-like String representation of this object 
     **/
    public String toString() {
	StringBuffer str = new StringBuffer("( ap-description ");
	if ((name!=null)&&(name.length()>0))
	    str.append(" :name " + name);
        APService s=null;
        str.append(" :ap-services (set");
        for (Iterator i=services.iterator(); i.hasNext(); ) {
            s=(APService)(i.next());
            str.append(" "+s.toString());
        }
	str.append("))");
    	return str.toString();
    }

    
}
