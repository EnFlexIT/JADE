package jade.onto;

import java.lang.reflect.*;

import java.io.Serializable;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

class FrameSchema implements Cloneable, Serializable {

  static class WrongTermTypeException extends OntologyException {
    public WrongTermTypeException(String functorName, String termName, String termType) {
      super("No term of type " + termType + " named " + termName + " in functor " + functorName);
    }
  }

  private Ontology myOntology;
  private int functorKind;
  private Name myName;
  private List terms;


  public FrameSchema(Ontology o, String n, int kind) {
    myOntology = o;
    functorKind = kind;
    myName = new Name(n);
    terms = new ArrayList();
  }

  public String getName() {
    return myName.toString();
  }

  public void addTerm(TermDescriptor td) {
    terms.add(td);
  }

  public boolean isConcept() {
    return functorKind == Ontology.CONCEPT;
  }

  public boolean isAction() {
    return functorKind == Ontology.ACTION;
  }

  public boolean isPredicate() {
    return functorKind == Ontology.PREDICATE;
  }

  public void checkAgainst(Frame f) throws OntologyException {
    for(int i = 0; i < terms.size(); i++) {
      TermDescriptor td = (TermDescriptor)terms.get(i);
      String name = td.getName();
      if(!td.isOptional()) {
	Object o = f.getSlot(name);
	switch(td.getType()) {
	case Ontology.BOOLEAN_TYPE: {
	  if(!(o instanceof Boolean))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.BOOLEAN_TYPE]); 
	  break;
	}
	case Ontology.BYTE_TYPE: {
	  if(!(o instanceof Byte))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.BYTE_TYPE]); 
	  break;
	}
	case Ontology.CHARACTER_TYPE: {
	  if(!(o instanceof Character))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.CHARACTER_TYPE]); 
	  break;
	}
	case Ontology.DOUBLE_TYPE: {
	  if(!(o instanceof Double))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.DOUBLE_TYPE]); 
	  break;
	}
	case Ontology.FLOAT_TYPE: {
	  if(!(o instanceof Float))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.FLOAT_TYPE]); 
	  break;
	}
	case Ontology.INTEGER_TYPE: {
	  if(!(o instanceof Integer))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.INTEGER_TYPE]); 
	  break;
	}
	case Ontology.LONG_TYPE: {
	  if(!(o instanceof Long))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.LONG_TYPE]); 
	  break;
	}
	case Ontology.SHORT_TYPE: {
	  if(!(o instanceof Short))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.SHORT_TYPE]); 
	  break;
	}
	case Ontology.STRING_TYPE: {
	  if(!(o instanceof String))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.STRING_TYPE]); 
	  break;
	}
	case Ontology.BINARY_TYPE: {
	  if(!(o instanceof Byte[]))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.BINARY_TYPE]); 
	  break;
	}
	case Ontology.CONCEPT_TYPE:
	case Ontology.ACTION_TYPE:
	case Ontology.PREDICATE_TYPE: {
	  if(!(o instanceof Frame))
	    throw new WrongTermTypeException(f.getName(), name, Ontology.typeNames[Ontology.CONCEPT_TYPE]); 
	  myOntology.check((Frame)o, td.getTypeName());
	  break;
	}
	default:
	  throw new InternalError("Non existent term type");
	}

      } // End of 'if slot is not optional'

    } // End of slot iteration

  }

  Iterator subSchemas() {
    return terms.iterator();
  }

  TermDescriptor[] termsArray() {
    Object[] objs = terms.toArray();
    TermDescriptor[] result = new TermDescriptor[objs.length];
    System.arraycopy(objs, 0, result, 0, objs.length);
    return result;
  }

}

