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


package examples.ex2;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Università di Parma
@version  $Date$ $Revision$  
*/


// An agent who continuously receives messages and sends back replies.
public class AgentTimeout extends Agent {

  private ReceiverBehaviour.Handle h;

  protected void setup() {

    ComplexBehaviour main = new SequentialBehaviour(this) {
      protected void postAction() {
	reset();
      }

    };

    MessageTemplate mt = MessageTemplate.MatchType("inform");
    h = ReceiverBehaviour.newHandle();
    main.addSubBehaviour(new ReceiverBehaviour(this, h, 5000, mt));
    main.addSubBehaviour(new OneShotBehaviour(this) {
      public void action() {
	try {
	  System.out.println("About to read the message...");
	  ACLMessage msg = h.getMessage();
	  msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
	}
	catch(ReceiverBehaviour.TimedOut rbto) {
	  System.out.println("Exception caught: " + rbto.getMessage());
	}
	catch(ReceiverBehaviour.NotYetReady rbnyr) {
	  System.out.println("ERROR !!! It should't happen.");
	  rbnyr.printStackTrace();
	}
      }
    });

    addBehaviour(main);

  }

}

