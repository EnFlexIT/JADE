package jade.onto;

public class TermDescriptor {

  private Name myName;
  private int type;
  private boolean optionality;


  public TermDescriptor(String n, int t, boolean o) {
    myName = new Name(n);
    type = t;
    optionality = o;
  }

  public String getName() {
    return myName.toString();
  }

  public int getType() {
    return type;
  }

  public boolean isOptional() {
    return optionality;
  }

  public boolean isComplex() {
    return ( type == Ontology.CONCEPT_TYPE) || (type == Ontology.ACTION_TYPE) || ( type == Ontology.PREDICATE_TYPE);
  }

}

