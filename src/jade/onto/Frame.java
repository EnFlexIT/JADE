package jade.onto;

import java.io.Writer;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Frame extends Functor {

  public Frame(String name) {
    super(name);
  }

  public String getName() {
    return super.getName();
  }

  public void putSlot(String name, Object value) {
    putTerm(name, value);
  }

  public Object getSlot(String name) throws Functor.NoSuchTermException {
    return getTerm(name);
  }

}
