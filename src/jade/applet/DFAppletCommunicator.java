package jade.applet;

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
   * This method allows this class to call the method showErrorMsg implemented
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
    gui.showErrorMsg("Receiving a message");
    return parser.Message();
   }

  /**
   * shows the message not authorized and does nothing.
   */
   public void doDelete() {
     gui.showErrorMsg("Operation not authorized");
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

  /**
   * registers an agent descriptor with the DF
   * @param parentName is not used
   * @param dfd is the agent descriptor
   */
public void postRegisterEvent(String parentName, AgentManagementOntology.DFAgentDescriptor dfd) {
  try {
    StringWriter w = new StringWriter();
    w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action DF (register (:df-description ");
    dfd.toText(w);
    w.write("))))");
    //System.out.println(w.toString());
    sendMessage(w.toString());
    ACLMessage msg=receiveMessage();
    //System.err.println("Received the message"+msg.toString());
    if ("agree".equalsIgnoreCase(msg.getType())) {
      msg=receiveMessage();
      //System.err.println("Received the message"+msg.toString());
      if (! "inform".equalsIgnoreCase(msg.getType())) 
	gui.showErrorMsg("Register unsucceeded because received "+msg.getType()+" "+msg.getContent());
    } else gui.showErrorMsg("Register unsucceeded because received "+msg.getType()+" "+msg.getContent());
  } catch (ParseException e) {
    gui.showErrorMsg("Register unsucceeded because "+e.getMessage());
  }
}

  /**
   * deregisters an agent descriptor with the DF
   * @param parentName is not used
   * @param dfd is the agent descriptor
   */
public void postDeregisterEvent(String parentName, AgentManagementOntology.DFAgentDescriptor dfd) {
  try {
    StringWriter w = new StringWriter();
    w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action DF (deregister  (:df-description ");
    dfd.toText(w);
    w.write("))))");
    //System.out.println(w.toString());
    sendMessage(w.toString());
    ACLMessage msg=receiveMessage(); 
    //System.err.println("Received the message"+msg.toString());
    if ("agree".equalsIgnoreCase(msg.getType())) {
      msg=receiveMessage();
      //System.err.println("Received the message"+msg.toString());
      if (! "inform".equalsIgnoreCase(msg.getType())) 
	gui.showErrorMsg("Deregister unsucceeded because received "+msg.getType()+" "+msg.getContent());
    } else gui.showErrorMsg("Deregister unsucceeded because received "+msg.getType()+" "+msg.getContent());
  } catch (ParseException e) {
    gui.showErrorMsg("Deregister unsucceeded because "+e.getMessage());
  }
    
}

  /**
   * returns all the agent descriptors registered with the DF.
   * So far, there is a bug and only the agents in "active" state are
   * returned.
   */
public Enumeration getDFAgentDescriptors() {
  try {
    StringWriter w = new StringWriter();
    w.write("(request :receiver df :ontology fipa-agent-management :language SL0 :protocol fipa-request :content (action DF (search (:df-description (:df-state active)) (:df-depth Max 1)))) "); // FIXME ricevo solo gli agenti active
    //System.out.println(w.toString());
    sendMessage(w.toString());
    ACLMessage msg=receiveMessage(); 
    //System.err.println("Received the message"+msg.toString());
    if ("agree".equalsIgnoreCase(msg.getType())) {
      msg=receiveMessage();
      //System.err.println("Received the message"+msg.toString());
      if (! "inform".equalsIgnoreCase(msg.getType())) 
	gui.showErrorMsg("Search unsucceeded because received "+msg.getType()+" "+msg.getContent());
      else {
	StringReader textIn = new StringReader(msg.getContent());
	AgentManagementOntology.DFSearchResult found = AgentManagementOntology.DFSearchResult.fromText(textIn);
	return found.elements();
      }
    } else gui.showErrorMsg("Search with DF unsucceeded because received "+msg.getType()+" "+msg.getContent());
  } catch (ParseException e) { 
    gui.showErrorMsg("Search unsucceeded because "+e.getMessage()); 
  } catch (FIPAException e1) { 
    gui.showErrorMsg("Search unsucceeded because "+ e1.getMessage()); 
  } catch (jade.domain.ParseException e2) { 
    gui.showErrorMsg("Search unsucceeded because "+e2.getMessage()); 
  }
  return (new Hashtable()).elements();
}


}


