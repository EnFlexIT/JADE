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


package examples.ex1;

import java.io.*;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version  $Date$ $Revision$  
*/


// A test on Message Templates ...
public class Agent2 extends Agent {

  class Behaviour2 extends OneShotBehaviour {

    public Behaviour2(Agent a) {
      super(a);
    }

    public void action() {
      System.out.println(getName() + ": executing...");
      MessageTemplate mt1 = MessageTemplate.MatchProtocol("fipa-request");
      MessageTemplate mt2 = MessageTemplate.MatchOntology("fipa-agent-management");
      MessageTemplate mtAnd = MessageTemplate.and(mt1, mt2);

      System.out.println("Message Template 1:");
      mt1.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

      System.out.println("Message Template 2:");
      mt2.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

      System.out.println("Message Template 1 AND Message Template 2:");
      mtAnd.toText(new BufferedWriter(new OutputStreamWriter(System.out)));

      ACLMessage msg = new ACLMessage("inform");

      msg.setProtocol("fipa-request");
      msg.setOntology("pifa-chicken-management");

      System.out.println("Message :");
      msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
      if(mt1.match(msg))
	System.out.println("Matches 'mt1' template.");
      else
	System.out.println("Doesn't match 'mt1' template.");
      if(mt2.match(msg))
	System.out.println("Matches 'mt2' template.");
      else
	System.out.println("Doesn't match 'mt2' template.");
      if(mtAnd.match(msg))
	System.out.println("Matches 'mt1 AND mt2' template.");
      else
	System.out.println("Doesn't match 'mt1 AND mt2' template.");

      doDelete();
    }

  }


  protected void setup() {
    addBehaviour(new Behaviour2(this));
  }

}
