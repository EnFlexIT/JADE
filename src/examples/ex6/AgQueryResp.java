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

package examples.ex6;

import jade.core.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.lang.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universit� di Parma
@version $Date$ $Revision$
*/

public class AgQueryResp extends Agent {


class myBehaviour extends FipaQueryResponderBehaviour {


public ACLMessage handleQueryMessage(ACLMessage msg) {
  System.err.println(myAgent.getLocalName()+" has replied to the following message: ");
  msg.dump();
  ACLMessage msg1 = msg.createReply(); 
  msg1.setContent(msg.getContent());
  msg1.setType("inform");
  return msg1;
 }

public myBehaviour(Agent a) {
 super(a);
 }

}

protected void setup() {
    addBehaviour(new myBehaviour(this));
  }

}


