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

package test.domain.ams.tests;

import jade.core.Agent;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.lang.acl.*;
import jade.domain.JADEAgentManagement.*;
import jade.domain.FIPAAgentManagement.*;
import jade.util.leap.*;
import test.common.*;
import test.domain.ams.*;

/**
   Performs the following steps:
   @author Giovanni Caire - TILAB
 */
public class TestInstallMTP extends Test {
	
	private JadeController jc;
  private int ret;
  private String address;
  private Logger l = Logger.getLogger();
  private ContentManager cm = new ContentManager();
  private Codec codec = new SLCodec();
  private Ontology jadeOnto = JADEManagementOntology.getInstance();
  private Ontology fipaOnto = FIPAManagementOntology.getInstance();
  
  public Behaviour load(Agent a, DataStore ds, String resultKey) throws TestException {
  	final DataStore store = ds;
  	final String key = resultKey;
  	
  	cm.registerLanguage(codec);
  	cm.registerOntology(jadeOnto);
  	cm.registerOntology(fipaOnto);
  	
		jc = TestUtility.launchJadeInstance("Container-1", null, new String("-container -host "+TestUtility.getLocalHostName()+" -port "+String.valueOf(Test.DEFAULT_PORT)), null); 
		ret = Test.TEST_FAILED;
  	
		SequentialBehaviour sb = new SequentialBehaviour(a) {
  		public int onEnd() {
  			store.put(key, new Integer(ret));
  			return 0;
  		}	
  	};
  		
   	// 1) Requests the AMS to install an MTP on a given container
  	Behaviour b = new OneShotBehaviour(a) {			
  		public void action() {
  			InstallMTP im = new InstallMTP();
  			im.setClassName("jade.mtp.iiop.MessageTransportProtocol");
  			im.setContainer(new ContainerID(jc.getContainerName(), null));
  			Action slAct = new Action(myAgent.getAMS(), im);
  			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  			request.addReceiver(myAgent.getAMS());
  			request.setLanguage(codec.getName());
  			request.setOntology(jadeOnto.getName());
  			try {
  				cm.fillContent(request, slAct);
  				ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request);
  				Result r = (Result) cm.extractContent(inform);
  				List items = r.getItems();
  				address = (String) items.get(0);
  				l.log("MTP correctly installed. Address is: "+address);
  			}
  			catch (OntologyException oe) {
  				l.log("Error encoding/decoding MTP installation request/reply.");
  				oe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Codec.CodecException ce) {
  				l.log("Error encoding/decoding MTP installation request/reply.");
  				ce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (FIPAException fe) {
  				l.log("Installation error.");
  				fe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (ClassCastException cce) {
  				l.log("InstallMTP: Unexpected result.");
  				cce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Exception e) {
  				l.log("Unexpected error.");
  				e.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);
  			
   	// 2) Check if the new address has been added to the agent AID
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			AID id = myAgent.getAID();
  			Iterator it = id.getAllAddresses();
  			while (it.hasNext()) {
  				if (address.equals(it.next())) {
  					l.log("New address correctly added to the local AID.");
  					return;
  				}
  			}
  			l.log("The new address has NOT been added to the local AID.");
	  		((SequentialBehaviour) parent).skipNext();
  		}
  	};
  	sb.addSubBehaviour(b);

  	// 3) Searches the AMS for himself and checks that the new 
  	// address has been added to the AMSDescription		
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			AMSAgentDescription amsd = new AMSAgentDescription();
  			amsd.setName(new AID(myAgent.getLocalName(), AID.ISLOCALNAME));
  			try {
	  			AMSAgentDescription[] results = AMSService.search(myAgent, myAgent.getAMS(), amsd, new SearchConstraints());
	  			AID id = results[0].getName();
 					if (!myAgent.getAID().equals(id)) {
 						l.log("AMS-Description not found!");
	  				((SequentialBehaviour) parent).skipNext();
 					}
	  			Iterator it = id.getAllAddresses();
	  			while (it.hasNext()) {
	  				if (address.equals(it.next())) {
	  					l.log("New address correctly added to the AMS-Description.");
	  					return;
	  				}
	  			}
	  			l.log("The new address has NOT been added to the AMS-Description.");
		  		((SequentialBehaviour) parent).skipNext();
  			}
  			catch (FIPAException fe) {
  				l.log("AMS-Search error.");
  				fe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Exception e) {
  				l.log("Unexpected error.");
  				e.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);

  	// 4) Gets the platform description and check that the new 
  	// address has been added		
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			GetDescription gd = new GetDescription();
  			Action slAct = new Action(myAgent.getAMS(), gd);
  			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  			request.addReceiver(myAgent.getAMS());
  			request.setLanguage(codec.getName());
  			request.setOntology(fipaOnto.getName());
  			try {
  				cm.fillContent(request, slAct);
  				ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request);
  				Result r = (Result) cm.extractContent(inform);
  				List items = r.getItems();
  				APDescription dsc = (APDescription) items.get(0);
                                for (Iterator is = dsc.getAllAPServices(); is.hasNext(); ) {
                                    APService s = (APService)is.next();
                                    Iterator addresses = s.getAllAddresses();
                                    if (address.equals(addresses.next())) {
			  			l.log("New address correctly added to the AP-Description.");
  						return;
  					}     
                                }  				
	  			l.log("The new address has NOT been added to the AP-Description.");
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (OntologyException oe) {
  				l.log("Error encoding/decoding GetDescription request/reply.");
  				oe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Codec.CodecException ce) {
  				l.log("Error encoding/decoding GetDescription request/reply.");
  				ce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (FIPAException fe) {
  				l.log("AMS-GET-Description error.");
  				fe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (ClassCastException cce) {
  				l.log("GetDescription: Unexpected result.");
  				cce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Exception e) {
  				l.log("Unexpected error.");
  				e.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);

   	// 5) Requests the AMS to un-install the MTP
  	b = new OneShotBehaviour(a) {			
  		public void action() {
  			UninstallMTP um = new UninstallMTP();
  			um.setAddress(address);
  			um.setContainer(new ContainerID(jc.getContainerName(), null));
  			Action slAct = new Action(myAgent.getAMS(), um);
  			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  			request.addReceiver(myAgent.getAMS());
  			request.setLanguage(codec.getName());
  			request.setOntology(jadeOnto.getName());
  			try {
  				cm.fillContent(request, slAct);
  				ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request);
  				Done d = (Done) cm.extractContent(inform);
  				l.log("MTP correctly un-installed.");
  			}
  			catch (OntologyException oe) {
  				l.log("Error encoding/decoding MTP un-installation request/reply.");
  				oe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Codec.CodecException ce) {
  				l.log("Error encoding/decoding MTP un-installation request/reply.");
  				ce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (FIPAException fe) {
  				l.log("Un-installation error.");
  				fe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (ClassCastException cce) {
  				l.log("UninstallMTP: Unexpected notification.");
  				cce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Exception e) {
  				l.log("Unexpected error.");
  				e.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);

   	// 6) Check if the address of the removed MTP has been removed from the agent AID
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			AID id = myAgent.getAID();
  			Iterator it = id.getAllAddresses();
  			while (it.hasNext()) {
  				if (address.equals(it.next())) {
		  			l.log("The address of the removed MTP has NOT been removed from the local AID.");
			  		((SequentialBehaviour) parent).skipNext();
						return;
  				}
  			}
				l.log("Removed MTP address correctly removed from the local AID.");
  		}
  	};
  	sb.addSubBehaviour(b);

  	// 7) Searches the AMS for himself and checks that the address of 
  	// the removed MTP has been removed from the AMS-Description
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			AMSAgentDescription amsd = new AMSAgentDescription();
  			amsd.setName(new AID(myAgent.getLocalName(), AID.ISLOCALNAME));
  			try {
	  			AMSAgentDescription[] results = AMSService.search(myAgent, myAgent.getAMS(), amsd, new SearchConstraints());
	  			AID id = results[0].getName();
 					if (!myAgent.getAID().equals(id)) {
 						l.log("AMS-Description not found!");
	  				((SequentialBehaviour) parent).skipNext();
 					}
	  			Iterator it = id.getAllAddresses();
	  			while (it.hasNext()) {
	  				if (address.equals(it.next())) {
			  			l.log("The address of the removed MTP has NOT been removed from the AMS-Description.");
				  		((SequentialBehaviour) parent).skipNext();
							return;
	  				}
	  			}
					l.log("Removed MTP address correctly removed from the AMS-Description.");
  			}
  			catch (FIPAException fe) {
  				l.log("AMS-Search error.");
  				fe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Exception e) {
  				l.log("Unexpected error.");
  				e.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);

  	// 8) Gets the platform description and check that the  
  	// address has been removed		
  	b = new OneShotBehaviour(a) {	
  		public void action() {
  			GetDescription gd = new GetDescription();
  			Action slAct = new Action(myAgent.getAMS(), gd);
  			ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  			request.addReceiver(myAgent.getAMS());
  			request.setLanguage(codec.getName());
  			request.setOntology(fipaOnto.getName());
  			try {
  				cm.fillContent(request, slAct);
  				ACLMessage inform = FIPAService.doFipaRequestClient(myAgent, request);
  				Result r = (Result) cm.extractContent(inform);
  				List items = r.getItems();
  				APDescription dsc = (APDescription) items.get(0);
                                for (Iterator is = dsc.getAllAPServices(); is.hasNext(); ) {
                                    APService s = (APService)is.next();
                                    Iterator addresses = s.getAllAddresses();
                                    if (address.equals(addresses.next())) {
			  			l.log("The removed MTP address has NOT been removed from the AP-Description.");
			  			((SequentialBehaviour) parent).skipNext();
  						return;
  					}     
                                }
				l.log("Removed MTP  address correctly removed from the AP-Description.");
  			}
  			catch (OntologyException oe) {
  				l.log("Error encoding/decoding GetDescription request/reply.");
  				oe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Codec.CodecException ce) {
  				l.log("Error encoding/decoding GetDescription request/reply.");
  				ce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (FIPAException fe) {
  				l.log("AMS-GET-Description error.");
  				fe.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (ClassCastException cce) {
  				l.log("GetDescription: Unexpected result.");
  				cce.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  			catch (Exception e) {
  				l.log("Unexpected error.");
  				e.printStackTrace();
	  			((SequentialBehaviour) parent).skipNext();
  			}
  		}
  	};
  	sb.addSubBehaviour(b);

  	// If all steps were OK the test is passed
  	b = new OneShotBehaviour(a) {			
  		public void action() {
  			ret = Test.TEST_PASSED;
  		}
  	};
  	sb.addSubBehaviour(b);
  		  	
  	return sb;
  }
  
  public void clean(Agent a) {
  	jc.kill();
  }
}
