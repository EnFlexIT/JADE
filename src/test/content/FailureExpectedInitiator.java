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

package test.content;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.content.onto.OntologyException;

import test.common.Test;

public abstract class FailureExpectedInitiator extends OneShotBehaviour {

	private String resultKey;
	private boolean verbose;
	
  public FailureExpectedInitiator(Agent a, DataStore ds, String key) {
  	super(a);
  	setDataStore(ds);
  	resultKey = key;
  	verbose = false;
  }
  
  public void action() {
  	try {
  		prepareMessage();
  	}
  	catch (OntologyException oe) {
  		System.out.println("Ontology exception thrown as expected: "+oe.getMessage());
  		if (verbose) {
  			oe.printStackTrace();
  		}
  		getDataStore().put(resultKey, new Integer(Test.TEST_PASSED));
  		return;
  	}
  	catch (Throwable t) {
  		if (verbose) {
  			t.printStackTrace();
  		}
  	}
  	getDataStore().put(resultKey, new Integer(Test.TEST_PASSED));
  }
  
  protected abstract ACLMessage prepareMessage() throws Exception;
}  
