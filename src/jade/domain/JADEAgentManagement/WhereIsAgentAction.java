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

package jade.domain.JADEAgentManagement;

import jade.core.*;
import jade.content.*;


/**

  This class represents the <code>where-is-agent</code> action,
  requesting to send the location where the given agent is deployed.

   @author Giovanni Rimassa -  Universita' di Parma
   @version $Date$ $Revision$
*/
public class WhereIsAgentAction implements AgentAction {


    private AID agentName;


    /**
       Default constructor. A default constructor is necessary for
       ontological classes.
    */
    public WhereIsAgentAction() {
    }

    /**
       Set the <code>agent-identifier</code> slot of this action.
       @param id The agent identifier for the agent whose location is
       requested.
    */
    public void setAgentIdentifier(AID id) {
	agentName = id;
    }

    /**
       Retrieve the value of the <code>agent-identifier</code> slot of
       this action, containing the agent identifier for the agent
       whose location is requested.
       @return The value of the <code>agent-identifier</code> slot, or
       <code>null</code> if no value was set.
    */
    public AID getAgentIdentifier() {
	return agentName;
    }

}
