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



package examples.ex_mobile;

import java.util.StringTokenizer;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
Javadoc documentation for the file
@author Giovanni Caire - CSELT S.p.A
@version $Date$ $Revision$
*/
class ExecutorBehaviour extends SimpleBehaviour
{
	ExecutorBehaviour(Agent a)
	{
		super(a);
	}

	public boolean done()
	{
		return false;
	}

	public void action()
	{
		ACLMessage msg;
		MessageTemplate mt = MessageTemplate.MatchType("request");

		// Get a message from the queue or wait for a new one if queue is empty
		msg = myAgent.receive(mt);
		if (msg == null)
		{
			block();
		 	msg = myAgent.receive(mt);
		}
		
		if (msg != null)
		{
			String replySentence = new String("");

			// Get action to perform
			StringTokenizer st = new StringTokenizer(msg.getContent(), " ()");
			String action = (st.nextToken()).toLowerCase();
			// EXIT
			if      (action.equals("exit"))
			{
				System.out.println("They requested me to exit (Sob!)");
				// Set reply sentence
				replySentence = new String("\"OK exiting\"");
				myAgent.doDelete();
			}
			// STOP COUNTING
			else if (action.equals("stop"))
			{
				System.out.println("They requested me to stop counting");
				((MobileAgent) myAgent).cntEnabled = false;
				// Set reply sentence
				replySentence = new String("\"OK stopping\"");
			} 				
			// CONTINUE COUNTING
			else if (action.equals("continue"))
			{
				System.out.println("They requested me to continue counting");
				((MobileAgent) myAgent).cntEnabled = true;
				// Set reply sentence
				replySentence = new String("\"OK continuing\"");
			} 
			// MOVE TO ANOTHER LOCATION				
			else if (action.equals("move"))
			{
				// Get destination
				String dest = st.nextToken();
				System.out.println("They requested me to go to " + dest);
				// Set reply sentence
				replySentence = new String("\"OK moving to " + dest+" \"");
				// Prepare to move
				myAgent.doMove(dest);
			}

			// Reply
			ACLMessage replyMsg = msg.createReply();
			replyMsg.setType("inform");
			replyMsg.setContent(replySentence);
			myAgent.send(replyMsg);
		}

		return;
	}
}

