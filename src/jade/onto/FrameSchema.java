package jade.onto;

import java.lang.reflect.*;

import java.io.Serializable;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

class FrameSchema implements Cloneable, Serializable {

  private static final short BOOLEAN_TYPE = 0;
  private static final short BYTE_TYPE = 1;
  private static final short CHARACTER_TYPE = 2;
  private static final short DOUBLE_TYPE = 3;
  private static final short FLOAT_TYPE = 4;
  private static final short INTEGER_TYPE = 5;
  private static final short LONG_TYPE = 6;
  private static final short SHORT_TYPE = 7;

  private static final short STRING_TYPE = 8;
  private static final short BINARY_TYPE = 9;
  private static final short FRAME_TYPE = 10;

  private static final String typeNames[] = { "boolean", "byte", "char", "double",
					      "float", "int", "long", "short",
					      "String", "Binary", "Frame" };

  private static class SlotSchema {

    private short slotType;
    private boolean optional;

    public SlotSchema(short t, boolean opt) {
      slotType = t;
      optional = opt;
    }

    public short getType() {
      return slotType;
    }

    public String getTypeName() {
      return typeNames[slotType];
    }

    public void setOptional(boolean value) {
      optional = value;
    }

    public boolean isOptional() {
      return optional;
    }

  }

  private Ontology myOntology;
  private String myName;
  private Map slotsByName;
  private Name[] slotNames;


  public FrameSchema(Ontology o, String name) {
    myOntology = o;
    myName = new String(name);
    slotsByName = new HashMap();
    slotNames = new Name[0];
  }

  public String getName() {
    return myName;
  }

  public void addSlot(String slotName, boolean optional, short typeID) {
    SlotSchema schema = new SlotSchema(typeID, optional);
    slotsByName.put(new Name(slotName), schema);
  }

  public short getSlotType(String slotName) throws OntologyException {
    SlotSchema ss = (SlotSchema)slotsByName.get(new Name(slotName));
    if(ss == null)
      throw new OntologyException("No slot " + slotName + " in frame " + myName);
    return ss.getType();
  }

  public void setOptionality(String slotName, boolean value) throws OntologyException {
    SlotSchema ss = (SlotSchema)slotsByName.get(new Name(slotName));
    if(ss == null)
      throw new OntologyException("No slot " + slotName + " in frame " + myName);
    ss.setOptional(value);
  }

  public void setNameList(String[] names) throws OntologyException {
    slotNames = new Name[names.length];
    for(int i = 0; i < names.length; i++)
      slotNames[i] = new Name(names[i]);
    if(slotNames.length != slotsByName.size())
      throw new OntologyException("The length of the name list is wrong: it should be " + slotsByName.size());
  }

  public String getSlotName(int index) throws OntologyException {
    try {
      return slotNames[index].toString();
    }
    catch(ArrayIndexOutOfBoundsException aioobe) {
      throw new OntologyException("No slot at position " + index + " in frame " + myName);
    }
  }

  public int getSlotPosition(String name) throws OntologyException {
    Name n = new Name(name);
    for(int i = 0; i < slotNames.length; i++) {
      if(n.equals(slotNames[i]))
	return i;
    }
    throw new OntologyException("No slot named " + name + " in frame " + myName);
  }

  void addBooleanSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, BOOLEAN_TYPE);
  }

  void addByteSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, BYTE_TYPE);
  }

  void addCharacterSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, CHARACTER_TYPE);
  }

  void addDoubleSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, DOUBLE_TYPE);
  }

  void addFloatSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, FLOAT_TYPE);
  }

  void addIntSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, INTEGER_TYPE);
  }

  void addLongSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, LONG_TYPE);
  }

  void addShortSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, SHORT_TYPE);
  }

  void addStringSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, STRING_TYPE);
  }

  void addBinarySlot(String slotName, boolean optional) {
    addSlot(slotName, optional, BINARY_TYPE);
  }

  void addFrameSlot(String slotName, boolean optional) {
    addSlot(slotName, optional, FRAME_TYPE);
  }


  public void toText(Writer w) {
    try {
      w.write("Schema for " + myName + "\n");
      Set slots = slotsByName.entrySet();
      Iterator i = slots.iterator();
      while(i.hasNext()) {
	Map.Entry e = (Map.Entry)i.next();
	Name slotName = (Name)e.getKey();
	SlotSchema schema = (SlotSchema)e.getValue();
	w.write(slotName + " " + schema.getTypeName() + " " +  (schema.isOptional() ? "Optional" : "Mandatory"));
	w.write("\n");
      }
      w.flush();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
  }

  public static FrameSchema fromText(Reader r) { 
    return null;
  }

  public Frame buildFromObject(Object o, Class c) throws OntologyException {
    Frame f = new Frame(myName);
    try {
      Method[] methods = c.getMethods();

      for(int i = 0; i < methods.length; i++) {
	Method m = methods[i];
	String s = m.getName();
	if(s.startsWith("get")) {

	  // Recover the slot name removing "get" from the method name
	  String slotName = s.substring(3);

	  // Eliminate spurious methods inherited from Object:
	  // - getClass()
	  if(slotName.equalsIgnoreCase("Class"))
	    continue;

	  // We can be sure that this method takes no arguments, since the class passed through addConcept()

	  Class slotType = m.getReturnType();
	  Object slotValue = m.invoke(o, new Object[0]);

	  SlotSchema ss = (SlotSchema)slotsByName.get(new Name(slotName));

	  if(slotValue == null) {
	    if(!ss.isOptional()) // A mandatory slot cannot be 'null'
	      throw new OntologyException("The object has a 'null' value for the mandatory " + slotName + " slot.");
	  }
	  else {
	    // If the slot schema states this is a sub-frame, than we must convert the slot value to a Frame
	    if(ss.getType() == FRAME_TYPE) {
	      Frame subFrame = myOntology.createFrame(slotValue, slotName);
	      f.putSlot(slotName, subFrame);
	    }
	    else { // Otherwise, we can just set the slot of the Frame to the current value, via Reflection
	      Method putSlot = (Frame.class).getMethod("putSlot", new Class[] { String.class, slotType });
	      putSlot.invoke(f, new Object[] { slotName, slotValue });
	    }
	  }
	}
      }
    }
    catch(InvocationTargetException ite) {
      String msg = ite.getTargetException().getMessage();
      throw new OntologyException("Internal error: a reflected method threw an exception.\nMessage was " + msg);
    }
    catch(IllegalAccessException iae) {
      throw new OntologyException("Internal error: the required method is not accessible [" + iae.getMessage() + "]");
    }
    catch(NoSuchMethodException nsme) {
      throw new OntologyException("Internal error: the required method does not exist [" + nsme.getMessage() + "]");
    }
    catch(SecurityException se) {
      throw new OntologyException("Wrong class: some required method is not accessible."); 
    }
    return f;
  }

  public void checkAgainst(Frame f) throws OntologyException {
    Iterator i = slotsByName.entrySet().iterator();
    while(i.hasNext()) {
      Map.Entry e = (Map.Entry)i.next();
      Name name = (Name)e.getKey();
      SlotSchema schema = (SlotSchema)e.getValue();
      if(!schema.isOptional()) {
	switch(schema.getType()) {
	case BOOLEAN_TYPE: {
	  boolean value = f.getBooleanSlot(name.toString());
	  break;
	}
	case BYTE_TYPE: {
	  byte value = f.getByteSlot(name.toString());
	  break;
	}
	case CHARACTER_TYPE: {
	  char value = f.getCharacterSlot(name.toString());
	  break;
	}
	case DOUBLE_TYPE: {
	  double value = f.getDoubleSlot(name.toString());
	  break;
	}
	case FLOAT_TYPE: {
	  float value = f.getFloatSlot(name.toString());
	  break;
	}
	case INTEGER_TYPE: {
	  int value = f.getIntegerSlot(name.toString());
	  break;
	}
	case LONG_TYPE: {
	  long value = f.getLongSlot(name.toString());
	  break;
	}
	case SHORT_TYPE: {
	  short value = f.getShortSlot(name.toString());
	  break;
	}
	case STRING_TYPE: {
	  String value = f.getStringSlot(name.toString());
	  break;
	}
	case BINARY_TYPE: {
	  byte[] value = f.getBinarySlot(name.toString());
	  break;
	}
	case FRAME_TYPE: {
	  Frame subFrame = f.getFrameSlot(name.toString());
	  //	  FrameSchema subSchema = myOntology.lookupSchema(subFrame.getName());
	  //      subSchema.checkAgainst(subFrame);
	  myOntology.check(subFrame, name.toString());
	  break;
	}
	default:
	  throw new InternalError("Non existent slot type");
	}

      } // End of 'if slot is not optional'

    } // End of slot iteration

  }

}

