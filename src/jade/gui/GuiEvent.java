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

package jade.gui;

// Import required Jade classes
import jade.core.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
This class defines the object type <code>GuiEvent</code> used to notify 
an event to a GuiAgent. It has two mandatory attributes:the source of the event 
and an integer identifying the type of event and an optional list of parameters 
than can be added to the event object.The type of each parameter must extends
<code>java.lang.Object</code>; therefore primitive object (e.g.int) should be wrapped
into appropriate objects(e.g <code>java.lang.Integer</code>).

@see jade.gui.GuiAgent
@author Giovanni Caire - CSELT S.p.A.
@version $Date$ $Revision$
*/

public class GuiEvent 
{
  protected Object source;
  protected int type;
  private List parameters;

 
  public GuiEvent(Object eventSource, int eventType)
  {
    source = eventSource;
    type = eventType;	
    parameters = new ArrayList();
  }

  public int getType()
  {
    return(type);
  }

  public Object getSource()
  {
    return(source);
  }

  /** adds a new parameter to this event 
  * @param param is the parameter 
  **/
  public void addParameter(Object param) {
    parameters.add(param);
  }     

  /**
   * get the parameter in the given position
   * @return the Object with the prameter
   **/
  public Object getParameter(int number) {
    return parameters.get(number);
  }

  /**
   * get an Iterator over all the parameters
   **/
  public Iterator getAllParameter() {
    return parameters.iterator();
  }
}