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

import java.lang.reflect.Method;

import java.io.StringReader;
import java.io.StringWriter;

import java.util.Vector;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.net.InetAddress;

import java.io.*;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAAgentManagement.Register;
import jade.domain.FIPAAgentManagement.Deregister;
import jade.domain.FIPAAgentManagement.Modify;
import jade.domain.FIPAAgentManagement.Search;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;
import jade.domain.FIPAAgentManagement.MissingParameter;
import jade.domain.FIPAAgentManagement.NotRegistered;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.sl.SL0Codec;

import jade.onto.basic.Action;
import jade.onto.basic.ResultPredicate;

import jade.proto.FipaRequestResponderBehaviour;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.gui.DFGUI;
import jade.gui.GUI2DFCommunicatorInterface;

import jade.proto.FipaRequestInitiatorBehaviour;


/**
  Standard <em>Directory Facilitator</em> agent. This class implements
  <em><b>FIPA</b></em> <em>DF</em> agent. <b>JADE</b> applications
  cannot use this class directly, but interact with it through
  <em>ACL</em> message passing. More <em>DF</em> agents can be created
  by application programmers to divide a platform into many
  <em><b>Agent Domains</b></em>.

  Each DF has a GUI but, by default, it is not visible. The GUI of the
  agent platform includes a menu item that allows to show the GUI of the
  default DF. 

  In order to show the GUI, you should simply send the following message
  to each DF agent: <code>(request :content (action DFName (SHOWGUI))
  :ontology jade-extensions :protocol fipa-request)</code>
 
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

*/
public class df extends GuiAgent implements GUI2DFCommunicatorInterface {

  private abstract class DFBehaviour
    extends FipaRequestResponderBehaviour.ActionHandler 
    implements FipaRequestResponderBehaviour.Factory {

    protected DFBehaviour(ACLMessage req) {
      super(df.this,req);
    }
  

    // Each concrete subclass will implement this deferred method to
    // do action-specific work
    protected abstract void processAction(Action a) throws FIPAException;

    public void action() {

      try {
      	
      	ACLMessage msg = getRequest();

	// Extract the Action object from the message content
	List l = extractContent(msg);
	Action a = (Action)l.get(0);

      	// Do real action, deferred to subclasses
      	processAction(a);
      
      }
      catch(FIPAException fe) {	
      	sendReply(ACLMessage.REFUSE,fe.getMessage());
      }

    }

      public boolean done() {
      	return true;
      }

      public void reset() {}

  } // End of DFBehaviour class


  // These four concrete classes serve both as a Factory and as
  // Action: when seen as Factory they can spawn a new
  // Behaviour to process a given request, and when seen as
  // Action they process their request and terminate.

  private class RegBehaviour extends DFBehaviour {

    public RegBehaviour(ACLMessage msg) {
      super(msg);
    }

    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new RegBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Register r = (Register)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)r.get_0();
      DFRegister(dfd);
      sendReply(ACLMessage.AGREE,"FIXME");
      sendReply(ACLMessage.INFORM,"FIXME");
    }

  } // End of RegBehaviour class

  private class DeregBehaviour extends DFBehaviour {
    public DeregBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new DeregBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Deregister d = (Deregister)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)d.get_0();
      DFDeregister(dfd);
      sendReply(ACLMessage.AGREE,"FIXME");
      sendReply(ACLMessage.INFORM,"FIXME");
    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends DFBehaviour {
    public ModBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new ModBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Modify m = (Modify)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)m.get_0();
      DFModify(dfd);
      sendReply(ACLMessage.AGREE,"FIXME");
      sendReply(ACLMessage.INFORM,"FIXME");
    }

  } // End of ModBehaviour class

   private class SrchBehaviour extends DFBehaviour {
    public SrchBehaviour(ACLMessage msg) {
      super(msg);
    }
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new SrchBehaviour(msg);
    }

    protected void processAction(Action a) throws FIPAException {
      Search s = (Search)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)s.get_0();
      SearchConstraints constraints = s.get_1();
      List l = DFSearch(dfd, constraints, getReply());
      sendReply(ACLMessage.AGREE,"FIXME");
      ACLMessage msg = getRequest().createReply();
      msg.setPerformative(ACLMessage.INFORM);
      ResultPredicate r = new ResultPredicate();
      r.set_0(a);
      for (int i=0; i<l.size(); i++)
      	r.add_1(l.get(i));
      l.clear();
      l.add(r);
      fillContent(msg,l); 
      send(msg);
    }
     

  } // End of SrchBehaviour class


  private class ShowGUIBehaviour extends FipaRequestResponderBehaviour.ActionHandler 
                                 implements FipaRequestResponderBehaviour.Factory 
  {
  	protected ShowGUIBehaviour(ACLMessage msg) 
	{
      	super(df.this,msg);
  	}

  	public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) 
  	{
      	return new ShowGUIBehaviour(msg);
  	}

  	public void action () 
  	{ 
	    sendReply(ACLMessage.AGREE,"FIXME");
	    if (((df)myAgent).showGui())
	      sendReply(ACLMessage.INFORM,"FIXME");
	    else
	      sendReply(ACLMessage.FAILURE,"Gui_is_being_shown_already");
  	}
      
      public boolean done() 
      {
	  return true;
      }

      public void reset() 
      {
      }
  } // End of ShowGUIBehaviour class

  // This behaviour will be used to respond to request from the applet to know the parent with which this df is federated.
  private class GetParentsBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory
  {
      protected GetParentsBehaviour(ACLMessage msg)
      {
	super(df.this,msg);
      }
  	
      public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg)
      {
	return new GetParentsBehaviour(msg);
      }

      public void action ()
      {
	sendReply(ACLMessage.AGREE,"FIXME");
  		
	try {
	  // FIXME: Construct the reply, using the FIPAAgentManagementOntology singleton to build the sequence of
	  // DFAgentDescription objects for the parents.
	  throw new FIPAException("STUB CODE !!!");
	}
	catch(FIPAException e) {
	  sendReply(ACLMessage.FAILURE,"Impossible to provide the needed information");
	}
      }

      public boolean done()
      {
	return true;
      }

      public void reset()
      {

      }
  	
  } // End of GetParentsBehaviour class
  
  //This Behaviour returns the description of this df used to federate with another df 
  //It is used to reply to a request from the applet 
  private class GetDescriptionOfThisDFBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory
  {
    protected GetDescriptionOfThisDFBehaviour(ACLMessage msg)
    {
      super(df.this,msg);
    }
  	
    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg)
    {
      return new GetDescriptionOfThisDFBehaviour(msg);
    }
  	
    public void action()
      {
      	sendReply(ACLMessage.AGREE,"FIXME");

	      // FIXME: Create an 'inform' ACL message containing the DFAgentDescription object for this DF.

      }

    public boolean done()
    {
      return true;
    }

    public void reset()
    {

    }

  } //  End of GetDescriptionOfThisDFBehaviour class
  
  // This behaviour allows the federation of this df with another df required by the APPLET
  private class FederateWithBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory {

    protected FederateWithBehaviour(ACLMessage msg) {
      super(df.this,msg);
    }

    public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg) {
      return new FederateWithBehaviour(msg);
    }

    public void action() {
      // FIXME: Federate with the given DF
    }

    public boolean done() {
      return true;
    }

    public void reset(){}


  } // End of FederateWithBehaviour
  

  
  //This behaviour allow the applet to required the df to deregister itself from a parent of the federation
  private class DeregisterFromBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory
  {

  	protected DeregisterFromBehaviour(ACLMessage msg)
  	{
  		super(df.this,msg);
  	}
  	
  	public FipaRequestResponderBehaviour.ActionHandler create(ACLMessage msg)
  	{
  		return new DeregisterFromBehaviour(msg);
  	}
  	
  	public void action()
  	{
	  // FIXME:Deregister from the given DF;
  	}
  	
  	public boolean done()
  	{
  		return true;
  	}
  	public void reset(){}
  	
  }//End DeregisterFromBehaviour
  
  private class GUIRequestDFServiceBehaviour extends RequestFIPAServiceBehaviour
  {
    String actionName;
    DFGUI gui;
    AID receiverDF;
    
  	GUIRequestDFServiceBehaviour(AID receiverDF, String actionName, DFAgentDescription dfd, SearchConstraints constraints, DFGUI gui) throws FIPAException
  	{
  		
  		super(df.this,receiverDF,actionName,dfd,constraints);
  		
  		this.actionName = actionName;
  		this.gui = gui;
  		this.receiverDF = receiverDF;
  	}
  	
  	protected void handleInform(ACLMessage msg)
  	{
  		super.handleInform(msg);
  		if(actionName.equalsIgnoreCase(FIPAAgentManagementOntology.SEARCH))
  		{
  			try{
  				gui.showStatusMsg("Search request Processed. Ready for new request");
  				gui.refreshLastSearchResults(getSearchResult());
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}
  		else
  		if(actionName.equalsIgnoreCase(FIPAAgentManagementOntology.REGISTER))
  		{
  			try{
  				gui.showStatusMsg("Federation request Processed. Ready for new request");
  				parents.add(receiverDF);
  				gui.addParent(receiverDF);
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}
  		else
  		if(actionName.equalsIgnoreCase(FIPAAgentManagementOntology.DEREGISTER))
  		{
  			try{
  				gui.showStatusMsg("Deregister request Processed. Ready for new request");
  				//this behaviour is never used to deregister an agent of this DF
  				// but only to deregister a parent or an agent that was registered with
  				// one of my parents or my children
  				parents.remove(receiverDF); 
  				gui.removeParent(receiverDF);
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}

  	}
  }
  
  private static int NUMBER_OF_AGENT_FOUND = 1000;

  /**
  @serial
  */
  private FipaRequestResponderBehaviour dispatcher;

  /**
  @serial
  */
  private FipaRequestResponderBehaviour jadeExtensionDispatcher;

  /**
  @serial
  */
  private List children = new ArrayList();
  /**
  @serial
  */

  private List parents = new ArrayList();
  /**

  @serial
  */
  private DFGUI gui;

  // Current description of the df
  /**
  @serial
  */
  private DFAgentDescription thisDF = null;
  
 
  
  /**
    This constructor creates a new <em>DF</em> agent. This can be used
    to create additional <em>DF</em> agents, beyond the default one
    created by <em><b>JADE</b></em> on platform startup.
  */
  public df() {

    MessageTemplate mt = 
      MessageTemplate.and(MessageTemplate.MatchLanguage(SL0Codec.NAME),
			  MessageTemplate.MatchOntology(FIPAAgentManagementOntology.NAME));

    dispatcher = new FipaRequestResponderBehaviour(this, mt);

    // Associate each DF action name with the behaviour to execute
    // when the action is requested in a 'request' ACL message

    dispatcher.registerFactory(FIPAAgentManagementOntology.REGISTER, new RegBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.DEREGISTER, new DeregBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.MODIFY, new ModBehaviour(null));
    dispatcher.registerFactory(FIPAAgentManagementOntology.SEARCH, new SrchBehaviour(null));

    // Behaviour to deal with the GUI
    
    MessageTemplate mt1 = MessageTemplate.MatchOntology("jade-extensions");
    jadeExtensionDispatcher = new FipaRequestResponderBehaviour(this, mt1);
    jadeExtensionDispatcher.registerFactory("SHOWGUI", new ShowGUIBehaviour(null));
    // The following three actions are used only by the DFApplet	
    jadeExtensionDispatcher.registerFactory("GETPARENTS", new GetParentsBehaviour(null)); 
    jadeExtensionDispatcher.registerFactory("FEDERATE_WITH", new FederateWithBehaviour(null));
    jadeExtensionDispatcher.registerFactory("DEREGISTER_FROM", new DeregisterFromBehaviour(null));
    jadeExtensionDispatcher.registerFactory("GETDEFAULTDESCRIPTION", new GetDescriptionOfThisDFBehaviour(null)); 
  }

  /**
    This method starts all behaviours needed by <em>DF</em> agent to
    perform its role within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {
    // register the codec of the language
    registerLanguage(SL0Codec.NAME,new SL0Codec());	
		
    // register the ontology used by application
    registerOntology(FIPAAgentManagementOntology.NAME, FIPAAgentManagementOntology.instance());

    // Add a message dispatcher behaviour
    addBehaviour(dispatcher);
    addBehaviour(jadeExtensionDispatcher);
    setDescriptionOfThisDF(getDefaultDescription());
    //FIXMe just for debugging
    showGui();
    //setConstraints();
  }  // End of method setup()

	/**
	  This method make visible the GUI of the DF.
	  @return true if the GUI was not visible already, false otherwise.
	*/
  public boolean showGui() {
   if (gui == null) 
  		{
			gui = new DFGUI(df.this, false);
			gui.refresh(PROVA.values().iterator(),parents.iterator(),children.iterator());
			
			gui.setVisible(true);
			return true;
  		}
  	
   return false;
  }
   
 


  /**
    Cleanup <em>DF</em> on exit. This method performs all necessary
    cleanup operations during agent shutdown.
  */
  protected void takeDown() {

    if(gui != null)
	gui.disposeAsync();
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    Iterator it = parents.iterator();
    while(it.hasNext()) {
      AID parentName = (AID)it.next();
      try {
        DFServiceCommunicator.deregister(this, parentName, dfd);
      }
      catch(FIPAException fe) {
        fe.printStackTrace();
      }
    }
  }

  private boolean isADF(DFAgentDescription dfd) {
  	try {
  		return ((ServiceDescription)dfd.getAllServices().next()).getType().equalsIgnoreCase("fipa-df");
  	} catch (Exception e) {
  		return false;
  	}
  }
  
  /**
  * checks that all the mandatory slots for a register/modify/deregister action
  * are present.
  * @param actionName is the name of the action (one of 
  * <code>FIPAAgentManagementOntology.REGISTER</code>,
  * <code>FIPAAgentManagementOntology.MODIFY</code>,
  * <code>FIPAAgentManagementOntology.DEREGISTER</code>)
  * @param dfd is the DFAgentDescription to be checked for
  * @throws MissingParameter if one of the mandatory slots is missing
  **/
  private void checkMandatorySlots(String actionName, DFAgentDescription dfd) throws MissingParameter {
  	try {
  	  if (dfd.getName().getName().length() == 0)
  		  throw new MissingParameter(FIPAAgentManagementOntology.DFAGENTDESCRIPTION, "name");
  	} catch (Exception e) {
  		e.printStackTrace();
  		throw new MissingParameter(FIPAAgentManagementOntology.DFAGENTDESCRIPTION, "name");
  	}
  	if (!actionName.equalsIgnoreCase(FIPAAgentManagementOntology.DEREGISTER))
  	 for (Iterator i=dfd.getAllServices(); i.hasNext();) {
  		ServiceDescription sd =(ServiceDescription)i.next();
  		try {
  		  if (sd.getName().length() == 0)
  		   throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "name");
  	  } catch (Exception e) {
  	  	e.printStackTrace();
  		   throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "name");
  	  }
  	  try {
  		  if (sd.getType().length() == 0)
  		   throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "type");
  	  } catch (Exception e) {
  	  	e.printStackTrace();
  		   throw new MissingParameter(FIPAAgentManagementOntology.SERVICEDESCRIPTION, "type");
  	  }
  	 } //end of for
  }
  
  
private HashMap PROVA = new HashMap(); // solo qui come prova. DA RIMUOVERE
  
private void DFRegister(DFAgentDescription dfd) throws FIPAException {
    System.out.println("df::DFRegister() called.");
    
    checkMandatorySlots(FIPAAgentManagementOntology.REGISTER, dfd);
    
    PROVA.put(dfd.getName(),dfd);
    if (isADF(dfd)) {
    	children.add(dfd.getName());
    	try {
    		gui.addChildren(dfd.getName());
    	} catch (Exception ex) {}
    }
    try{ //refresh the GUI if shown, exception thrown if the GUI was not shown
    		        gui.addAgentDesc(dfd.getName());	
    }catch(Exception ex){}
    
  }


  private void DFDeregister(DFAgentDescription dfd) throws FIPAException {
    System.out.println("df::DFDeregister() called.");
    checkMandatorySlots(FIPAAgentManagementOntology.DEREGISTER, dfd);

    if (PROVA.remove(dfd.getName()) == null)
    	throw new NotRegistered();
    if (children.remove(dfd.getName()))
    	try {
    		gui.removeChildren(dfd.getName());
    	} catch (Exception e) {}
    try{ //refresh the GUI if shown, exception thrown if the GUI was not shown
    	// this refresh must be here, otherwise the GUI is not synchornized with 
    	// registration/deregistration made without using the GUI
    		        gui.removeAgentDesc(dfd.getName());		
						}catch(Exception e1){}	
    

  }

  private void DFModify(DFAgentDescription dfd) throws FIPAException {
    System.out.println("df::DFModify() called.");
    checkMandatorySlots(FIPAAgentManagementOntology.MODIFY, dfd);

    if (PROVA.put(dfd.getName(),dfd) == null)
    	throw new NotRegistered();
    
    

  }

  private List DFSearch(DFAgentDescription dfd, SearchConstraints constraints, ACLMessage reply) throws FIPAException {
    System.out.println("df::DFSearch() called.");

    return new ArrayList(PROVA.values());
    
  }

  
	// GUI HANDLING

	// DF GUI EVENT 
	private class DFGuiEvent extends GuiEvent
	{
		public static final int REGISTER = 1001;
		public static final int DEREGISTER = 1002;
		public static final int MODIFY = 1003;
		public static final int SEARCH = 1004;
		public static final int FEDERATE = 1005;

	
		public AID dfName;
		public DFAgentDescription dfd;
		public SearchConstraints constraints = new SearchConstraints();

		public DFGuiEvent(Object source, int type, AID dfName, DFAgentDescription dfd)
		{
			super(source, type);
			this.dfName = dfName;
			this.dfd =dfd;
		}
		
		public DFGuiEvent(Object source, int type, AID dfName, DFAgentDescription dfd, SearchConstraints constraints)
		{
			super(source,type);
      this.dfName = dfName;
      this.dfd = dfd;
		  this.constraints = constraints; 
		}
	}
		
	// METHODS PROVIDED TO THE GUI TO POST EVENTS REQUIRING AGENT DATA MODIFICATIONS 
	// This methods are executed by the GUI Thread and their execution results into
	// posting an event into the Agent Thread. Having received that event, the
	// Agent thread executes the method onGuiEvent
	public void postRegisterEvent(Object source, AID dfName, DFAgentDescription dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.REGISTER, dfName, dfd);
		postGuiEvent(ev);
	}
	
	public void postDeregisterEvent(Object source, AID dfName, DFAgentDescription dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.DEREGISTER, dfName, dfd);
		postGuiEvent(ev);
	}

	public void postModifyEvent(Object source, AID dfName, DFAgentDescription dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.MODIFY, dfName, dfd);
		postGuiEvent(ev);
	}

	public void postSearchEvent(Object source, AID dfName, DFAgentDescription dfd, SearchConstraints c)
	{
	 DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.SEARCH, dfName, dfd, c);
	 postGuiEvent(ev);
	}	
	
	public void postFederateEvent(Object source, AID dfName, DFAgentDescription dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source,DFGuiEvent.FEDERATE, dfName, dfd);
		postGuiEvent(ev);
	
	}
	
	
	
	// AGENT DATA MODIFICATIONS FOLLOWING GUI EVENTS
	protected void onGuiEvent(GuiEvent ev)
	{
		try
		{
			DFGuiEvent e;
			switch(ev.getType()) 
			{
			case DFGuiEvent.EXIT:
				gui.disposeAsync();
				gui = null;
				doDelete();
				break;
			case DFGuiEvent.CLOSEGUI:
				gui.disposeAsync();
				gui = null;
				break;
			case DFGuiEvent.REGISTER:
				e = (DFGuiEvent) ev;
				if (e.dfName.equals(getName()) || e.dfName.equals(getLocalName())) 
				{
					// Register an agent with this DF
						DFRegister(e.dfd);
					
				}
				else 
				{
										// Register an agent with another DF. 
				try
		 		{
		  	   gui.showStatusMsg("Process your request & waiting for result...");
		  		 addBehaviour(new GUIRequestDFServiceBehaviour(e.dfName,FIPAAgentManagementOntology.REGISTER,e.dfd,null,gui));
		 		}catch (FIPAException fe) {
		 			fe.printStackTrace(); //it should never happen
		 			} catch(Exception ex){} //Might happen if the gui has been closed
		  	
				
				}
				break;
			case DFGuiEvent.DEREGISTER:
				e = (DFGuiEvent) ev;
				if(e.dfName.equals(getName()) || e.dfName.equals(getLocalName())) 
				{
					// Deregister an agent with this DF
					DFDeregister(e.dfd);
					
				}
				else 
				{
					// Deregister an agent with another DF. 
				try
		 		{
		  	   gui.showStatusMsg("Process your request & waiting for result...");
		  		 addBehaviour(new GUIRequestDFServiceBehaviour(e.dfName,FIPAAgentManagementOntology.DEREGISTER,e.dfd,null,gui));
		 		}catch (FIPAException fe) {
		 			fe.printStackTrace(); //it should never happen
		 			} catch(Exception ex){} //Might happen if the gui has been closed
				}
				break;
			case DFGuiEvent.MODIFY:
				e = (DFGuiEvent) ev;
				if(e.dfName.equals(getName()) || e.dfName.equals(getLocalName())) 
				{
					// Modify the description of an agent with this DF
					DFModify(e.dfd);
					
				}
				else 
				{
					// Modify the description of an agent with another DF
					// The agent whose description has to be modified should be this DF --> MODIFY FEDERATION
				    //					modifyDFData(e.dfName, e.dfd);
				}
				break;
		  case DFGuiEvent.SEARCH:
		  	e = (DFGuiEvent) ev;
		  	
		  	if (gui != null)
		  	   gui.showStatusMsg("Process your request & waiting for result...");
		  	try{
	  		  addBehaviour(new GUIRequestDFServiceBehaviour(e.dfName,FIPAAgentManagementOntology.SEARCH,e.dfd,e.constraints,gui));
	  	  }catch(FIPAException fe){
	  	   fe.printStackTrace();
	  	  }
		  	 
		  	break;
		 	case DFGuiEvent.FEDERATE:
		 		e = (DFGuiEvent) ev;
		 	
		 		try
		 		{
		  	   gui.showStatusMsg("Process your request & waiting for result...");
		  	   
		  	   if(e.dfName.equals(getAID()) || e.dfName.equals(getLocalName()))
		 	  		gui.showStatusMsg("Self Federation not allowed");
		  		else
		  		addBehaviour(new GUIRequestDFServiceBehaviour(e.dfName,FIPAAgentManagementOntology.REGISTER,e.dfd,null,gui));
		 		}catch (FIPAException fe) {
		 			fe.printStackTrace(); //it should never happen
		 			} catch(Exception ex){} //Might happen if the gui has been closed
		  	  
		  	
		 		break;
		 	
		 
			} // END of switch
		} // END of try
		catch(FIPAException fe) 
		{
			fe.printStackTrace();
			//FIXME showErrorMsg
		}
	}

	
	// This method returns the descriptor of an agent registered with the df.
	public DFAgentDescription getDFAgentDsc(AID name) throws FIPAException
	{
	  return  (DFAgentDescription)PROVA.get(name);
	}
	
	
	
	/**
  * This method creates the DFAgent descriptor for this df used to federate with other df.
	*/
	private DFAgentDescription getDefaultDescription()
	{
	  	DFAgentDescription out = new DFAgentDescription();
	
			out.setName(getAID());
			out.addOntologies(FIPAAgentManagementOntology.NAME);
			out.addLanguages(SL0Codec.NAME);
			out.addProtocols("fipa-request");
			ServiceDescription sd = new ServiceDescription();
			sd.setName("df-service");
			sd.setType("fipa-df");
			sd.addOntologies(FIPAAgentManagementOntology.NAME);
			sd.addLanguages(SL0Codec.NAME);
			sd.addProtocols("fipa-request");
      try{
		  	sd.setOwnership(InetAddress.getLocalHost().getHostName());
		  }catch (java.net.UnknownHostException uhe){
		  	sd.setOwnership("unknown");}
		  
		  out.addServices(sd);
		  
		  return out;
	}

	
	/*
	* This method set the description of the df according to the DFAgentAgentDescription passed.
	* The programmers can call this method to provide a different initialization of the description of the df they are implemented.
	* The method is called inside the setup of the agent and set the df description using a default description.
	*/
	public void setDescriptionOfThisDF(DFAgentDescription dfd)
	{
		thisDF = dfd;
	}
	/**
	* This method returns the current description of this DF
	*/
	public DFAgentDescription getDescriptionOfThisDF()
	{
	    return thisDF;
	}
	
	
	/*
	This method can be used to add a parent (a DF with which the this DF is federated). 
	*/
	public void addParent(AID dfName)
	{
	  parents.add(dfName);
	}
	
	
	
	
	
	

	
}
