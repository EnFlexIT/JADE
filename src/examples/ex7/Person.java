package examples.ex7;

import java.util.*;
import java.io.*;

public class Person implements Serializable {

String name;
String surname;
Date   birthdate;
int    age;

  Person(String n, String s, Date d, int a) {
    name = n;
    surname = s;
    birthdate = d;
    age = a;
  }

  public String toString() {
    return(name+surname+" born on "+birthdate.toString()+" age = "+age);
  }
  /*
private void readObject(java.io.ObjectInputStream stream)
  throws IOException, ClassNotFoundException {
    name=stream.readUTF();
    surname=stream.readUTF();
    birthdate = new Date();
    System.out.println(stream.readUTF());
    age = Integer.parseInt(stream.readUTF());
}

 private void writeObject(java.io.ObjectOutputStream stream)
   throws IOException {
     stream.writeUTF(name);
     stream.writeUTF(surname);
     stream.writeUTF(birthdate.toString());
     stream.writeUTF(Integer.toString(age));
 }
 */

}
