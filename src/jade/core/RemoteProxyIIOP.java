/*
  $Log$
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
      throw new NotFoundException("IIOP communication failure: [" + oocse.getMessage() + "]");
    }

  }

  public void ping() throws UnreachableException {
    // FIXME: Should implement pinging for CORBA objects too
  }

}
