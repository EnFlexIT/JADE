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

package jade.domain;

//#MIDP_EXCLUDE_FILE

import jade.core.CaseInsensitiveString;
import jade.core.Agent;
import jade.core.AID;
import jade.core.Location;
import jade.content.Concept;
import jade.content.Predicate;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.content.onto.basic.Done;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.domain.JADEAgentManagement.*;
import jade.domain.mobility.*;
import jade.mtp.MTPDescriptor;
import jade.security.AuthException;

/**
   Extends RequestManagementBehaviour and implements performAction() to 
   i) call the method of the AMS corresponding to the requested 
   action and ii) prepare the result notification depending on 
   - whether a result should be returned (RESULT or DONE)
   - whether the notification can be sent immediately or must be delayed
   at a later time.
   @author Tiziana Trucco - Tilab
   @author Giovanni Caire - Tilab
   @version $Date$ $Revision$
*/

class AMSJadeAgentManagementBehaviour extends RequestManagementBehaviour{

	private ams theAMS;
	
  protected AMSJadeAgentManagementBehaviour(ams a, MessageTemplate mt) {
		super(a,mt);
		theAMS = a;
  }
	
  /**
     Call the proper method of the ams and prepare the notification 
     message
   */
  protected ACLMessage performAction(Action slAction, ACLMessage request) throws FIPAException {
  	Concept action = slAction.getAction();
  	List resultItems = null;
  	Object asynchNotificationKey = null;
  	
  	// CREATE AGENT
  	if (action instanceof CreateAgent) {
  		theAMS.createAgentAction((CreateAgent) action, request.getSender());
  	}
  	// KILL AGENT (asynchronous notification to requester)
  	else if (action instanceof KillAgent) {
  		theAMS.killAgentAction((KillAgent) action, request.getSender());
  		asynchNotificationKey = ((KillAgent) action).getAgent();
  	}
  	// CLONE AGENT (asynchronous notification to requester)
  	// Note that CloneAction extends MoveAction --> must be considered first!!!
  	else if (action instanceof CloneAction) {
  		theAMS.cloneAgentAction((CloneAction) action, request.getSender());
  		asynchNotificationKey = new AID(((CloneAction) action).getNewName(), AID.ISLOCALNAME); 
  	}
  	// MOVE AGENT (asynchronous notification to requester)
  	else if (action instanceof MoveAction) {
  		theAMS.moveAgentAction((MoveAction) action, request.getSender());
  		asynchNotificationKey = ((MoveAction) action).getMobileAgentDescription().getName();
  	}
  	// KILL CONTAINER (asynchronous notification to requester)
  	else if (action instanceof KillContainer) {
  		theAMS.killContainerAction((KillContainer) action, request.getSender());
  		asynchNotificationKey = ((KillContainer) action).getContainer();
  	}
  	// INSTALL MTP
  	else if (action instanceof InstallMTP) {
  		MTPDescriptor dsc = theAMS.installMTPAction((InstallMTP) action, request.getSender());
  		resultItems = new ArrayList();
  		resultItems.add(dsc.getAddresses()[0]);
  	}
  	// UNINSTALL MTP
  	else if (action instanceof UninstallMTP) {
  		theAMS.uninstallMTPAction((UninstallMTP) action, request.getSender());
  	}
  	// SNIFF ON
  	else if (action instanceof SniffOn) {
  		theAMS.sniffOnAction((SniffOn) action, request.getSender());
  	}
  	// SNIFF OFF
  	else if (action instanceof SniffOff) {
  		theAMS.sniffOffAction((SniffOff) action, request.getSender());
  	}
  	// DEBUG ON
  	else if (action instanceof DebugOn) {
  		theAMS.debugOnAction((DebugOn) action, request.getSender());
  	}
  	// DEBUG OFF
  	else if (action instanceof DebugOff) {
  		theAMS.debugOffAction((DebugOff) action, request.getSender());
  	}
  	// WHERE IS AGENT
  	else if (action instanceof WhereIsAgentAction) {
  		Location l = theAMS.whereIsAgentAction((WhereIsAgentAction) action, request.getSender());
  		resultItems = new ArrayList();
  		resultItems.add(l);
  	}
  	// QUERY PLATFORM LOCATIONS
  	else if (action instanceof QueryPlatformLocationsAction) {
  		resultItems = theAMS.queryPlatformLocationsAction((QueryPlatformLocationsAction) action, request.getSender());
  	}
  	// QUERY AGENTS ON LOCATION
  	else if (action instanceof QueryAgentsOnLocation) {
  		resultItems = theAMS.queryAgentsOnLocationAction((QueryAgentsOnLocation) action, request.getSender());
  	}
  	
  	// Prepare the notification
  	ACLMessage notification = request.createReply();
  	notification.setPerformative(ACLMessage.INFORM);
  	Predicate p = null;
  	if (resultItems != null) {
  		// The action produced a result
  		p = new Result(slAction, resultItems);
  	}
  	else {
  		p = new Done(slAction);
  	}
  	try {
	  	theAMS.getContentManager().fillContent(notification, p);
  	}
  	catch (Exception e) {
  		// Should never happen
  		e.printStackTrace();
  	}
  	
  	if (asynchNotificationKey != null) {
  		// The event forced by the action has not happened yet. Store the
  		// notification so that the AMS will send it when the event will
  		// be happened.
  		theAMS.storeNotification(action, asynchNotificationKey, notification);
  		return null;
  	}
  	else {
	  	return notification;
  	}
  }  
}
