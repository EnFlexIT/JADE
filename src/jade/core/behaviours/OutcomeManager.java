/*****************************************************************
 WADE - Workflow and Agent Development Environment is a framework to develop 
 multi-agent systems able to execute tasks defined according to the workflow
 metaphor.
 Copyright (C) 2008 Telecom Italia S.p.A. 

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
package jade.core.behaviours;

//#J2ME_EXCLUDE_FILE

import java.io.Serializable;
import jade.core.behaviours.Behaviour;
import jade.util.Logger;

/**
 * Utility class to manage the outcome of a behaviour in a uniform way
 * @author Giovanni Caire
 */
public class OutcomeManager implements Serializable {
	private static final long serialVersionUID = -87842234567654L;

	public static final int OK = 1;
	public static final int KO = 0;
	
	private Behaviour bh;
	
	private int exitCode = OK;
	private String errorMsg;
	
	private Logger myLogger = Logger.getJADELogger(getClass().getName());
	
	public OutcomeManager(Behaviour bh) {
		this.bh = bh;
	}
	
	/**
	 * Retrieve the exit-code that indicates whether the behaviour holding this OutcomeManager succeeded (<code>OK</code>) or failed (<code>KO</code>)
	 * @return The exit-code that indicates whether the behaviour holding this OutcomeManager succeeded (<code>OK</code>) or failed (<code>KO</code>)
	 */
	public int getExitCode() {
		return exitCode;
	}
	
	public boolean isSuccessful() {
		return exitCode == OK;
	}
	
	/**
	 * Retrieve the error-message providing details about the reason of failure of the behaviour holding this OutcomeManager .
	 * @return The error-message providing details about the reason of failure of the behaviour holding this OutcomeManager or null if the behaviour succeeded
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * Mark the behaviour holding this OutcomeManager as failed and store the error-message that can then be retrieved by means of the 
	 * <code>getErrorMsg()</code> method
	 * @param msg The error message. 
	 * @param e The Exception, if any, that caused the behaviour to fail
	 */
	public void error(String msg, Exception e) {
		exitCode = KO;
		if (e != null) {
			myLogger .log(Logger.WARNING, "Agent "+bh.getAgent().getLocalName()+" - "+msg, e);
			String exMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
			errorMsg = msg+" - "+exMsg;
		}
		else {
			myLogger .log(Logger.WARNING, "Agent "+bh.getAgent().getLocalName()+" - "+msg);
			errorMsg = msg;
		}
	}

	/**
	 * Used to propagate an already logged error through a hierarchy of behaviours
	 */
	public void propagateError(String msg) {
		exitCode = KO;
		errorMsg = msg;
	}
}
