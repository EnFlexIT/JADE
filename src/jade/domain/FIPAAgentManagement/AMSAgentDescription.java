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

import jade.core.AID;

public class AMSAgentDescription {

public static final String INITIATED = "initiated";
public static final String ACTIVE = "active";
public static final String SUSPENDED = "suspended";
public static final String WAITING = "waiting";
public static final String TRANSIT = "transit";

private AID name;
private String ownership;
private String state;

public void setName(AID n){
  name = n;
}

public void setOwnership(String n) {
  ownership = n;
}

public void setState(String n) {
  state = n;
}

public AID getName(){
  return name;
}

public String getOwnership(){
  return ownership;
}

public String getState(){
  return state;
}
}
