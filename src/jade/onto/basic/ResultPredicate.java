/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

This work has been partially supported by the IST-1999-10211 LEAP Project

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

import java.util.*;

/**
   @author Fabio Bellifemine - CSELT S.p.A.
   @version $Date$ $Revision$
*/

/**
   This class represents the result predicate ontological role, i.e. the
   result of an action performed by an agent.
*/
public class ResultPredicate {
	private Action s;
	List result = new ArrayList();
	
	
	/**
 	  This method sets the action expression for of which the result is
 	  indicated.
 	  @see jade.onto.basic.Action
	*/
	public void set_0(Action a){s=a;}
	
	/**
 	  This method gets the action expression for of which the result is
 	  indicated.
 	  @see jade.onto.basic.Action
	*/
	public Action get_0() {return s;}
	
	/**
 	  This method adds an item to the sequence of items representing the 
 	  result
	*/
	public void add_1(Object o) {result.add(o);}
	
	/**
 	  This method gets an <code>Iterator</code> over the sequence of 
 	  items representing the result
	*/
	public Iterator getAll_1(){return result.iterator();}
}