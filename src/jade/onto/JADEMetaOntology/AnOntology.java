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


package jade.onto.JADEMetaOntology;
import java.util.ArrayList;
import java.util.Iterator;


public class AnOntology {
  private String name;
  private ArrayList roles = new ArrayList();
  public AnOntology() {}
  public void setName(String n) { name=n; }
  public String getName() {return name;}
  public void set_0(String n) { setName(n); }
  public String get_0() {return getName();}
  public void addRoles(Role r) {roles.add(r); }
  public boolean removeRoles(Role r) {return roles.remove(r);}
  public void clearAllRoles() {roles.clear();}
  public Iterator getAllRoles() {return roles.iterator();}
  public void add_1(Role r) {addRoles(r);}
  public Iterator getAll_1() {return getAllRoles();}
}
