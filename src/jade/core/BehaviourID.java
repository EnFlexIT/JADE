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

package jade.core;

import jade.core.behaviours.*;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Collection;

import jade.content.Concept;

/**

  This class represents an unique identifier referring to a specific
  agent behaviour.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class BehaviourID implements Concept {

	private String name;
	private String className;
  private String kind; 
  private List children = new ArrayList();

  public BehaviourID () {
  }
  
  public BehaviourID (Behaviour b) {
      
      name = b.getBehaviourName();
      className = b.getClass().getName();      
      kind = getClassKind(b.getClass());      

      // If we have a composite behaviour, add the
      // children to this behaviour id.
      if (b instanceof CompositeBehaviour) {
          CompositeBehaviour c = (CompositeBehaviour)b;
          Iterator iter = c.getChildren().iterator();
          while (iter.hasNext()) {
              addChildren(new BehaviourID((Behaviour)iter.next()));
          }
      }
  }

  private String getClassKind(Class c) {
  	if (c == null) {
  		return null;
  	}
  	
  	String className = c.getName();
    // Remove the class name and the '$' characters from
    // the class name for readability.
    int dotIndex = className.lastIndexOf('.');
    int dollarIndex = className.lastIndexOf('$');
  	int lastIndex = (dotIndex > dollarIndex ? dotIndex : dollarIndex);
    if (lastIndex == -1) {
    	return className;
    }
    else if (lastIndex == dotIndex) {
      return className.substring(lastIndex+1);
    }
    else {
    	// This is an anonymous inner class (the name is not meaningful) --> 
    	// Use the extended class 
    	return getClassKind(c.getSuperclass());
    }
  }
      
  public void setName(String n) {
    name = n;
  }

  public String getName() {
    return name;
  }

  public void setClassName(String n) {
    className = n;
  }

  public String getClassName() {
    return className;
  }

  public void setKind(String k) {
    kind = k;
  }

  public String getKind() {
    return kind;
  }

  public void addChildren(BehaviourID bid) {
      children.add(bid);
  }

  public Iterator getAllChildren() {
      return children.iterator();
  }
  
  public boolean isSimple() {
      return (children.size() == 0);
  }
  
  public boolean equals(Object o) {
      boolean bEqual = false;
      if (o != null && o instanceof BehaviourID) {
          BehaviourID b = (BehaviourID)o;
          bEqual = b.name.equals(name) && b.className.equals(className) && b.kind.equals(kind);
      }
      return bEqual;
  }
  
  public String toString() {
      return name;
  }
}
