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
package examples.protocols;

import jade.proto.*;
import jade.core.*;
import jade.core.behaviours.*;
import jade.util.leap.*;
import jade.lang.acl.*;

/**
 * This example shows how the AchieveREInitiator can be used
 * by registering some behaviours to handle some states.
 * @author Fabio Bellifemine - TILab
 * @version $Date$ $Revision$
 **/
public class ComplexInitiator extends Initiator {

    public void setup() {
	// create a MyInitiator (see class Initiator) that restarts
	// the protocol when it terminates
	AchieveREInitiator b = new Initiator.MyInitiator(this, createNewMessage(), true);
	b.registerPrepareRequests(new RWBehaviour(b.REQUEST_KEY, b.ALL_REQUESTS_KEY,true));
	b.registerHandleAgree(new RWBehaviour(b.REPLY_KEY, null,true));
	b.registerHandleRefuse(new RWBehaviour(b.REPLY_KEY, null,true));
	b.registerHandleNotUnderstood(new RWBehaviour(b.REPLY_KEY, null,true));
	b.registerHandleInform(new RWBehaviour(b.REPLY_KEY, null,true));

	b.registerHandleFailure(new RWBehaviour(b.REPLY_KEY, null,true));
	b.registerHandleAllResponses(new RWBehaviour(b.ALL_RESPONSES_KEY, null,true));

	// when all the result notifications are collected, 
	// a new initiator is launched
	// notice that this second MyInitiator has false in the constuctor
	// in order to avoid restarting the protocol when it terminates.
	AchieveREInitiator b2 = new Initiator.MyInitiator(this, createNewMessage(), false);
	b.registerHandleAllResultNotifications(b2); // take care that this method has shared the datastore of b and b2 !
	//b2.setDataStore(new DataStore()); // this method separates the 2 datastores

	//b.registerHandleAllResultNotifications(new RWBehaviour(b.ALL_RESULT_NOTIFICATIONS_KEY, null,true));
	addBehaviour(b);
    }

    /**
     * This behaviour just reads something from the datastore
     * and writes something into the datastore.
     * It has a lot of parameters in order to reuse all time the same
     * behaviour. However, understanding how this specific behaviour
     * works does not help so much; it is better to concentrate on
     * code above.
     **/
    class RWBehaviour extends OneShotBehaviour {
	Object rk, wk;
	boolean wl;

	RWBehaviour(Object readKey, Object writeKey, boolean writeAList) {
	    super(ComplexInitiator.this);
	    //setDataStore(b.getDataStore());
	    rk = readKey;
	    wk = writeKey;
	    wl = writeAList;
	}

	public void action() {
	    if (rk != null) {
		System.out.print(myAgent.getLocalName()+" read from datastore at key "+rk+":");
		Object val = getDataStore().get(rk);
		if (val instanceof List)
		    for (Iterator i=((List)val).iterator(); i.hasNext(); )
			System.out.print("  "+ACLMessage.getPerformative(((ACLMessage)i.next()).getPerformative()));
		else 
		    System.out.print("  "+ACLMessage.getPerformative(((ACLMessage)val).getPerformative()));
		System.out.println();
	    } 
	    if (wk != null) {
		Object valR = getDataStore().get(rk);
		Object valW;
		if (wl) {
		    if (valR instanceof List) 
			valW = valR;
		    else {
			valW = new ArrayList();
			((List)valW).add(valR);
		    }
		    /*System.out.println(myAgent.getLocalName()+" write into datastore at key "+wk+":");
		    for (Iterator i=((List)valW).iterator(); i.hasNext(); )
		    System.out.println("  "+i.next());*/
		} else {
		    if (valR instanceof List) 
			valW = ((List)valR).get(0);
		    else
			valW = valR;
		    /*System.out.println(myAgent.getLocalName()+" write into datastore at key "+wk+":"+valW);*/
		}
		getDataStore().put(wk,valW);
	    }
	}
    }
}
