/*
 * TestRegistrationRenewerAgent.java
 *
 * Created on 3 febbraio 2003, 11.14
 */

package test.domain.df.tests;
import jade.core.Agent;
import jade.domain.FIPAException;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import java.util.Date;
import jade.domain.DFRegistrationRenewer;

/** This agent uses RegistrationRenewerBehaviour to keep itself registered
 *  in case the register methofd return a lease_time lesser thant one requested
 * @author  chiarotto
 */
public class TestRegistrationRenewerAgent extends Agent {
   // Register itself and than re-register itselt using the RegistrationRenewer
   // behaviour (note the in JADE a the method register assign the lease_time
   // requested by an agent
   public void setup() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setLeaseTime(new Date(System.currentTimeMillis()+5000));
        dfd.setName(getAID());
        // register the agent for 5 seconds create RegistrationRenewerBehaviour 

        try {
                // register agent for 5 seconds
                Date exactLeaseTime = DFService.register(this,dfd);
                System.out.println("lease time returned by the register:"+exactLeaseTime);
                // register the agent for other 10 seconds
                dfd.setLeaseTime(new Date(dfd.getLeaseTime().getTime()+10000));
                
                addBehaviour(new DFRegistrationRenewer(this,exactLeaseTime,dfd));
        }catch (FIPAException fe) {
            fe.printStackTrace();
            return;
        }	
   }
    
   public void takeDown() {
       try{
            DFService.deregister(this);
       }catch(Exception e) {
            e.printStackTrace();
       }
        doDelete();
   }
}
