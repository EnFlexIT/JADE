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

package jade.imtp.leap;

//#MIDP_EXCLUDE_FILE

import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;

import java.util.Vector;
import java.util.Enumeration;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class FrontEndStub extends MicroStub implements FrontEnd {
	
	public FrontEndStub(Dispatcher d) {
		super(d);
	}	
	
	/**
	 */
  public void createAgent(String name, String className, String[] args) throws IMTPException {
  	Command c = new Command(FrontEndSkel.CREATE_AGENT);
  	c.addParam(name);
  	c.addParam(className);
  	c.addParam(args);
  	// The CREATE_AGENT command must not be postponed  	
  	executeRemotely(c, 0);
  }

  /**
	 */
  public void killAgent(String name) throws NotFoundException, IMTPException {
  	Command c = new Command(FrontEndSkel.KILL_AGENT);
  	c.addParam(name);
		Command r = executeRemotely(c, -1);
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote FrontEnd
			// --> It must be a NotFoundException --> throw it
			throw new NotFoundException((String) r.getParamAt(2));
		}
  }
  
  /**
	 */
  public void suspendAgent(String name) throws NotFoundException, IMTPException {
  	Command c = new Command(FrontEndSkel.SUSPEND_AGENT);
  	c.addParam(name);
		Command r = executeRemotely(c, -1);
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote FrontEnd
			// --> It must be a NotFoundException --> throw it
			throw new NotFoundException((String) r.getParamAt(2));
		}
  }
  
  /**
	 */
  public void resumeAgent(String name) throws NotFoundException, IMTPException {
  	Command c = new Command(FrontEndSkel.RESUME_AGENT);
  	c.addParam(name);
		Command r = executeRemotely(c, -1);
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote FrontEnd
			// --> It must be a NotFoundException --> throw it
			throw new NotFoundException((String) r.getParamAt(2));
		}
  }
  
  /**
	 */
  public void messageIn(ACLMessage msg, String receiver) throws NotFoundException, IMTPException {
  	Command c = new Command(FrontEndSkel.MESSAGE_IN);
  	c.addParam(msg);
  	c.addParam(receiver);
  	//Logger.println(Thread.currentThread().getName()+": Executing MESSAGE_IN");
		Command r = executeRemotely(c, -1);
  	//Logger.println(Thread.currentThread().getName()+": MESSAGE_IN executed");
		if (r != null && r.getCode() == Command.ERROR) {
			// One of the expected exceptions occurred in the remote FrontEnd
			// --> It must be a NotFoundException --> throw it
			throw new NotFoundException((String) r.getParamAt(2));
		}
  }
  
  /**
	 */
  public void exit(boolean self) throws IMTPException {
  	Command c = new Command(FrontEndSkel.EXIT);
  	c.addParam(new Boolean(self));
		// The EXIT command must not be postponed
		executeRemotely(c, 0);
  }
  
  /**
	 */
  public void synch() throws IMTPException {
  	Command c = new Command(FrontEndSkel.SYNCH);
		// The SYNCH command must not be postponed
		executeRemotely(c, 0);
  }
    
  public List removePendingMessages(MessageTemplate template) {
  	synchronized (pendingCommands) {
  		List messages = new ArrayList();
  		List commands = new ArrayList();
  		Enumeration e = pendingCommands.elements();
  		while (e.hasMoreElements()) {
  			Command c = (Command) e.nextElement();
  			if (c.getCode() == FrontEndSkel.MESSAGE_IN) {
					ACLMessage msg = (ACLMessage) c.getParamAt(0);
  				if (template.match(msg)) {
  					Object[] oo = new Object[]{msg, c.getParamAt(1)};
  					messages.add(oo);
  					commands.add(c);
  				}
  			}
  		}
  		// Remove all the commands carrying matching messages
	  	Iterator it = commands.iterator();
	  	while (it.hasNext()) {
	  		pendingCommands.remove(it.next());
	  	}	
	  	
	  	// Return the list of matching messages
	  	return messages; 
  	}
  }
  
  /**
     Inner class AddressedMessage.
   */
  private static class AddressedMessage extends ACLMessage {
  	private AID receiver;
  	private ACLMessage wrapped;
  	
  	private AddressedMessage(AID receiver, ACLMessage msg) {
  		super(msg.getPerformative());
  		this.receiver = receiver;
  		wrapped = msg;
  	}
  	
		private static AddressedMessage wrap(AID r, ACLMessage msg) {
			AddressedMessage wrapper = null;
			if (msg != null) {
				wrapper = new AddressedMessage(r, msg);
				wrapper.setSender(msg.getSender());
				Iterator it = msg.getAllReceiver();
				while (it.hasNext()) {
					wrapper.addReceiver((AID) it.next());
				}
				
				it = msg.getAllReplyTo();
				while (it.hasNext()) {
					wrapper.addReplyTo((AID) it.next());
				}
				
	    	wrapper.setLanguage(msg.getLanguage());
	    	wrapper.setOntology(msg.getOntology());
	    	wrapper.setProtocol(msg.getProtocol());
	    	wrapper.setInReplyTo(msg.getInReplyTo());
	      wrapper.setReplyWith(msg.getReplyWith()); 
	    	wrapper.setConversationId(msg.getConversationId());
	    	wrapper.setReplyByDate(msg.getReplyByDate());
	    	if (msg.hasByteSequenceContent()) {
	    		wrapper.setByteSequenceContent(msg.getByteSequenceContent());
	    	}
	    	else {
	    		wrapper.setContent(msg.getContent());
	    	}
	    	wrapper.setEncoding(msg.getEncoding());
	    	wrapper.setEnvelope(msg.getEnvelope());
	    	
	    	Properties p = msg.getAllUserDefinedParameters();
	    	Enumeration e = p.propertyNames();
	    	while (e.hasMoreElements()) {
	    		String key = (String) e.nextElement();
	    		String value = (String) p.getProperty(key);
	    		wrapper.addUserDefinedParameter(key, value);
	    	}	
			}
			return wrapper; 
		}
		
		/**
		   Redefines the getAllIntendedReceiver() method in order to return 
		   the receiver.
		 */
  	public Iterator getAllIntendedReceiver() {
  		List l = new ArrayList(1);
  		l.add(receiver);
  		return l.iterator();
  	}
  	
  	public synchronized Object clone() {
  		return wrapped;
  	}
  }  // END of inner class AddressedMessage
}

