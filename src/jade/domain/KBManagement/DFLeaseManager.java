/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

The updating of this file to JADE 2.0 has been partially supported by the IST-1999-10211 LEAP Project

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

package jade.domain.KBManagement;

//#MIDP_EXCLUDE_FILE

import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.KBManagement.*;

import java.util.Date;

/**
   This class manages lease times for DF agent description 
   registrations. By default the policy is to accept whatever 
   requested lease time. You can change this policy by extending 
   this class redefining the <code>grantLeaseTime()</code> method
   and telling the DF to use the extended class. The latter can 
   be done by specifying the command line option
   -jade_domain_df_lease-manager <lease manager class>
   @author Elisabetta Cortese - TILab
 */
public class DFLeaseManager implements LeaseManager{
	
	public Date getLeaseTime(Object item){
		return ((DFAgentDescription) item).getLeaseTime();
	}
	
	public void setLeaseTime(Object item, Date lease){
		((DFAgentDescription) item).setLeaseTime(lease);
	}

	public Object grantLeaseTime(Object item){
		// Always grant the requested lease time
		return item;
	}
	
	public boolean isExpired(Date lease){
		return (lease != null && (lease.getTime() <= System.currentTimeMillis()));
	}
}
