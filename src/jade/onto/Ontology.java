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

package jade.onto;
import java.util.List;
import jade.lang.Codec.CodecException;
import jade.onto.JadeMetaOntology.AnOntology;

/**
	An application specific ontology is represented by a properly initialized
	instance of 
	a class implementing the <code>Ontology</code>interface. It should be noticed 
	in fact that two instances of the same class implementing the 
	<code>Ontology</code> interface can represent two different ontologies,
	provided that they have been initialized differently.<br>	
	<br>
	In the adopted approach, a generic item included in an ontology is called 
	a <b>role</b>. For example the concepts <i>Company<i> and <i>Person</i>, the 
	predicate <i>WorfsFor</i> and	the action <i>Engage</i> can be <b>roles</b> 
	in an ontology dealing with employees.<br>
  Each ontological role is characterised by a <b>name</b> and a structure
	defined in terms of a number of <b>slots</b>. For instance the <i>Person</i> role
	will have the name "person" and some slots describing the person's first-name, 
	family-name and age. In case of an ontological role that is an action the
	slots describes the arguments of the action<br>
	A slot on its turn is characterised by 
	<ul>
		<li> a <b>name</b> identifying the slot </li>
		<li> a <b>category</b> stating that the value of the slot can be a primitive 
		entity such as a string or an integer (<code>PRIMITIVE_SLOT<code>), an 
		instance of another ontologycal role (<code>FRAME_SLOT<code>) or a set 
		(<code>SET_SLOT<code>) or sequence (<code>SEQUENCE_SLOT<code>) 
		of entities.</li>
		<li> a <b>type</b> defining the primitive type (for <code>PRIMITIVE_SLOT<code>)
		or role (for <code>FRAME_SLOT<code>) of the value of the 
		slot or of the elements in the set/sequence in case of <code>SET_SLOT<code> 
		or <code>SEQUENCE_SLOT<code>. </li>
		<li> a <b>presence<b> flag defining whether the slot is mandatory or 
		optional.</li>
	</ul>
	<br>
	Entities in a specific domain, i.e. instances of the ontological roles, 
	can be conveniently represented inside an agent as instances of  
	application-specific Java classes each one representing a role.<br>
	For example the class
	<code>
	public class Person {
		String name;
		int    age;
		
		void setName(String n) { name = n; }
		String getName() { return name; }
		void setAge(int a) { age = a; }
		int getAge() { return age; }
	}
	</code>
	can represent the <i>Person</i> role and instances of this role
	can be represented as <code>Person</code> objects.<br>
	<br>
	An alternative, yet less convenient, way of representing a domain 
	entity is as an instance of the <code>Frame</code> class that is designed 
	so that each entity (regardless of the role it is an instance of)
	can be represented 
	as a <code>Frame</code> object. This class is however mostly used 
	in JADE internal conversions.<br>
	<br>	
	The methods in the <code>Ontology</code> interface allows to 
	<ul>
		<li> Initialize an object representing an ontology (i.e. an
		instance of a class implementing the <code>Ontology</code> interface)
		by adding to it all the ontological roles included in the ontology 
		and specifying for each role the application specific class 
		representing that role</li>
		
		<li> Convert a <code>Frame</code> representing an entity
		into/from an instance of the application specific class 
		representing the role this entity is an instance of</li>
		
		<li> In the above conversion perform all the necessary ontological
		checks e.g. that the age of a person is an integer value</li>
	</ul>
	<br>
  In order to represent an ontological role, a Java class must
  obey to some rules:

  <ol>
   
  	<li><i> Primitive types such as <code>int</code> and <code>boolean</code>
  	cannot be used. Use <code>Integer</code> and <code>Boolean</code> classes
  	instead.</i>
  	</li>
   
  	<li><i> For every <code>slot</code> in the role named <code>XXX</code>, 
  	of category <code>PRIMITIVE_SLOT</code> or <code>FRAME_SLOT</code> and 
  	of type <code>T</code> the class must have two accessible methods, 
  	with the following signature:</i>
    	<ul>
    	<li> <code>T getXXX()</code>
    	<li> <code>void setXXX(T t)</code>
    	</ul>
		</li>
		
  	<li><i> For every <b>slot</b> in the role named <code>XXX</code>,
  	of category <code>SET_TERM</code> or <code>SEQUENCE_TERM</code> and 
  	with elements of type <code>T</code>, the class must have two accessible 
  	methods, with the following signature:</i>
    	<ul>
    	<li> <code>Iterator getAllXXX()</code>
    	<li> <code>void addXXX(T t)</code>
    	</ul>
		</li>
		
   </ol>

   As long as the above rules are followed, any user-defined class
   can be added to the Ontology object. As a useful technique, one
   can define compliant Java interfaces and add them to the
   Ontology; this way useful OO idioms such as polymorphism and
   mix-in inheritance can be exploited for the Java representations
   of ontological objects.

   Due to different lexical conventions between the Java language
   and FIPA ACL and content languages, some name translation must be
   performed to map the name of a slot into the name of the 
   corresponding get and set methods.
   Name translation works as follows:
   <ol>
   <li> Any <code>':'</code> character must be removed.
   <li> Any <code>'-'</code> character must be removed.
   </ol>
   Moreover, a case insensitive match is followed.

   As an example, a role with an integer slot named
   <code>:user-age</code>, will require the following methods (case
   is not important, but according to a popular Java coding
   convention, the two methods have capital letters whenever a
   <code>'-'</code> is present in the slot name):

   <ul>
   <li><code>int getUserAge()</code>
   <li><code>void setUserAge(int age)</code>
   </ul>

	@author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
  @see jade.lang.Codec
  @see jade.onto.Frame
  @see jade.onto.SlotDescriptor
*/

public interface Ontology {

  /**
     Boolean constant for <i>Optional</i> slots.
   */
  static final boolean O = true;

  /**
     Boolean constant for <i>Mandatory</i> slots.
   */
  static final boolean M = false;



  // Constants for slot types. 

  /**
     Constant for <code>boolean</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
	*/
  static final String BOOLEAN_TYPE = "java.lang.Boolean";

  /**
     Constant for <code>byte</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String BYTE_TYPE = "java.lang.Byte";

  /**
     Constant for <code>char</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String CHARACTER_TYPE = "java.lang.Character";

  /**
     Constant for <code>double</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String DOUBLE_TYPE = "java.lang.Double";

  /**
     Constant for <code>float</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String FLOAT_TYPE = "java.lang.Float";

  /**
     Constant for <code>int</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String INTEGER_TYPE = "java.lang.Integer";

  /**
     Constant for <code>long</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String LONG_TYPE = "java.lang.Long";

  /**
     Constant for <code>short</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String SHORT_TYPE = "java.lang.Short";

  /**
     Constant for <code>String</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String STRING_TYPE = "java.lang.String";

  /**
     Constant for <code>byte[]</code> type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String BINARY_TYPE = "java.lang.Byte[]";

  /**
     Constant for <code>java.util.Date</code> type in a 
     <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String DATE_TYPE = "java.util.Date";

  /**
     Constant for any type in a <code>SlotDescriptor</code>.
     @see jade.onto.SlotDescriptor
  */
  static final String ANY_TYPE = "java.lang.Object";


  /** Symbolic constant identifying a frame representing a set **/ 
  public static String NAME_OF_SET_FRAME = "set";
  /** Symbolic constant identifying a frame representing a sequence **/ 
  public static String NAME_OF_SEQUENCE_FRAME = "sequence";


  /**
     Constant for category of slots whose value is an instance of a given
     ontological role (and can therefore be represented as a frame). 
     @see jade.onto.SlotDescriptor
     @see jade.onto.Frame
  */
  static final short FRAME_SLOT = 12;

  /**
     Constant for category of slots whose value is a <code>set</code> of entities
     @see jade.onto.SlotDescriptor
     @see jade.onto.Frame
  */
  static final short SET_SLOT = 13;

  /**
     Constant for category of slots whose value is a <code>sequence</code> of entities
     @see jade.onto.SlotDescriptor
     @see jade.onto.Frame
  */
  static final short SEQUENCE_SLOT = 14;

  /**
     Constant for category of slots whose value is a primitive entity
     @see jade.onto.SlotDescriptor
     @see jade.onto.Frame
  */
  static final short PRIMITIVE_SLOT = 15;

  /**
     Constant for slots whose category is not specified
     @see jade.onto.SlotDescriptor
     @see jade.onto.Frame
  */
  static final short ANY_SLOT = 16;


  

  /**
    Adds to the ontology a role without any application-specific class 
    representing it. 
    @param roleName The name of this role (names are case
    preserving but the match is case insensitive).
    @param slots An array of descriptors; each one of them describes a
    slot of the role, providing:
    <ul>
    <li> The name of the slot.
    <li> The category of the slot
    <li> The type of the slot.
    <li> The optionality of the slot (i.e. whether a value is required or not).
    <li> The position of the slot (implicitly defined by the position in the array).
    </ul>
  */
  void addRole(String roleName, SlotDescriptor[] slots) throws OntologyException;
  
  /**
    Adds to the ontology a role with an application-specific class 
    representing it.  
    @param roleName The name of this role (names are case
    preserving but the match is case insensitive).
    @param slots An array of descriptors; each one of them describes a
    slot of the role, providing:
    <ul>
    <li> The name of the slot.
    <li> The category of the slot
    <li> The type of the slot.
    <li> The optionality of the slot (i.e. whether a value is required or not).
    <li> The position of the slot (implicitly defined by the position in the array).
    </ul>
    @param ref the <code>Class</code> which will be used to create
    application specific Java objects representing instances of the role that
    is being added.
 */
  void addRole(String roleName, SlotDescriptor[] slots, Class c) throws OntologyException;

  /**
    Adds to this ontology all roles included into another ontology 
    @param o The <code>Ontology</code> object whose roles will 
    be added
  */
  void joinOntology(Ontology o) throws OntologyException;

  /**
     Creates a list of Java objects representing each one an instance of 
     a given role, getting the
     information from a given <code>List</code> of <code>Frame</code> 
     objects. This method
     requires that a factory for the given role is registered in this ontology,
     because it creates internally the returned object.
     @param v A <code>List</code> of <code>Frame</code> objects, 
     from which a <code>List</code> of Java objects is built.
     @return A newly created <code>List</code> of Java objects, 
     each Java object corresponding to a <code>Frame</code> and representing an 
     entity in the domain.
     @exception OntologyException If a <code>Frame</code> does
     not represent an instance of any role in the current ontology, 
     or if the registered class does not follow the rules for representing a role.
     @see jade.onto.Ontology#addRole(String roleName, SlotDescriptor[] slots, RoleEntityFactory ref)
  */
  List createObject(List v) throws OntologyException;

  /**
    Creates a <code>Frame</code> object from a given Java object. A
    suitable factory must be registered in the ontology to represent the
    given role, and the given object must be an instance of the class returned
    by the <code>getClassForRole()</code> method of the <code>RoleEntityFactory</code>
    (an indirect instance, i.e. an instance of the class itself or of a subclass).
    @param o The Java object, from which the <code>Frame</code> will
    be built.
    @param roleName The name of the role represented in this ontology by
    the class of the given object. Note that the role name does not 
    necessarily coincide with the name of the class representing the role.
    For this reason the role name must be explicitly indicated.
    @return A <code>Frame</code> object representing an instance of
    the given role, built from the given <code>Object</code>.
    @exception OntologyException If the given role does not exist, or
    the given object is not of the correct class.
  */
  Frame createFrame(Object o, String roleName) throws OntologyException;

  /**
    Checks whether the given <code>Frame</code> object represents a valid
    instance of some role, making sure that every slot has the correct
    category and type and that no mandatory slot has a <code>null</code> value.
    @param f The <code>Frame</code> object to check.
    @exception OntologyException If the check fails.
  */
  void check(Frame f) throws OntologyException;

  /**
    Checks whether the given Java object represents a valid instance of some
    role, making sure that every slot has the correct category and type and 
    that no mandatory slot has a <code>null</code> value.
    @param o The Java object to check.
    @param roleName The role against which to check the given object.
    @exception OntologyException If the check fails.
  */
  void check(Object o, String roleName) throws OntologyException;

  /**
    Tells whether a given string is the name of a role in the current
    ontology.
    @param roleName The name of the role to check.
    @return <code>true</code> if a role with the given name exists,
    <code>false</code> otherwise.
  */
  boolean isRole(String roleName) throws OntologyException;

  /**
    Returns the array of <code>SlotDescriptor</code> objects that
    represent the structure of the given ontological role. 
    @param roleName The name of the ontological role to examine.
    @return The descriptors for the selected ontology role.
    @see jade.onto.SlotDescriptor
  */
  SlotDescriptor[] getSlots(String roleName) throws OntologyException;

  /** 
  	@return the name of the role represented by the passed class as 
  	registered in this ontology
    @throws OntologyException if no role is found for this class
  */
  String getRoleName(Class c) throws OntologyException; 
  
  /**
  	@return a <code>List</code> including the names of all the roles
  	in the ontology, i.e. the Vocabulary used by the ontology
  */
  List getVocabulary();
  
  /**
    Creates an object, starting from a given frame. This method can just create
    the object ignoring its argument, or it can use the frame to select the
    concrete class to instantiate.
    @param f A frame containing initialization data for the object.
    @return A Java object, instance of the proper class (either the class
    returned by <code>getClassForRole()</code>, or one of its subclasses).
  */
  Object create(Frame f) throws OntologyException;

  /**
    Provides the Java class associated with this ontological role. This class is
    usually the class used by the <code>create()</code> method to instantiate
    objects. A useful technique is returning an interface or an abstract class,
    while using concrete subclasses to create objects.
    @param a string representing the name of the ontological role
    @return the Java class that plays this ontological role (e.g. <code>DFAgentDescription.class</code>, null if no class has been registered for this role.
  */
  Class getClassForRole(String roleName); 

  /**
   * This method initialized this ontology object on the basis of its
   * representation as an SL-0 expression.
   * This expression is based on the JADE-Meta-Ontology encoded in the
   * package <code>jade.onto.JadeMetaOntology</code
   * @param str is the SL-0 expression representing this ontology 
   * @return the name of the ontology
   * @see toSL0String()
   * @see jade.lang.sl.SL0Codec
   * @see jade.onto.JadeMetaOntology.JADEMetaOntology
  **/
  String fromSL0String(String str) throws CodecException, OntologyException;

  /**
   * This method encodes the current ontology according to the SL-0 syntax
   * and the JADE-meta-ontology ontology.
   * @param ontologyName the name of this ontology
   * @return a String that is an SL-0 expression representing this ontology
   * @see fromSL0String(String)
   * @see jade.lang.sl.SL0Codec
   * @see jade.onto.JadeMetaOntology.JADEMetaOntology
  **/
  public String toSL0String(String ontologyName) throws OntologyException;

  /**
   * Return an object representing this ontology in terms of the
   * JADE-Meta-Ontology and that is suitable to be encoded as a String and,
   * for instance, become the content of an ACLMessage.
   * @param ontologyName is the name of this ontology. It cannot be either
   * null or an empty String.
   * @return a JADE-Meta-Ontology
  **/
  public AnOntology toMetaOntologyRepresentation(String ontologyName);

  /**
   * Initialize this ontology based on the passed meta-ontology
   * @param o is a JADE-Meta-Ontology
  **/
  public void fromMetaOntologyRepresentation(AnOntology o) throws OntologyException;
}
