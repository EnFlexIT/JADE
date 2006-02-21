/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
 * Copyright (C) 2001 Telecom Italia LAB S.p.A.
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
 * **************************************************************
 */

package jade.imtp.leap;

import jade.core.*;
import jade.mtp.TransportAddress;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.Logger;

/**
 * @author Giovanni Caire - Telecom Italia LAB
 */
class Stub {
	protected static final String UNRCH_ERROR_MSG = "Remote object unreachable";
	protected static final String DISP_ERROR_MSG = "Dispatcher error";
	
	// The addresses of the remote object
	protected List              remoteTAs = new ArrayList();
	
	// The ID of the remote object
	protected int               remoteID;
	
	// The local singleton CommandDispatcher
	//protected CommandDispatcher theDispatcher = null;
	protected StubHelper theDispatcher = null;
	
	protected Logger myLogger = Logger.getMyLogger(getClass().getName());
	
	/**
	 * Default Constructor
	 */
	protected Stub() {
		//theDispatcher = CommandDispatcher.getDispatcher();
	}
	
	/**
	 * Build a stub for a given already remotized object.
	 * Note that TAs are added separately as we don't know
	 * at this stage whether this is a Stub to reach a remote
	 * object or a Stub for a local object.
	 */
	protected Stub(int id) {
		this();
		remoteID = id;
	}
	
	protected void bind(StubHelper sh) {
		theDispatcher = sh;
	}
	
	/**
	 * Add a TransportAddress to the list of addresses of the remote
	 * object this is a stub of
	 */
	protected void addTA(TransportAddress ta) {
		remoteTAs.add(ta);
	} 
	
	protected void removeTA(TransportAddress ta) {
		remoteTAs.remove(ta);
	}
	
	protected void clearTAs() {
		remoteTAs.clear();
	}
	
	/**
	 * Check whether an exception occurred in the remote site
	 */
	protected int checkResult(Command result, String[] expectedExceptions) throws IMTPException {
		if (result.getCode() == Command.ERROR) {
			
			// An exception was thrown in the remote container.
			// Check if it is one of the expected exceptions.
			String exceptionName = (String) result.getParamAt(0);
			
			if (expectedExceptions != null) {
				for (int i = 0; i < expectedExceptions.length; ++i) {
					// FIXME: This check does not work for extended exceptions
					if (exceptionName.equals(expectedExceptions[i])) {
						
						// Return the index of the expected exception (first index is 1)
						return i+1;
					} 
				}
			}
			
			// The exception thrown is not among the expected exceptions -->
			// Print a notification and throw IMTPException
			myLogger.log(Logger.WARNING, "EXCEPTION in remote container: "+exceptionName);
			throw new IMTPException(exceptionName+" occurred in remote container ["
					+(String) result.getParamAt(1)+"]");
		} 
		else if (result.getCode() != Command.OK) {
			throw new IMTPException("Unknown code in result command");
		} 
		
		// If no exception occurred, return 0
		return 0;
	} 
	
}

