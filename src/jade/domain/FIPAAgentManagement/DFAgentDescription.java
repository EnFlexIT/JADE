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

package jade.domain.FIPAAgentManagement;

import jade.util.leap.*;
import jade.core.AID;
import jade.content.Concept;

  /**
  *  Models a DF agent descriptor.  This class provides platform-level
  *  support to <em>DF</em> agent, holding all informations needed by
  *  <code>DF-agent-description</code> objects in
  *  <code>fipa-agent-management</code> ontology.
  *  @see jade.domain.FIPAAgentManagement.FIPAManagementOntology
  *  @author Fabio Bellifemine - CSELT S.p.A.
  *  @version $Date$ $Revision$
  *
  */

import java.util.Date;

/** This type of object represents the description that can be registered with the
 * DF service.
 */
  public class DFAgentDescription implements Concept{

    private AID name;
    private List services = new ArrayList();
    private List interactionProtocols = new ArrayList();
    private List ontology = new ArrayList();
    private List language = new ArrayList();
	

	// Added lease default value -1
	//private Date leaseTime = new Date(-1);
	private Date leaseTime;

        /** Set the identifier of the agent
         * @param n the identifier of the agent
         */        
    public void setName(AID n) {
      name = n;
    }

    /**
     * @return the identifier of the agent
     */    
    public AID getName() {
      return name;
    }
    
    /**
       Set the lease time for the registration of this DFAgentDescription
       as an absolute time.
       @param absoluteTime The lease time for the registration of this 
       DFAgentDescription as an absolute time. Use <code>null</code> 
       (default) to indicate an infinite lease time
     */
    public void setLeaseTime(Date absoluteTime) {
      leaseTime = absoluteTime;
    }

    /**
       @return The lease time for the registration of this DFAgentDescription
       as an absolute time. A <code>null</code> value indicates an infinite 
       time.
     */
    public Date getLeaseTime() {
      return leaseTime;
    }
    
    /**
       Set the lease time for the registration of this DFAgentDescription
       as a relative time.
       @param relativeTime The lease time for the registration of this 
       DFAgentDescription as a relative time.
     */
    public void setRelativeLeaseTime(long relativeTime){
    	leaseTime = new Date(System.currentTimeMillis()+relativeTime);
    }
    
    /**
       Indicates whether the lease time for the registration of this 
       DFAgentDescription has expired.
     */
    public boolean checkLeaseTimeExpired(){
    	if (leaseTime == null) {
    		return false;
    	}
    	else {
    		return (System.currentTimeMillis() > leaseTime.getTime());
    	}
    }

    public void addServices(ServiceDescription a) {
      services.add(a);
    }

    public boolean removeServices(ServiceDescription a) {
      return services.remove(a);
    }

	public void clearAllServices(){
	  services.clear();
	}
	
	public Iterator getAllServices(){
	  return services.iterator();
	}

    public void addProtocols(String ip) {
	interactionProtocols.add(ip);
    }
    public boolean removeProtocols(String ip) {
      return interactionProtocols.remove(ip);
    }
    public void clearAllProtocols(){
      interactionProtocols.clear();
    }
    public Iterator getAllProtocols() {
      return interactionProtocols.iterator();
    }



    public void addOntologies(String ip) {
	ontology.add(ip);
    }
    public boolean removeOntologies(String ip) {
      return ontology.remove(ip);
    }
    public void clearAllOntologies(){
      ontology.clear();
    }
    public Iterator getAllOntologies() {
      return ontology.iterator();
    }

    public void addLanguages(String ip) {
	language.add(ip);
    }
    public boolean removeLanguages(String ip) {
      return language.remove(ip);
    }
    public void clearAllLanguages(){
      language.clear();
    }
    public Iterator getAllLanguages() {
      return language.iterator();
    }

  } 
