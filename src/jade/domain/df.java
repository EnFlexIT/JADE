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
import jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import jade.lang.sl.SL0Codec;

import jade.onto.basic.Action;

import jade.proto.FipaRequestResponderBehaviour;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
// import jade.gui.DFGUI;
import jade.gui.GUI2DFCommunicatorInterface;

import jade.proto.FipaRequestInitiatorBehaviour;

class DFGUI { // <----------------------------------------------- STUB CLASS. JUST TO COMPILE THE DF WITHOUT GUI
    public DFGUI(df p1, boolean p2) { }
    public void refresh() { }
    public void disposeAsync() { }
    public void setVisible(boolean b) { }
    public void showStatusMsg(String s) { }
}            // <----------------------------------------------



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

    protected DFBehaviour() {
      super(df.this);
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
      	sendRefuse(fe.getMessage());
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

    public FipaRequestResponderBehaviour.ActionHandler create() {
      return new RegBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
      Register r = (Register)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)r.get_0();
      DFRegister(dfd);
      sendAgree();
      sendInform();
    }

  } // End of RegBehaviour class

  private class DeregBehaviour extends DFBehaviour {

    public FipaRequestResponderBehaviour.ActionHandler create() {
      return new DeregBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
      Deregister d = (Deregister)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)d.get_0();
      DFDeregister(dfd);
      sendAgree();
      sendInform();
    }

  } // End of DeregBehaviour class

  private class ModBehaviour extends DFBehaviour {

    public FipaRequestResponderBehaviour.ActionHandler create() {
      return new ModBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
      Modify m = (Modify)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)m.get_0();
      DFModify(dfd);
      sendAgree();
      sendInform();
    }

  } // End of ModBehaviour class

   private class SrchBehaviour extends DFBehaviour {

    public FipaRequestResponderBehaviour.ActionHandler create() {
      return new SrchBehaviour();
    }

    protected void processAction(Action a) throws FIPAException {
      Search s = (Search)a.getAction();
      DFAgentDescription dfd = (DFAgentDescription)s.get_0();
      SearchConstraints constraints = s.get_1();
      DFSearch(dfd, constraints, getReply());

    }

  } // End of SrchBehaviour class


  private class ShowGUIBehaviour extends FipaRequestResponderBehaviour.ActionHandler 
                                 implements FipaRequestResponderBehaviour.Factory 
  {
  	protected ShowGUIBehaviour() 
	{
      	super(df.this);
  	}

  	public FipaRequestResponderBehaviour.ActionHandler create() 
  	{
      	return new ShowGUIBehaviour();
  	}

  	public void action () 
  	{ 
	    sendAgree();
	    if (((df)myAgent).showGui())
	      sendInform();
	    else
	      sendFailure("Gui_is_being_shown_already");
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
      protected GetParentsBehaviour()
      {
	super(df.this);
      }
  	
      public FipaRequestResponderBehaviour.ActionHandler create()
      {
	return new GetParentsBehaviour();
      }

      public void action ()
      {
	sendAgree();
  		
	try {
	  // Construct the reply, using the FIPAAgentManagementOntology singleton to build the sequence of
	  // DFAgentDescription objects for the parents.
	  throw new FIPAException("STUB CODE !!!");
	}
	catch(FIPAException e) {
	  sendFailure("Impossible to provide the needed information");
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
    protected GetDescriptionOfThisDFBehaviour()
    {
      super(df.this);
    }
  	
    public FipaRequestResponderBehaviour.ActionHandler create()
    {
      return new GetDescriptionOfThisDFBehaviour();
    }
  	
    public void action()
      {
	sendAgree();

	// Create an 'inform' ACL message containing the DFAgentDescription object for this DF.

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

    protected FederateWithBehaviour() {
      super(df.this);
    }

    public FipaRequestResponderBehaviour.ActionHandler create() {
      return new FederateWithBehaviour();
    }

    public void action() {
      // Federate with the given DF
    }

    public boolean done() {
      return true;
    }

    public void reset(){}


  } // End of FederateWithBehaviour
  

  
  //This behaviour allow the applet to required the df to deregister itself form a parent of the federation
  private class DeregisterFromBehaviour extends FipaRequestResponderBehaviour.ActionHandler implements FipaRequestResponderBehaviour.Factory
  {

  	protected DeregisterFromBehaviour()
  	{
  		super(df.this);
  	}
  	
  	public FipaRequestResponderBehaviour.ActionHandler create()
  	{
  		return new DeregisterFromBehaviour();
  	}
  	
  	public void action()
  	{
	  // Deregister from the given DF;
  	}
  	
  	public boolean done()
  	{
  		return true;
  	}
  	public void reset(){}
  	
  }//End DeregisterFromBehaviour
  
 
   // This private class extends the RequestDFActionBehaviour to make the search of agents with a DF. 
    //  private class mySearchWithDFBehaviour extends RequestDFActionBehaviour {
     
    //  }
 
 // This private class extends the RequestDFActionBehaviour to make the registration of an agent with a DF.
 // In particular it will be use to manage the federate action with a df.
 // The methos handle Inform has been override to update the parent and to refresh the gui if showed. 
    // private class myRegisterWithDFBehaviour extends RequestDFActionBehaviour {

    // }
 
 
 // This private class extends the RequestDFActionBehavior in order to provide the deregistration 
 // of this df with another df.
    // private class myDeregisterWithDFBehaviour extends RequestDFActionBehaviour {

    // }

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
  
  // List to maintain the constraint for the search inserted by the user.
  /**
  @serial
  */
  private List searchConstraint = null;
  
  
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

    dispatcher.registerFactory(FIPAAgentManagementOntology.REGISTER, new RegBehaviour());
    dispatcher.registerFactory(FIPAAgentManagementOntology.DEREGISTER, new DeregBehaviour());
    dispatcher.registerFactory(FIPAAgentManagementOntology.MODIFY, new ModBehaviour());
    dispatcher.registerFactory(FIPAAgentManagementOntology.SEARCH, new SrchBehaviour());

    // Behaviour to deal with the GUI
    // FIXME. Fabio. This is only necessary because AgentManagementParser.jj
    // parses also the action names. Asap AgentManagementParser.jj is
    // fixed, we can register SHOWGUI with dispatcher
    MessageTemplate mt1 = MessageTemplate.MatchOntology("jade-extensions");
    jadeExtensionDispatcher = new FipaRequestResponderBehaviour(this, mt1);
    jadeExtensionDispatcher.registerFactory("SHOWGUI", new ShowGUIBehaviour());
    // The following three actions are used only by the DFApplet	
    jadeExtensionDispatcher.registerFactory("GETPARENTS", new GetParentsBehaviour()); 
    jadeExtensionDispatcher.registerFactory("FEDERATE_WITH", new FederateWithBehaviour());
    jadeExtensionDispatcher.registerFactory("DEREGISTER_FROM", new DeregisterFromBehaviour());
    jadeExtensionDispatcher.registerFactory("GETDEFAULTDESCRIPTION", new GetDescriptionOfThisDFBehaviour()); 
  }

  /**
    This method starts all behaviours needed by <em>DF</em> agent to
    perform its role within <em><b>JADE</b></em> agent platform.
  */
  protected void setup() {
    
    // Add a message dispatcher behaviour
    addBehaviour(dispatcher);
    addBehaviour(jadeExtensionDispatcher);
    setDescriptionOfThisDF(getDefaultDescription());
    setConstraints();
  }  // End of method setup()

	/**
	  This method make visible the GUI of the DF.
	  @return true if the GUI was not visible already, false otherwise.
	*/
  public boolean showGui() {
   if (gui == null) 
  		{
			gui = new DFGUI(df.this, false);
			gui.refresh();
			
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
        deregisterWithDF(parentName, dfd);
      }
      catch(FIPAException fe) {
        fe.printStackTrace();
      }
    }
  }

  private void DFRegister(DFAgentDescription dfd) throws FIPAException {
    System.out.println("df::DFRegister() called.");
  }


  private void DFDeregister(DFAgentDescription dfd) throws FIPAException {
    System.out.println("df::DFDeregister() called.");
  }

  private void DFModify(DFAgentDescription dfd) throws FIPAException {
    System.out.println("df::DFModify() called.");
  }

  private void DFSearch(DFAgentDescription dfd, SearchConstraints constraints, ACLMessage reply) throws FIPAException {
    System.out.println("df::DFSearch() called.");
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
		public static final int SEARCH_WITH_CONSTRAINT = 1006;
	
		public String dfName;
		public DFAgentDescription dfd;
		public Vector constraints = null;

		public DFGuiEvent(Object source, int type, String dfName, DFAgentDescription dfd)
		{
			super(source, type);
			this.dfName = dfName;
			this.dfd =dfd;
		}
		
		public DFGuiEvent(Object source, int type, String dfName, DFAgentDescription dfd, Vector constraints)
		{
			super(source,type);
      this.dfName = dfName;
      this.dfd = dfd;
		  this.constraints = constraints; 
		}
	}
		
	// METHODS PROVIDED TO THE GUI TO POST EVENTS REQUIRING AGENT DATA MODIFICATIONS 	
	public void postRegisterEvent(Object source, String dfName, DFAgentDescription dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.REGISTER, dfName, dfd);
		postGuiEvent(ev);
	}
	
	public void postDeregisterEvent(Object source, String dfName, DFAgentDescription dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.DEREGISTER, dfName, dfd);
		postGuiEvent(ev);
	}

	public void postModifyEvent(Object source, String dfName, DFAgentDescription dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.MODIFY, dfName, dfd);
		postGuiEvent(ev);
	}

	public void postSearchEvent(Object source, String dfName, DFAgentDescription dfd)
	{
	 DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.SEARCH, dfName, dfd);
	 postGuiEvent(ev);
	}	
	
	public void postFederateEvent(Object source, String dfName, DFAgentDescription dfd)
	{
		DFGuiEvent ev = new DFGuiEvent(source,DFGuiEvent.FEDERATE, dfName, dfd);
		postGuiEvent(ev);
	
	}
	
	public void postSearchWithConstraintEvent(Object source, String dfName, DFAgentDescription dfd, List constraint)
	{
	    /*
		DFGuiEvent ev = new DFGuiEvent(source, DFGuiEvent.SEARCH_WITH_CONSTRAINT,dfName,dfd,constraint);
		postGuiEvent(ev);
	    */
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
				if (e.dfName.equalsIgnoreCase(getName()) || e.dfName.equalsIgnoreCase(getLocalName())) 
				{
					// Register an agent with this DF
						DFRegister(e.dfd);
				}
				else 
				{
					// Register an agent with another DF. 
					// The agent to be registered should be this DF --> FEDERATE
				    //					addBehaviour(new myRegisterWithDFBehaviour(this,e.dfName, e.dfd));
				}
				break;
			case DFGuiEvent.DEREGISTER:
				e = (DFGuiEvent) ev;
				if(e.dfName.equalsIgnoreCase(getName()) || e.dfName.equalsIgnoreCase(getLocalName())) 
				{
					// Deregister an agent with this DF
					DFDeregister(e.dfd);
				}
				else 
				{
					// Deregister an agent with another DF. 
					// The agent to be deregistered should be this DF --> DEFEDERATE
				    // addBehaviour(new myDeregisterWithDFBehaviour(this,e.dfName,e.dfd));  
				}
				break;
			case DFGuiEvent.MODIFY:
				e = (DFGuiEvent) ev;
				if(e.dfName.equalsIgnoreCase(getName()) || e.dfName.equalsIgnoreCase(getLocalName())) 
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
		  	searchDFAgentDescriptor(e.dfd,e.dfName,null);	 
		  	break;
		 	case DFGuiEvent.FEDERATE:
		 		e = (DFGuiEvent) ev;
		 	  //registerWithDF(e.dfName, e.dfd);
		 	  if (!(e.dfName.equalsIgnoreCase(getName()) || e.dfName.equalsIgnoreCase(getLocalName())))
		 	  	{
		 	  		if(gui != null)
		 	  			gui.showStatusMsg("Process your request & waiting for result.");
					//		 	  		addBehaviour(new myRegisterWithDFBehaviour(this,e.dfName,e.dfd));
		 	  	}
		 	  else
		 	  { 
		 	  	if (gui != null)
		 	  		gui.showStatusMsg("Self Federation not allowed");
		 	  	throw new FIPAException("Self Federation not allowed");
		 	  }
		 		break;
		 	case DFGuiEvent.SEARCH_WITH_CONSTRAINT:
			    /*		 		e = (DFGuiEvent)ev;
		 		searchConstraint = (Vector)(e.constraints).clone();
		 		searchDFAgentDescriptor(e.dfd,e.dfName,searchConstraint);
			    */
		 		break;
		 
			} // END of switch
		} // END of try
		catch(FIPAException fe) 
		{
			fe.printStackTrace();
		}
	}

	////////////////////////////////////////////////
	// Methods used by the DF GUI to get the DF data
	public Iterator getAllDFAgentDsc() 
	{
	  return new ArrayList().iterator();
	}

	// This method returns the descriptor of an agent registered with the df.
	public DFAgentDescription getDFAgentDsc(String name) throws FIPAException
	{
	  return new DFAgentDescription();
	}
	
	// returns the description of the agents result of the search. 
	public DFAgentDescription getDFAgentSearchDsc(String name) throws FIPAException
	{
	    /*
		DFAgentDescription out = null;
	
		Enumeration e = found.elements();
		while (e.hasMoreElements())
		{
			 DFAgentDescription dfd = (DFAgentDescription) e.nextElement();
			 if (dfd.getName().equalsIgnoreCase(name))
			  out = dfd;	
		}
		if (out == null) throw myOntology.getException(AgentManagementOntology.Exception.INCONSISTENCY);
	    */
	    DFAgentDescription out = new DFAgentDescription();
		return out;
	}
	//This method implements the search feature. 
	
	public void searchDFAgentDescriptor(DFAgentDescription dfd, String responder,Vector constraints)
	{
	    /*
	  	gui.showStatusMsg("Process your request & waiting for result...");
	  	try{
	  		addBehaviour(new mySearchWithDFBehaviour(this,responder,dfd,constraints,null));
	  	}catch(FIPAException fe){
	  	  fe.printStackTrace();
	  	}
	    */
	}
	
	/**
  * This method creates the DFAgent descriptor for this df used to federate with other df.
	*/
	private DFAgentDescription getDefaultDescription()
	{
	  	DFAgentDescription out = new DFAgentDescription();
		/*
	  	//System.out.println("Initialization of the description of thisDF");
			out.setName(getName());
		  out.addAddress(getAddress());
		  try{
		  	out.setOwnership(InetAddress.getLocalHost().getHostName());
		  }catch (java.net.UnknownHostException uhe){
		  	out.setOwnership("unknown");}
		  out.setType("fipa-df");
		  out.setDFState("active");
		  out.setOntology("fipa-agent-management");
		  //thisDF.setLanguage(SL0Codec.NAME); not exist method setLanguage() for dfd in Fipa97
		  out.addInteractionProtocol("fipa-request");
		  AgentManagementOntology.ServiceDescriptor sd = new 	AgentManagementOntology.ServiceDescriptor();
	    sd.setType("fipa-df");
	    sd.setName("federate-df");
	    sd.setOntology("fipa-agent-management");
		  out.addAgentService(sd);
		*/
		  return out;
	}

	//Method to know the parent of this df. DFs with which the current DF is federated.
	public Iterator getParents()
	{
	  return parents.iterator();	
	}

	//Method to know the df registered with this df
	public Iterator getChildren()
	{
	  return children.iterator();
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
	
	// This method is used to get the constraint manitained by the df to perform a search with constraint
	public List getConstraints()
	{
	  return searchConstraint;
	}
	
	/*
	This method can be used to add a parent (a DF with which the this DF is federated). 
	*/
	public void addParent(String dfName)
	{
	  parents.add(dfName);
	}
	
	
	//This method is called in the setup method to initialize the constraint vector for the search operation.
	private void setConstraints()
	{
	    /*
		AgentManagementOntology.Constraint c = new 	AgentManagementOntology.Constraint();
		c.setName(	AgentManagementOntology.Constraint.DFDEPTH);
		c.setFn(AgentManagementOntology.Constraint.MAX);
		c.setArg(1);
		searchConstraint = new Vector();
		searchConstraint.add(c);
	    */
	}
	
	// Method for refresh the gui of an applet of the df.
	public void postRefreshAppletGuiEvent(Object o){}
}
