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

/*
 * RegistrationRenewerBehaviour.java
 *
 * Created on 31 gennaio 2003, 14.08
 */

package jade.domain;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.core.Agent;
import java.util.Date;

/** Behaviour that allow an agent to keep registered on a DF Agent if the lease_time
 * requested is greater than the one assigned by the DF.
 * The behaviour will use the modify method of a DFService to extend the time of
 * the registration. The following example shows how to use the behaviour:
 * <CODE>
 *    ...
 *    try {
 *            // register agent for 5 seconds
 *            long exactLeaseTime = DFService.register(this,dfd);
 *            addBehaviour(new RegistrationRenewerBehaviour(this,exactLeaseTime,dfd));
 *        }catch (FIPAException fe) {
 *            fe.printStackTrace();
 *            return;
 *        }
 *        ...
 * </CODE>
 *
 * @author Chiarotto alessandro - TILAB
 */
public class DFRegistrationRenewer extends jade.core.behaviours.TickerBehaviour {
    private DFAgentDescription dfd;
    private Agent a;
    private final static long OFFSET_LT = 5000;
    private long OFFSET_LTModifable;
    /** Creates a new instance of RegistrationRenewerBehaviour.
     * @param a is the pointer to the agent
     * @param renewerTime expire date of the registration
     */
    public DFRegistrationRenewer(Agent a,Date renewerTime,DFAgentDescription dfd) {
        super(a,((renewerTime != null) && (renewerTime.getTime() - System.currentTimeMillis() - OFFSET_LT > 0) ? renewerTime.getTime() - System.currentTimeMillis() - OFFSET_LT : 1));
        
        this.dfd = dfd;
        this.a = a;
        this.OFFSET_LTModifable = OFFSET_LT;
    }
    // Modify the registration, if the agent is not register it register the agent
    // this behaviour stops when the remaining lease time is =< 0
    protected void onTick() {
        if(dfd.getLeaseTime() == null) {
            stop();
            return;
        }
        long exactLeaseTime = 0;
        long remainingLeaseTime = dfd.getLeaseTime().getTime() - System.currentTimeMillis();
        if(remainingLeaseTime > 0) {
            try { 
                Date dateLeaseTime = DFService.modify(a,dfd);
                // if leasetime is null then it means that the exipire date is infinite
                if(dateLeaseTime != null) {
                    exactLeaseTime = dateLeaseTime.getTime();
                    if(exactLeaseTime < dfd.getLeaseTime().getTime()) {
                        reset(exactLeaseTime - System.currentTimeMillis() - OFFSET_LTModifable);
                    }
                    else {
                        stop();
                    }
                }else {
                    stop();
                }
            }catch(Exception e) {
                try {
                    Date dateLeaseTime = DFService.register(a,dfd);
                    if(dateLeaseTime != null) {
                        exactLeaseTime = dateLeaseTime.getTime();
                        if(exactLeaseTime < dfd.getLeaseTime().getTime()) {
                            reset(exactLeaseTime - System.currentTimeMillis() - OFFSET_LTModifable);
                        }
                        else
                            stop();
                    } else
                        stop();
                } catch(Exception er) {
                    er.printStackTrace();
                }
            }
        } else {
            stop();
        }
        
    }
    
    /** Set the offset time to renew the registrations before its expiration.
     * @param ms
     */    
    public void setReneweOffset(long ms) {
        OFFSET_LTModifable = ms;
    }
    
}
