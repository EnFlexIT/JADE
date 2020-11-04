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

import java.awt.*;
import java.awt.event.*;

/**
   @author Giovanni Caire - TILAB
 */
class ParticipantsFrame extends Frame {
	private AWTChatGui parent;
	private TextArea participants;
	private String me;
	
	ParticipantsFrame(AWTChatGui parent, String me) {
		this.parent = parent;
		this.me = me;
		
		setTitle("Participants: ");
		setSize(parent.getSize());
		
		participants = new TextArea();
		participants.setEditable(false);
		participants.setBackground(Color.white);
		participants.setText(me+"\n");
		add(participants, BorderLayout.CENTER);
				
		Button b = new Button("Close");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			} 
		} );
		
		add(b, BorderLayout.SOUTH);
		addWindowListener(new	WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}
		} );
	}
	
	void refresh(String[] ss) {
		participants.setText(me+"\n");
		if (ss != null) {
			for (int i = 0; i < ss.length; ++i) {
				participants.append(ss[i]+"\n");
			}
		}
	}
	
}