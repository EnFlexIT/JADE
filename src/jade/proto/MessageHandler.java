
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

package jade.proto;

import jade.lang.acl.ACLMessage;

/**
Javadoc documentation for the file
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/

/**************************************************************

  Name: MessageHandler

  Responsibility and Collaborations:

  + Provides an abstract interface for ProtocolDrivenBehaviour to
    invoke agent-specific code in order to perform some action when a
    specific message is received. An Interaction object is passed to
    user code, thus making the current interaction state accessible.
    (ProtocolDrivenBehaviour, ACLMessage, Interaction)


****************************************************************/
interface MessageHandler {

  public void handle(ACLMessage msg, Interaction i);
}
