/**
 * @version $Id: OntologyObject.java,v 1.1 1999/01/29 03:34:13 bellifemine Exp bellifemine $
 *
 * Copyright (c) 1998 CSELT Centro Studi e Laboratori Telecomunicazioni S.p.A.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * CSELT Centro Studi e Laboratori Telecomunicazioni S.p.A. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the agreement you entered into with CSELT.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 */

package demo.MeetingScheduler.CLP;

import java.util.*;
import java.lang.*;

/**
 * This class implements each node of the tree returned by the parser of 
 * the content language.
 * <p>
 * 6 types of nodes have been implemented, where the method 
 * <code>getType()</code> returns the node type:
 * <ul>
 * <li> <code>OBJECT_TYPE</code>, when the node represents an object of the 
 * world 
 * <li> <code>ACTION_TYPE</code>, when the node represents an action
 * <li> <code>PROPOSITION_TYPE</code>, when the node represents a proposition
 * <li> <code>OBJECTDESCRIPTOR_TYPE</code>, when the node represents an 
 * identifying expression, i.e. the iota operator
 * <li> <code>SIMPLEVALUE_TYPE</code>, when the node represents a single value
 * <li> <code>MULTIVALUE_TYPE</code>, when the node represents several values
 * <p> Each type of node is implemented by a specific class as shown in
 * the following figure
 * <p>
 * <img src="OntologyObject.jpg">
 * @see Concept 
 * @see Action 
 * @see Proposition 
 * @see ObjectDescriptor 
 * @see SimpleValue 
 * @see MultiValue
*/
 /* </ul>
 * <p>
 * In the following it is explained how to access the content 
 * of each type of node.
 * <ul>
 * <li> <code>OBJECT_TYPE</code>. 
 * <ul>
 * <li> <code>getName()</code> returns the object type (e.g. user).
 * <li> <code>getNumberOfSlots()</code> returns the number of slots of the
 * object 
 * <li> <code>getSlotValue(slotname)</code> returns the String representing the
 * value of the slot <code>slotname</code>
 * <li> <code>getSlot(slotname)</code> returns the OntologyObject representing
 * the value of the slot <code>slotname</code>
 * <li> <code>getAllSlots()</code> returns an Enumeration with all the slots
 * of the object
 * </ul>
 *
 * <li> <code>ACTION_TYPE</code>. 
 * This is a specialization of <code>OBJECT_TYPE</code>, where the object name
 * is "action", and the actor,
 * the action name, and the action parameters are represented as slots of the
 * action object.
 * <ul>
 * <li> <code>getNumberOfSlots()</code> returns the number of slots of the
 * object, that is, usually, 3. 
 * <li> <code>getSlotValue(slotname)</code> returns the String representing the
 * value of the slot <code>slotname</code>. It must be used to get the actor
 * (i.e. <code>getSlotValue("agent-name")</code>), and to get the action name
 * (i.e. <code>getSlotValue("action-type")</code>).
 * <li> <code>getSlot(slotname)</code> returns the OntologyObject representing
 * the value of the slot <code>slotname</code>. It is best to be used to
 * get the action parameters (i.e. <code> getSlot("action-parameters")</code>)
 * <li> <code>getAllSlots()</code> returns an Enumeration with all the slots
 * of the object
 * </ul>
 *
 * <li> <code>PROPOSITION_TYPE</code>. 
 * This is a specialization of <code>OBJECT_TYPE</code>, where the object name
 * is the Proposition Symbol (it can be got via <code>getName()</code>), 
 * and the elements of the propositions 
 * are represented as slots of the
 * object.
 * 
 * <li> <code>OBJECTDESCRIPTOR_TYPE</code>.
 * <ul>
 * <li> <code>getName()</code> returns the name of the iota variable 
 * <li> all the terms of the conjunctions are represented as slots
 * <li> <code>getIotaPattern()</code> returns the number of the pattern
 * bound to the iota variable (-1 when no pattern has been bound)
 * </ul>
 *
 * <li> <code>SIMPLEVALUE_TYPE</code>.
 * <ul>
 * <li> <code>getName()</code> returns the String representing the value
 * </ul>
 *
 * <li> <code>MULTIVALUE_TYPE</code>.
 * <ul>
 * <li> each slot of this node represent a value, and can be retrived
 * via the <code>getSlot()</code>, <code>getSlotValue()</code>, and 
 * <code>getAllSlots()</code> methods.
 * </ul>
 * </ul>
 */
public class OntologyObject {

  private int    type;
  /** 
   * this constant indicates a node whose type is OBJECT_TYPE
   */
  public static final int OBJECT_TYPE = 0; 
  /** 
   * this constant indicates a node whose type is ACTION_TYPE
   */
  public static final int ACTION_TYPE = 1; 
  /** 
   * this constant indicates a node whose type is PROPOSITION_TYPE
   */
  public static final int PROPOSITION_TYPE = 2; 
  /** 
   * this constant indicates a node whose type is OBJECTDESCRIPTOR_TYPE
   */
  public static final int OBJECTDESCRIPTOR_TYPE = 3;
  /** 
   * this constant indicates a node whose type is SIMPLEVALUE_TYPE
   */
  public static final int SIMPLEVALUE_TYPE = 4;
  /** 
   * this constant indicates a node whose type is MULTIVALUE_TYPE
   */
  public static final int MULTIVALUE_TYPE = 5;

  private String name;
  
  private Hashtable slots;
  private final int INITIAL_CAPACITY = 3;
  private final float LOAD_FACTOR = 0.8f;   

  
  /**
   * constructor of the OntologyObject
   */
  public OntologyObject(){
    slots = new Hashtable(INITIAL_CAPACITY, LOAD_FACTOR);
  }

        
  //  public OntologyObject (Parser p, String s) {
  //}
  
  /**
   * returns the type of the node
   */
  public int getType() {
   return type;
  }

  /**
   * set the type of the node
   */
protected void setType(int t) {
   type = t;
}

  /**
   * sets the name of the node
   */
  protected void setName(String n){
    this.name=n;
  }

  /**
   * returns the name of the node
   */
  protected String getName() {
   return name;
  }



  /**
   * adds a Slot to the node
   * @param name is the name of the slot
   * @param value is the OntologyObject representing the slot value
   */
    protected void addSlot( String name, OntologyObject value) {
     this.slots.put(name, value);
    }



  /**
   * returns the OntologyObject representing a slot 
   * @param name is the name of the slot
   */
    protected OntologyObject getSlot (String name) {
     return((OntologyObject)slots.get(name));
    }

  /**
   * returns the String representing a slot value
   * @param name is the name of the slot
   */
    protected String getSlotValue(String name) {
     return(slots.get(name).toString());
    }

  /**
   * returns the number of slots in this object
   */
    protected int getNumberOfSlots(){
      return slots.size();
    }


  /**
   * returns an Enumaeration with all the slots in this object
   */
    protected Enumeration getAllSlots() {
      return slots.elements();
    }
 
  /**
   * returns an  Enumeration with all the slot names in this object 
   */   
protected Enumeration getAllSlotNames() {
return slots.keys();
}


  /**
   * returns a String representing the object
   */
    public String toString() {
      switch (getType()) {
      case SIMPLEVALUE_TYPE: { 
	SimpleValue obj = (SimpleValue)this;
	return obj.toString();
      }
      case OBJECT_TYPE: {
	Concept obj = (Concept)this;
	return obj.toString();
      }
      case MULTIVALUE_TYPE: {
	MultiValue obj = (MultiValue)this;
	return obj.toString();
      }
      case ACTION_TYPE: {
	Action obj = (Action)this;
	return obj.toString();
      }
      case PROPOSITION_TYPE: {
	Proposition obj = (Proposition)this;
	return obj.toString();
      }
      case OBJECTDESCRIPTOR_TYPE: {
	ObjectDescriptor obj = (ObjectDescriptor)this;
	return obj.toString();
      }
      }
    return "Some Errors in the OntologyObject code";
    }
          

}











