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

import jade.core.BackEnd;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

/**
 * Class declaration
 * @author Giovanni Caire - TILAB
 */
public class BackEndStub extends MicroStub implements BackEnd {
	static final int BORN_AGENT = 20;
	static final int DEAD_AGENT = 21;
	static final int SUSPENDED_AGENT = 22;
	static final int RESUMED_AGENT = 23;
	static final int MESSAGE_OUT = 24;
	
	public BackEndStub(Dispatcher d) {
		super(d);
	}	
	
	/**
	 */
  public String[] bornAgent(String name) throws IMTPException {
  	Command c = new Command(BORN_AGENT);
  	c.addParam(name);
  	try {
  		disableFlush();
  		//Logger.println("Executing BORN_AGENT");
  		Command r = executeRemotely(c);
  		if (r.getParamCnt() > 0) {
  			return (String[]) r.getParamAt(0);
  		}
  		else {
  			return null;
  		}
  	}
  	catch (ICPException icpe) {
  		// Destination unreachable
  		// The BORN_AGENT command must not be buffered
  		throw new IMTPException("Destination unreachable", icpe);
  	}		  			  			
  	finally {
  		enableFlush();
  	}
  }

  /**
	 */
  public void deadAgent(String name) throws IMTPException {
  	Command c = new Command(DEAD_AGENT);
  	c.addParam(name);
  	try {
  		disableFlush();
  		//Logger.println("Executing DEAD_AGENT");
  		Command r = executeRemotely(c);
  	}
  	catch (ICPException icpe) {
  		// Destination unreachable. Store the command for later delivery
  		store(c);
  	}		  			
  	finally {
  		enableFlush();
  	}
  }
  
  /**
	 */
  public void suspendedAgent(String name) throws NotFoundException, IMTPException {
  	Command c = new Command(SUSPENDED_AGENT);
  	c.addParam(name);
  	try {
  		disableFlush();
  		Command r = executeRemotely(c);
  		if (r.getCode() == Command.ERROR) {
  			// One of the expected exceptions occurred in the remote BackEnd
  			// --> It must be a NotFoundException --> throw it
  			throw new NotFoundException((String) r.getParamAt(2));
  		}
  	}
  	catch (ICPException icpe) {
  		// Destination unreachable. Store the command for later delivery
  		store(c);
  	}		  			
  	finally {
  		enableFlush();
  	}
  }
  
  /**
	 */
  public void resumedAgent(String name) throws NotFoundException, IMTPException {
  	Command c = new Command(RESUMED_AGENT);
  	c.addParam(name);
  	try {
  		disableFlush();
  		Command r = executeRemotely(c);
  		if (r.getCode() == Command.ERROR) {
  			// One of the expected exceptions occurred in the remote BackEnd
  			// --> It must be a NotFoundException --> throw it
  			throw new NotFoundException((String) r.getParamAt(2));
  		}
  	}
  	catch (ICPException icpe) {
  		// Destination unreachable. Store the command for later delivery
  		store(c);
  	}		  			
  	finally {
  		enableFlush();
  	}
  }
  
  /**
	 */
  public void messageOut(ACLMessage msg, String sender) throws NotFoundException, IMTPException {
  	Command c = new Command(MESSAGE_OUT);
  	c.addParam(msg);
  	c.addParam(sender);
  	try {
  		disableFlush();
  		//Logger.println("Executing MESSAGE_OUT");
  		Command r = executeRemotely(c);
  		if (r.getCode() == Command.ERROR) {
  			// One of the expected exceptions occurred in the remote BackEnd
  			// --> It must be a NotFoundException --> throw it
  			throw new NotFoundException((String) r.getParamAt(2));
  		}
  	}
  	catch (ICPException icpe) {
  		// Destination unreachable. Store the command for later delivery
  		store(c);
  	}		  			
  	finally {
  		enableFlush();
  	}
  }
}

