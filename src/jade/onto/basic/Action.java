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

package jade.onto.basic;
import jade.core.AID;

public class Action {
AID actor;
Object action;
public void set_0(AID a) { actor=a;}
public AID get_0() {return actor;}
public void set_1(Object a) { action=a;}
public Object get_1() {return action;}
public AID getActor() { return get_0(); }
public void setActor(AID a) {set_0(a); }
public Object getAction() { return get_1();}
public void setAction(Object a) {set_1(a);}

}
