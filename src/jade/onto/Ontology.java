package jade.onto;


public interface Ontology {

  // Boolean constants for 'Optional' and 'Mandatory'
  public static final boolean O = true;
  public static final boolean M = false;

  void addConcept(String roleName, Class c, String[] slotNames, boolean[] optionality) throws OntologyException;
  void addConcept(String roleName, Frame f, String slotNames[], boolean[] optionality) throws OntologyException;

  Object createObject(Frame f, String roleName) throws OntologyException;
  Frame createFrame(Object o, String roleName) throws OntologyException;

  boolean check(Frame f, String roleName);
  boolean check(Object o, String roleName);

  String getSlotName(String roleName, int index) throws OntologyException;
  int getSlotPosition(String roleName, String name) throws OntologyException;

}
