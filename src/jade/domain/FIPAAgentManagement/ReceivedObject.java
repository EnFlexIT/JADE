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
/** 
* 
* @see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
* @author Fabio Bellifemine - CSELT S.p.A.
* @version $Date$ $Revision$
*/

import jade.util.leap.Serializable;
import java.util.Date;

public class ReceivedObject implements Serializable {

  private String by;
  private String from;
  private Date date;
  private String id;
  private String via;

    /**
     * The constructor initializes the date to current time and 
     * all the Strings to an empty string.
     **/
    public ReceivedObject() {
	date = new Date();
	by = new String();
	from = new String();
	id = new String();
	via = new String();
  
    }
  public void setBy(String b) {
    by = b;
  }

  public String getBy() {
    return by;
  }

  public void setFrom(String f) {
    from = f;
  }

  public String getFrom() {
    return from;
  }

  public void setDate(Date d) {
    date = d;
  }

  public Date getDate() {
    return date;
  }

  public void setId(String i) {
    id = i;
  }

  public String getId() {
    return id;
  }

  public void setVia(String v) {
    via = v;
  }

  public String getVia() {
    return via;
  }


    public String toString() {
	String s = new String("(ReceivedObject ");
	if (date != null)
	    s = s + " :date "+date.toString();
	if ((by != null) && (by.trim().length()>0))
	    s = s + " :by "+by;
	if ((from != null) && (from.trim().length()>0))
	    s = s + " :from "+from;
	if ((id != null) && (id.trim().length()>0))
	    s = s + " :id "+id;
	if ((via != null) && (via.trim().length()>0))
	    s = s + " :via "+via;
	return s;
    }
}
