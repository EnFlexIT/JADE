/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
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

package jade.lang.sl;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.OntologyException;
import jade.onto.TermDescriptor;

/**
  Javadoc documentation for the file
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
 */
class SL0Encoder {

  public String encode(Frame f, Ontology o) {

    StringWriter out = new StringWriter();

    try {
      writeFrame(f, o, out);
      out.flush();
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }

    return out.toString();
  }

  private void writeFrame(Frame f, Ontology o, Writer w) throws OntologyException, IOException {

    String name = f.getName();
    TermDescriptor[] terms = o.getTerms(name);

    if(o.isConcept(name))
      writeConcept(name, f, o, terms, w);
    if(o.isAction(name))
      writeAction(name, f, o, terms, w);
    if(o.isPredicate(name))
      writePredicate(name, f, o, terms, w);
  }

  private void writeConcept(String name, Frame f, Ontology o, TermDescriptor[] terms, Writer w) throws OntologyException, IOException {
    w.write("( " + name + " ");
    for(int i = 0; i < terms.length; i++) {
      TermDescriptor current = terms[i];
      String slotName = current.getName();
      Object slotValue = f.getSlot(slotName);
      w.write("( ");
      w.write(slotName + " ");
      if(current.isComplex()) {
	Frame complexSlot = (Frame)slotValue;
	writeFrame(complexSlot, o, w);
	w.write(" ");
      }
      else {
	w.write(slotValue + " ");
      }
      w.write(" )");
    }
    w.write(" )");
  }

  private void writeAction(String name, Frame f, Ontology o, TermDescriptor[] terms, Writer w) throws OntologyException, IOException {
    String actor = (String)f.getSlot(":actor");
    w.write("( action " + actor + " ");
    w.write("( " + name + " ");
    for(int i = 1; i < terms.length; i++) {
      TermDescriptor current = terms[i];
      Object slotValue = f.getSlot(i);
      if(current.isComplex()) {
	Frame complexSlot = (Frame)slotValue;
	writeFrame(complexSlot, o, w);
	w.write(" ");
      }
      else {
	w.write(slotValue + " ");
      }
    }
    w.write(" ) )");
  }

  private void writePredicate(String name, Frame f, Ontology o, TermDescriptor[] terms, Writer w) throws OntologyException, IOException {
    w.write("( " + name + " ");
    for(int i = 0; i < terms.length; i++) {
      TermDescriptor current = terms[i];
      Object slotValue = f.getSlot(i);
      if(current.isComplex()) {
	Frame complexSlot = (Frame)slotValue;
	writeFrame(complexSlot, o, w);
	w.write(" ");
      }
      else {
	w.write(slotValue + " ");
      }
    }
    w.write(" )");
  }

}
