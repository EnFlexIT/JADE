///////////////////////////////////////////////////////////////
//   
//   /     /  ___/  ___/   / /_   _/ 
//  /  /--/___  /  ___/   /_/ / / /   
// /_____/_____/_____/_____/_/_/_/
// 
// -----------------------------------------------------------
// PROJECT:    DUMMY AGENT	
// FILE NAME:  DummyAgent.java	
// CONTENT:	   This file includes the definition of the DummyAgent class.
// AUTHORS:	   Giovanni Caire	
// RELEASE:	   5.0	
// MODIFIED:   21/06/1999	
// 
//////////////////////////////////////////////////////////////

package jade.tools.DummyAgent;

// Import required Java classes 
import java.awt.*;
import javax.swing.*;

// Import required Jade classes
import jade.core.*;
import jade.core.behaviours.Behaviour;


public class DummyAgent extends Agent 
{
	private DummyAgentGui myGui;

	// Extends the Agent setup method
	protected void setup()
	{
		///////////////////////////////
		// Create and display agent GUI
		myGui = new DummyAgentGui(this);
		myGui.showCorrect();

		///////////////////////
		// Add agent behaviour
		Behaviour b = new DummyBehaviour(this);
		addBehaviour(b);	
	
	}

        protected void takeDown() {
	    SwingUtilities.invokeLater(new Runnable() {
	      public void run() {
		myGui.setVisible(false);
		myGui.dispose();
	      }
	    });
	}

	public DummyAgentGui getGui()
	{
		return myGui;
	}

}

