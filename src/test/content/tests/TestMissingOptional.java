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

package test.content.tests;

import test.content.Test;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.content.ContentManager;
import examples.content.ecommerceOntology.*;
import test.content.testOntology.Exists;

public class TestMissingOptional extends Test{
  public String getName() {
  	return "Missing-optional-attribute";
  }
  public String getDescription() {
  	StringBuffer sb = new StringBuffer("Tests a content including a concept with a missing optional attribute");
  	return sb.toString();
  }
  public int execute(ACLMessage msg,  Agent a, boolean verbose) {
  	try {
  		Item i = new Item();
  		// Optional attribute serialID not set 
  		Exists e = new Exists(i);
  		a.getContentManager().fillContent(msg, e);
  		return SEND_MSG;
  	}
  	catch (Throwable t) {
  		if (verbose) {
  			t.printStackTrace();
  		}
  		return DONE_FAILED;
  	}
  }
}
