package jade.onto;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

abstract class Functor {

  protected static class NoSuchTermException extends OntologyException {
    public NoSuchTermException(String functorName, String termName) {
      super("No term named " + termName + " in functor " + functorName);
    }
  }

  private String myName;
  private Map termsByName;
  private List termsByPosition;

  protected Functor(String name) {
    myName = name;
    termsByName = new HashMap();
    termsByPosition = new ArrayList();
  }

  protected String getName() {
    return myName;
  }

  protected void putTerm(String name, Object value) {
    termsByName.put(new Name(name), value);
    termsByPosition.add(value);
  }

  protected Object getTerm(String name) throws NoSuchTermException {
    Object result = termsByName.get(new Name(name));
    if(result == null)
      throw new NoSuchTermException(myName, name);
    return result;
  }

  protected Object getTerm(int position) throws NoSuchTermException { 
    try {
      return termsByPosition.get(position);
    }
    catch(IndexOutOfBoundsException ioobe) {
      throw new NoSuchTermException(myName, "@" + position);
    }
  }

  final Iterator terms() {
    return termsByPosition.iterator();
  }

}

