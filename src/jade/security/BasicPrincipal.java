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

package jade.security;

import jade.util.leap.Serializable;


/**

  This is the base class of all JADE Principals.

  @author Michele Tomaiuolo - Universita` di Parma
  @version $Date$ $Revision$
*/
public class BasicPrincipal implements Serializable
//__JADE_ONLY__BEGIN
		, java.security.Principal
//__JADE_ONLY__END
		{

  public static final String NONE = "none";
  protected String name;


  public BasicPrincipal() {
    this(NONE);
  }

  public BasicPrincipal(String name) {
    this.name = name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
  
  public String getParentName() {
    int dot = name.lastIndexOf('.');
    if (dot != -1)
      return name.substring(0, dot);
    else
      return null;
  }
  
  public String getShortName() {
    int dot = name.lastIndexOf('.');
    if (dot != -1)
      return name.substring(dot + 1, name.length());
    else
      return name;
  }
  
  public BasicPrincipal getParent() {
    String parentName = getParentName();
    if (parentName != null)
      return new BasicPrincipal(parentName);
    else
      return null;
  }

}
