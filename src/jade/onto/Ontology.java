package jade.onto;


public interface Ontology {

  // Boolean constants for 'Optional' and 'Mandatory'
  public static final boolean O = true;
  public static final boolean M = false;

  // Constants for the various term types.
  static final short BOOLEAN_TYPE = 0;
  static final short BYTE_TYPE = 1;
  static final short CHARACTER_TYPE = 2;
  static final short DOUBLE_TYPE = 3;
  static final short FLOAT_TYPE = 4;
  static final short INTEGER_TYPE = 5;
  static final short LONG_TYPE = 6;
  static final short SHORT_TYPE = 7;

  static final short STRING_TYPE = 8;
  static final short BINARY_TYPE = 9;
  static final short CONCEPT_TYPE = 10;
  static final short ACTION_TYPE = 11;
  static final short PREDICATE_TYPE = 12;

  static final String typeNames[] = { "boolean", "byte", "char", "double",
				      "float", "int", "long", "short",
				      "String", "Binary", "Concept", "Action", "Predicate" };

  void addClass(String roleName, Class c) throws OntologyException;

  void addConcept(String conceptName, TermDescriptor[] slots) throws OntologyException;
  void addAction(String actionName, TermDescriptor[] args) throws OntologyException; 
  void addPredicate(String predicateName, TermDescriptor[] terms) throws OntologyException;

  Object createObject(Frame f, String roleName) throws OntologyException;
  Object createObject(Action a, String roleName) throws OntologyException;
  Object createObject(Predicate p, String roleName) throws OntologyException;

  Frame createConcept(Object o, String roleName) throws OntologyException;
  Action createAction(Object o, String roleName) throws OntologyException;
  Predicate createPredicate(Object o, String roleName) throws OntologyException;

  void check(Frame f, String roleName) throws OntologyException;
  void check(Action a, String roleName) throws OntologyException;
  void check(Predicate p, String roleName) throws OntologyException;
  void check(Object o, String roleName) throws OntologyException;

  TermDescriptor[] getTerms(String roleName) throws OntologyException;

}
