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


package jade.domain.JADEAgentManagement;

/**
   
   @author Giovanni Rimassa -  Universita` di Parma
   @version $Date$ $Revision$
*/

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import jade.core.ContainerID;

public class CreateAgent {

  private String agentName;
  private String className;
  private ContainerID container;
  private String password;
  private List arguments = new ArrayList();
  
  public void setAgentName(String an) {
    agentName = an;
  }

  public String getAgentName() {
    return agentName;
  }

  public void setClassName(String cn) {
    className = cn;
  }

  public String getClassName() {
    return className;
  }

  public void setContainer(ContainerID cid) {
    container = cid;
  }

  public ContainerID getContainer() {
    return container;
  }

  public void setPassword(String p) {
    password = p;
  }

  public String getPassword() {
    return password;
  }
  
  
  public void addArguments(Object a)
  {
    arguments.add(a);
  }
  
  
  public Iterator getAllArguments()
  {
  	
  	return arguments.iterator();
  }
}
