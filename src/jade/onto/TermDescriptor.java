package jade.onto;

public class TermDescriptor {

  private Name myName;
  private int type;
  private String typeName;
  private boolean optionality;


  public TermDescriptor(String n, int t, String tn, boolean o) {
    myName = new Name(n);
    type = t;
    typeName = tn;
    optionality = o;
  }

  public TermDescriptor(String n, int t, boolean o) {
    this(n, t, Ontology.typeNames[t], o);
  }

  public String getName() {
    return myName.toString();
  }

  public int getType() {
    return type;
  }

  public String getTypeName() {
    return typeName;
  }

  public boolean isOptional() {
    return optionality;
  }

  public boolean isComplex() {
    return ( type == Ontology.CONCEPT_TYPE) || (type == Ontology.ACTION_TYPE) || ( type == Ontology.PREDICATE_TYPE);
  }

}

