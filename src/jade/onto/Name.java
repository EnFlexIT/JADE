package jade.onto;

public class Name {
  String s;
  public Name(String name) {
    s = name;
  }

  public String toString() {
    return s.toString();
  }

  // These two methods provide case-insensitive comparison and
  // hashing.
  public boolean equals(Object o) {
    if(o instanceof String) {
      return s.equalsIgnoreCase((String)o);
    }
    try {
      Name sn = (Name)o;
      return s.equalsIgnoreCase(sn.s);
    }
    catch(ClassCastException cce) {
      return false;
    }
  }

  public int hashCode() {
    return s.toLowerCase().hashCode();
  }

}
