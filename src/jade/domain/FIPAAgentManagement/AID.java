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

import java.util.*;
import java.io.*;

/**
 * This class represents the AgentIdentifier concept in this ontology.
 * It has various get- and set- methods, according to the 
 * rules for ontological classes in JADE.
 * <p>
 * This class complies with the frame specified in the Agent Management
 * document no. 23 of FIPA 2000.
 * The name is a GUID for the agent. A simple way to construct it is
 * by concatenating the local name to the HAP identifier.
    @see jade.onto.Ontology
  */
public class AID implements Cloneable, Serializable {
 private String name = new String();
 private List addresses = new ArrayList();
 private List resolvers = new ArrayList();
 private Properties userDefSlots = new Properties();

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
  userDefSlots.setProperty(key,value);
}

  /**
   * This method is called from ACLMessage in order to create
   * the String encoding of an ACLMessage.
   */
  public void toText(Writer w) {
  try {
    w.write("(AID ");
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
    Hash code. This method returns an hash code in such a way that two
    <code>AID</code> objects with equal names or with names differing
    only in case have the same hash code.
    @return The hash code for this <code>AID</code> object.
  */
  public int hashCode() {
    return name.toLowerCase().hashCode();
  }

}
