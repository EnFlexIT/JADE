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

import jade.core.FrontEnd;
import jade.core.IMTPException;
import jade.core.NotFoundException;
import jade.lang.acl.ACLMessage;

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
		Command r = executeRemotely(c, -1);
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
		Command r = executeRemotely(c, 0);
  }
}

