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

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

public class TermDescriptor {

  private Name myName;
  private int type;
  private String typeName;
  private boolean optionality;


  public TermDescriptor(String n, int t, String tn, boolean o) {
    myName = new Name(n);
    type = t;
    typeName = tn;
    optionality = o;
  }

  public TermDescriptor(String n, int t, boolean o) {
    this(n, t, Ontology.typeNames[t], o);
  }

  public String getName() {
    return myName.toString();
  }

  public int getType() {
    return type;
  }

  public String getTypeName() {
    return typeName;
  }

  public boolean isOptional() {
    return optionality;
  }

  public boolean isComplex() {
    return ( type == Ontology.CONCEPT_TYPE) || (type == Ontology.ACTION_TYPE) || ( type == Ontology.PREDICATE_TYPE);
  }

}

