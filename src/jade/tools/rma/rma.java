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

package jade.tools.rma;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.InputStreamReader;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.net.URL;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAException;
import jade.domain.FIPAServiceCommunicator;
import jade.domain.AMSServiceCommunicator;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;
import jade.domain.MobilityOntology;
import jade.gui.AgentTreeModel;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.sl.SL0Codec;

import jade.onto.basic.ResultPredicate;
import jade.onto.basic.Action;

import jade.proto.FipaRequestInitiatorBehaviour;

import jade.tools.ToolAgent;


/**
  <em>Remote Management Agent</em> agent. This class implements
  <b>JADE</b> <em>RMA</em> agent. <b>JADE</b> applications cannot use
  this class directly, but interact with it through <em>ACL</em>
  message passing. Besides, this agent has a <em>GUI</em> through
  which <b>JADE</b> Agent Platform can be administered.
  
  
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class rma extends ToolAgent {

  private APDescription myPlatformProfile;
  
  // Sends requests to the AMS
  private class AMSClientBehaviour extends FipaRequestInitiatorBehaviour {

    private String actionName;

    public AMSClientBehaviour(String an, ACLMessage request) {
      super(rma.this, request,
	    MessageTemplate.and(
	      MessageTemplate.or(MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME),
				 MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME)),
	      MessageTemplate.MatchLanguage(SL0Codec.NAME))
	    );
      actionName = an;
    }

    protected void handleNotUnderstood(ACLMessage reply) {
      myGUI.showErrorDialog("NOT-UNDERSTOOD received by RMA during " + actionName, reply);
    }

    protected void handleRefuse(ACLMessage reply) {
      myGUI.showErrorDialog("REFUSE received during " + actionName, reply);
    }

    protected void handleAgree(ACLMessage reply) {
      // System.out.println("AGREE received");
    }

    protected void handleFailure(ACLMessage reply) {
      myGUI.showErrorDialog("FAILURE received during " + actionName, reply);
    }

    protected void handleInform(ACLMessage reply) {
      // System.out.println("INFORM received");
    }

  } // End of AMSClientBehaviour class


  private class handleAddRemotePlatformBehaviour extends AMSClientBehaviour{
       		
    	public handleAddRemotePlatformBehaviour(String an, ACLMessage request){
    		super(an,request);
    	
    	}
    	
    	protected void handleInform(ACLMessage msg){
    		//System.out.println("arrived a new APDescription");
    		try{
    			AID sender = msg.getSender();
    			ResultPredicate r =(ResultPredicate) extractContent(msg).get(0); 

    			Iterator i = r.getAll_1();
    			APDescription APDesc = (APDescription)i.next();
    			if(APDesc != null){
    			myGUI.addRemotePlatformFolder();
    			myGUI.addRemotePlatform(sender,APDesc);}
    		}catch(jade.domain.FIPAException e){
    		e.printStackTrace();
    		}
    	}
    
    }//end handleAddRemotePlatformBehaviour
    
    private class handleRefreshRemoteAgentBehaviour extends AMSClientBehaviour{
     
    	private APDescription platform;
    	
    	public handleRefreshRemoteAgentBehaviour(String an, ACLMessage request,APDescription ap){
    		super(an,request);
    		platform = ap;
    		
    	}
    	
    	protected void handleInform(ACLMessage msg){
	    //System.out.println("arrived a new agents from a remote platform");
    		try{
    			AID sender = msg.getSender();
    			ResultPredicate r = (ResultPredicate)extractContent(msg).get(0);
    			Iterator i = r.getAll_1();
    			myGUI.addRemoteAgentsToRemotePlatform(platform,i);
    		}catch(FIPAException e){
    		e.printStackTrace();
    		}
    	}
    
    }//end handleAddRemotePlatformBehaviour


  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  private transient MainWindow myGUI;

  private String myContainerName;

  class RMAAMSListenerBehaviour extends AMSListenerBehaviour {
      protected void installHandlers(Map handlersTable) {

	// Fill the event handler table.
	handlersTable.put(JADEIntrospectionOntology.ADDEDCONTAINER, new EventHandler() {
	  public void handle(Event ev) {
	    AddedContainer ac = (AddedContainer)ev;
	    ContainerID cid = ac.getContainer();
	    String name = cid.getName();
	    String address = cid.getAddress();
	    try {
	      InetAddress addr = InetAddress.getByName(address);
	      myGUI.addContainer(name, addr);
	    }
	    catch(UnknownHostException uhe) {
	      myGUI.addContainer(name, null);
	    }
	  }
	});
	
	
	handlersTable.put(JADEIntrospectionOntology.REMOVEDCONTAINER, new EventHandler() {
	  public void handle(Event ev) {
	    RemovedContainer rc = (RemovedContainer)ev;
	    ContainerID cid = rc.getContainer();
	    String name = cid.getName();
	    myGUI.removeContainer(name);
	  }
	});

	handlersTable.put(JADEIntrospectionOntology.BORNAGENT, new EventHandler() {
          public void handle(Event ev) {
	    BornAgent ba = (BornAgent)ev;
	    ContainerID cid = ba.getWhere();
	    String container = cid.getName();
	    AID agent = ba.getAgent();
	    myGUI.addAgent(container, agent);
	    if(agent.equals(getAID()))
	      myContainerName = container;
	  }
	});

        handlersTable.put(JADEIntrospectionOntology.DEADAGENT, new EventHandler() {
          public void handle(Event ev) {
	    DeadAgent da = (DeadAgent)ev;
	    ContainerID cid = da.getWhere();
	    String container = cid.getName();
	    AID agent = da.getAgent();
	    myGUI.removeAgent(container, agent);
	  }
        });

        handlersTable.put(JADEIntrospectionOntology.MOVEDAGENT, new EventHandler() {
          public void handle(Event ev) {
	    MovedAgent ma = (MovedAgent)ev;
	    AID agent = ma.getAgent();
	    ContainerID from = ma.getFrom();
	    myGUI.removeAgent(from.getName(), agent);
	    ContainerID to = ma.getTo();
	    myGUI.addAgent(to.getName(), agent);
	  }
        });

	handlersTable.put(JADEIntrospectionOntology.ADDEDMTP, new EventHandler() {
	  public void handle(Event ev) {
	    AddedMTP amtp = (AddedMTP)ev;
	    String address = amtp.getAddress();
	    ContainerID where = amtp.getWhere();
	    myGUI.addAddress(address, where.getName());
	  }
        });

        handlersTable.put(JADEIntrospectionOntology.REMOVEDMTP, new EventHandler() {
	  public void handle(Event ev) {
	    RemovedMTP rmtp = (RemovedMTP)ev;
	    String address = rmtp.getAddress();
	    ContainerID where = rmtp.getWhere();
	    myGUI.removeAddress(address, where.getName());
	  }
	});
	
	//handle the APDescription provided by the AMS 
	handlersTable.put(JADEIntrospectionOntology.PLATFORMDESCRIPTION, new EventHandler(){
	  public void handle(Event ev){
	    PlatformDescription pd = (PlatformDescription)ev;
	    APDescription APdesc = pd.getPlatform();
	    myPlatformProfile = APdesc;
	    myGUI.refreshLocalPlatformName(myPlatformProfile.getName());
	  }
        });

      } 
  } // END of inner class RMAAMSListenerBehaviour

  
  /**
   This method starts the <em>RMA</em> behaviours to allow the agent
   to carry on its duties within <em><b>JADE</b></em> agent platform.
  */
  protected void toolSetup() {

    // Register the supported ontologies 
    registerOntology(MobilityOntology.NAME, MobilityOntology.instance());

    // Send 'subscribe' message to the AMS
    AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

    // Handle incoming 'inform' messages
    AMSSubscribe.addSubBehaviour(new RMAAMSListenerBehaviour());
	
    // Schedule Behaviour for execution
    addBehaviour(AMSSubscribe);

    // Show Graphical User Interface
    myGUI = new MainWindow(this);
    myGUI.ShowCorrect();

  }

  /**
   Cleanup during agent shutdown. This method cleans things up when
   <em>RMA</em> agent is destroyed, disconnecting from <em>AMS</em>
   agent and closing down the platform administration <em>GUI</em>.
  */
  protected void toolTakeDown() {
    send(getCancel());
    if (myGUI != null) {
	myGUI.setVisible(false);
	myGUI.disposeAsync();
    }
  }

  protected void beforeClone() {
  }

  protected void afterClone() {
    // Add yourself to the RMA list
    ACLMessage AMSSubscription = getSubscribe();
    AMSSubscription.setSender(getAID());
    send(AMSSubscription);
    myGUI = new MainWindow(this);
    myGUI.ShowCorrect();
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public AgentTreeModel getModel() {
    return myGUI.getModel();
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void newAgent(String agentName, String className, String arg[], String containerName) {

    CreateAgent ca = new CreateAgent();

    if(containerName.equals(""))
      containerName = AgentManager.MAIN_CONTAINER_NAME;

    ca.setAgentName(agentName);
    ca.setClassName(className);
    ca.setContainer(new ContainerID(containerName, null));
    for(int i = 0; i<arg.length ; i++)
    	ca.addArguments((Object)arg[i]);
    
    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(ca);
      List l = new ArrayList(1);
      l.add(a);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(JADEAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("CreateAgent", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void suspendAgent(AID name) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setState(AMSAgentDescription.SUSPENDED);
    Modify m = new Modify();
    m.set_0(amsd);

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(m);
      List l = new ArrayList(1);
      l.add(a);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(FIPAAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("SuspendAgent", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }
  }
  
  

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void suspendContainer(String name) {
    // FIXME: Not implemented
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void resumeAgent(AID name) {
    AMSAgentDescription amsd = new AMSAgentDescription();
    amsd.setName(name);
    amsd.setState(AMSAgentDescription.ACTIVE);
    Modify m = new Modify();
    m.set_0(amsd);

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(m);
      List l = new ArrayList(1);
      l.add(a);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(FIPAAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("ResumeAgent", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void resumeContainer(String name) {
    // FIXME: Not implemented
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void killAgent(AID name) {

    KillAgent ka = new KillAgent();

    ka.setAgent(name);

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(ka);
      List l = new ArrayList(1);
      l.add(a);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(JADEAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("KillAgent", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void killContainer(String name) {

    KillContainer kc = new KillContainer();

    kc.setContainer(new ContainerID(name, null));

    try {
      Action a = new Action();
      a.set_0(getAMS());
      a.set_1(kc);
      List l = new ArrayList(1);
      l.add(a);

      ACLMessage requestMsg = getRequest();
      requestMsg.setOntology(JADEAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("KillContainer", requestMsg));
    }
    catch(FIPAException fe) {
      fe.printStackTrace();
    }

  }
  
  /**
  Callback method for platform management
  */

  public void moveAgent(AID name, String container)
  {
     MobilityOntology.MoveAction moveAct = new MobilityOntology.MoveAction();
     MobilityOntology.MobileAgentDescription desc = new MobilityOntology.MobileAgentDescription();
     desc.setName(name);
     ContainerID dest = new ContainerID(container, null);

     desc.setDestination(dest);
     moveAct.set_0(desc);
         	
      try{
      	Action a = new Action();
     	  a.set_0(getAMS());
     	  a.set_1(moveAct);
     	  List l = new ArrayList(1);
     	  l.add(a);
	  ACLMessage requestMsg = getRequest();
     	  requestMsg.setOntology(MobilityOntology.NAME);
     	  fillContent(requestMsg,l);
     	  addBehaviour(new AMSClientBehaviour("MoveAgent",requestMsg));
     	  
      }catch(FIPAException fe){fe.printStackTrace();}
  }
  
  /**
  Callback method for platform management
  */
  public void cloneAgent(AID name,String newName, String container)
  {
  	MobilityOntology.CloneAction cloneAct = new MobilityOntology.CloneAction();
  	MobilityOntology.MobileAgentDescription desc = new MobilityOntology.MobileAgentDescription();
  	desc.setName(name);
  	ContainerID dest = new ContainerID(container, null);
  	desc.setDestination(dest);
  	cloneAct.set_0(desc);
  	cloneAct.set_1(newName);
  	
  	try{
  		Action a = new Action();
  		a.set_0(getAMS());
  		a.set_1(cloneAct);
  		List l = new ArrayList(1);
  		l.add(a);
		ACLMessage requestMsg = getRequest();
  		requestMsg.setOntology(MobilityOntology.NAME);
  		fillContent(requestMsg,l);
  		addBehaviour(new AMSClientBehaviour("CloneAgent",requestMsg));
  		
  	}catch(FIPAException fe){fe.printStackTrace();}
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void exit() {
    if(myGUI.showExitDialog("Exit this container"))
      killContainer(myContainerName);
  }

  /**
   Callback method for platform management <em>GUI</em>.
   */
  public void shutDownPlatform() {
    if(myGUI.showExitDialog("Shut down the platform"))
      killContainer(AgentManager.MAIN_CONTAINER_NAME);
  }

  public void installMTP(String containerName) {
    InstallMTP imtp = new InstallMTP();
    imtp.setContainer(new ContainerID(containerName, null));
    if(myGUI.showInstallMTPDialog(imtp)) {
      try {
	Action a = new Action();
	a.set_0(getAMS());
	a.set_1(imtp);
	List l = new ArrayList(1);
	l.add(a);

	ACLMessage requestMsg = getRequest();
	requestMsg.setOntology(JADEAgentManagementOntology.NAME);
	fillContent(requestMsg, l);
	addBehaviour(new AMSClientBehaviour("InstallMTP", requestMsg));
      }
      catch(FIPAException fe) {
	fe.printStackTrace();
      }

    }
  }

  public void uninstallMTP(String containerName) {
    UninstallMTP umtp = new UninstallMTP();
    umtp.setContainer(new ContainerID(containerName, null));
    if(myGUI.showUninstallMTPDialog(umtp)) {
      try {
	Action a = new Action();
	a.set_0(getAMS());
	a.set_1(umtp);
	List l = new ArrayList(1);
	l.add(a);

	ACLMessage requestMsg = getRequest();
	requestMsg.setOntology(JADEAgentManagementOntology.NAME);
	fillContent(requestMsg, l);
	addBehaviour(new AMSClientBehaviour("UninstallMTP", requestMsg));
      }
      catch(FIPAException fe) {
	fe.printStackTrace();
      }

    }
  }

  //this method sends a request to a remote AMS to know the APDescription of a remote Platform
  public void addRemotePlatform(AID remoteAMS){
  
      //System.out.println("AddRemotePlatform"+remoteAMS.toString());
  	try{

  		ACLMessage requestMsg = new ACLMessage(ACLMessage.REQUEST);
		requestMsg.setSender(getAID());
    		requestMsg.clearAllReceiver();
    		requestMsg.addReceiver(remoteAMS);
    		requestMsg.setProtocol("fipa-request");
    		requestMsg.setLanguage(SL0Codec.NAME);
		requestMsg.setOntology(FIPAAgentManagementOntology.NAME);
    	
		GetDescription action = new GetDescription();
    		List l = new ArrayList(1);
    		requestMsg.setOntology(FIPAAgentManagementOntology.NAME);
    		Action a = new Action();
    		a.set_0(remoteAMS);
    		a.set_1(action);
    		l.add(a);
    		fillContent(requestMsg,l);
    		addBehaviour(new handleAddRemotePlatformBehaviour("GetDescription",requestMsg));
  		
  	}catch(FIPAException e){
  		e.printStackTrace();
  	}
  }
  
  
  public void addRemotePlatformFromURL(String url){
  
  	try{
  		URL AP_URL = new URL(url);
    	BufferedReader in = new BufferedReader(new InputStreamReader(AP_URL.openStream()));

    	String inputLine = in.readLine();
			
			//to parse the APDescription it is put in a Dummy ACLMessage 
     	ACLMessage dummyMsg = new ACLMessage(ACLMessage.NOT_UNDERSTOOD);
     	dummyMsg.setOntology(FIPAAgentManagementOntology.NAME);
     	dummyMsg.setLanguage(SL0Codec.NAME);
     	String content = "(( result (action ( agent-identifier :name ams :addresses (sequence IOR:00000000000000) :resolvers (sequence ) ) (get-description ) ) (set " + inputLine +" ) ) )";
     	dummyMsg.setContent(content);
     	try{
     	
     	ResultPredicate r = (ResultPredicate)extractContent(dummyMsg).get(0);
     	
    	Iterator i = r.getAll_1();
    	
    	APDescription APDesc = (APDescription)i.next();
   
    	if(APDesc != null){
    			String amsName = "ams@" + APDesc.getName();

    			if(amsName.equalsIgnoreCase(getAMS().getName())){
    				System.out.println("ERROR: Action not allowed.");
    			}
    				else
    			{
    				AID ams = new AID(amsName, AID.ISGUID);
    				Iterator TP = (APDesc.getTransportProfile()).getAllAvailableMtps();
    			
    				while(TP.hasNext())
    				{
    					MTPDescription mtp = (MTPDescription)TP.next();
    					Iterator add = mtp.getAllAddresses();
    					while(add.hasNext())
    					{
    						String address = (String)add.next();
    						ams.addAddresses(address);
    					}
    				}
    				myGUI.addRemotePlatformFolder();
    				myGUI.addRemotePlatform(ams,APDesc);
    			}
    	}
     	
     	}catch(jade.domain.FIPAException e){
     	
     	e.printStackTrace();}
    
    	in.close();
  	}catch(java.net.MalformedURLException e){
  		e.printStackTrace();
  	}catch(java.io.IOException ioe){
  	ioe.printStackTrace();
  	}
  }
  
  public void viewAPDescription(String title){
  	myGUI.viewAPDescriptionDialog(myPlatformProfile,title);
  }
  
  public void viewAPDescription(APDescription remoteAP, String title){
  	myGUI.viewAPDescriptionDialog(remoteAP,title);
  }
  
  public void removeRemotePlatform(APDescription platform){
  	myGUI.removeRemotePlatform(platform.getName());
  }
  
  //make a search on a specified ams in order to return 
  //all the agents registered with that ams.
  public void refreshRemoteAgent(APDescription platform,AID ams){
  	try{
  		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
  		request.setSender(getAID());
  		request.addReceiver(ams);
      request.setLanguage(SL0Codec.NAME);
      request.setOntology(FIPAAgentManagementOntology.NAME);
  		AMSAgentDescription amsd = new AMSAgentDescription();
  		SearchConstraints constraints = new SearchConstraints();
    	// Build a AMS action object for the request
    	Search s = new Search();
    	s.set_0(amsd);
    	s.set_1(constraints);

    	Action act = new Action();
    	act.set_0(ams);
    	act.set_1(s);

    	List l = new ArrayList(1);
			l.add(act);
			fillContent(request,l);
			
			addBehaviour(new handleRefreshRemoteAgentBehaviour ("search",request,platform));
			
  	}catch(jade.domain.FIPAException e){
  		e.printStackTrace();
  	}
  }
  
  // ask the local AMS to register a remote Agent.
  public void registerRemoteAgentWithAMS(AMSAgentDescription amsd){
 		
  	Register register_act = new Register();
  	register_act.set_0(amsd);
  	
  	try{
  		Action a = new Action();
  		a.set_0(getAMS());
  		a.set_1(register_act);
  		List l = new ArrayList();
  		l.add(a);
		ACLMessage requestMsg = getRequest();
  		requestMsg.setOntology(FIPAAgentManagementOntology.NAME);
      fillContent(requestMsg, l);
      addBehaviour(new AMSClientBehaviour("Register", requestMsg));

  	}catch(FIPAException e){e.printStackTrace();}
 } 	
}
