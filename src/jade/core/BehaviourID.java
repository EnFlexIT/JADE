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

import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CompositeBehaviour;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Collection;

import java.util.StringTokenizer;
/**

  This class represents an unique identifier referring to a specific
  agent behaviour.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class BehaviourID {

  private String name;
  private String kind;
  private List children = new ArrayList();

  public BehaviourID () {
  }
  
  public BehaviourID (Behaviour b) {
      
      name = b.getClass().getName();
      
      // Remove the class name and the '$' characters from
      // the class name for readability.
      int dotIndex = name.lastIndexOf('.');
      int dollarIndex = name.lastIndexOf('$');
      int lastIndex = Math.max(dotIndex, dollarIndex);
      
      if (lastIndex != -1) {
          name = name.substring(lastIndex+1);
      }

      // If we have a composite behaviour, add the
      // children to this behaviour id.
      if (b instanceof CompositeBehaviour) {
          kind = "CompositeBehaviour";
          CompositeBehaviour c = (CompositeBehaviour)b;
          Iterator iter = c.getChildren().iterator();
          while (iter.hasNext()) {
              addChildren(new BehaviourID((Behaviour)iter.next()));
          }
      }
      else {
          kind = "Behaviour";
      }
  }

  public void setName(String n) {
    name = n;
  }

  public String getName() {
    return name;
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
      return (children.size() == 0) ? true : false;
  }
  
  public boolean equals(Object o) {
      boolean bEqual = false;
      if (o instanceof BehaviourID) {
          BehaviourID b = (BehaviourID)o;
          bEqual = b.name.equals(name) && b.kind.equals(kind);
      }
      return bEqual;
  }
  
  public String toString() {
      return name;
  }
}
