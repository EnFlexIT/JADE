/*****************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 **************************************************************/

package jade.tools.applet;

import java.io.PrintStream;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLParser;
import jade.lang.acl.ParseException;
import jade.onto.Ontology;
import jade.onto.Frame;
import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;
import jade.lang.Codec;

/**
 * This class implements a request protocol that can be used by an applet
 * to comunicate with a proxy agent.
 * @author Tiziana Trucco - CSELT S.p.A.
 * @version $Date$  $
 */

abstract class AppletRequestProto
{
    
    ACLMessage reqMsg;
    boolean notYetReady;
    PrintStream myOut;
    ACLParser myParser;
    
        /**
         * Constructor of the class.
         * @param out The printStream used to send/receive the message
         * @param parser the ACLParser used to read the message
         * @param request The ACLMessage to send
         */
    AppletRequestProto(PrintStream out, ACLParser parser,ACLMessage request )
    {
        this.myOut = out;
        this.myParser = parser;
        this.notYetReady = true;
        
        try{
            this.reqMsg = (ACLMessage)request.clone();
            this.reqMsg.setPerformative(ACLMessage.REQUEST);
            
        }catch(Exception e){
            this.reqMsg = new ACLMessage(ACLMessage.REQUEST);
        }
        
    }
    
        /**
         * This method starts the protocol.
         */
    public void doProto()
    {
        //System.out.println(reqMsg.toString());
        
        sendMessage(reqMsg.toString());
        
        try{
            ACLMessage reply = receiveMessage();
            System.out.println("Received Message:" + reply.toString());
            if(ACLMessage.AGREE == (reply.getPerformative()))
            {
	
                handleAgree(reply);
	
                ACLMessage inform = receiveMessage();
                
                System.out.println(inform.toString());
                
                if(ACLMessage.INFORM == inform.getPerformative())
                    handleInform(inform);
                else
                    if(ACLMessage.FAILURE == inform.getPerformative())
                        handleFailure(inform);
                    else
                        handleOtherMessage(inform);
            }
            else
                if(ACLMessage.INFORM == reply.getPerformative())
                    handleInform(reply);
                else if(ACLMessage.REFUSE == reply.getPerformative())
                    handleRefuse(reply);
                else if(ACLMessage.NOT_UNDERSTOOD == reply.getPerformative())
                    handleNotUnderstood(reply);
                else handleOtherMessage(reply);
        }catch(ParseException e){
	    e.printStackTrace();
	}
        return;
    }
    
        /**
         * private method to read the message
         */
    private ACLMessage receiveMessage() throws ParseException {
	System.out.println("In receiveMessage");
        return myParser.Message();
       
    }
    
  /**
   * private method to send the message
   */
    
    private void sendMessage(String msg){
        myOut.println(msg);
    }
    
  /**
   * This method can be overriden by the programmer in order to handle
   * the received agree message.
   */
    protected abstract void handleAgree(ACLMessage msg);
    
  /**
   * This method can be overriden by the programmer in order to handle
   * the inform message received.
   */
    protected abstract void handleInform(ACLMessage inform);
    
  /**
   * This method can be overriden by the programmer in order to handle
   * the refuse message received.
   */
    protected abstract void handleRefuse(ACLMessage msg);
    
  /**
   * This method can be overriden by the programmer in order to handle
   * the failure message received.
   */
    protected abstract void handleFailure(ACLMessage msg);
    
  /**
   * This method can be overriden by the programmer in order to handle
   * the notUnderstood message received.
   */
    protected abstract void handleNotUnderstood(ACLMessage msg);
    
  /**
   * This method can be overriden by the programmer in order to handle
   * the message not stated by the protocol eventually received.
   */
    protected abstract void handleOtherMessage(ACLMessage msg);
    
    /**
     * Use this method to set the content of an ACLMessage.
     **/
    static String encode(Action act, Codec c, Ontology o) throws jade.domain.FIPAException {
    // Write the action in the :content slot of the request
        List l = new ArrayList();
        try {
            Frame f = o.createFrame(act, o.getRoleName(act.getClass()));
            l.add(f);
        } catch (jade.onto.OntologyException oe) {
            throw new jade.domain.FIPAException(oe.getMessage());
        }
         return c.encode(l,o);
    }
    
    /**
   * Use this method to extract the content of an ACLMessage
   */
  static ResultPredicate extractContent(String content, Codec c, Ontology o) throws jade.domain.FIPAException {
    try {
      List tuple = c.decode(content,o);
      tuple = o.createObject(tuple);
      return (ResultPredicate)tuple.get(0);
    } catch (Codec.CodecException e1) {
      e1.printStackTrace();
      throw new jade.domain.FIPAException(e1.getMessage());
    } catch (jade.onto.OntologyException e2) {
      e2.printStackTrace();
      throw new jade.domain.FIPAException(e2.getMessage());
    }
  }
}
