///////////////////////////////////////////////////////////////
//   
//   /     /  ___/  ___/   / /_   _/ 
//  /  /--/___  /  ___/   /_/ / / /   
// /_____/_____/_____/_____/_/_/_/
// 
// -----------------------------------------------------------
// PROJECT:   DUMMY AGENT	
// FILE NAME: DummyBehaviour.java	
// CONTENT:   This file includes the definition of the DummyBehaviour class
//            the defines the main behaviour of a DummyAgent
// AUTHORS:	  Giovanni Caire	
// RELEASE:	  2.0	
// MODIFIED:  18/06/1999	
// 
//////////////////////////////////////////////////////////////

package jade.tools.DummyAgent;

// Import required Java classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.util.*;

// Import required Jade classes
import jade.core.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.*;

class DummyBehaviour extends SimpleBehaviour
{
	DummyBehaviour(Agent a)
	{
		super(a);
	}

	public boolean done()
	{
		return false;
	}

	public void action()
	{
		ACLMessage msg = myAgent.blockingReceive();
		
		// ATTENTION!! In order to insert the received message in the queued message list 
		// I cannot simply do it e.g. 
		// ((DummyAgent)myAgent).getGui().queuedMsgListModel.add(0, (Object) new MsgIndication(msg, MsgIndication.INCOMING, new Date()));
		// WHY?
		// Because this is not thread safe!!!
		// In fact, if this operation is executed from this thread while the AWT Event Dispatching 
		// Thread is updating the JList component that shows the queued message list in the DummyAgent
		// GUI (e.g. because the user has just sent a message), this can cause an inconsistency
		// between what is shown in the GUI and what the queued message list actually contains.
		// HOW TO SOLVE THE PROBLEM?
		// I need to request the AWT Event Dispatching Thread to insert the received message
		// in the queued message list!
		// This can be done by using the invokeLater static method of the SwingUtilities class 
		// as below

		SwingUtilities.invokeLater(new EDTRequester((DummyAgent)myAgent, msg));

		return;
	}

	class EDTRequester implements Runnable
	{
		DummyAgent agent;
		ACLMessage msg;
		
		EDTRequester(DummyAgent a, ACLMessage m)
		{
			agent = a;
			msg = m;
		}

		public void run()
		{
			agent.getGui().queuedMsgListModel.add(0, (Object) new MsgIndication(msg, MsgIndication.INCOMING, new Date()));
		}
	}

}

	

