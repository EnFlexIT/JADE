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


package jade.onto;

import java.lang.reflect.*;

import java.io.Serializable;
import java.io.Reader;
import java.io.Writer;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
class FrameSchema implements Cloneable, Serializable {

  static class WrongTermTypeException extends OntologyException {
    public WrongTermTypeException(String functorName, String termName, String termType) {
      super("No term of type " + termType + " named " + termName + " in functor " + functorName);
    }
  }

  private Ontology myOntology;
  private Name myName;
  private List terms;


  public FrameSchema(Ontology o, String n) {
    myOntology = o;
    myName = new Name(n);
    terms = new ArrayList();
  }

  public String getName() {
    return myName.toString();
  }

  public void addTerm(TermDescriptor td) {
    terms.add(td);
  }


private boolean isGoodConstantType(String required, Object current) {
  try { 
    return (Class.forName(required).isInstance(current));
  } catch (Exception e) {
    e.printStackTrace();
    return false;
  }
}


  public void checkAgainst(Frame f) throws OntologyException {
    for(int i = 0; i < terms.size(); i++) {
      TermDescriptor td = (TermDescriptor)terms.get(i);
      String name = td.getName();
      if(!td.isOptional()) {
	Object o = f.getSlot(name);
	if (td.isConstant()) {
	  if (!isGoodConstantType(td.getValueType(),o))
	    throw new WrongTermTypeException(f.getName(), name, td.getValueType()); 
	} else {
	  if(!(o instanceof Frame))
	    throw new WrongTermTypeException(f.getName(), name, "Frame"); 
	  myOntology.check((Frame)o);
	}

      } // End of 'if slot is not optional'

    } // End of slot iteration

  }

  Iterator subSchemas() {
    return terms.iterator();
  }

  TermDescriptor[] termsArray() {
    Object[] objs = terms.toArray();
    TermDescriptor[] result = new TermDescriptor[objs.length];
    System.arraycopy(objs, 0, result, 0, objs.length);
    return result;
  }

}

