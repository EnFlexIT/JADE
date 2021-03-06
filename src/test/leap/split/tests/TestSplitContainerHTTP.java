package test.leap.split.tests;

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

import jade.core.Agent;
import test.common.JadeController;
import test.common.Test;
import test.common.TestException;
import test.common.TestUtility;

/**
 * 
 * @author Tiziana Trucco
 * @version $Date:  $ $Revision: $
 *
 */
public class TestSplitContainerHTTP extends TestSplitContainerBasic{
	
	JadeController jc1, jc;

	//create a container with an HTTPPeer
	//create a split-container using HTTPFEDispatcher
	protected String createSplitContainer() throws TestException{
		
		log("Launching normal container with HTTPPeer...");
		jc1 = TestUtility.launchJadeInstance("Container-2", null, new String("-container -host "+TestUtility.getLocalHostName()+ " -port "+String.valueOf(Test.DEFAULT_PORT)+" -icps jade.imtp.leap.JICP.JICPPeer;jade.imtp.leap.http.HTTPPeer(3099)"), null);
		log("Launching split-container...");
		jc = TestUtility.launchSplitJadeInstance("Split-Container-1", null, new String("-host "+TestUtility.getContainerHostName(myAgent, jc1.getContainerName())+" -port 3099" + " -connection-manager jade.imtp.leap.http.HTTPFEDispatcher"));
		return jc.getContainerName();
	}
	
	public void clean(Agent a) {
  	try {
  		jc1.kill();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  	try {
  		jc.kill();
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }  
	
}
