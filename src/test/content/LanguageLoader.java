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

package test.content;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.content.*;
import jade.content.lang.*;

import jade.proto.*;
import jade.domain.FIPAAgentManagement.*;

public class LanguageLoader extends AchieveREResponder {
	public static final String LANGUAGE_LOADER_CONVERSATION = "_Language_Loader_";
	
  public LanguageLoader() {
  	super(null, MessageTemplate.and(
  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
  		MessageTemplate.MatchConversationId(LANGUAGE_LOADER_CONVERSATION)
  	));
  }
  
  protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
  	ACLMessage response = request.createReply();
  	response.setPerformative(ACLMessage.AGREE);
  	return response;
  }
  	
  protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
  	try {
  		String language = request.getLanguage();
  		if (language != null) {
  			Codec c = (Codec) Class.forName(language).newInstance();
  			myAgent.getContentManager().registerLanguage(c);
  		}
  		ACLMessage notif = request.createReply();
  		notif.setPerformative(ACLMessage.INFORM);
  		return notif;
  	}
  	catch (Exception e) {
  		throw new FailureException(e.getMessage());
  	}
  }
} 
