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
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.proto.AchieveREInitiator;
import test.common.*;

import java.util.Vector;
import java.util.List;
import java.util.ArrayList;

/**
   @author Giovanni Caire - TILAB
 */
public class TestSerializableProperty extends Test {
	private static final String A_STRING = "abcdefgh";
	private static final Integer AN_INTEGER = new Integer(1234);
	private static final String SER_PROP_NAME = "serializable-property";
	
  public Behaviour load(Agent a) throws TestException {
 		final Codec codec = new SLCodec();
 		final Ontology ontology = FIPAManagementOntology.getInstance();
 	
  	SequentialBehaviour sb = new SequentialBehaviour(a) {
  		public void onStart() {
 				myAgent.getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);
 				myAgent.getContentManager().registerOntology(ontology);
  		}
  	};
  	
  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
  	msg.addReceiver(a.getDefaultDF());
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());
		
		// Step 1: Register a DFD including a Serializable property
 		sb.addSubBehaviour(new AchieveREInitiator(a, msg) {
 			protected Vector prepareRequests(ACLMessage request) {
 				log("Registering a DFD including a Serializable property...");
 				// Prepare registration message
 				Vector v = new Vector();
 				try {
 					java.util.List l = new ArrayList();
 					l.add(A_STRING);
 					l.add(AN_INTEGER);
 					Property p = new Property(SER_PROP_NAME, l);
 					ServiceDescription sd = new ServiceDescription();
 					sd.setType("test-type");
 					sd.setName("test-name");
 					sd.addProperties(p);
  				DFAgentDescription dfd = new DFAgentDescription();
  				dfd.setName(myAgent.getAID());
  				dfd.addServices(sd);
  				Register r = new Register();
  				r.setDescription(dfd);
  				Action act = new Action(myAgent.getDefaultDF(), r);
  				myAgent.getContentManager().fillContent(request, act);
  				v.add(request);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 					failed("Error encoding registration request. "+e.toString());
 				}
 				return v;
 			}
 			
 			protected void handleInform(ACLMessage inform) {
 				// Decode registration confirmation. If this succeeds set OK return code
 				try {
 					Done d = (Done) myAgent.getContentManager().extractContent(inform);
 					log("Registration OK");
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 					failed("Error decoding registration reply. "+e.toString());
 				}
 			}
 			
 			protected void handleFailure(ACLMessage failure) {
 				failed("Registration failed: "+failure.getContent());
 			}
 			
 			protected void handleRefuse(ACLMessage refuse) {
 				failed("Registration refused"+refuse.getContent());
 			}
 			
 			protected void handleNotUnderstood(ACLMessage notUnderstood) {
 				failed("Registration notUnderstood"+notUnderstood.getContent());
 			}
 		} );
 		
		// Step 2: Search the registered DFD and check the serializable property
 		sb.addSubBehaviour(new AchieveREInitiator(a, msg) {
 			protected Vector prepareRequests(ACLMessage request) {
 				log("Searching for the registered DFD...");
 				// Prepare search message
 				Vector v = new Vector();
  			try {
 					Property p = new Property(SER_PROP_NAME, null);
 					ServiceDescription sd = new ServiceDescription();
 					sd.addProperties(p);
  				DFAgentDescription template = new DFAgentDescription();
  				template.addServices(sd);
  				Search s = new Search();
  				s.setDescription(template);
  				s.setConstraints(new SearchConstraints());
  				Action act = new Action(myAgent.getDefaultDF(), s);
  				myAgent.getContentManager().fillContent(request, act);
  				v.add(request);
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 					failed("Error encoding search request. "+e.toString());
 				}
 				return v;
 			}
 			
 			protected void handleInform(ACLMessage inform) {
 				// Decode search result
 				try {
 					Result r = (Result) myAgent.getContentManager().extractContent(inform);
 					if (r.getItems().size() == 1) {
 						try {
	 						DFAgentDescription dfd = (DFAgentDescription) r.getItems().get(0);
	 						ServiceDescription sd = (ServiceDescription) dfd.getAllServices().next();
	 						Property p = (Property) sd.getAllProperties().next();
	 						Object obj = p.getValue();
	 						if (check(obj)) {
	 							passed("Serializable property OK");
	 						}
 						}
 						catch (Exception e) {
 							failed("Uncorrect DFD. "+e.toString());
 						}
 					}
 					else {
  					failed(r.getItems().size()+" items found while 1 was expected.");
 					}	
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 					failed("Error decoding search reply. "+e.toString());
 				}
 			}
 			
 			protected void handleFailure(ACLMessage failure) {
 				failed("Search failed: "+failure.getContent());
 			}
 			
 			protected void handleRefuse(ACLMessage refuse) {
 				failed("Search refused"+refuse.getContent());
 			}
 			
 			protected void handleNotUnderstood(ACLMessage notUnderstood) {
 				failed("Search notUnderstood"+notUnderstood.getContent());
 			}
 		} );
 		
  	return sb;
  }
  
  public void clean(Agent a) {
  	try {
  		DFService.deregister(a);
  	}
  	catch (Exception e) {
  		e.printStackTrace();
  	}
  }
  
  private boolean check(Object obj) {
  	if (obj == null) {
			failed("Null serializable property");
			return false;
  	}
  	else {
	  	if (obj instanceof List) {
	  		List l = (List) obj;
	  		if (l.size() == 2) {
	  			if (!A_STRING.equals(l.get(0))) {
	  				failed("Wrong serializable property: "+l.get(0)+" found while "+A_STRING+" was expected");
						return false;
	  			}
	  			if (!AN_INTEGER.equals(l.get(1))) {
	  				failed("Wrong serializable property: "+l.get(1)+" found while "+AN_INTEGER+" was expected");
						return false;
	  			}
	  		}
	  		else {
	  			failed("Wrong serializable property: "+l.size()+" elements found while "+2+" were expected");
					return false;
	  		}
	  	}
	  	else {
  			failed("Wrong serializable property type: "+obj.getClass().getName()+" found while java.util.List was expected");
				return false;
	  	}
  	}
  	return true;
  }
}
