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
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

/**
 * This class is used by DFApplet in order to communicate with the DF agent
 * via a socket connection. The socket server is implemented by the agent
 * jade.tools.SocketProxyAgent.
 * @see jade.tools.SocketProxyAgent.SocketProxyAgent
 * @author Fabio Bellifemine - CSELT - 25/8/1999
 */
public class DFAppletCommunicator implements GUI2DFCommunicatorInterface {
  private Applet a;
  private DataInputStream in;
  private PrintStream out;
  private final static int DEFAULT_PORT = 6789;
  private ACLParser parser;
  private DFGUI gui;
  private String address;

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
    gui.showStatusMsg("Receiving a message");
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
  return "df@"+address; 
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

  /**
   * registers an agent descriptor with the DF
   * @param parentName is not used
   * @param dfd is the agent descriptor
   */
public void postRegisterEvent(Object source, String parentName, AgentManagementOntology.DFAgentDescriptor dfd) {
  try {
    StringWriter w = new StringWriter();
    w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action DF (register (:df-description ");
    dfd.toText(w);
    w.write("))))");
    System.out.println(w.toString());
    sendMessage(w.toString());
    ACLMessage msg=receiveMessage();
    System.err.println("Received the message"+msg.toString());
    if (ACLMessage.AGREE ==(msg.getPerformative())) {
      msg=receiveMessage();
      System.err.println("Received the message"+msg.toString());
      if (! (ACLMessage.INFORM ==(msg.getPerformative()))) 
      	gui.showStatusMsg("Register unsucceeded because received "+msg.toString());
	
    gui.refresh();  	
    } else gui.showStatusMsg("Register unsucceeded because received "+msg.toString());
  } catch (ParseException e) {
    gui.showStatusMsg("Register unsucceeded because "+e.getMessage());
  }
}

  /**
   * deregisters an agent descriptor with the DF
   * @param parentName is not used
   * @param dfd is the agent descriptor
   */
public void postDeregisterEvent(Object source, String parentName, AgentManagementOntology.DFAgentDescriptor dfd) {
  try {
    StringWriter w = new StringWriter();
    w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action DF (deregister  (:df-description ");
    dfd.toText(w);
    w.write("))))");
    sendMessage(w.toString());
    ACLMessage msg=receiveMessage(); 
    
    if (ACLMessage.AGREE == msg.getPerformative()) {
      msg=receiveMessage();
      
      if (! (ACLMessage.INFORM == msg.getPerformative())) 
      	gui.showStatusMsg("Deregister unsucceeded because received "+msg.toString());
    gui.refresh();
    } else gui.showStatusMsg("Deregister unsucceeded because received "+msg.toString());
  } catch (ParseException e) {
    gui.showStatusMsg("Deregister unsucceeded because "+e.getMessage());
  }
    
}
/**
Modify the descriptor of an agent
*/
  public void postModifyEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd) {
    
  try {
    StringWriter w = new StringWriter();
    w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action df (modify  (:df-description ");
    dfd.toText(w);
    w.write(" ))))");
    System.out.println(w.toString());
    sendMessage(w.toString());
    ACLMessage msg=receiveMessage(); 
    //System.err.println("Received the message"+msg.toString());
    if (ACLMessage.AGREE ==msg.getPerformative()) {
      msg=receiveMessage();
      //System.err.println("Received the message"+msg.toString());
      if (! (ACLMessage.INFORM ==msg.getPerformative())) 
      	gui.showStatusMsg("Modify unsucceeded because received "+msg.toString());
    } else 
    gui.showStatusMsg("Modify unsucceeded because received "+msg.toString());
  } catch (ParseException e) {
    gui.showStatusMsg("Modify unsucceeded because "+e.getMessage());
  }	
  	
  	//gui.showStatusMsg("Modify not yet implemented via applet");
  }

  /**
   * returns all the agent descriptors registered with the DF.
   * So far, there is a bug and only the agents in "active" state are
   * returned.
   */
public Enumeration getAllDFAgentDsc() {
  try {
    StringWriter w = new StringWriter();
    w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action DF (search (:df-description (:df-state active)) (:df-depth Max 1)))) "); // FIXME ricevo solo gli agenti active
    //System.out.println(w.toString());
    sendMessage(w.toString());
    ACLMessage msg=receiveMessage(); 
    //System.err.println("Received the message"+msg.toString());
    if (ACLMessage.AGREE ==(msg.getPerformative())) {
      msg=receiveMessage();
      //System.err.println("Received the message"+msg.toString());
      if (! (ACLMessage.INFORM == msg.getPerformative())) 
	gui.showStatusMsg("Search unsucceeded because received "+msg.toString());
      else {
	StringReader textIn = new StringReader(msg.getContent());
	AgentManagementOntology.DFSearchResult found = AgentManagementOntology.DFSearchResult.fromText(textIn);
	return found.elements();
      }
    } else gui.showStatusMsg("Search with DF unsucceeded because received "+msg.toString());
  } catch (ParseException e) { 
    gui.showStatusMsg("Search unsucceeded because "+e.getMessage()); 
  } catch (FIPAException e1) { 
    gui.showStatusMsg("Search unsucceeded because "+ e1.getMessage()); 
  } catch (jade.domain.ParseException e2) { 
    gui.showStatusMsg("Search unsucceeded because "+e2.getMessage()); 
  }
  return (new Hashtable()).elements();
}

/**
Returns the agent descriptor of the agent with the given name  
*/  
public AgentManagementOntology.DFAgentDescriptor getDFAgentDsc(String name) throws FIPAException {
    
  	try{
  	
  		StringWriter w = new StringWriter();
   	  w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action DF (search (:df-description (:agent-name "+ name + " )) (:df-depth Max 1)))) ");
  		sendMessage(w.toString());
  		ACLMessage msg = receiveMessage();
  		if(ACLMessage.AGREE == (msg.getPerformative()))
  		{
  			msg = receiveMessage();
  			if(! (ACLMessage.INFORM ==(msg.getPerformative())))
  				gui.showStatusMsg("Search unsucceeded because received "+ msg.toString());
  				else
  				{
  					StringReader textIn = new StringReader(msg.getContent());
  					AgentManagementOntology.DFSearchResult found =  AgentManagementOntology.DFSearchResult.fromText(textIn);
  				
  					Enumeration e = found.elements();
  					AgentManagementOntology.DFAgentDescriptor out = (AgentManagementOntology.DFAgentDescriptor)e.nextElement();
  				
  					return out;
  				  
  				}
  		
  		}
  		else gui.showStatusMsg("Search unsucceeded because received "+ msg.toString());  		
  			
  	}catch(ParseException e ){
  		gui.showStatusMsg("Search unsucceeded because received "+ e. getMessage());
  	}catch(FIPAException e1){
  		gui.showStatusMsg("Search unsucceeded because received "+ e1. getMessage());
  	}catch(jade.domain.ParseException e2){
  			gui.showStatusMsg("Search unsucceeded because received "+ e2. getMessage());
  	
  	}
  
    return null;
  }
  
  /**
  Da riscrivere non esiste più dovrò scrivere postsearchaction
  Finds all the agent descriptors that match the given agent descriptor
  */
  public void postSearchEvent(Object source, String parentname, AgentManagementOntology.DFAgentDescriptor dfd)
  {
  
  	try 
  	{
    StringWriter w = new StringWriter();
    // FIXME: to use this method for search with other df it's necessary the address; parentname infact is only  df@null so it can be used
    w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action DF (search (:df-description "); 
    dfd.toText(w);
    w.write (")");
    w.write ("(:df-depth Max 1)");
    w.write(")))");
    System.out.println(w.toString());
    sendMessage(w.toString());
    ACLMessage msg=receiveMessage(); 
    //System.err.println("Received the message"+msg.toString());
    if (ACLMessage.AGREE == (msg.getPerformative())) {
      msg=receiveMessage();
      //System.err.println("Received the message"+msg.toString());
      if (! (ACLMessage.INFORM == msg.getPerformative())) 
      	gui.showStatusMsg("Search unsucceeded because received "+msg.toString());
      else {
      	StringReader textIn = new StringReader(msg.getContent());
      	AgentManagementOntology.DFSearchResult found = AgentManagementOntology.DFSearchResult.fromText(textIn);
        gui.refreshLastSearch(found.elements()); 
      }
    } 
    else 
    gui.showStatusMsg("Search with DF unsucceeded because received "+msg.toString());
  	} catch (ParseException e) { 
    gui.showStatusMsg("Search unsucceeded because "+e.getMessage()); 
  	} catch (FIPAException e1) { 
    gui.showStatusMsg("Search unsucceeded because "+ e1.getMessage()); 
  	} catch (jade.domain.ParseException e2) { 
    gui.showStatusMsg("Search unsucceeded because "+e2.getMessage()); 
  	}
  }
  
  //FIXME these methods must be implemented
  public AgentManagementOntology.DFAgentDescriptor getDFAgentSearchDsc(String value) {return null;}
  
  public void postFederateEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd){}
  public Enumeration getChildren(){return null;}
  public Vector getConstraints(){ return null;}
  public Enumeration getParents(){return null;}
  public void postSearchWithConstraintEvent(Object source, String dfName, AgentManagementOntology.DFAgentDescriptor dfd, Vector constraint){}
  public AgentManagementOntology.DFAgentDescriptor getDescriptionOfThisDF(){return null;}

}