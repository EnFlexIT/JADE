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


package jade.tools.SocketProxyAgent;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.*;
import jade.core.behaviours.SimpleBehaviour;

import java.net.*;
import java.io.*;
import java.util.*;

/**
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

public class SocketProxyAgent extends Agent {

PrintWriter logFile;
BufferedReader in;
Server s;

protected void setup() {
  try {
  
    in = new BufferedReader(new FileReader(getLocalName()+".inf"));
    logFile = new PrintWriter(new FileWriter(getLocalName()+".log",true));
    int portNumber = Integer.parseInt(in.readLine());
    Vector agentNames = new Vector();
    StringTokenizer st = new StringTokenizer(in.readLine());
        
    //verify if the name of the agents have the hap or not. 
    //If not add the local hap (of the dfproxy agent).
    while (st.hasMoreTokens())
    { 
      String name = st.nextToken();
    	int atPos = name.lastIndexOf('@');
    	if(atPos == -1)
    		name = name + "@" + getHap();
   	
    	agentNames.add(name);
    }
		
    s = new Server(portNumber, this, agentNames);
  } catch(Exception e) {
    System.err.println(getLocalName()+" NEEDS THE FILE "+getLocalName()+".inf IN THE WORKING DIRECTORY WITH ITS PARAMETERS (port number and agent names)");
    e.printStackTrace();
    doDelete();
  } 

}

protected void takeDown(){
  try {
    if (in != null) 
      in.close();
  } catch (Exception e) {}

  try {
    if (logFile != null) 
      logFile.close();
  } catch (Exception e) {}

  try {
    if (s != null) 
      s.stop();
  } catch (Exception e) {}
}

public synchronized void log(String str) {
  
  	String tmp = (new Date()).toString() + " - "+ str; 
    logFile.println(tmp);
    logFile.flush();
}
}




class Server extends Thread {
  private final static int DEFAULT_PORT = 6789;
  private ServerSocket listen_socket; 
  private Agent myAgent;
  private Vector myOnlyReceivers;

  /**
   * Constructor of the class.
   * It creates a ServerSocket to listen for connections on.
   * @param port is the port number to listen for. If 0, then it uses
   * the default port number.
   * @param a is the pointer to agent to be used to send messages.
   * @param receiver is the vector with the names of all the agents that 
   * wish to receive messages through this proxy.
   */
  Server(int port, Agent a, Vector receivers) {
    if (port == 0) port = DEFAULT_PORT;
    myAgent = a;
    myOnlyReceivers = receivers;
    try { listen_socket = new ServerSocket(port); }
    catch (IOException e) {e.printStackTrace(); myAgent.doDelete(); return;}
    String str = myAgent.getLocalName()+" is listening on port "+port+" to proxy messages to agents (";
    StringBuffer st = new StringBuffer(str);
    
    for (int i=0; i<myOnlyReceivers.size(); i++)
    {
      st.append((String)myOnlyReceivers.elementAt(i));
      st.append(" "); 
    }
    st.append(")");
    System.out.println(st.toString());
    ((SocketProxyAgent)myAgent).log(st.toString());
    start();
  }

  Socket client_socket;
  Connection c;

  /**
   * The body of the server thread. It is executed when the start() method
   * of the server object is called.
   * Loops forever, listening for and accepting connections from clients.
   * For each connection, creates a Connection object to handle communication
   * through the new Socket. Each Connection object is a new thread.
   * The maximum queue length for incoming connection indications 
   * (a request to connect) is set to 50 (that is the default for the
   * ServerSocket constructor). If a connection indication
   * arrives when the queue is full, the connection is refused. 
   */
  public void run() {
    try {
      while (true) {
	client_socket = listen_socket.accept();
	((SocketProxyAgent)myAgent).log("\nNew Connection with "+client_socket.getInetAddress().toString()+" on remote port "+client_socket.getPort());
	c = new Connection(client_socket,myAgent,myOnlyReceivers);
      }
    } catch (IOException e) {e.printStackTrace(); myAgent.doDelete();}
  }

protected void finalize() {
  try {
    if (listen_socket != null)
      listen_socket.close();
  } catch (Exception e) {}
  try {
    if (client_socket != null)
      client_socket.close();
  } catch (Exception e) {}
  try {
    if (c != null)
      c.stop();
  } catch (Exception e) {}
}
}

class Connection extends Thread {
  private Agent myAgent;
  private Socket client;
  private DataInputStream in;
  private PrintStream out;
  
  /** Name of the agents who intend to receive any message from this agent */
  private Vector myOnlyReceivers; 


  Connection(Socket client_socket, Agent a, Vector receivers) {
    myAgent = a;
    client = client_socket;
    myOnlyReceivers = receivers;
    try {
      in = new DataInputStream(client.getInputStream());
      out = new PrintStream(client.getOutputStream(),true);
    } catch (IOException e) {
      try { client.close();} catch (IOException e2) {}
      e.printStackTrace();
      return;
    }
    start();
  }

  private boolean myOnlyReceiversContains(Iterator aids) {
    List l = new ArrayList();
    while (aids.hasNext()){
    	//if the name of the receiver has not the hap, it's local and the hap is added.
      String name = ((AID)aids.next()).getName();
      
      int atPos = name.lastIndexOf('@');
    	if(atPos == -1)
    		name = name + "@" + myAgent.getHap();

    	l.add(name);  
    
    }
    for (int i=0; i<myOnlyReceivers.size(); i++)
      if (l.contains(myOnlyReceivers.elementAt(i)))
	      return true;  
    return false;
  }

  public void run() {
    String line;
    try {
      ACLParser parser = new ACLParser(in);
      ACLMessage msg;
      while (true) {
      	msg = parser.Message(); // ACLMessage.fromText(new InputStreamReader(in)); 
     
      	if (myOnlyReceiversContains(msg.getAllReceiver())) 
      	{
      		msg.setSender(myAgent.getAID());
	        if ((msg.getReplyWith() == null) || (msg.getReplyWith().length()<1))
	          msg.setReplyWith(myAgent.getLocalName()+"."+getName()+"."+java.lang.System.currentTimeMillis()); 
	        myAgent.send(msg);
	        myAgent.addBehaviour(new WaitAnswersBehaviour(myAgent,msg,out));
	      } 
	      else 
	      { 
	        out.println("(refuse :content unauthorised)");
	        close(null); 
	        return; 
	      }
      }
    } catch(ParseException e) {
      close(e); return;
    }
  }

  void close(Exception e){
  try { client.close(); } catch (IOException e2) {}
    e.printStackTrace();
  }

protected void finalize() {
  try {
    if (client != null)
      client.close();
  } catch (Exception e) {}
  try {
    if (in != null)
      in.close();
  } catch (Exception e) {}
  try {
    if (out != null)
      out.close();
  } catch (Exception e) {}
}
}

class WaitAnswersBehaviour extends SimpleBehaviour {

  ACLMessage msg;
  PrintStream out;
  long timeout,blockTime,endingTime;
  final static long DEFAULT_TIMEOUT = 600000; // 10 minutes
  boolean finished;
  MessageTemplate mt;

  WaitAnswersBehaviour(Agent a, ACLMessage m, PrintStream o) {
    super(a);
    out = o;
    try {
      mt = MessageTemplate.and(MessageTemplate.MatchSender((AID)m.getAllReceiver().next()),MessageTemplate.MatchInReplyTo(m.getReplyWith()));
    } catch (Exception e) {
      mt = MessageTemplate.MatchInReplyTo(m.getReplyWith());
    }
    Date d = m.getReplyByDate();
    if(d != null)
      timeout = d.getTime() - (new Date()).getTime();
    
    if (timeout <= 1000) 
    	timeout = DEFAULT_TIMEOUT; 
    endingTime = System.currentTimeMillis() + timeout;
    finished = false;
  }

   public void action() {
     msg = myAgent.receive(mt);
     if (msg == null) {
       blockTime = endingTime - System.currentTimeMillis();
       if (blockTime <= 0) finished=true;
       else	           block(blockTime);
       return;
     }
     out.println(msg.toString());
     //System.err.println(myAgent.getLocalName()+" sent "+msg.toString());
   }

   public boolean done() {
     return finished;
   }

    
}
