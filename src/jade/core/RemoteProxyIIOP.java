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

package jade.core;

import java.io.StringWriter;

import FIPA_Agent_97;

import jade.lang.acl.ACLMessage;

/**
@author Giovanni Rimassa - Universita` di Parma
@version $Date$ $Revision$
*/
class RemoteProxyIIOP implements RemoteProxy {

  private FIPA_Agent_97 ref;
  private String addr;

  public RemoteProxyIIOP(FIPA_Agent_97 fa97, String platformAddress) {
    ref = fa97;
    addr = platformAddress;
  }

  public void dispatch(ACLMessage msg) throws NotFoundException {
    try {

      String sender = msg.getSource();
      if(sender.indexOf('@') == -1)
	msg.setSource(sender + '@' + addr);

      StringWriter msgText = new StringWriter();
      msg.toText(msgText);

      ref.message(msgText.toString()); // CORBA call
      System.out.println("\n\n"+(new java.util.Date()).toString()+" OUTGOING IIOP MESSAGE TO ADDRESS "+addr+". "+msg.toString());
    }
    catch(org.omg.CORBA.SystemException oocse) {
      oocse.printStackTrace();
      throw new NotFoundException("IIOP communication failure: [" + oocse.toString() + "]");
    }

  }

  public void ping() throws UnreachableException {
    // FIXME: Should implement pinging for CORBA objects too
  }

}
