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

import jade.lang.acl.ACLMessage;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

class LocalProxy implements AgentProxy {

  private AgentContainer container;
  private AID receiverID;

  public LocalProxy(AgentContainer ac, AID id) {
    container = ac;
    receiverID = id;
  }

  public void dispatch(ACLMessage msg) throws NotFoundException {
    try {
      container.dispatch(msg, receiverID);
    }
    catch(IMTPException imtpe) {
			throw new NotFoundException("Communication problem: " + imtpe.getMessage());
    }

  }

  public void ping() throws UnreachableException {
    try {
      container.ping(false);
    }
    catch(IMTPException imtpe) {
			throw new UnreachableException("Unreachable remote object: [" + imtpe.getMessage() + "]");
    }
  }
  	
}
