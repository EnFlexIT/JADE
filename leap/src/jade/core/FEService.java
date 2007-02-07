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

package jade.core;

/**
 * JADE kernel services providing a service-helper and whishing this helper to be available in the 
 * split execution mode too, need to provide a FEService class.
 * When starting a split container the <code>services</code> option works exactly as when starting a  
 * normal container, but the indicated classes must be concrete implementations of the <code>FEService</code>
 * abstract class.
 * 
 * @author Giovanni Caire - Telecom Italia
 */
public abstract class FEService {
	private BackEnd myBackEnd;
	
	public abstract String getName();
	public abstract ServiceHelper getHelper(Agent a);
	
	protected Object invoke(String actor, String methodName, Object[] methodParams) throws NotFoundException, ServiceException, IMTPException {
		return myBackEnd.serviceInvokation(actor, getName(), methodName, methodParams);
	}
	
	void init(BackEnd be) {
		myBackEnd = null;
	}
}
