package jade.onto;

public class Action extends Functor {

  private String actorName;

  public Action(String name, String actor) {
    super(name);
    actorName = actor;
  }

  public String getName() {
    return super.getName();
  }

  public String getActor() {
    return actorName;
  }

  public void putArg(String name, Object value) {
    putTerm(name, value);
  }

  public Object getArg(String name) throws Functor.NoSuchTermException {
    return getTerm(name);
  }

}
