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

package test.domain.df.tests;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import java.util.Date;

/** This agent uses RegistrationRenewerBehaviour to keep itself registered
 *  in case the register methofd return a lease_time lesser thant one requested
 * @author  chiarotto
 */
public class TestRegistrationRenewerAgent extends Agent {
   // Register itself and than re-register itselt using the RegistrationRenewer
   // behaviour (note the in JADE a the method register assign the lease_time
   // requested by an agent
   public void setup() {
   	// Wait for the startup message and get the requested lease time
   	ACLMessage msg = blockingReceive();
    System.out.println(getLocalName()+": Startup message received");
   	long leaseTime = Long.parseLong(msg.getContent());
   	
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setLeaseTime(new Date(System.currentTimeMillis()+leaseTime));
        dfd.setName(getAID());

        try {
                // register agent for requested lease time (40 secs)
                DFAgentDescription actualDfd = DFService.register(this, dfd);
                System.out.println(getLocalName()+": Requested lease time is "+dfd.getLeaseTime());
                System.out.println(getLocalName()+": Granted lease time is "+actualDfd.getLeaseTime());
                DFService.keepRegistered(this, getDefaultDF(), actualDfd, dfd.getLeaseTime());
        }catch (FIPAException fe) {
            fe.printStackTrace();
            return;
        }	
   }
    
   public void takeDown() {
       try{
            DFService.deregister(this);
       }catch(Exception e) {
       			// Do nothing as the registration should have already expired
       }
   }
}
