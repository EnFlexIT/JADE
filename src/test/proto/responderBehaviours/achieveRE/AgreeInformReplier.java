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

package test.proto.responderBehaviours.achieveRE;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.proto.*;
import jade.lang.acl.*;
import jade.domain.FIPAAgentManagement.*;

/**
   @author Giovanni Caire - TILAB
 */
public class AgreeInformReplier extends AchieveREResponder {
	public AgreeInformReplier() {
		super(null, createMessageTemplate(FIPAProtocolNames.FIPA_REQUEST));
	}
	
  protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
		System.out.println(myAgent.getName()+": prepare response");
  	ACLMessage response = request.createReply();
		response.setPerformative(ACLMessage.AGREE);
		return response;
  }

    	
	protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
		System.out.println(myAgent.getName()+": prepare result notification");
		ACLMessage resNotif = request.createReply();
		resNotif.setPerformative(ACLMessage.INFORM);
		return resNotif;
	}
}