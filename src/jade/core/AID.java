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

package jade.core;


import java.io.Serializable;
import java.io.Writer; // FIXME: This must go away
import java.io.IOException; // FIXME: This must go away

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.Enumeration;

/**
 This class represents a JADE Agent Identifier. JADE internal agent
 tables use this class to record agent names and addresses.
 */
public class AID implements Cloneable, Comparable, Serializable {
  
	/**
  @serial
  */
	private String name = new String();
  
	/**
  @serial
  */
	private List addresses = new ArrayList();
  
	/**
  @serial
  */
  private List resolvers = new ArrayList();
  
  /**
  @serial
  */
  private Properties userDefSlots = new Properties();


  /**
   * Constructs an Agent-Identifier whose slot name is set to an empty string
   */
  public AID() {
    this("");
  }

  /** constructor for an Agent-identifier
   * @param guid is the Globally Unique identifer for the agent. The slot name
   * assumes that value in the constructed object. 
   */
  public AID(String guid) {
    name =guid;
  }

  public void setName(String n){
    name = n;
  }

  public String getName(){
    return name;
  }

  public void addAddresses(String url) {
    addresses.add(url);
  }

  public boolean removeAddresses(String url) {
    return addresses.remove(url);
  }

  public void clearAllAddresses(){
    addresses.clear();
  }

  public Iterator getAllAddresses(){
    return addresses.iterator();
  }

  public void addResolvers(AID aid){
    resolvers.add(aid);
  }

  public boolean removeResolvers(AID aid){
    return resolvers.remove(aid);
  }

  public void clearAllResolvers(){
    resolvers.clear();
  }

  public Iterator getAllResolvers() {
    return resolvers.iterator();
  }

  public void addUserDefinedSlot(String key, String value){
    userDefSlots.setProperty(key, value);
  }

  /**
   * @return all the user-defined slots as a <code>java.util.Properties</code> java Object.
   */
  public Properties getAllUserDefinedSlot(){
    return userDefSlots;
  }

  /**
   * This method is called from ACLMessage in order to create
   * the String encoding of an ACLMessage.
   */
  public void toText(Writer w) {
  try {
    w.write("( agent-identifier ");
    if ((name!=null)&&(name.length()>0))
      w.write(":name "+name);
    if (addresses.size()>0)
      w.write(":addresses (sequence ");
    for (int i=0; i<addresses.size(); i++)
      try {
	w.write((String)addresses.get(i) + " ");
      } catch (IndexOutOfBoundsException e) {e.printStackTrace();}
    if (addresses.size()>0)
      w.write(")");
    if (resolvers.size()>0)
      w.write(":resolvers (sequence ");
    for (int i=0; i<resolvers.size(); i++) { 
      try {
	((AID)resolvers.get(i)).toText(w);
      } catch (IndexOutOfBoundsException e) {e.printStackTrace();}
      w.write(" ");
    }
    if (resolvers.size()>0)
      w.write(")");
    Enumeration e = userDefSlots.propertyNames();
    String tmp;
    while (e.hasMoreElements()) {
      tmp = (String)e.nextElement();
      w.write(" " + tmp + " " + userDefSlots.getProperty(tmp));
    }
    w.write(")");
    w.flush();
  } catch(IOException ioe) {
    ioe.printStackTrace();
  }
}

  public String toString() {
    return name;
  }

  public synchronized Object clone() {
    AID result;
    try {
      result = (AID)super.clone();
    }
    catch(CloneNotSupportedException cnse) {
      result = new AID();
      // throw new InternalError(); // This should never happen
    }
    return result;
  }

  /**
    Equality operation. This method compares an <code>AID</code> object with
    another or with a Java <code>String</code>. The comparison is case
    insensitive.
    @param o The Java object to compare this <code>AID</code> to.
    @return <code>true</code> if one of the following holds:
    <ul>
    <li> The argument <code>o</code> is an <code>AID</code> object
    with the same <em>GUID</em> in its name slot (apart from
    differences in case).
    <li> The argument <code>o</code> is a <code>String</code> that is
    equal to the <em>GUID</em> contained in the name slot of this
    Agent ID (apart from differences in case).
    </ul>
  */
  public boolean equals(Object o) {

    if(o instanceof String) {
      return name.equalsIgnoreCase((String)o);
    }
    try {
      AID id = (AID)o;
      return name.equalsIgnoreCase(id.name);
    }
    catch(ClassCastException cce) {
      return false;
    }

  }


  /**
     Comparison operation. This operation imposes a total order
     relationship over Agent IDs.
     @param o Another <code>AID</code> object, that will be compared
     with the current <code>AID</code>.
     @return -1, 0 or 1 according to the lexicographical order of the
     <em>GUID</em> of the two agent IDs, apart from differences in
     case.
  */
  public int compareTo(Object o) {
    AID id = (AID)o;
    return name.compareToIgnoreCase(id.name);
  }


  /**
    Hash code. This method returns an hash code in such a way that two
    <code>AID</code> objects with equal names or with names differing
    only in case have the same hash code.
    @return The hash code for this <code>AID</code> object.
  */
  public int hashCode() {
    return name.toLowerCase().hashCode();
  }

  String getLocalName() {
    int atPos = name.lastIndexOf('@');
    if(atPos == -1)
      return name;
    else
      return name.substring(0, atPos);
  }

  String getHap() {
    int atPos = name.lastIndexOf('@');
    if(atPos == -1)
      return name;
    else
      return name.substring(atPos + 1);
  }

}