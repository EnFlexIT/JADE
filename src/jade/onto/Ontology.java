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

package jade.onto;
import java.util.List;

/**
  Abstract interface for application defined ontology. Through this interface it
  is possible to manage a collection of <b><i>Frames</i></b>, 
  along with their structure. User
  defined Java classes can be registered with an ontology as playing a certain
  <i>role</i>; then, the <code>Ontology</code> object is able to convert back
  and forth between user defined Java objects and <code>Frame</code> objects.

  A role is identified by the name of the Frame that represents the role.
  The role played by the given
  class must have been previously inserted into this Ontology using the
  <code>addFrame()</code> method. To play an ontological role, a Java class must
  obey to some rules:

   <ol>

   <li><i> For every <code>TermDescriptor</code> object of the
   array, of type <code>SET_TYPE</code> or <code>SEQUENCE_TYPE</code>
   and named <code>XXX</code>, with elements of type <code>T</code>, the
   class must have four accessible methods, with the following
   signature:</i>
     <ul>
     <li> <code>Iterator getAllXXX()</code>
     <li> <code>void addXXX(T t)</code>
     </ul>

   <li><i> For every <code>TermDescriptor</code> object of the
   array, of type <code>T</code> and named <code>XXX</code>, the
   class must have two accessible methods, with the following
   signature:</i>
     <ul>
     <li> <code>T getXXX()</code>
     <li> <code>void setXXX(T t)</code>
     </ul>

   </ol>

   As long as the above rules are followed, any user-defined class
   can be added to the Ontology object. As an useful technique, one
   can define compliant Java interfaces and add them to the
   Ontology; this way useful OO idioms such as polymorphism and
   mix-in inheritance can be exploited for the Java representations
   of ontological objects.

   Due to different lexical conventions between the Java language
   and FIPA ACL and content languages, some name translation is
   performed to map the name of a term (that is, of a frame slot or
   of an action argument) into the name of the corresponding method.
   Name translation works as follows:
   <ol>
   <li> Any <code>':'</code> character is removed.
   <li> Any <code>'-'</code> character is removed.
   </ol>
   Moreover, a case insensitive match is followed.

   As an example, a frame with an integer slot named
   <code>:user-age</code>, will require the following methods (case
   is not important, but according to a popular Java coding
   convention, the two methods have capital letters whenever a
   <code>'-'</code> is present in the slot name:

   <ul>
   <li><code>int getUserAge()</code>
   <li><code>void setUserAge(int age)</code>
   </ul>

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
//     <li> <code>boolean removeXXX(T t)</code>
//     <li> <code>void clearAllXXX()</code>

public interface Ontology {

  /**
     Boolean constant for <i>Optional</i>.
   */
  static final boolean O = true;

  /**
     Boolean constant for <i>Mandatory</i>.
   */
  static final boolean M = false;



  // Constants for the various term types.

  /**
     Constant for <code>boolean</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short BOOLEAN_TYPE = 0;

  /**
     Constant for <code>byte</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short BYTE_TYPE = 1;

  /**
     Constant for <code>char</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short CHARACTER_TYPE = 2;

  /**
     Constant for <code>double</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short DOUBLE_TYPE = 3;

  /**
     Constant for <code>float</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short FLOAT_TYPE = 4;

  /**
     Constant for <code>int</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short INTEGER_TYPE = 5;

  /**
     Constant for <code>long</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short LONG_TYPE = 6;

  /**
     Constant for <code>short</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short SHORT_TYPE = 7;

  /**
     Constant for <code>String</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short STRING_TYPE = 8;

  /**
     Constant for <code>byte[]</code> type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short BINARY_TYPE = 9;

  /**
     Constant for any type in a <code>TermDescriptor</code>.
     @see jade.onto.TermDescriptor
   */
  static final short ANY_TYPE = 10;

  /**
     Constant for <code>Frame</code> type in a
     <code>TermDescriptor</code>. Ontology concepts can be represented
     as <code>Frame</code> instances or as instances of a user-defined
     class, obeying to some rules.
     @see jade.onto.TermDescriptor
     @see jade.onto.Frame
     @see jade.onto.Ontology#addFrame(String conceptName, int kind, TermDescriptor[] slots, RoleFactory rf)
  */
  static final short CONCEPT_TYPE = 11;

  /**
     Constant for <code>set</code> type in a
     <code>TermDescriptor</code>. 
     @see jade.onto.TermDescriptor
     @see jade.onto.Frame
  */
  static final short SET_TYPE = 12;

  /**
     Constant for <code>sequence</code> type in a
     <code>TermDescriptor</code>. 
     @see jade.onto.TermDescriptor
     @see jade.onto.Frame
  */
  static final short SEQUENCE_TYPE = 13;


  /**
     String array of names of the various types allowed for elements
     of ontological elements. It can be indexed using the constants in
     the Ontology interface.
  */
  static final String typeNames[] = { "boolean", "byte", "char", "double",
				      "float", "int", "long", "short",
				      "String", "Binary", "any", "Concept", "Set", "Sequence" };

  /**
    Adds a new concept role to the ontology, defined by the structure
    of all its slots.
    @param conceptName The name of this concept role (names are case
    preserving but the match is case insensitive).
    @param kind Tells whether the Frame represents an <i><b>Concept</b></i>,
    an <i><b>Action</b></i>, or a <i><b>Predicate</b></i> in this ontology.
    Use the three constants in Ontology interface to select one among the three
    options.
    @param slots An array of descriptors; each one of them describes a
    slot of the frame, providing:
    <ul>
    <li> The name of the slot.
    <li> The type of the slot.
    <li> The optionality of the slot (i.e. whether a value is required or not).
    <li> The position of the slot (implicitly defined by the position in the array).
    </ul>
    @see jade.onto.Ontology#CONCEPT_TYPE
    @see jade.onto.Ontology#ACTION_TYPE
    @see jade.onto.Ontology#PREDICATE_TYPE
  */
  void addFrame(String conceptName, int kind, TermDescriptor[] slots) throws OntologyException;

  /**
    Adds a new concept role to the ontology, defined by the structure
    of all its slots.
    @param conceptName The name of this concept role (names are case
    preserving but the match is case insensitive).
    @param kind Tells whether the Frame represents an <i><b>Concept</b></i>,
    an <i><b>Action</b></i>, or a <i><b>Predicate</b></i> in this ontology.
    Use the three constants in Ontology interface to select one among the three
    options.
    @param slots An array of descriptors; each one of them describes a
    slot of the frame, providing:
    <ul>
    <li> The name of the slot.
    <li> The type of the slot.
    <li> The optionality of the slot (i.e. whether a value is required or not).
    <li> The position of the slot (implicitly defined by the position in the array).
    </ul>
    @param rf A <code>Factory</code> object, which will be used to
    create user defined Java objects playing the given role.
    @see jade.onto.Ontology#CONCEPT_TYPE
    @see jade.onto.Ontology#ACTION_TYPE
    @see jade.onto.Ontology#PREDICATE_TYPE
 */
  void addFrame(String conceptName, int kind, TermDescriptor[] slots, RoleFactory rf) throws OntologyException;

  /**
     Creates a Java object representing a given concept, getting the
     information from a given <code>List</code> of <code>Frame</code> 
     objects. This method
     requires that a factory for the given role is registered in this ontology,
     because it creates internally the returned object.
     @param f A <code>List</code> of <code>Frame</code> objects, 
     from which a <code>List</code> of Java objects is built.
     @return A newly created <code>List</code> of Java objects, 
     each Java object representing a 
     <code>Frame</code> as a user-defined type.
     @exception OntologyException If a <code>Frame</code> does
     not play any role in the current ontology, or if the registered
     class does not follow the rules for representing a concept.
     @see jade.onto.Ontology#addFrame(String conceptName, int kind, TermDescriptor[] slots, RoleFactory rf)
  */
  List createObject(List v) throws OntologyException;

  /**
    Creates a <code>Frame</code> object from a given Java object. A
    suitable factory must be registered in the ontology to play the
    given role, and the given object must be an instance of the class returned
    by the <code>getClassForRole()</code> method of the <code>RoleFactory</code>
    (an indirect instance, i.e. an instance of the class itself or of a subclass).
    @param o The Java object, from which the <code>Frame</code> will
    be built.
    @param roleName The name of the role played in this ontology by
    the class of the given object.
    @return A <code>Frame</code> object playing the given role, built
    from the given <code>Object</code>.
    @exception OntologyException If the given role does not exist, or
    the given object is not of the correct class.
  */
  Frame createFrame(Object o, String roleName) throws OntologyException;

  /**
    Checks whether the given <code>Frame</code> object is a valid
    instance of some role, making sure that every slot has the correct
    type and that no mandatory slot has a <code>null</code> value.
    @param f The <code>Frame</code> object to check.
    @exception OntologyException If the check fails.
  */
  void check(Frame f) throws OntologyException;

  /**
    Checks whether the given Java object is a valid instance of some
    role, making sure that every slot has the correct type and that no
    mandatory slot has a <code>null</code> value.
    @param o The Java object to check.
    @param roleName The role against which to check the given object.
    @exception OntologyException If the check fails.
  */
  void check(Object o, String roleName) throws OntologyException;

  /**
    Tells whether the given roleName is a concept in the current
    ontology.
    @param roleName The name of the role to check.
    @return <code>true</code> if the given role is indeed a concept,
    <code>false</code> otherwise.
    @exception OntologyException If no role named
    <code>roleName</code> exists in the current ontology.
   */
  boolean isConcept(String roleName) throws OntologyException;

  /**
    Returns the array of <code>TermDescriptor</code> objects that
    represent the elements of the given ontological role (concept,
    action or predicate).
    @param roleName The name of the ontological role to examine.
    @return The descriptors for the selected ontology role.
    @see jade.onto.TermDescriptor
   */
  TermDescriptor[] getTerms(String roleName) throws OntologyException;

}
