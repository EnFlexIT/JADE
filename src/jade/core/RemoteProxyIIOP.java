/*
  $Log$
  Revision 1.4  1999/11/04 09:59:47  rimassaJade
  Removed TransientException specification from class declaration.

  Revision 1.3  1999/09/03 10:43:36  rimassa
  Changed CORBA exception handling.

  Revision 1.2  1999/08/27 15:46:57  rimassa
  Added support for TransientException in order to retry message
  dispatch when the receiver agent has moved.

  Revision 1.1  1999/03/17 13:14:26  rimassa
  A remote proxy for an agent that can be reached using IIOP (foreign agents and
  inter-platform mobile agents).

*/

package jade.core;

import java.io.StringWriter;

import FIPA_Agent_97;

import jade.lang.acl.ACLMessage;

class RemoteProxyIIOP extends RemoteProxy {

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
