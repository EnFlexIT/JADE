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


import jade.util.leap.Serializable;
import jade.util.leap.Comparable;
import java.io.Writer; // FIXME: This must go away
import java.io.IOException; // FIXME: This must go away
//import java.io.StringWriter; 

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;
import java.util.Enumeration;

/**
 This class represents a JADE Agent Identifier. JADE internal agent
 tables use this class to record agent names and addresses.
 */
public class AID implements Comparable, Serializable {
  
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
    this("",ISGUID);
  }

  /** Constructor for an Agent-identifier
   * This constructor (which is deprecated), examines the name
   * to see if the "@" chararcter is present.  If so, it calls 
   * <code> this(name, ISGUID)<code> 
   * otherwise it calls <code>this(name, ISLOCALNAME)</code>
   * This ensures better compatibility with JADE2.2 code.
   * @param guid is the Globally Unique identifer for the agent. The slot name
   * assumes that value in the constructed object. 
   * @deprecated This constructor might generate a wrong AID, if
   * the passed parameter is not a guid (globally unique identifier), but
   * the local name of an agent (e.g. "da0"). 
   * @see AID#AID(String boolean)
   */
  public AID(String guid) {
      this(guid,ISGUID);
  }


    /** Constructor for an Agent-identifier
     * @param name is the value for the slot name for the agent. 
     * @param isGUID indicates if the passed <code>name</code>
     * is already a globally unique identifier or not. Two
     * constants <code>ISGUID</code>, <code>ISLOCALNAME</code>
     * have also been defined for setting a value for this parameter.
     * If the name is a local name, then the HAP (Home Agent Platform)
     * is concatenated to the name, separated by  "@".
     **/
    public AID(String name, boolean isGUID) {
	// initialize the static variable atHAP, if not yet initialized
	if (atHAP == null)
	    atHAP = "@"+AgentContainerImpl.getPlatformID();
	if (isGUID)
	    setName(name);
	else
	    setLocalName(name);
    }

    /** constant to be used in the constructor of the AID **/
    public static final boolean ISGUID = true;
    /** constant to be used in the constructor of the AID **/
    public static final boolean ISLOCALNAME = false;

    /** private variable containing the right part of a local name **/
    private static String atHAP = null; 

  /**
  * This method permits to set the symbolic name of an agent.
  * The passed parameter must be a GUID and not a local name. 
  */
  public void setName(String n){
    name = n.trim();
  }

  /**
  * This method permits to set the symbolic name of an agent.
  * The passed parameter must be a local name. 
  */
  public void setLocalName(String n){
    name = n.trim();
    if ((name != null) && (!name.endsWith(atHAP))) 
	name = name.concat(atHAP); 
  }

  /**
  * This method returns the name of the agent.
  */
  public String getName(){
    return name;
  }

  /**
  * This method permits to add a transport address where 
  * the agent can be contacted.
  * The address is added only if not yet present
  */
  public void addAddresses(String url) {
  	if (!addresses.contains(url)) {
	    addresses.add(url);
  	}
  }

  /**
  * To remove a transport address.
  * @param url the address to remove
  * @return true if the addres has been found and removed, false otherwise.
  */
  public boolean removeAddresses(String url) {
    return addresses.remove(url);
  }
  
  /**
  * To remove all addresses of the agent
  */
  public void clearAllAddresses(){
    addresses.clear();
  }

  /**
  * Returns an iterator of all the addresses of the agent.
  * @see jade.util.leap.Iterator
  */
  public Iterator getAllAddresses(){
    return addresses.iterator();
  }

  /**
  * This method permits to add the AID of a resolver (an agent where name 
  * resolution services for the agent can be contacted) 
  */
  public void addResolvers(AID aid){
    resolvers.add(aid);
  }

  /**
  * To remove a resolver.
  * @param aid the AID of the resolver to remove
  * @return true if the resolver has been found and removed, false otherwise.
  */
  public boolean removeResolvers(AID aid){
    return resolvers.remove(aid);
  }

  /**
  * To remove all resolvers.
  */
  public void clearAllResolvers(){
    resolvers.clear();
  }

  /**
  * Returns an iterator of all the resolvers.
  * @see jade.util.leap.Iterator
  */
  public Iterator getAllResolvers() {
    return resolvers.iterator();
  }

  /**
  * To add a user defined slot (a pair key, value).
  * @param key the name of the property
  * @param value the corresponding value of the property
  */
  public void addUserDefinedSlot(String key, String value){
    userDefSlots.setProperty(key, value);
  }

  
  /**
  * Returns an array of string containing all the addresses of the agent
  */
  public String[] getAddressesArray() {
    Object[] objs = addresses.toArray();
    String[] result = new String[objs.length];
    System.arraycopy(objs, 0, result, 0, objs.length);
    return result;
  }

  /**
  * Returns an array containing all the AIDs of the resolvers.
  */
  public AID[] getResolversArray() {
    Object[] objs = resolvers.toArray();
    AID[] result = new AID[objs.length];
    System.arraycopy(objs, 0, result, 0, objs.length);
    return result;
  }

  /**
  * Returns the user-defined slots as properties. 
  * @return all the user-defined slots as a <code>jade.util.leap.Properties</code> java Object.
  * @see jade.util.leap.Properties
  */
  public Properties getAllUserDefinedSlot(){
    return userDefSlots;
  }


    /**
     * @return the String full representation of this AID
     **/
    public String toString() {
	StringBuffer s = new StringBuffer("( agent-identifier ");
	if ((name!=null)&&(name.length()>0)) {
	    s.append(" :name ");
			s.append(name);
	}
	if (addresses.size()>0)
	    s.append(" :addresses (sequence ");
	for (int i=0; i<addresses.size(); i++)
	    try {
				s.append((String)addresses.get(i));
				s.append(" ");
	    } 
	    catch (IndexOutOfBoundsException e) {e.printStackTrace();}
	if (addresses.size()>0)
	    s.append(")");
	if (resolvers.size()>0)
	    s.append(" :resolvers (sequence ");
	for (int i=0; i<resolvers.size(); i++) { 
	    try {
				s.append(resolvers.get(i).toString());
	    } 
	    catch (IndexOutOfBoundsException e) {e.printStackTrace();}
	    s.append(" ");
	}
	if (resolvers.size()>0)
	    s.append(")");
	Enumeration e = userDefSlots.propertyNames();
	String tmp;
	while (e.hasMoreElements()) {
	    tmp = (String)e.nextElement();
	    s.append(" ");
	    s.append(tmp);
	    s.append(" ");
	    s.append(userDefSlots.getProperty(tmp));
	}
	s.append(")");
	return s.toString();
    }

  /**
   * This method is called from ACLMessage in order to create
   * the String encoding of an ACLMessage.
   * @deprecated replaced by the method toString
   */
  public void toText(Writer w) {
  try {
      w.write(toString());
      w.flush();
  } catch(IOException ioe) {
    ioe.printStackTrace();
  }
  }



  /**
  * Clone the AID object.
  */
    public synchronized Object clone() {
        AID      result = new AID(this.name);

        result.addresses = (ArrayList)((ArrayList)addresses).clone();

        result.resolvers = (ArrayList)((ArrayList)resolvers).clone();

        // Copying user defined slots
        //Enumeration enum = userDefSlots.propertyNames();
        //while (enum.hasMoreElements()) {
        //    String key = (String) enum.nextElement();
        //    result.addUserDefinedSlot(key, 
        //                              (String) userDefSlots.getProperty(key));
        //}
        result.userDefSlots = userDefSlots;

        return result;
    } 
  /*public synchronized Object clone() {
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
	*/
	
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

      if (o == null)
	  return false;
    if(o instanceof String) {
      return CaseInsensitiveString.equalsIgnoreCase(name, (String)o);
    }
    try {
      AID id = (AID)o;
      return CaseInsensitiveString.equalsIgnoreCase(name, id.name);
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
    return name.toLowerCase().toUpperCase().compareTo(id.name.toLowerCase().toUpperCase());
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

  /**
  * Returns the local name of the agent (without the HAP).
  * If the agent is not local, then the method returns its GUID.
  */
  public String getLocalName() {
    int atPos = name.lastIndexOf('@');
    if(atPos == -1)
      return name;
    else
      return name.substring(0, atPos);
  }

  /**
  * Returns the HAP of the agent.
  */
  String getHap() {
    int atPos = name.lastIndexOf('@');
    if(atPos == -1)
      return name;
    else
      return name.substring(atPos + 1);
  }

}
