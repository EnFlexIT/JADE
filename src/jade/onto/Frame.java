package jade.onto;

import java.io.Writer;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Frame {

  public class WrongSlotTypeException extends OntologyException {
    public WrongSlotTypeException(String frameName, String slotName, String slotType) {
      super("No slot of type " + slotType + " named " + slotName + " in frame " + frameName);
    }
  }

  public class NoSuchSlotException extends OntologyException {
    public NoSuchSlotException(String frameName, String slotName) {
      super("No slot named " + slotName + " in frame " + frameName);
    }
  }

  private Map slotsByName;
  private String myName;

  public Frame(String name) {
    myName = new String(name);
    slotsByName = new HashMap();
  }

  public String getName() {
    return myName;
  }

  public void putSlot(String name, boolean value) {
    Boolean slot = new Boolean(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, byte value) {
    Byte slot = new Byte(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, char value) {
    Character slot = new Character(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, double value) {
    Double slot = new Double(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, float value) {
    Float slot = new Float(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, int value) {
    Integer slot = new Integer(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, long value) {
    Long slot = new Long(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, short value) {
    Short slot = new Short(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, String value) {
    String slot = new String(value);
    slotsByName.put(new Name(name), slot);
  }

  public void putSlot(String name, byte[] value) {
    byte[] slot = new byte[value.length];
    System.arraycopy(value, 0, slot, 0, value.length);
    slotsByName.put(new Name(name), slot);
  }

  /**
     Reference semantics...
   */
  public void putSlot(String name, Frame value) {
    slotsByName.put(new Name(name), value);
  }

  public boolean getBooleanSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Boolean result = (Boolean)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result.booleanValue();
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "boolean");
    }
  }

  public byte getByteSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Byte result = (Byte)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result.byteValue();
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "byte");
    }
  }

  public char getCharacterSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Character result = (Character)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result.charValue();
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "char");
    }
  }

  public double getDoubleSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Double result = (Double)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result.doubleValue();
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "double");
    }
  }

  public float getFloatSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Float result = (Float)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result.floatValue();
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "float");
    }
  }

  public int getIntegerSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Integer result = (Integer)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result.intValue();
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "int");
    }
  }

  public long getLongSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Long result = (Long)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result.longValue();
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "long");
    }
  }

  public short getShortSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Short result = (Short)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result.shortValue();
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "short");
    }
  }

  public String getStringSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      String result = (String)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result;
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "text");
    }
  }

  public byte[] getBinarySlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      byte[] result = (byte[])slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result;
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "binary");
    }
  }

  public Frame getFrameSlot(String name) throws NoSuchSlotException, WrongSlotTypeException {
    try {
      Frame result = (Frame)slotsByName.get(new Name(name));
      if(result == null)
	throw new NoSuchSlotException(myName, name);
      return result;
    }
    catch(ClassCastException cce) {
      throw new WrongSlotTypeException(myName, name, "frame");
    }
  }

  // Package scoped interface. Just for Ontology class
  Iterator slots() {
    return slotsByName.entrySet().iterator();
  }

  public void toText(Writer w) {
    try {
      w.write(toString());
      w.flush();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }

  }

  // Just for testing. Real world version will use a separate CL encoder
  public String toString() {
    String s = "( ";
    Iterator i = slotsByName.entrySet().iterator();
    while(i.hasNext()) {
      Map.Entry e = (Map.Entry)i.next();
      s = s.concat(e.getKey() + " " + e.getValue() + " ");
    }
    s = s.concat(")");
    return s;
  }

}
