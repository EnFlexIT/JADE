package jade.onto;

public class Predicate extends Functor {

  public Predicate(String name) {
    super(name);
  }

  public String getName() {
    return super.getName();
  }

  public void putArg(String name, Object value) {
    putTerm(name, value);
  }

  public Object getArg(String name) throws Functor.NoSuchTermException {
    return getTerm(name);
  }

}

