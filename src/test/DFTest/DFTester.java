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


package test.DFTest;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Date;
import java.io.*;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.DFService;
import jade.lang.acl.ACLMessage;

/**

*/

public class DFTester extends Agent {

    static int numOfTesterThatHaveFinished = 0;  
    static final long startingTime = System.currentTimeMillis();

    public void setup() {
	try {
	    Object[] args = getArguments();
	    AID parentName = getDefaultDF(); 
	   
	    DFService.register(this,parentName,getDescription());
	    
	    int i = Integer.parseInt((String)args[0]);
	    int count = Integer.parseInt((String)args[1]);
	    terminated(count);
	}catch(FIPAException fe){fe.printStackTrace();}
    }
    
    synchronized void terminated(int count) {
	numOfTesterThatHaveFinished++;
	if (numOfTesterThatHaveFinished == count) {
	    System.out.println("N="+count+" "+getClass().getName()+" agents have terminated in "+(System.currentTimeMillis()-startingTime)+" msec");
	}
	System.out.println("Agent: " + numOfTesterThatHaveFinished + " Terminated");
    }
	    
    private DFAgentDescription getDescription()
    {
	DFAgentDescription dfd = new DFAgentDescription();
	dfd.setName(getAID());
	ServiceDescription sd = new ServiceDescription();
	sd.setName(getLocalName() + "-Service");
	sd.setType("Tester-Agent");
	sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
	sd.addOntologies("fipa-agent-management");
	sd.setOwnership("JADE");
	dfd.addServices(sd);
	return dfd;
    }
    
}

