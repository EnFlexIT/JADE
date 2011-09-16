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

package chat.client;

//#MIDP_EXCLUDE_FILE
//#J2SE_EXCLUDE_FILE
//#PJAVA_EXCLUDE_FILE

import jade.util.Logger;
import android.content.Context;
import android.content.Intent;

/**
 * This class implement the chat GUI for the android specific implementation.
 * 
 * @author Michele Izzo - Telecom Italia
 */

public class AndroidChatGui implements ChatGui {
	private Logger logger = Logger.getMyLogger(this.getClass().getName());

	private static ChatClientAgent myAgent;
	private Context myContext;

	public AndroidChatGui(Context myContext, ChatClientAgent myAgent) {
		logger.info("GUI started!");
		AndroidChatGui.myAgent = myAgent;
		this.myContext = myContext;

		Intent broadcast = new Intent();
		broadcast.setAction("jade.demo.chat.SHOW_CHAT");
		logger.info("Sending broadcast " + broadcast.getAction());
		this.myContext.sendBroadcast(broadcast);
	}

	public static void handleSpoken(String s) {
		myAgent.handleSpoken(s);
	}

	@Override
	public void notifyParticipantsChanged(String[] names) {
		Intent broadcast = new Intent();
		broadcast.setAction("jade.demo.chat.REFRESH_PARTECIPANTS");
		broadcast.putExtra("partecipants", names);
		logger.info("Sending broadcast " + broadcast.getAction());
		myContext.sendBroadcast(broadcast);
	}

	@Override
	public void notifySpoken(String speaker, String sentence) {
		Intent broadcast = new Intent();
		broadcast.setAction("jade.demo.chat.REFRESH_CHAT");
		broadcast.putExtra("sentence", speaker + ": " + sentence + "\n");
		logger.info("Sending broadcast " + broadcast.getAction());
		myContext.sendBroadcast(broadcast);
	}

	@Override
	public void dispose() {
		// Nothing to do
	}
}
