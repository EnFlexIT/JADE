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

import java.util.Vector;
import java.util.Date;

import jade.util.leap.HashMap;
import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.util.leap.Iterator;
import jade.util.leap.Properties;

import java.net.InetAddress;

import jade.core.AID;
import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.behaviours.*;

import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAAgentManagement.InternalError;
import jade.domain.JADEAgentManagement.*;
import jade.domain.KBManagement.*;
import jade.domain.DFGUIManagement.*;


import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ISO8601;

import jade.gui.GuiAgent;
import jade.gui.GuiEvent;

import jade.proto.SubscriptionResponder;

import jade.content.*;
import jade.content.lang.*;
import jade.content.lang.sl.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.abs.*;

/**
  Standard <em>Directory Facilitator</em> agent. This class implements
  <em><b>FIPA</b></em> <em>DF</em> agent. <b>JADE</b> applications
  cannot use this class directly, but interact with it through
  <em>ACL</em> message passing. The <code>DFService</code> class provides
  a number of static methods that facilitate this task.
  More <em>DF</em> agents can be created
  by application programmers to divide a platform into many
  <em><b>Agent Domains</b></em>.
	<p>
	A DF agent accepts a number of optional configuration parameters that can be set
	either as command line options or within a properties file (to be passed to 
	the DF as an argument).
	<ul>
	<li>
	<code>jade_domain_df_maxleasetime</code> Indicates the maximum lease
	time (in millisecond) that the DF will grant for agent description registrations (defaults
	to infinite).
	</li>
	<li>
	<code>jade_domain_df_maxresult</code> Indicates the maximum number of items found
	in a search operation that the DF will return to the requester (defaults 
	to 100).
	</li>
	<li>
	<code>jade_domain_df_db-url</code> Indicates the JDBC URL of the database
	the DF will store its catalogue into. If this parameter is not specified 
	the DF will keep its catalogue in memory.
	</li>
	<li>
	<code>jade_domain_df_db-driver</code> Indicates the JDBC driver to be used
	to access the DF database (defaults to the ODBC-JDBC bridge). This parameter 
	is ignored if <code>jade_domain_df_db-url</code> is not set.
	</li>
	<li>
	<code>jade_domain_df_db-username</code>, <code>jade_domain_df_db-password</code>
	Indicate the username and password to be used to access the DF database 
	(default to null). These parameters are ignored if 
	<code>jade_domain_df_db-url</code> is not set.
	</li>
	</ul>
	For instance the following command line will launch a JADE main container
	with a DF that will store its catalogue into a database accessible at 
	URL jdbc:odbc:dfdb and that will keep agent registrations for 1 hour at most.
	<p>
	<code>
	java jade.Boot -gui -jade_domain_df_db-url jdbc:odbc:dfdb -jade_domain_df_maxleasetime 3600000
	</code>
	<p>
  Each DF has a GUI but, by default, it is not visible. The GUI of the
  agent platform includes a menu item that allows to show the GUI of the
  default DF. 
  In order to show the GUI, you should simply send the following message
  to each DF agent: <code>(request :content (action DFName (SHOWGUI))
  :ontology JADE-Agent-Management :protocol fipa-request)</code>
 
 	@see DFService
  @author Giovanni Rimassa - Universita` di Parma
  @author Tiziana Trucco - TILAB S.p.A.
  @author Elisabetta Cortese - TILAB S.p.A.
  @version $Date$ $Revision$
*/
public class df extends GuiAgent implements DFGUIAdapter {

  private class RecursiveSearchBehaviour extends RequestFIPAServiceBehaviour 
  {
  	RecursiveSearchHandler rsh;
  	RecursiveSearchBehaviour(RecursiveSearchHandler rsh, AID children, DFAgentDescription dfd, SearchConstraints constraints) throws FIPAException
  	{
  		super(df.this, children, FIPAManagementOntology.SEARCH, dfd, constraints);
  		this.rsh = rsh;
  	}
  	
  	protected void handleInform(ACLMessage reply)
  	{
  		super.handleInform(reply);
  		try{
  			// Convert search result from array to list
  			Object[] r = getSearchResults();
  			List l = new ArrayList();
  			for (int i = 0; i < r.length; ++i) {
  				l.add(r[i]);
  			}
  			rsh.addResults(this, l);
  		}catch (FIPAException e){ e.printStackTrace();
  		}catch(NotYetReady nyr){ nyr.printStackTrace();}
  	}
  	
  	protected void handleRefuse(ACLMessage reply)
  	{
  		super.handleRefuse(reply);
  		try{
  			rsh.addResults(this,new ArrayList(0));
  		}catch(FIPAException e){e.printStackTrace();}
  	}
  	
  	protected void handleFailure(ACLMessage reply)
  	{
  		super.handleFailure(reply);
  		try{
  			rsh.addResults(this,new ArrayList(0));
  		}catch(FIPAException e){e.printStackTrace();}

  	}
  	protected void handleNotUnderstood(ACLMessage reply)
  	{
  		super.handleNotUnderstood(reply);
  		try{
  			rsh.addResults(this,new ArrayList(0));
  		}catch(FIPAException e){e.printStackTrace();}

  	}

      //send a not understood message if an out of sequence message arrives.
      protected void handleOutOfSequence(ACLMessage reply){
	  super.handleOutOfSequence(reply);
	  try{
	      rsh.addResults(this,new ArrayList(0));
	      ACLMessage res = reply.createReply();
	      res.setPerformative(ACLMessage.NOT_UNDERSTOOD);
	      UnexpectedAct ua = new UnexpectedAct(ACLMessage.getPerformative(reply.getPerformative()));
	      String cont = "( (action "+reply.getSender()+" "+reply+") "+ua.getMessage()+")";
	      res.setContent(cont);
	      myAgent.send(res);
	  }catch(FIPAException e){e.printStackTrace();}
      }

      //called when the timeout is expired.
      protected void handleAllResponses(Vector reply){
	  super.handleAllResponses(reply);
	  try{
	      if(reply.size() == 0) //the timeout has expired: no replies received.
		  rsh.addResults(this,new ArrayList(0));
	  }catch(FIPAException e){e.printStackTrace();}
      }
  }//End class RecursiveSearchBehaviour
  
  

  // FIXME The size of the cache must be read from the Profile
  private jade.util.HashCache searchIdCache = new jade.util.HashCache(10);

 
  
  /**
   * @return 
   * <ul>
   * <li> 1 if constraints.maxResults == null (according to FIPA specs)
   * <li> LIMIT_MAXRESULT if constraints.maxResults < 0 (the FIPA specs requires it to be 
   * infinite, but for practical reason we prefer to limit it)
   * <li> constraints.maxResults otherwise
   * </ul>
   **/
  int getActualMaxResults(SearchConstraints constraints) {
      int maxResult = (constraints.getMaxResults() == null ? 1 : constraints.getMaxResults().intValue());
      maxResult = (maxResult < 0 ? maxResultLimit : maxResult); // limit the max num of results
      return maxResult;              
  }
  
  /**
  * Check if this search request must be propagated to the federated DFs.
  * The following conditions are checked:
  * <ul>
  * <li> max_depth must not be equal to 0 or to null 
  *      (the default value for max_depth is 0 according to FIPA)
  * <li> the number of found results must be less than max_results
  *      (the default value for max_results is 1 according to FIPA)
  * </ul>
  * @param constraints is the searchConstraints parameter received in the search request
  * @param foundResultSize is the number of results that have been already found
  * @return true if the search must be propagated, false otherwise
  **/
  boolean searchMustBePropagated(SearchConstraints constraints, int foundResultSize) 
  {
  Long maxDepth = constraints.getMaxDepth();
  if ( (maxDepth == null) || (maxDepth.intValue() == 0) ) 
  	return false;
  if (foundResultSize >= getActualMaxResults(constraints))
  	return false;
  return true;
  }
  
  /**    
   * Check if this search must be served, i.e. if it has not yet been received.
   * In particular the value of search_id must be different from any prior value that was received.
   * If search_id is not null and it has not yet been received, search_id is
   * added into the cache.
   * If search_id is null, the method returns true.
   * @return true if the search must be served, false otherwise
  **/
	boolean searchMustBeServed(SearchConstraints constraints) 
	{
	String searchId = constraints.getSearchId();
	if (searchId == null)
		return true;
	else if (searchIdCache.contains(searchId))
		return false;
	else {
   	    searchIdCache.add(searchId);
		return true;
	}
	}
	 
  /**
    This method called into the DFFIPAAgentManagementBehaviour add the behaviours for a recursive search.
	If constraints contains a null search_id, then a new one is generated and
	the new search_id is stored into searchIdCache 
	for later check (i.e. to avoid search loops). 
		@return true if the Df has children, false otherwise
   */
	boolean performRecursiveSearch(List l, SearchConstraints constraints, DFAgentDescription dfd, ACLMessage request, Action action){

	    boolean out = false;
	    //Long maxResults=constraints.getMaxResults();
		Long maxResults= new Long(getActualMaxResults(constraints));
	    RecursiveSearchHandler rsh = new RecursiveSearchHandler(l, constraints, dfd, request, action);
	    SearchConstraints newConstr = new SearchConstraints();
	    
	    newConstr.setMaxDepth(new Long ((new Integer(constraints.getMaxDepth().intValue()-1)).longValue()));
	    
		if(maxResults != null)
		//newConstr.setMaxResults(new Long((new Integer(constraints.getMaxResults().intValue() - l.size())).longValue()));	    
			newConstr.setMaxResults(new Long((new Integer(getActualMaxResults(constraints) - l.size())).longValue()));	    	
		
		String searchId = constraints.getSearchId();
		if (searchId == null) {
			// then create a globally unique searchId and store into searchIdCache
			searchId = getName() + System.currentTimeMillis();
	  	    searchIdCache.add(searchId);
		}
			
	    newConstr.setSearchId(searchId);
			
	    Iterator childIt = children.iterator();
				
	    while(childIt.hasNext()){
		try{
		    out = true;
		    RecursiveSearchBehaviour b = new RecursiveSearchBehaviour(rsh,(AID)childIt.next(), dfd, newConstr);
		    addBehaviour(b);
		    rsh.addChildren(b);
		}catch(FIPAException e){}
	    }
	    return out;
	}
	
  private class RecursiveSearchHandler {
  	List children;
  	long deadline;
  	List results;
  	int max_results; 
  	DFAgentDescription dfd;
  	ACLMessage request;
  	Action action;
        int DEFAULTTIMEOUT = 300000; // 5 minutes	
  	
    //constructor
    RecursiveSearchHandler(List l, SearchConstraints c, DFAgentDescription dfd, ACLMessage msg, Action a) { 
	    this.results = l;
		max_results = getActualMaxResults(c); 
        // 1 is the default value defined by FIPA
	    this.dfd = dfd;
	    this.request = msg;
	    this.children = new ArrayList();
	 
	    //the replybyDate should have been set; if not the recursive handler set a deadline after 5 minutes.  
	  if (this.request.getReplyByDate() == null)
  		   this.deadline = System.currentTimeMillis() + DEFAULTTIMEOUT;
  	  else 
  	     this.deadline = this.request.getReplyByDate().getTime();

  	  this.action = a;   
  	}
  	
    void addChildren(Behaviour b) {
  		this.children.add(b);
  	}
  	void removeChildren(Behaviour b) {
  		this.children.remove(b);
  	}
  	
    void addResults(Behaviour b, List localResults) throws FIPAException {
	  	this.children.remove(b);
	  
	  	if(max_results >= 0){
	      //number of results still missing	 
	      int remainder = max_results - results.size();

	      if(remainder > 0){
		  		//add the local result to fill the list of results
		  		Iterator it = localResults.iterator();
		  		for(int i =0; i < remainder && it.hasNext(); i++){
		      	results.add(it.next());
		  		}
	      }
	  	}
	  	else {// add all the results returned by the children.
	      for (Iterator i=localResults.iterator(); i.hasNext(); )
		  		results.add(i.next());    
	  		}

	  		if   ((System.currentTimeMillis() >= deadline) || (children.size() == 0)) {
		  		ACLMessage inform = request.createReply();
		  		inform.setPerformative(ACLMessage.INFORM);
		  		Result rs = new Result(action, results);
		  		try {
			  		getContentManager().fillContent(inform, rs);
		  		}
		  		catch (Exception e) {
		  			throw new FIPAException(e.getMessage());
		  		}
		  		send(inform);
	    	} 
    	}
  	}
   
    //performs the ShowGui action: show the GUI of the DF.
    protected void showGuiAction(Action a) throws FailureException{
	//no AGREE sent
	if (!showGui()){
	    throw new FailureException("Gui_is_being_shown_already");
	}
    }

    //this method return an ACLMessage that will be sent to the applet to know the parent with which this df is federated.
    protected ACLMessage getParentAction(Action a,ACLMessage request)throws FailureException{
	try {
	    	    
	    ACLMessage inform = request.createReply();
	    inform.setPerformative(ACLMessage.INFORM);

	    Result rs = new Result(a, parents);
	    try {
	    	getContentManager().fillContent(inform, rs);
		  }
		  catch (Exception e) {
		  	throw new FIPAException(e.getMessage());
		  }
	    
	    return inform;
	    
	}
	catch(FIPAException e) { //FIXME no exception predicate in the DFApplet ontology
	    throw new InternalError("Impossible_to_provide_the_needed_information");
	}
    }


    //Returns the description of this df. 
    //Used to reply to a request from the applet  
    protected ACLMessage getDescriptionOfThisDFAction(Action a,ACLMessage request) throws FailureException{

        try{
      	  ACLMessage inform = request.createReply();      
          inform.setPerformative(ACLMessage.INFORM);

	  List tmp = new ArrayList();
	  tmp.add(thisDF);
	  Result rs = new Result(a, tmp);
	  try {
	  	getContentManager().fillContent(inform, rs);
		}
		catch (Exception e) {
		  throw new FIPAException(e.getMessage());
		}

	  return inform;
	  
	  }catch(FIPAException e) { //FIXME no exception predicate in the DFApplet ontology
	   throw new InternalError("Impossible_to_provide_the_needed_information");
       }
    }
 
   
 	//this behaviour send the reply to the dfproxy	
    private class ThirdStep extends SimpleBehaviour
    {
	private boolean finished = false;
	private GUIRequestDFServiceBehaviour previousStep;
	private String token;
	private ACLMessage request;
	ThirdStep(GUIRequestDFServiceBehaviour b, String action,ACLMessage msg)
	{
	    super(df.this);
	    previousStep = b;
	    token = action;
	    request = msg;
	}
	
	public void action() {
		System.out.println("Agent: " + getName() + "in ThirdStep...Token: " + token);
	  ACLMessage reply = request.createReply();
	  if(previousStep.correctly) {
	    if(token.equalsIgnoreCase(DFAppletOntology.SEARCHON)) {			
				try{	
		    	reply.setPerformative(ACLMessage.INFORM); 
		    
		    	// Convert search result from array to list
		    	Object[] r = previousStep.getSearchResults();
		    	List result = new ArrayList();
		    	for (int i = 0; i < r.length; ++i) {
						result.add(r[i]);
		    	}
		    
		    	try {
		    		Action a = (Action) getContentManager().extractContent(request);
		    		Result rs = new Result(a, result);
		    		getContentManager().fillContent(reply, rs);
	  			}
	  			catch (Exception e) {
	  				throw new FIPAException(e.getMessage());
	  			}
				}
				catch(FIPAException e){ //FIXME no exception predicate in the DFApplet ontology
		    	reply.setPerformative(ACLMessage.FAILURE);
		    	reply.setContent("( ( action " + myAgent.getLocalName() + " "+ token + " )" +" action_not_possible )");
				}
				catch(RequestFIPAServiceBehaviour.NotYetReady nyr){
		    	reply.setPerformative(ACLMessage.FAILURE);
		    	reply.setContent("( ( action " + myAgent.getLocalName() + " "+ token + " )" +" action_not_possible )");
				}
		  }
		  else {
			  reply.setPerformative(ACLMessage.INFORM);
		    try {
		    	Action a = (Action) getContentManager().extractContent(request);
		    	Done d = new Done(a);
		    	getContentManager().fillContent(reply, d);
	  		}
	  		catch (Exception e) {
	  			// Try in any case to send back something meaningful
			  	reply.setContent("( ( done ( action " + myAgent.getLocalName() + " " + token + " ) ) )" );
	  		}
			}
		}
	  else {
		  reply.setPerformative(ACLMessage.FAILURE);
		  reply.setContent("( ( action " + myAgent.getLocalName() + " "+ token + " )" +" action_not_possible )");
		}
	  send(reply);
	  finished = true;
	}			
	
	
	public boolean done()
	{
	    return finished;
	}
	
    }

    // request another DF to federate this DF (require by the applet)
    protected void federateWithAction(Action a, ACLMessage request){
	FederateWithBehaviour fwb = new FederateWithBehaviour(a,request);
	addBehaviour(fwb);
    }

  //This behaviour allows the federation of this df with another df required by the APPLET
  private class FederateWithBehaviour extends SequentialBehaviour {
    			
      FederateWithBehaviour(Action action, ACLMessage msg)
      {
	  super(df.this);
	  String token = DFAppletOntology.FEDERATEWITH;
 			
	  try{
	      Federate f = (Federate)action.getAction(); 	
	      AID parentDF = (AID)f.getParentDF();
	      DFAgentDescription dfd = (DFAgentDescription)f.getChildrenDF();
	      if (dfd == null)
		  dfd = getDescriptionOfThisDF();
	      //send request to parentDF
	      GUIRequestDFServiceBehaviour secondStep = new GUIRequestDFServiceBehaviour(parentDF,FIPAManagementOntology.REGISTER,dfd,null,gui);
	      addSubBehaviour(secondStep);
	      
	      addSubBehaviour(new ThirdStep(secondStep,token,msg));
	      
	  }catch(FIPAException e){
	      //FIXME: set the content of the failure message
	      System.err.println(e.getMessage());
	      ACLMessage failure = msg.createReply();
	      failure.setPerformative(ACLMessage.FAILURE);
	      msg.setContent(createExceptionalMsgContent(action, e)); 
	      send(failure);
	  }
      }
  }//end FederateWithBehaviour
    
    
    //This returns the description of this df. 
    //It is used to reply to a request from the applet 
    protected ACLMessage getDescriptionUsedAction(Action a, ACLMessage request) throws FailureException{
	try{
      	  
	    GetDescriptionUsed act = (GetDescriptionUsed)a.getAction();
	    AID parent = (AID)act.getParentDF();
	        
	    ACLMessage inform = request.createReply();      
	    inform.setPerformative(ACLMessage.INFORM);
       
	    List tmp = new ArrayList();
	    tmp.add(dscDFParentMap.get(parent));
	    Result rs = new Result(a, tmp);
	    try {
	    	getContentManager().fillContent(inform, rs);
		  }
		  catch (Exception e) {
		  	throw new FIPAException(e.getMessage());
		  }
	    
	    return inform;

       }catch(FIPAException e) { //FIXME no exception predicate in the DFApplet ontology
	   throw new InternalError("Impossible_to_provide_the_needed_information");
       }
    }


    protected void deregisterFromAction(Action a, ACLMessage request){
		DeregisterFromBehaviour dfb = new DeregisterFromBehaviour(a,request);
		addBehaviour(dfb);
    }
   
  //This behaviour allow the applet to required the df to deregister itself from a parent of the federation
  private class DeregisterFromBehaviour extends SequentialBehaviour{
     
      DeregisterFromBehaviour(Action action, ACLMessage msg)
      {
	  String token = DFAppletOntology.DEREGISTERFROM;
 			
	  try{
	     
	      DeregisterFrom f = (DeregisterFrom)action.getAction(); 	
	      AID parentDF = (AID)f.getParentDF();
	      DFAgentDescription dfd = (DFAgentDescription)f.getChildrenDF();
	      //send request to parentDF
	      GUIRequestDFServiceBehaviour secondStep = new GUIRequestDFServiceBehaviour(parentDF,FIPAManagementOntology.DEREGISTER,dfd,null,gui);
	      addSubBehaviour(secondStep);
	      
	      addSubBehaviour(new ThirdStep(secondStep,token,msg));
	      
	  }catch(FIPAException e){ //FIXME no exception predicate in the DFApplet ontology
	      //FIXME: send a failure
	      ACLMessage failure = msg.createReply();
	      failure.setPerformative(ACLMessage.FAILURE);
	      failure.setContent(createExceptionalMsgContent(action, e)); 
	      send(failure); 
	      System.err.println(e.getMessage());
	  }
	  
      }
      
   } // End of DeregisterFromBehaviour
  

    protected void registerWithAction(Action a, ACLMessage request){
	RegisterWithBehaviour rwb = new RegisterWithBehaviour(a,request);
	addBehaviour(rwb);
    }

  //This behaviour allow the applet to require the df to register an agent with another df federated with it
  private class RegisterWithBehaviour extends SequentialBehaviour{

      RegisterWithBehaviour(Action a, ACLMessage msg){
 		
	  String token = DFAppletOntology.REGISTERWITH;
 		
	  try{
             
	      RegisterWith rf = (RegisterWith)a.getAction(); 	
	      AID df = rf.getDf();
	      DFAgentDescription dfd = rf.getDescription();
	      //send request to the DF indicated
	      GUIRequestDFServiceBehaviour secondStep = new GUIRequestDFServiceBehaviour(df,FIPAManagementOntology.REGISTER,dfd,null,gui);
	      addSubBehaviour(secondStep);
	      
	      addSubBehaviour(new ThirdStep(secondStep,token,msg));
	      
	  }catch(FIPAException e){ //FIXME no exception predicate in the DFApplet ontology
	      //FIXME: send a failure
	      ACLMessage failure = msg.createReply();
	      failure.setPerformative(ACLMessage.FAILURE);
	      failure.setContent(createExceptionalMsgContent(a, e)); 
	      send(failure); 
	      System.err.println(e.getMessage());
	  }
      }
     
  } // End of RegisterWithBehaviour
  
    protected void modifyOnAction(Action a, ACLMessage request){
		ModifyOnBehaviour mob = new ModifyOnBehaviour(a, request);
		addBehaviour(mob);
    }

  //This behaviour allow the applet to require the df to modify the DFAgentDescription of an agent register with another df
  private class ModifyOnBehaviour extends SequentialBehaviour{
      ModifyOnBehaviour(Action a, ACLMessage msg){
	  String token = DFAppletOntology.MODIFYON;
	  try{

	      ModifyOn mod = (ModifyOn)a.getAction(); 	
	      AID df = mod.getDf();
	      DFAgentDescription dfd = mod.getDescription();
	      //send request to the DF indicated
	      GUIRequestDFServiceBehaviour secondStep = new GUIRequestDFServiceBehaviour(df,FIPAManagementOntology.MODIFY,dfd,null,gui);
	      addSubBehaviour(secondStep);
	      
	      addSubBehaviour(new ThirdStep(secondStep,token,msg));
	      
	  }catch(FIPAException e){ //FIXME no exception predicate in the DFApplet ontology
	      // send a failure
	      ACLMessage failure = msg.createReply();
	      failure.setPerformative(ACLMessage.FAILURE);
	      failure.setContent(createExceptionalMsgContent(a, e)); 
	      send(failure); 
	      System.err.println(e.getMessage());
	  }
	  
      }   		
  } // End of ModifyOnBehaviour
  
    protected void searchOnAction(Action a, ACLMessage request){
		SearchOnBehaviour sob = new SearchOnBehaviour(a,request);
		addBehaviour(sob);
    }
  
//this class is  used to request an agent to perform a search. Used for the applet.
  private class SearchOnBehaviour extends SequentialBehaviour{
      
      SearchOnBehaviour(Action a, ACLMessage msg)
      {
		 String token = DFAppletOntology.SEARCHON;
		  try{
	      SearchOn s = (SearchOn)a.getAction(); 	
	      AID df = s.getDf();
	      DFAgentDescription dfd = s.getDescription();
	      SearchConstraints sc = s.getConstraints();
	      
	      //send request to the DF
	      GUIRequestDFServiceBehaviour secondStep = new GUIRequestDFServiceBehaviour(df,FIPAManagementOntology.SEARCH,dfd,sc,gui);
	      addSubBehaviour(secondStep);
	      
	      addSubBehaviour(new ThirdStep(secondStep,token,msg));
	      
		  }catch(FIPAException e){ //FIXME no exception predicate in the DFApplet ontology
	      //FIXME: send a failure
	      // send a failure
	      ACLMessage failure = msg.createReply();
	      failure.setPerformative(ACLMessage.FAILURE);
	      failure.setContent(createExceptionalMsgContent(a, e)); 
	      send(failure); 
	      System.err.println(e.getMessage());
	  	}
      }
  } // End of SearchOnBehaviour

  /**
  All the actions requested via the DFGUI to another df extends this behaviour
  **/
  private class GUIRequestDFServiceBehaviour extends RequestFIPAServiceBehaviour
  {
    String actionName;
    DFGUIInterface gui;
    AID receiverDF;
    DFAgentDescription dfd;
    boolean correctly = false; //used to verify if the protocol finish correctly
    
  	GUIRequestDFServiceBehaviour(AID receiverDF, String actionName, DFAgentDescription dfd, SearchConstraints constraints, DFGUIInterface gui) throws FIPAException{
  		
  		super(df.this,receiverDF,actionName,dfd,constraints);
  		
  		this.actionName = actionName;
  		this.gui = gui;
  		this.receiverDF = receiverDF;
  		this.dfd = dfd;
  	}
  	
  	protected void handleInform(ACLMessage msg)
  	{
	    super.handleInform(msg);
  		correctly =true;
  		if(actionName.equalsIgnoreCase(FIPAManagementOntology.SEARCH))
  		{
  			try{
  				if(gui != null)
  				{ //the applet can request a search on a different df so the gui can be null
  					//the lastSearchResult table is update also in this case.
  					gui.showStatusMsg("Search request Processed. Ready for new request");
  					// Convert search result from array to list
  					Object[] r = getSearchResults();
  					List result = new ArrayList();
  					for (int i = 0; i < r.length; ++i) {
  						result.add(r[i]);
  					}
  				  gui.refreshLastSearchResults(result, msg.getSender());
  				}
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}
  		else if(actionName.equalsIgnoreCase(FIPAManagementOntology.REGISTER))
  		{
  			try{
  			  
  				if(gui != null) 
  					//this control is needed since this behaviour is used to handle the registration request by an applet.
  					//so the gui can be null and an exception can be thrown.	
  				  gui.showStatusMsg("Request Processed. Ready for new request");
  				
  				if(dfd.getName().equals(df.this.getAID()))
  				{ //if what I register is  myself then I have federated with a parent
  					addParent(receiverDF,dfd);
  				}
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}
  		else if(actionName.equalsIgnoreCase(FIPAManagementOntology.DEREGISTER))
  		{
  			try
  			{
  				//this control is needed since the request could be made by the applet.
  				if(gui != null)
  				   gui.showStatusMsg("Deregister request Processed. Ready for new request");
			       // this behaviour is never used to deregister an agent of this DF
			       // but only to deregister a parent or an agent that was registered with
			       // one of my parents or my children
  			    if(dfd.getName().equals(df.this.getAID()))
			      { 
			        //I deregister myself from a parent
			        removeParent(receiverDF);
			      }
  			    else
			      { 
			      	if(gui != null) //the deregistration can be requested by the applet
			         gui.removeSearchResult(dfd.getName());
			      }
  			}catch (Exception e){
  			e.printStackTrace();// should never happen
  			}
  		}
  		else if(actionName.equalsIgnoreCase(FIPAManagementOntology.MODIFY))
  		{
  			try{
  				gui.showStatusMsg("Modify request processed. Ready for new request");
  			}catch(Exception e){
  			e.printStackTrace();
  			}
  		}
  	}
  	
  	protected void handleRefuse(ACLMessage msg)
      {
	  super.handleRefuse(msg);
	    try{
		gui.showStatusMsg("Request Refused: " + msg.getContent());
	    }catch(Exception e)
  		{}
  	}
  	
  	protected void handleFailure(ACLMessage msg)
  	{
	    super.handleFailure(msg);
  		try{
  		gui.showStatusMsg("Request Failed: " + msg.getContent());
  		}catch(Exception e){}
  	}
  	
  	protected void handleNotUnderstood(ACLMessage msg)
  	{
	    super.handleNotUnderstood(msg);
  		try{
  			gui.showStatusMsg("Request not understood: " + msg.getContent());
  		}catch(Exception e){}
  	}

      protected void handleOutOfSequence(ACLMessage msg){
	  super.handleOutOfSequence(msg);
	  try{
	      //the receiver replied with an out of sequence message.
	      gui.showStatusMsg("Out of sequence response." );
	 } catch(Exception e){}
      }

      //called when the timeout is expired.
      protected void handleAllResponses(Vector reply){
	  super.handleAllResponses(reply);
	  try{
	      if(reply.size() == 0)
		  gui.showStatusMsg("Timeout expired for request");
	  }catch(Exception e){}
      }
  }
  
  /**
     DFSubscriptionResponder BAHAVIOUR. An extended version of the 
     SubscriptionResponder that manages the CANCEL message
   *
  private class DFSubscriptionResponder extends SubscriptionResponder {	
  	private static final String HANDLE_CANCEL = "Handle-cancel";
    public SubscriptionManager mySubscriptionManager = null; // patch to allow compiling with JDK1.2

		DFSubscriptionResponder(Agent a, MessageTemplate mt, SubscriptionManager sm) {
	    super(a, MessageTemplate.or(mt, MessageTemplate.MatchPerformative(ACLMessage.CANCEL)), sm);

	    mySubscriptionManager = sm;
	    registerTransition(RECEIVE_SUBSCRIPTION, HANDLE_CANCEL, ACLMessage.CANCEL);
	    registerDefaultTransition(HANDLE_CANCEL, RECEIVE_SUBSCRIPTION);
		    
			// HANDLE_CANCEL 
			Behaviour b = new OneShotBehaviour(myAgent) {
				public void action() {
			    DataStore ds = getDataStore();
			    // If we are in this state the SUBSCRIPTION_KEY actually contains a CANCEL message 
			    ACLMessage cancel = (ACLMessage) ds.get(SUBSCRIPTION_KEY);
					try {
						Action act = (Action) getContentManager().extractContent(cancel);
						ACLMessage subsMsg = (ACLMessage)act.getAction();
						mySubscriptionManager.deregister(new SubscriptionResponder.Subscription(DFSubscriptionResponder.this, subsMsg));
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
			};
			b.setDataStore(getDataStore());		
			registerState(b, HANDLE_CANCEL);	
		}
  }
  */
		
    
  private List children = new ArrayList();
  private List parents = new ArrayList();
  private HashMap dscDFParentMap = new HashMap(); //corrispondence parent --> dfd description (of this df) used to federate.
  private DFGUIInterface gui;

  // Current description of this df
  private DFAgentDescription thisDF = null;
  
  private Codec codec = new SLCodec();
  
  private DFFipaAgentManagementBehaviour fipaRequestResponder;
  private DFJadeAgentManagementBehaviour jadeRequestResponder;
  private DFAppletManagementBehaviour appletRequestResponder;
  private SubscriptionResponder dfSubscriptionResponder;
  
  // Configuration parameter keys
  private static final String VERBOSITY = "jade_domain_df_verbosity";
	private static final String MAX_LEASE_TIME = "jade_domain_df_maxleasetime";
	private static final String MAX_RESULTS = "jade_domain_df_maxresult";
  private static final String DB_DRIVER = "jade_domain_df_db-driver";
  private static final String DB_URL = "jade_domain_df_db-url";
  private static final String DB_USERNAME = "jade_domain_df_db-username";
  private static final String DB_PASSWORD = "jade_domain_df_db-password";

  // limit of searchConstraints.maxresult
  // FIPA Agent Management Specification doc num: SC00023J (6.1.4 Search Constraints)
  // a negative value of maxresults indicates that the sender agent is willing to receive
  // all available results 
  private static final String DEFAULT_MAX_RESULTS = "100";
  /*
   * This is the actual value for the limit on the maximum number of results to be
   * returned in case of an ulimited search. This value is read from the Profile,
   * if no value is set in the Profile, then DEFAULT_MAX_RESULTS is used instead.
   */
  private int maxResultLimit = Integer.parseInt(DEFAULT_MAX_RESULTS);
  private Date maxLeaseTime = null;
  private int verbosity = 0; 

	private KB agentDescriptions = null;
	private KBSubscriptionManager subManager = null;	
  

  /**
    This method starts all behaviours needed by <em>DF</em> agent to
    perform its role within <em><b>JADE</b></em> agent platform.
   */
  protected void setup() {
		//#PJAVA_EXCLUDE_BEGIN
		// Read configuration:
		// If an argument is specified, it indicates the name of a properties
		// file where to read DF configuration from. Otherwise configuration 
		// properties are read from the Profile.
		// Values in a property file override those in the profile if
		// both are specified.
		String sVerbosity = getProperty(VERBOSITY, null);
		String sMaxLeaseTime = getProperty(MAX_LEASE_TIME, null);
		String sMaxResults = getProperty(MAX_RESULTS, DEFAULT_MAX_RESULTS);
		String dbUrl = getProperty(DB_URL, null);
		String dbDriver = getProperty(DB_DRIVER, null);
		String dbUsername = getProperty(DB_USERNAME, null);
		String dbPassword = getProperty(DB_PASSWORD, null);
		
		Object[] args = this.getArguments();
		if(args != null && args.length > 0) {
			Properties p = new Properties();
			try {
				p.load((String) args[0]);
				sVerbosity = p.getProperty(VERBOSITY, sVerbosity);
				sMaxLeaseTime = p.getProperty(MAX_LEASE_TIME, sMaxLeaseTime);
				sMaxResults = p.getProperty(MAX_RESULTS, sMaxResults);
				dbUrl = p.getProperty(DB_URL, dbUrl);
				dbDriver = p.getProperty(DB_DRIVER, dbDriver);
				dbUsername = p.getProperty(DB_USERNAME, dbUsername);
				dbPassword = p.getProperty(DB_PASSWORD, dbPassword);
			}
			catch (Exception e) {
				log("Error loading configuration from file "+args[0]+" ["+e+"].", 0);
			}
		}
		
		// Convert verbosity into a number
  	try {
  		verbosity = Integer.parseInt(sVerbosity);
  	}
  	catch (Exception e) {
      // Keep default
  	}
		// Convert max lease time into a Date
  	try {
  		maxLeaseTime = new Date(Long.parseLong(sMaxLeaseTime));
  	}
  	catch (Exception e) {
      // Keep default
  	}
		// Convert max results into a number	
  	try {
  		maxResultLimit = Integer.parseInt(sMaxResults);
		if(maxResultLimit < 0){
			maxResultLimit = Integer.parseInt(DEFAULT_MAX_RESULTS);
			log("WARNING: The maxResult parameter of the DF Search Constraints can't be a negative value. It has been set to the default value: " + DEFAULT_MAX_RESULTS ,0);
		}else if(maxResultLimit > Integer.parseInt(DEFAULT_MAX_RESULTS)){
			log("WARNING: Setting the maxResult of the DF Search Constraint to large values can cause low performance or system crash !!",0);
		}
  	}
  	catch (Exception e) {
      // Keep default
  	}
				
  	// Instantiate the knowledge base 
		log("DF KB configuration:", 2);
		if (dbUrl != null) {
			log("- Type = persistent", 2);
			log("- DB url = "+dbUrl, 2);
			log("- DB driver = "+dbDriver, 2);
			log("- DB username = "+dbUsername, 2);
			log("- DB password = "+dbPassword, 2);
			try {
	  			agentDescriptions = new DFDBKB(maxResultLimit, dbDriver, dbUrl, dbUsername, dbPassword);
			}
			catch (Exception e) {
				log("Error creating persistent KB ["+e+"]. Use a volatile KB.", 0);
				e.printStackTrace();
			}
		}
		if (agentDescriptions == null) {
			log("- Type = volatile", 2);
			agentDescriptions = new DFMemKB(maxResultLimit);
		}
		log("- Max lease time = "+(maxLeaseTime != null ? ISO8601.toRelativeTimeString(maxLeaseTime.getTime()) : "infinite"), 2);
		log("- Max search result = "+maxResultLimit, 2);
		//#PJAVA_EXCLUDE_END
		/*#PJAVA_INCLUDE_BEGIN
		agentDescriptions = new DFMemKB(Integer.parseInt(getProperty(MAX_RESULTS, DEFAULT_MAX_RESULTS)));
		#PJAVA_INCLUDE_END*/
	
		// Initiate the SubscriptionManager used by the DF 
		subManager = new KBSubscriptionManager(agentDescriptions);	
		subManager.setContentManager(getContentManager());

    // Register languages and ontologies
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL0);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL1);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL2);	
    getContentManager().registerLanguage(codec, FIPANames.ContentLanguage.FIPA_SL);	
    getContentManager().registerOntology(FIPAManagementOntology.getInstance());
    getContentManager().registerOntology(JADEManagementOntology.getInstance());
    getContentManager().registerOntology(DFAppletOntology.getInstance());

		// Create and add behaviours
  	MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
  	MessageTemplate mt1 = null;
  	
    // Behaviour dealing with FIPA management actions
		mt1 = MessageTemplate.and(mt, MessageTemplate.MatchOntology(FIPAManagementOntology.getInstance().getName()));
    fipaRequestResponder = new DFFipaAgentManagementBehaviour(this, mt1);
    addBehaviour(fipaRequestResponder);

    // Behaviour dealing with JADE management actions
    mt1 = MessageTemplate.and(mt, MessageTemplate.MatchOntology(JADEManagementOntology.getInstance().getName()));
    jadeRequestResponder = new DFJadeAgentManagementBehaviour(this, mt1);
    addBehaviour(jadeRequestResponder);

    // Behaviour dealing with DFApplet management actions
    mt1 = MessageTemplate.and(mt, MessageTemplate.MatchOntology(DFAppletOntology.getInstance().getName()));
    appletRequestResponder = new DFAppletManagementBehaviour(this, mt1);
    addBehaviour(appletRequestResponder);

		// Behaviour dealing with subscriptions
		mt1 = MessageTemplate.and(
			MessageTemplate.MatchOntology(FIPAManagementOntology.getInstance().getName()),
			MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE), MessageTemplate.MatchPerformative(ACLMessage.CANCEL)));
		dfSubscriptionResponder = new SubscriptionResponder(this, mt1, subManager) {
			// Note that the DF does not use the default handleCancel() as
			// the way it retrieve 
    	protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
				ACLMessage subsMsg = null;
				try {
					Action act = (Action) myAgent.getContentManager().extractContent(cancel);
					subsMsg = (ACLMessage)act.getAction();
				}
				catch(Exception e) {
					log("WARNING: unexpected CANCEL content", 1);
					// Create a dummy SUBSCRIBE with the proper sender and conv-id
					subsMsg = new ACLMessage(ACLMessage.SUBSCRIBE);
					subsMsg.setSender(cancel.getSender());
					subsMsg.setConversationId(cancel.getConversationId());
				}
				mySubscriptionManager.deregister(createSubscription(subsMsg));
				return null;
    	}
    };
    addBehaviour(dfSubscriptionResponder);
		    
    // Set the DFDescription of thie DF
    setDescriptionOfThisDF(getDefaultDescription());

		// Set lease policy and subscription responder to the knowledge base  
		agentDescriptions.setSubscriptionResponder(dfSubscriptionResponder);
		agentDescriptions.setLeaseManager(new LeaseManager() {
			public Date getLeaseTime(Object item){
				return ((DFAgentDescription) item).getLeaseTime();
			}
			
			public void setLeaseTime(Object item, Date lease){
				((DFAgentDescription) item).setLeaseTime(lease);
			}
		
			/**
			 * Grant a lease to this request according to the
			 * policy of the DF: if the requested lease is
			 * greater than the max lease policy of this DF, then
			 * the granted lease is set to the max of the DF.
			 **/
			public Object grantLeaseTime(Object item){
				if (maxLeaseTime != null) {
					Date lease = getLeaseTime(item);
					long current = System.currentTimeMillis();
					if ( (lease != null && lease.getTime() > (current+maxLeaseTime.getTime())) ||
					( (lease == null) && (maxLeaseTime != null))) {
					    // the first condition of this if considers the case when the agent requestes a leasetime greater than the policy of this DF. The second condition, instead, considers the case when the agent requests an infinite leasetime and the policy of this DF does not allow infinite leases. 
						setLeaseTime(item, new Date(current+maxLeaseTime.getTime()));
					} 
				}
				return item;
			}
			
			public boolean isExpired(Date lease){
				return (lease != null && (lease.getTime() <= System.currentTimeMillis()));
			}
		} );
  }  // End of method setup()



  /**
    Cleanup <em>DF</em> on exit. This method performs all necessary
    cleanup operations during agent shutdown.
  */
  protected void takeDown() {

    if(gui != null) {
			gui.disposeAsync();
    }
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    Iterator it = parents.iterator();
    while(it.hasNext()) {
      AID parentName = (AID)it.next();
      try {
        DFService.deregister(this, parentName, dfd);
      }
      catch(FIPAException fe) {
        fe.printStackTrace();
      }
    }
  }

    /**
     * Create the content for a so-called "exceptional" message, i.e.
     * one of NOT_UNDERSTOOD, FAILURE, REFUSE message
     * @param a is the Action that generated the exception
     * @param e is the generated Exception
     * @return a String containing the content to be sent back in the reply
     * message; in case an exception is thrown somewhere, the method
     * try to return anyway a valid content with a best-effort strategy
     **/
    //FIXME. This method is only used for create the reply to the APPLET request.
    private String createExceptionalMsgContent(Action a, FIPAException e) {
	    return e.getMessage();
    }

	/**
	  This method make visible the GUI of the DF.
	  @return true if the GUI was not visible already, false otherwise.
	*/
  public boolean showGui() {
   if (gui == null) 
  		{
  			try{
  				Class c = Class.forName("jade.tools.dfgui.DFGUI");
  			  gui = (DFGUIInterface)c.newInstance();
		      gui.setAdapter(df.this); //this method must be called to avoid reflection (the constructor of the df gui has no parameters).		
  			  DFAgentDescription matchEverything = new DFAgentDescription();
		      List agents = agentDescriptions.search(matchEverything);
		      List AIDList = new ArrayList();
		      Iterator it = agents.iterator();
		      while(it.hasNext())
		      	AIDList.add(((DFAgentDescription)it.next()).getName());
		    
		      gui.refresh(AIDList.iterator(), parents.iterator(), children.iterator());
		      gui.setVisible(true);
		      return true;
  			
  			}catch(Exception e){e.printStackTrace();}
  		}
 
   return false;
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
  * <code>FIPAManagementOntology.REGISTER</code>,
  * <code>FIPAManagementOntology.MODIFY</code>,
  * <code>FIPAManagementOntology.DEREGISTER</code>)
  * @param dfd is the DFAgentDescription to be checked for
  * @throws MissingParameter if one of the mandatory slots is missing
  **/
  void checkMandatorySlots(String actionName, DFAgentDescription dfd) throws MissingParameter {
  	try {
  	  if (dfd.getName().getName().length() == 0)
  		  throw new MissingParameter(FIPAManagementOntology.DFAGENTDESCRIPTION, FIPAManagementOntology.DFAGENTDESCRIPTION_NAME);
  	} catch (Exception e) {
  		throw new MissingParameter(FIPAManagementOntology.DFAGENTDESCRIPTION, FIPAManagementOntology.DFAGENTDESCRIPTION_NAME);
  	}
  	if (!actionName.equalsIgnoreCase(FIPAManagementOntology.DEREGISTER))
  	 for (Iterator i=dfd.getAllServices(); i.hasNext();) {
  		ServiceDescription sd =(ServiceDescription)i.next();
  		try {
  		  if (sd.getName().length() == 0)
  		   throw new MissingParameter(FIPAManagementOntology.SERVICEDESCRIPTION, FIPAManagementOntology.SERVICEDESCRIPTION_NAME);
  	  } catch (Exception e) {
  		   throw new MissingParameter(FIPAManagementOntology.SERVICEDESCRIPTION, FIPAManagementOntology.SERVICEDESCRIPTION_NAME);
  	  }
  	  try {
  		  if (sd.getType().length() == 0)
  		   throw new MissingParameter(FIPAManagementOntology.SERVICEDESCRIPTION, FIPAManagementOntology.SERVICEDESCRIPTION_TYPE);
  	  } catch (Exception e) {
  		   throw new MissingParameter(FIPAManagementOntology.SERVICEDESCRIPTION, FIPAManagementOntology.SERVICEDESCRIPTION_TYPE);
  	  }
  	 } //end of for
  }
  
  
  private String kbResource ="default";
  
    void DFRegister(DFAgentDescription dfd) throws AlreadyRegistered {
	
	//checkMandatorySlots(FIPAAgentManagementOntology.REGISTER, dfd);
	Object old = agentDescriptions.register(dfd.getName(), dfd);
	if(old != null)
	    throw new AlreadyRegistered();
	
	if(isADF(dfd)) {
	    children.add(dfd.getName());
	    try {
    		gui.addChildren(dfd.getName());
	    } catch (Exception ex) {}
	}
	// for subscriptions
	subManager.handleChange(dfd);

	try{ //refresh the GUI if shown, exception thrown if the GUI was not shown
	    gui.addAgentDesc(dfd.getName());
	    gui.showStatusMsg("Registration of agent: " + dfd.getName().getName() + " done.");
	}catch(Exception ex){}
	
    }

    //this method is called into the prepareResponse of the DFFipaAgentManagementBehaviour to perform a Deregister action
    void DFDeregister(DFAgentDescription dfd) throws NotRegistered {
	//checkMandatorySlots(FIPAAgentManagementOntology.DEREGISTER, dfd);
      Object old = agentDescriptions.deregister(dfd.getName());

      if(old == null)
	  	throw new NotRegistered();
      
      
      if (children.remove(dfd.getName()))
		  try {
		      gui.removeChildren(dfd.getName());
		  } catch (Exception e) {}
      try{ 
		  // refresh the GUI if shown, exception thrown if the GUI was not shown
		  // this refresh must be here, otherwise the GUI is not synchronized with 
		  // registration/deregistration made without using the GUI
		  gui.removeAgentDesc(dfd.getName(),df.this.getAID());
		  gui.showStatusMsg("Deregistration of agent: " + dfd.getName().getName() +" done.");
      }catch(Exception e1){}	
    }
    
    
    void DFModify(DFAgentDescription dfd) throws NotRegistered {
	//	checkMandatorySlots(FIPAAgentManagementOntology.MODIFY, dfd);
		Object old = agentDescriptions.register(dfd.getName(), dfd);
		if(old == null) {
				// Rollback
				agentDescriptions.deregister(dfd.getName());
		    throw new NotRegistered();
		}
		// for subscription
		subManager.handleChange(dfd);
		try{
		    gui.removeAgentDesc(dfd.getName(), df.this.getAID());
		    gui.addAgentDesc(dfd.getName());
		    gui.showStatusMsg("Modify of agent: "+dfd.getName().getName() + " done.");
		}catch(Exception e){}
		
    }

  List DFSearch(DFAgentDescription dfd, SearchConstraints constraints, ACLMessage reply){
    // Search has no mandatory slots
    return agentDescriptions.search(dfd);
  }
	
	// GUI EVENTS
	protected void onGuiEvent(GuiEvent ev)
	{
		try
		{
			switch(ev.getType()) 
			{
			case DFGUIAdapter.EXIT:
				gui.disposeAsync();
				gui = null;
				doDelete();
				break;
			case DFGUIAdapter.CLOSEGUI:
				gui.disposeAsync();
				gui = null;
				break;
			case DFGUIAdapter.REGISTER:
		
				if (ev.getParameter(0).equals(getName()) || ev.getParameter(0).equals(getLocalName())) 
				{
					// Register an agent with this DF
				    DFAgentDescription dfd = (DFAgentDescription)ev.getParameter(1);
				    checkMandatorySlots(FIPAManagementOntology.REGISTER, dfd);
				    DFRegister(dfd);
				}
				else 
				{
				  // Register an agent with another DF. 
				  try
				    {
				      gui.showStatusMsg("Process your request & waiting for result...");
				      addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0),FIPAManagementOntology.REGISTER,(DFAgentDescription)ev.getParameter(1),null,gui));
				    }catch (FIPAException fe) {
				      fe.printStackTrace(); //it should never happen
				    } catch(Exception ex){} //Might happen if the gui has been closed
				}
				break;
			case DFGUIAdapter.DEREGISTER:

				if(ev.getParameter(0).equals(getName()) || ev.getParameter(0).equals(getLocalName())) 
				{
					// Deregister an agent with this DF
				    DFAgentDescription dfd = (DFAgentDescription)ev.getParameter(1);
				    checkMandatorySlots(FIPAManagementOntology.DEREGISTER, dfd);
				    DFDeregister(dfd);
				}
				else 
				{
					// Deregister an agent with another DF. 
					try
		 			{
		  	  	gui.showStatusMsg("Process your request & waiting for result...");
		  			addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0),FIPAManagementOntology.DEREGISTER,(DFAgentDescription)ev.getParameter(1),null,gui));
		 			}catch (FIPAException fe) {
		 				fe.printStackTrace(); //it should never happen
		 			} catch(Exception ex){} //Might happen if the gui has been closed
				}
				break;
			case DFGUIAdapter.MODIFY:
				
				if(ev.getParameter(0).equals(getName()) || ev.getParameter(0).equals(getLocalName())) 
				{
					// Modify the description of an agent with this DF
				    DFAgentDescription dfd = (DFAgentDescription)ev.getParameter(1);
				    checkMandatorySlots(FIPAManagementOntology.MODIFY, dfd);
				    DFModify(dfd);
				}
				else 
				{
					// Modify the description of an agent with another DF
					try{
						gui.showStatusMsg("Process your request & waiting for result..");
						addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0), FIPAManagementOntology.MODIFY, (DFAgentDescription)ev.getParameter(1),null,gui));
					}catch(FIPAException fe1){
						fe1.printStackTrace();
					}//it should never happen
		 			catch(Exception ex){} //Might happen if the gui has been closed
				}
				break;
		  case DFGUIAdapter.SEARCH:
		  	 
		  	try{
		  		gui.showStatusMsg("Process your request & waiting for result...");
	  		  addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0),FIPAManagementOntology.SEARCH,(DFAgentDescription)ev.getParameter(1),(SearchConstraints)ev.getParameter(2),gui));
	  	  }catch(FIPAException fe){
	  	   fe.printStackTrace();
	  	  }catch(Exception ex1){} //Might happen if the gui has been closed.
		  	 
		  	break;
		 	case DFGUIAdapter.FEDERATE:
		 		try
		 		{
		  	  gui.showStatusMsg("Process your request & waiting for result...");
		  	   
		  	  if(ev.getParameter(0).equals(getAID()) || ev.getParameter(0).equals(getLocalName()))
		 	  		gui.showStatusMsg("Self Federation not allowed");
		  		else
		  		  addBehaviour(new GUIRequestDFServiceBehaviour((AID)ev.getParameter(0),FIPAManagementOntology.REGISTER,(DFAgentDescription)ev.getParameter(1),null,gui));
		 		}catch (FIPAException fe) {
		 			fe.printStackTrace(); //it should never happen
		 		} catch(Exception ex){} //Might happen if the gui has been closed
		 		break;
			} // END of switch
		} // END of try
		catch(FIPAException fe) 
		{
			fe.printStackTrace();
		}
	}

	
	/**
	This method returns the descriptor of an agent registered with the df.
	*/
	public DFAgentDescription getDFAgentDsc(AID name) throws FIPAException
	{
	  DFAgentDescription template = new DFAgentDescription();
	  template.setName(name);
	  List l = agentDescriptions.search(template);
	  if(l.isEmpty())
	    return null;
	  else
	    return (DFAgentDescription)l.get(0);
	}

	/**
  * This method creates the DFAgent descriptor for this df used to federate with other df.
	*/
	private DFAgentDescription getDefaultDescription()
	{
	  	DFAgentDescription out = new DFAgentDescription();
	
			out.setName(getAID());
			out.addOntologies(FIPAManagementOntology.getInstance().getName());
			out.addLanguages(FIPANames.ContentLanguage.FIPA_SL0);
			out.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
			ServiceDescription sd = new ServiceDescription();
			sd.setName("df-service");
			sd.setType("fipa-df");
			sd.addOntologies(FIPAManagementOntology.getInstance().getName());
			sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL0);
			sd.addProtocols(FIPANames.InteractionProtocol.FIPA_REQUEST);
      try{
		  	sd.setOwnership(InetAddress.getLocalHost().getHostName());
		  }catch (java.net.UnknownHostException uhe){
		  	sd.setOwnership("unknown");
		  }
		  
		  out.addServices(sd);
		  
		  return out;
	}

	
	/**
	* This method set the description of the df according to the DFAgentDescription passed.
	* The programmers can call this method to provide a different initialization of the description of the df they are implementing.
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
	    thisDF.setName(getAID());
	    return thisDF;
	}
	
	/**
	* This method returns the description of this df used to federate with the given parent
	*/
	public DFAgentDescription getDescriptionOfThisDF(AID parent)
	{
		return (DFAgentDescription)dscDFParentMap.get(parent);
	}
	
	/**
	* This method can be used to add a parent (a DF with which the this DF is federated). 
	* @param dfName the parent df (the df with which this df has been registered)
	* @param dfd the description used by this df to register with the parent.
	*/
	public void addParent(AID dfName, DFAgentDescription dfd)
	{
	  parents.add(dfName);
	  if(gui != null) // the gui can be null if this method is called in order to manage a request made by the df-applet.
	    gui.addParent(dfName);
    dscDFParentMap.put(dfName,dfd); //update the table of corrispondence between parents and description of this df used to federate.

	}
	
	/**
	this method can be used to remove a parent (a DF with which this DF is federated).
	*/
	public void removeParent(AID dfName)
	{
		parents.remove(dfName); 
		if(gui != null) //the gui can be null is this method is called in order to manage a request from the df applet
		  gui.removeParent(dfName);
		dscDFParentMap.remove(dfName);
	}
	
  private void log(String s, int level) {
  	if (verbosity >= level) {
	  	System.out.println("DF-log: "+s);
  	}
  }
  
}
