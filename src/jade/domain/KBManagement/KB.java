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

import jade.content.ContentManager;
import jade.content.abs.AbsIRE;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.proto.SubscriptionResponder;
import jade.util.leap.List;

import java.util.Enumeration;

//#MIDP_EXCLUDE_FILE


/**
 * @author Elisabetta Cortese - TILab
 *
 */


/** Interface for AMS and DF Knowledge Base*/
public interface KB {
	Object register(Object name, Object fact);
	Object deregister(Object name);
	List search(Object template);
	 
	void setLeaseManager(LeaseManager lm);
	  
	void setSubscriptionResponder(SubscriptionResponder sr);
	  
	void subscribe(Object aclMessage, SubscriptionResponder.Subscription s) throws NotUnderstoodException;
	Enumeration getSubscriptions();
	void unsubscribe(SubscriptionResponder.Subscription sub);
}