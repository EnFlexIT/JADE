/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package examples.ex7;

import java.util.*;
import java.io.*;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

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
