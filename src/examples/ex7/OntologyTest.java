package examples.ex7;

import java.io.*;

import jade.onto.*;

public class OntologyTest {

  // These two classes are provided by application programmers...

  // An abstract interface to represent the "real" Person
  // abstraction. As far as ontology is concerned, this *is* a Person
  // abstraction.
  public static interface OntologicalPerson {

    void setName(String s);
    String getName();

    void setSurname(String s);
    String getSurname();

    int getAge();
    void setAge(int i);

    PlainAddress getAddress();
    void setAddress(PlainAddress p);

  }

  public static class Person implements OntologicalPerson {

    private String name;
    private String surname;
    private int age;
    private PlainAddress address = new PlainAddress();

    public Person() {
    }

    public Person(PlainAddress p) { // Get address from the outside
      address = p;
    }

    public String getName() {
      return name;
    }

    public void setName(String s) {
      name = s;
    }

    public String getSurname() {
      return surname;
    }

    public void setSurname(String s) {
      surname = s;
    }

    public int getAge() {
      return age;
    }

    public void setAge(int i) {
      age = i;
    }

    public PlainAddress getAddress() {
      return address;
    }

    public void setAddress(PlainAddress p) {
      address = p;
    }

    public void dump() {
      System.out.println("I'm an User");
      System.out.println("My name is " + getName());
      System.out.println("My age is " + getAge());
      System.out.println("My address is ");
      getAddress().dump();
    }

  } // End of Person class


  public static class PlainAddress {

    private String street;
    private int number;
    private String city;


    public void dump() {
      System.out.println(getStreet() + ", " + getN() + " - " + getCity());
    }

    public String getStreet() {
      return street;
    }

    public void setStreet(String s) {
      street = new String(s);
    }

    public int getN() {
      return number;
    }

    public void setN(int i) {
      number = i;
    }

    public String getCity() {
      return city;
    }

    public void setCity(String s) {
      city = new String(s);
    }

  } // End of PlainAddress class


  public static class FullAddress extends PlainAddress { // A subclass, maybe added later to support newer application
    private String email; // Mandatory
    private byte[] photo; // Optional


    public void dump() {
      System.out.print(getStreet() + ", " + getN() + " - " + getCity());
      System.out.println(" <Email: " + getEmail() + ">");
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String s) {
      email = new String(s);
    }

    public byte[] getPhoto() {
      return photo;
    }

    public void setPhoto(byte[] data) {
      photo = data;
    }

  }


  public static void main(String args[]) {

    // We receive some Frame objects from the CL parser...

    Frame address = new Frame("address");
    address.putSlot("Street", "parco Area delle Scienze");
    address.putSlot("N", new Integer(181));
    address.putSlot("City", "Parma");
    address.putSlot("Email", "rimassa@ce.unipr.it");

    Frame user = new Frame("user");

    user.putSlot("name", "Giovanni");
    user.putSlot("surname", "Rimassa");
    user.putSlot("age", new Integer(28));
    user.putSlot("address", address);

    // Application 1: Using the raw Frame interface, without Java classes.
    System.out.println("Now using simple Frame objects");
    System.out.println();
    user.dump();
    System.out.println();

    Ontology withFrames = new DefaultOntology();
    try {
      withFrames.addFrame("Address", Ontology.CONCEPT_TYPE, new TermDescriptor[] { 
	new TermDescriptor("STREET", Ontology.STRING_TYPE, Ontology.M),
	new TermDescriptor("N", Ontology.INTEGER_TYPE, Ontology.M),
	new TermDescriptor("CITY", Ontology.STRING_TYPE, Ontology.M)
	    });

      withFrames.addFrame("User", Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor("NAME", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("SURNAME", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("AGE", Ontology.INTEGER_TYPE, Ontology.M),
	  new TermDescriptor("ADDRESS", Ontology.CONCEPT_TYPE, "Address", Ontology.M)
	      });

      System.out.print("Checking user frame... ");
      try {
	withFrames.check(user);
	System.out.println("PASSED");
      }
      catch(OntologyException oe) {
	System.out.println("FAILED");
	oe.printStackTrace();
      }

      System.out.print("Checking user frame... ");
      try {
	withFrames.check(address);
	System.out.println("PASSED");
      }
      catch(OntologyException oe) {
	System.out.println("FAILED");
	oe.printStackTrace();
      }


    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }

    // Application 2: using ontological classes, doing without Frame objects.
    System.out.println("Now using user-defined ontological classes");

    Ontology withClasses = new DefaultOntology();
    try {

      withClasses.addFrame("Address", Ontology.CONCEPT_TYPE, new TermDescriptor[] { 
	new TermDescriptor("STREET", Ontology.STRING_TYPE, Ontology.M),
	new TermDescriptor("N", Ontology.INTEGER_TYPE, Ontology.M),
	new TermDescriptor("CITY", Ontology.STRING_TYPE, Ontology.M)
	    });

      withClasses.addFrame("User", Ontology.CONCEPT_TYPE, new TermDescriptor[] {
	  new TermDescriptor("NAME", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("SURNAME", Ontology.STRING_TYPE, Ontology.M),
	  new TermDescriptor("AGE", Ontology.INTEGER_TYPE, Ontology.M),
	  new TermDescriptor("ADDRESS", Ontology.CONCEPT_TYPE, "Address", Ontology.M)
	      });

      withClasses.addClass("User", OntologicalPerson.class); // <-- Using an abstract interface !!!
      withClasses.addClass("Address", PlainAddress.class);

      System.out.println("Building an ontological object from a Frame...");

      Person p = new Person(); // Implementation-specific initialization.

      // Now get information from frame and write it into the (already created) Java object
      Person pWithPlainAddr = (Person)withClasses.initObject(user, p);
      System.out.println();
      System.out.println();
      pWithPlainAddr.dump();

      System.out.println("Now building a Frame from an ontological object...");
      Frame fromPerson = withClasses.createFrame(pWithPlainAddr, "User");

      System.out.print("Checking Java object... ");
      try {
	withClasses.check(pWithPlainAddr, "User");
	System.out.println("PASSED");
      }
      catch(OntologyException oe) {
	System.out.println("FAILED");
	oe.printStackTrace();
      }

      withClasses.addFrame("Address", Ontology.CONCEPT_TYPE, new TermDescriptor[] { 
	new TermDescriptor("STREET", Ontology.STRING_TYPE, Ontology.M),
	new TermDescriptor("N", Ontology.INTEGER_TYPE, Ontology.M),
	new TermDescriptor("CITY", Ontology.STRING_TYPE, Ontology.M),
	new TermDescriptor("EMAIL", Ontology.STRING_TYPE, Ontology.M)
	    });

      withClasses.addClass("Address", FullAddress.class);

      Person pWithFullAddr = (Person)withClasses.initObject(user, new Person(new FullAddress()));
      System.out.println();
      System.out.println();
      pWithFullAddr.dump();

      System.out.print("Checking Java object... ");
      try {
	withClasses.check(pWithFullAddr, "User");
	System.out.println("PASSED");
      }
      catch(OntologyException oe) {
	System.out.println("FAILED");
	oe.printStackTrace();
      }


      // Now create a Frame object from the user-defined class instance, via Reflection
      Frame personFrame = withClasses.createFrame(pWithFullAddr, "User");
      System.out.println();

      personFrame.dump();
      System.out.println();
      System.out.print("Checking Frame... ");
      try {
	withFrames.check(personFrame);
	System.out.println("PASSED");
      }
      catch(OntologyException oe) {
	System.out.println("FAILED");
	oe.printStackTrace();
      }

      System.out.println();

      System.out.println("Now using unnamed, order dependent slots...");
      String[] tokens = new String[] { "Via Dante", "99", "Firenze", "pippo@disney.it" };
      System.out.println("The CL parser reads these tokens:");
      for(int i = 0; i < tokens.length; i++) {
	System.out.println("  " + tokens[i]);
      }
      System.out.println("The parser asks the ontology for slot names and types.");
      Frame addrFromOrderedCL = new Frame("address");
      TermDescriptor[] terms = withClasses.getTerms("address");
      for(int i = 0; i < terms.length; i++) {
	String slotName = terms[i].getName();
	String slotValue = tokens[i];
	switch(terms[i].getType()) { // <-- N.B. Due to weakly typed functors, client code is polluted :-(
	case Ontology.BOOLEAN_TYPE:
	  addrFromOrderedCL.putSlot(slotName, Boolean.valueOf(slotValue));
	  break;
	case Ontology.BYTE_TYPE:
	  addrFromOrderedCL.putSlot(slotName, Byte.valueOf(slotValue));
	  break;
	case Ontology.CHARACTER_TYPE:
	  addrFromOrderedCL.putSlot(slotName, new Character(slotValue.charAt(0)));
	  break;
	case Ontology.DOUBLE_TYPE:
	  addrFromOrderedCL.putSlot(slotName, Double.valueOf(slotValue));
	  break;
	case Ontology.FLOAT_TYPE:
	  addrFromOrderedCL.putSlot(slotName, Float.valueOf(slotValue));
	  break;
	case Ontology.INTEGER_TYPE:
	  addrFromOrderedCL.putSlot(slotName, Integer.valueOf(slotValue));
	  break;
	case Ontology.LONG_TYPE:
	  addrFromOrderedCL.putSlot(slotName, Long.valueOf(slotValue));
	  break;
	case Ontology.SHORT_TYPE:
	  addrFromOrderedCL.putSlot(slotName, Short.valueOf(slotValue));
	  break;
	case Ontology.STRING_TYPE:
	  addrFromOrderedCL.putSlot(slotName, slotValue);
	  break;
	case Ontology.BINARY_TYPE:
	  addrFromOrderedCL.putSlot(slotName, slotValue.getBytes());
	  break;
	default:
	  System.out.println("Sorry. Complex slots not supported.");
	  break;
	}
      }

      addrFromOrderedCL.dump();
      System.out.println();
      System.out.print("Now checking the newly constructed Frame... ");
      try {
	withClasses.check(addrFromOrderedCL);
	System.out.println("PASSED");
      }
      catch(OntologyException oe) {
	System.out.println("FAILED");
	oe.printStackTrace();
      }
      System.out.println();

    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }

  }

}
