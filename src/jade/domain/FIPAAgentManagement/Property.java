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
import jade.util.leap.*;
import jade.content.Concept;

/** 
 This class implements the property type, a pair of a name and value.
 @see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
 @author Fabio Bellifemine - CSELT S.p.A.
 @version $Date$ $Revision$
*/

  public class Property implements Concept {

    private String name;
    private Object value;
    
public Property() {
}

public Property(String name, Object value) {
	this.name = name;
	this.value = value;
}

public void setName(String n) {
  name = n;
}

public String getName() {
  return name;
}

public void setValue(Object o) {
  value = o;
}

public Object getValue(){
  return value;
}
  }
