/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

/**
   @author Fabio Bellifemine - CSELT S.p.A.
   @version $Date$ $Revision$
*/

/**
   This class represents the action expression ontological role i.e.
   a t-uple including an agent identifier and an action performed 
   by that agent.
*/
public class Action {
	AID actor;
	Object action;
	
	
	/**
  	 Sets the identifier of the agent performing the action.
	*/
	public void set_0(AID a) { actor=a;}
	
	/**
  	 Gets the identifier of the agent performing the action.
	*/
	public AID get_0() {return actor;}
	
	/**
  	 Sets the action object 
	*/
	public void set_1(Object a) { action=a;}
	
	/**
  	 Gets the action object 
	*/	
	public Object get_1() {return action;}
	
	/**
  	 Sets the identifier of the agent performing the action.
  	 This is equivalent to <code>set_0()</code> 
	*/	
	public void setActor(AID a) {set_0(a); }
	
	/**
  	 Gets the identifier of the agent performing the action.
  	 This is equivalent to <code>get_0()</code> 
	*/	
	public AID getActor() { return get_0(); }
	
	/**
  	 Sets the action object. 
  	 This is equivalent to <code>set_1()</code> 
	*/	
	public void setAction(Object a) {set_1(a);}
	
	/**
  	 Gets the action object 
  	 This is equivalent to <code>get_1()</code> 
	*/	
	public Object getAction() { return get_1();}

}