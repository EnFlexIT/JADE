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

/**
   @author Fabio Bellifemine - CSELT S.p.A.
   @version $Date$ $Revision$
*/

/**
   This class represents the done predicate ontological role i.e. the 
   fact that an action has been performed by an agent
*/
public class DonePredicate {
	Action s;
	
	
	/**
 	  This method sets the action expression indicating the action that 
 	  has been performed and the agent who performed it.
 	  @see jade.onto.basic.Action
	*/
	public void set_0(Action a){s=a;}
	
	/**
 	  This method gets the action expression indicating the action that 
 	  has been performed and the agent who performed it.
 	  @see jade.onto.basic.Action
	*/
	public Action get_0() {return s;}
}
