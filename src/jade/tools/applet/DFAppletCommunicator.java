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
**************************************************************/

package jade.tools.applet;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.ACLParser;
import jade.lang.acl.ParseException;
import jade.domain.AgentManagementOntology;
import jade.domain.FIPAException;
import jade.gui.GUI2DFCommunicatorInterface;
import jade.gui.DFGUI;

import java.util.*;
import java.applet.Applet;
import java.net.*;
import java.io.*;


/**
 * This class is used by DFApplet in order to communicate with the DF agent
 * via a socket connection. The socket server is implemented by the agent
 * jade.tools.SocketProxyAgent.
 * @see jade.tools.SocketProxyAgent.SocketProxyAgent
 * 
 * @author Fabio Bellifemine - CSELT - 25/8/1999
 * @version $Date$ $Revision$
 */


public class DFAppletCommunicator implements GUI2DFCommunicatorInterface {
  private Applet a;
  private DataInputStream in;
  private PrintStream out;
  private final static int DEFAULT_PORT = 6789;
  private ACLParser parser;
  private DFGUI gui;
  private String address;
  
  private Vector searchConstraint = null;
  
  private AgentManagementOntology.DFSearchResult found = new AgentManagementOntology.DFSearchResult();
  
  private AgentManagementOntology.DFAgentDescriptor thisDF = null;
  /**
   * Create a socket to communicate with a server on port 6789 of the
   * host that the applet's code is on. Create streams to use with the socket.
   * Finally, gets the value of the parameter <code>JADEAddress</code>
   * from the HTML file.
   */
public DFAppletCommunicator(Applet applet) {
  try {
    a = applet;
    
    Socket s = new Socket(a.getCodeBase().getHost(), DEFAULT_PORT);
    System.out.println("DFAppletClient connected to local port "+s.getLocalPort()+" and remote port "+s.getPort());
    in = new DataInputStream(s.getInputStream());
    parser = new ACLParser(in);
    out = new PrintStream(s.getOutputStream(),true);
    address=a.getParameter("JADEAddress");
  } catch (IOException e) {e.printStackTrace(); a.stop();}
}

  /**
   * This method allows this class to call the method showStatusMsg implemented
   * by DFGUI
   */
  void setGUI(DFGUI g){
    gui = g;
  }

  /**
   * writes a String on the socket
   */
   private void sendMessage(String msg){
     out.println(msg);
   }

  /**
   * reads an ACLMessage from the socket, passes it through the parser,
   * and returns it.
   */
   private ACLMessage receiveMessage() throws ParseException {
     return parser.Message();
   }

  /**
   * shows the message not authorized and does nothing.
   */
   public void doDelete() {
     gui.showStatusMsg("Operation not authorized");
   }

  /**
   * returns the full name of the default DF
   */
public String getName() {
  return "df@"+ address; 
}

  /**
   * returns the address of the default DF
   */
public String getAddress() {
  return address; 
} 

  /**
   * returns "DF" that is the name of the default DF. In fact, so far,
   * this applet can be used only to interact with the default DF.
   */
public String getLocalName() {
  return "df";
}

  public void postCloseGuiEvent(Object g) {
    gui.dispose();
    a.destroy(); 
  }

  public void postExitEvent(Object g) {
    gui.dispose();
    a.destroy(); 
  }

  /*
  This method refresh the gui for the applet
  */
  public void postRefreshAppletGuiEvent(Object g)
  {
  	gui.refresh();
  }
  /**
   * registers an agent descriptor with the DF
   * @param parentName is not used
   * @param dfd is the agent descriptor
   */
public void postRegisterEvent(Object source, String parentName, AgentManagementOntology.DFAgentDescriptor dfd) {
  
  sendRequestForAction(getLocalName(),AgentManagementOntology.DFAction.REGISTER, dfd);
    
}

  /**
   * deregisters an agent descriptor with the DF
   * @param parentName is not used
   * @param dfd is the agent descriptor
   */
public void postDeregisterEvent(Object source, String parentName, AgentManagementOntology.DFAgentDescriptor dfd) {
  
	if(getLocalName().equalsIgnoreCase(parentName))
		//deregister the an agent from the df
	  sendRequestForAction(getLocalName(),AgentManagementOntology.DFAction.DEREGISTER, dfd);
  else
  {//deregister thd df from a parent
    gui.showStatusMsg("Waiting...process request");
     
  	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
    msg.addDest(getLocalName());
    msg.setOntology("jade-extensions");
    msg.setProtocol("fipa-request");
    msg.setContent("(action df (DEREGISTER_FROM "+ parentName +"))");
    sendMessage(msg.toString());
    //System.out.println(msg.toString());
     
    try
    {
     	ACLMessage reply = receiveMessage(); 
      //System.out.println(msg.toString());

      if (ACLMessage.AGREE == (reply.getPerformative())) 
      {
     	  reply = receiveMessage();
        if (! (ACLMessage.INFORM == reply.getPerformative())) 
          gui.showStatusMsg("Deregistration unsucceeded because received"+ reply.toString());
        else
        {
           gui.refreshFederation();
           gui.showStatusMsg("Deregistration succeeded");
        }
       }
       else
         gui.showStatusMsg("Deregistration unsucceeded because received"+ reply.toString());
     }catch(jade.lang.acl.ParseException e){
     	gui.showStatusMsg("Deregistration unsucceeded because "+e.getMessage());}

  }
}
/**
Modify the descriptor of an agent
*/
  public void postModifyEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd) {
    
    sendRequestForAction(getLocalName(),AgentManagementOntology.DFAction.MODIFY,dfd);
  
  }

  /**
   * Returns all the agent descriptors registered with the DF.
   * So far, there is a bug and only the agents in "active" state are
   * returned.
   */
public Enumeration getAllDFAgentDsc() {
  AgentManagementOntology.DFSearchResult found = new AgentManagementOntology.DFSearchResult();
  try{
  	AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
    dfd.setDFState("active");
    AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
    c.setName(AgentManagementOntology.Constraint.DFDEPTH);
    c.setFn(AgentManagementOntology.Constraint.MAX);
    c.setArg(1);
    Vector constraint = new Vector();
    constraint.add(c);
    
    found = sendRequestForSearch(getLocalName(),dfd,constraint);
	  return found.elements();
  }catch(FIPAException e){
  	gui.showStatusMsg("Search unsucceeded because "+ e.getMessage());
  }
  
	return (new Hashtable()).elements();
}
	

/**
Returns the agent descriptor of the agent with the given name  
*/  
public AgentManagementOntology.DFAgentDescriptor getDFAgentDsc(String name) throws FIPAException {
	
	AgentManagementOntology.DFAgentDescriptor out = null;
	try{
		AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
    dfd.setName(name);
	  AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
    c.setName(AgentManagementOntology.Constraint.DFDEPTH);
    c.setFn(AgentManagementOntology.Constraint.MAX);
    c.setArg(1);
    Vector constraint = new Vector();
    constraint.add(c);
    AgentManagementOntology.DFSearchResult found = sendRequestForSearch(getLocalName(),dfd,constraint);
    Enumeration e = found.elements();
    out = (AgentManagementOntology.DFAgentDescriptor)e.nextElement();
	}catch(FIPAException e){
		gui.showStatusMsg("Search unsucceeded because "+ e.getMessage());
	}
	return out;
}
  
  
  /**
  Finds all the agent descriptors that match the given agent descriptor
  */
  public void postSearchEvent(Object source, String parentname, AgentManagementOntology.DFAgentDescriptor dfd)
  {
    try 
  	  {
      
  		  AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
        c.setName(AgentManagementOntology.Constraint.DFDEPTH);
        c.setFn(AgentManagementOntology.Constraint.MAX);
        c.setArg(1);
        Vector constraints = new Vector();
        constraints.add(c);
        AgentManagementOntology.DFSearchResult found = sendRequestForSearch(parentname,dfd,constraints);
        gui.refreshLastSearch(found.elements()); 
     } catch (FIPAException e1) { 
      gui.showStatusMsg("Search unsucceeded because "+ e1.getMessage()); 
      }
   
  }
  
  
  /*
  Returns the agent descriptor of an agent result of a search operation (these results are maintented in a private variable).
  */
  public AgentManagementOntology.DFAgentDescriptor getDFAgentSearchDsc(String value) throws FIPAException
  {
  	AgentManagementOntology.DFAgentDescriptor out = null;
  	Enumeration el = found.elements();
  	while (el.hasMoreElements())
  	{
  	  	AgentManagementOntology.DFAgentDescriptor dfd = (AgentManagementOntology.DFAgentDescriptor)el.nextElement();
  	  	if(dfd.getName().equalsIgnoreCase(value))
  	  		out = dfd;  	  	
  	}
  	if(out == null)
  		throw new FIPAException("No agent found");
    return out;
  }
  
  /*
  This method allows the applet to required the df to federate with another df
  */
  public void postFederateEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd)
  {	
     gui.showStatusMsg("Waiting...process request");
     
  	 ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
     msg.addDest(getLocalName());
     msg.setOntology("jade-extensions");
     msg.setProtocol("fipa-request");
     msg.setContent("(action df (FEDERATE_WITH "+ dfName +"))");
     sendMessage(msg.toString());
     //System.out.println(msg.toString());
     
     try
     {
     	 ACLMessage reply = receiveMessage(); 
       //System.out.println(msg.toString());

       if (ACLMessage.AGREE == (reply.getPerformative())) 
       {
     	   reply = receiveMessage();
         if (! (ACLMessage.INFORM == reply.getPerformative())) 
           gui.showStatusMsg("Federation unsucceeded because received"+ reply.toString());
         else
           {
           	gui.refreshFederation();
           	gui.showStatusMsg("Federation succeeded");
           }
       }
       else
         gui.showStatusMsg("Federation unsucceeded because received"+ reply.toString());
     }catch(jade.lang.acl.ParseException e){
     	gui.showStatusMsg("Federation unsucceeded because "+e.getMessage());}
  }
  
  /*
  return the constraints used by the search with constraint feature
  */
  public Vector getConstraints()
  { 
  	return searchConstraint;
  }
  
  //This methods send a request to the df to know the name of the parent with which he is federated.
  public Enumeration getParents()
  {  
  	Vector out = new Vector();
  	try
  	{
  		gui.showStatusMsg("Waiting...process request");
  		
  		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
      msg.addDest(getLocalName());
      msg.setOntology("jade-extensions");
      msg.setProtocol("fipa-request");
      msg.setContent("(action "+ getLocalName() +" (GETPARENTS))");
      sendMessage(msg.toString());
      //System.out.println(msg.toString());
      
      ACLMessage reply = receiveMessage(); 
      
      if (ACLMessage.AGREE == (reply.getPerformative())) 
      {
        reply=receiveMessage();
        
        if (! (ACLMessage.INFORM == reply.getPerformative())) 
      	  gui.showStatusMsg("Search unsucceeded because received "+msg.toString());
        else 
        { 
        	StringReader textIn = new StringReader(reply.getContent());
      	  AgentManagementOntology.DFSearchResult found = AgentManagementOntology.DFSearchResult.fromText(textIn);
      	  
      	  Enumeration el = found.elements();
      	  while(el.hasMoreElements())
      	  {
      	  
      	  	AgentManagementOntology.DFAgentDescriptor a = (AgentManagementOntology.DFAgentDescriptor) el.nextElement();
      	  	out.addElement(a.getName());
      	  }
      	  gui.showStatusMsg("Search succeeded");
        }
     }
     else
      gui.showStatusMsg("Search with DF unsucceeded because received "+ reply.toString());
  	}catch(FIPAException e){
  		gui.showStatusMsg("Search unsucceeded because "+e.getMessage());
  	}catch(jade.domain.ParseException e1) { 
      gui.showStatusMsg("Search unsucceeded because "+e1.getMessage()); 
  	}catch(jade.lang.acl.ParseException e2){
  		gui.showStatusMsg("Search unsucceeded because "+e2.getMessage());}

  	return out.elements();
  }
  
  
  /** 
  This method requires the df the description of the children (df-agent register with him) 
  */
  public Enumeration getChildren()
  { 
    Vector out = new Vector();   
  	
    AgentManagementOntology.DFAgentDescriptor dfd = new AgentManagementOntology.DFAgentDescriptor();
    dfd.setType("fipa-df");
      
    AgentManagementOntology.Constraint c = new AgentManagementOntology.Constraint();
    c.setName(AgentManagementOntology.Constraint.DFDEPTH);
    c.setFn(AgentManagementOntology.Constraint.MAX);
    c.setArg(1);
    
    Vector constraints = new Vector();
    constraints.add(c);
    
    AgentManagementOntology.DFSearchResult found = sendRequestForSearch(getLocalName(),dfd,constraints);
      
    try{
    	Enumeration en = found.elements();
      while (en.hasMoreElements())
      {
        AgentManagementOntology.DFAgentDescriptor desc = (AgentManagementOntology.DFAgentDescriptor) en.nextElement();
        out.add(desc.getName());
     }
    }catch (FIPAException e){
    	gui.showStatusMsg("Search unsucceeded because "+ e.getMessage());
    }
      
    return out.elements();
}
 /*
 this method allows the applet to make a search specifing the constraints to use
 */
  
 public void postSearchWithConstraintEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd, Vector constraint)
  {
  	try
  	{
  	  searchConstraint = constraint;
  		AgentManagementOntology.DFSearchResult found = sendRequestForSearch(dfName,dfd,constraint);
      gui.refreshLastSearch(found.elements());
  	}catch(FIPAException e){
  		  gui.showStatusMsg("Search unsucceeded because "+ e.getMessage());
  	  }
  }
  
  /*
  this method requires the df his description.
  */
  public AgentManagementOntology.DFAgentDescriptor getDescriptionOfThisDF()
  {
  	if (thisDF == null)
  	{
  		  //initialization the variable thisDF
  			try
  	    {
  		    gui.showStatusMsg("Waiting...process request to DF");
  	    	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
          msg.addDest(getLocalName());
          msg.setOntology("jade-extensions");
          msg.setProtocol("fipa-request");
          msg.setContent("(action "+ getName() + " (GETDEFAULTDESCRIPTION))");
          sendMessage(msg.toString());
          
          ACLMessage reply = receiveMessage(); 
          if (ACLMessage.AGREE == (reply.getPerformative())) 
          {
            reply = receiveMessage();
            //System.out.println("Received the message" + reply.toString());
            if (! (ACLMessage.INFORM == reply.getPerformative())) 
      	      gui.showStatusMsg("Request unsucceeded because received "+ reply.toString());
            else 
            {
        	   StringReader textIn = new StringReader(reply.getContent());
      	     thisDF = AgentManagementOntology.DFAgentDescriptor.fromText(textIn);
      	     gui.showStatusMsg("Request succeeded");
            }
          }
          else
           gui.showStatusMsg("Request unsucceeded because received "+ reply.toString());
  	    }catch(jade.domain.ParseException e1) { 
         gui.showStatusMsg("Request unsucceeded because "+e1.getMessage()); 
  	    }catch(jade.lang.acl.ParseException e2){
  		   gui.showStatusMsg("Request unsucceeded because "+e2.getMessage());
  	  }
  	}	
  	
  	return thisDF;
  }

  // This method send a request for a search to the stated df according to the dfd and the constraints and returns the agent description found 
  private AgentManagementOntology.DFSearchResult sendRequestForSearch(String dfName, AgentManagementOntology.DFAgentDescriptor dfd, Vector c)
  {
  	Vector out = new Vector();
    String warning = "";
  	
    // only search on  the DF allowed
    
    if(!(getLocalName().equalsIgnoreCase(dfName)))
    	{
    		warning = "WARNING:Only search on itself allowed";
    		dfName = getLocalName();
    	}
    	
  	try 
  	{
      gui.showStatusMsg("Waiting...process request");
      
  		StringWriter textOut = new StringWriter();
      ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
      request.addDest(dfName);
      request.setOntology("fipa-agent-management");
      request.setLanguage("SL0");
      request.setProtocol("fipa-request");
      AgentManagementOntology.DFSearchAction dfsa = new AgentManagementOntology.DFSearchAction();
      dfsa.setActor(dfName);
      dfsa.setArg(dfd);
      
      Enumeration el = c.elements();
      while(el.hasMoreElements())
      {
        	AgentManagementOntology.Constraint con = (AgentManagementOntology.Constraint)el.nextElement();
        	dfsa.addConstraint(con);
      }
      
      dfsa.toText(textOut);
      
      request.setContent(textOut.toString());
      //System.out.println(request.toString());
      sendMessage(request.toString());
      
      ACLMessage msg=receiveMessage(); 
      
      //System.out.println(msg.toString());
      
      if (ACLMessage.AGREE == (msg.getPerformative())) 
      {
        msg=receiveMessage();
        
        if (! (ACLMessage.INFORM == msg.getPerformative())) 
      	  gui.showStatusMsg("Search unsucceeded because received "+msg.toString());
         
	
        else {
      	  
        	StringReader textIn = new StringReader(msg.getContent());
      	  found = AgentManagementOntology.DFSearchResult.fromText(textIn);
      	  StringBuffer st = new StringBuffer(warning);
          st.append(" Search succeeded");
      	  gui.showStatusMsg(st.toString());
        } 
      } 
      else 
        gui.showStatusMsg("Search with DF unsucceeded because received "+msg.toString());
  	  } catch (ParseException e) { 
        gui.showStatusMsg("Search unsucceeded because "+e.getMessage()); 
  	  } catch (jade.domain.ParseException e2) { 
        gui.showStatusMsg("Search unsucceeded because "+e2.getMessage()); 
  	  }
  	  return found;
  }
  
  /*
  this method can be used to send a request for a generic action to the df
  */
  private void sendRequestForAction(String dfName,String action,AgentManagementOntology.DFAgentDescriptor dfd)
  {
  	try
  	{
  		gui.showStatusMsg("Waiting....process request");
  		
  		StringWriter textOut = new StringWriter();
      ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
      request.addDest(dfName);
      request.setOntology("fipa-agent-management");
      request.setLanguage("SL0");
      request.setProtocol("fipa-request");
      AgentManagementOntology.DFAction dfa = new AgentManagementOntology.DFAction();
      dfa.setName(action);
      dfa.setActor(dfName);
      dfa.setArg(dfd);
    
      dfa.toText(textOut);
      
      request.setContent(textOut.toString());
      
      //System.out.println(request.toString());
      sendMessage(request.toString());
      
      ACLMessage msg=receiveMessage(); 
    
      if (ACLMessage.AGREE == (msg.getPerformative())) 
      {
        msg=receiveMessage();
         
        if (! (ACLMessage.INFORM == msg.getPerformative())) 
      	  gui.showStatusMsg(action + " unsucceeded because received "+msg.toString());
        
        else 
        if( !(AgentManagementOntology.DFAction.MODIFY.equalsIgnoreCase(action)))
          {
          	gui.refresh();
          	gui.showStatusMsg(action + " succeeded");
          }
      } 
      else 
        gui.showStatusMsg(action + " unsucceeded because received "+msg.toString());
  	}catch (ParseException e) { 
        gui.showStatusMsg(action +" unsucceeded because "+e.getMessage()); 
  	  } 

  }

}