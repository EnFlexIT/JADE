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
// RELEASE:	  1.0	
// MODIFIED:  24/03/1999	
// 
//////////////////////////////////////////////////////////////
package jade.tools.DummyAgent;
// Import AWT classes 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// Import the java.util classes
import java.util.*;

// Import useful Jade classes
import jade.core.*;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.*;

public class DummyBehaviour extends SimpleBehaviour
{
	public DummyBehaviour(Agent a)
	{
		super(a);
	}

	public void action()
	{
		ACLMessage msg = myAgent.blockingReceive();
		((DummyAgent)myAgent).queuedMsgListModel.add(0, (Object) new MsgIndication(msg, MsgIndication.INCOMING, new Date()));

		return;
	}

	public boolean done()
	{
		return false;
	}
}

	

