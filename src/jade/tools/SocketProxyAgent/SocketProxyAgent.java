
/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */
package jade.tools.SocketProxyAgent;

import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.*;
import jade.core.behaviours.SimpleBehaviour;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Modified version of Jade's SocketProxyAgent to correct bug and
 * extend functionality. This will be contributed back to the
 * Jade development team.
 * @author Fabio Bellifemine - CSELT S.p.A
 * @author (HP modifications) Dick Cowan, David Bell, Sebastien Siva
 */
public class SocketProxyAgent extends Agent
{
    protected final static int DEFAULT_PORT = 6789;
    
    PrintStream logFile;
    BufferedReader in;
    Server proxyServer;

    protected void setup()
    {
        try
	{
            logFile = new PrintStream(new FileOutputStream(getLocalName() + ".log",
                                                           true));
            log("----------------  Startup "
                  + (new GregorianCalendar().getTime())
                  + "  ----------------"
               );
            log("My agent name:" + getLocalName()); 
            log("Opening configuration file:\""
                               + getLocalName() + ".inf\"");

            int portNumber  = DEFAULT_PORT;
            Vector agentNames = new Vector();
            try {
                in = new BufferedReader(new FileReader(getLocalName() + ".inf"));
                portNumber = Integer.parseInt(in.readLine());
                StringTokenizer st = new StringTokenizer(in.readLine());

                //verify if the name of the agents have the hap or not. 
                //If not add the local hap (of the dfproxy agent).
                while (st.hasMoreTokens()) {
                    String name = st.nextToken();
                    if (!name.equals("*")) {
                        int atPos = name.lastIndexOf('@');

                        if (atPos == -1) {
                            name = name + "@" + getHap();
                        }
                    }
                    log("Legal addressee:" + name);
                    agentNames.add(name);
                }
            }
            catch(Exception e)
            {
                log("Unable to read file "+getLocalName() +".inf, so will use default settings.");
                portNumber = DEFAULT_PORT;       // Force Server to use its default port
                agentNames.add("*");  // Allow messages to any agent.
            }
            log("Attempting to open a server socket on port: " + portNumber);
            proxyServer = new Server(portNumber, this, agentNames);
        }
	catch (Exception e) {
            log("Failed to start server socket" + e);
            e.printStackTrace();
            doDelete();
        }
    } // END setup()

    protected void takeDown()
    {
        try
	{
            if (in != null) {
                in.close();
                in = null;
            }
        }
	catch (Exception e) {}

        try
	{
            if (logFile != null) {
                logFile.close();
                logFile = null;
            }
        }
	catch (Exception e) {}

        try
	{
            if (proxyServer != null) {
                proxyServer.closeDown();
                proxyServer.join(1000);
                proxyServer = null;
            }
        }
	catch (Exception e) {}
    } // END takeDown()

    /**
     * Method log
     *
     *
     * @param str
     *
     */
    public synchronized void log(String str) {
        logFile.println(str);
        logFile.flush();
    }
} // END class SocketProxyAgent

class Server extends Thread
{
    
    private ServerSocket listen_socket;
    private Agent myAgent;
    private Vector myOnlyReceivers;
    private boolean done = false;
    Socket client_socket;
    Connection c;

    /**
     * Constructor of the class.
     * It creates a ServerSocket to listen for connections on.
     * @param port is the port number to listen for. If 0, then it uses
     * the default port number.
     * @param a is the pointer to agent to be used to send messages.
     * @param receiver is the vector with the names of all the agents that
     * wish to receive messages through this proxy.
     */
    Server(int port, Agent a, Vector receivers)
    {
        myAgent = a;
        setName (myAgent.getLocalName() + "-SocketListener");
        if (port == 0) {
            port = SocketProxyAgent.DEFAULT_PORT;
        }
        
        myOnlyReceivers = receivers;

        try {
            listen_socket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            myAgent.doDelete();

            return;
        }

        ((SocketProxyAgent) myAgent).log(getName() + ": Listening on port: " + port);
        start();
    } // END constructor Server(int port, Agent a, Vector receivers)


    

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
    public void run()
    {
        try {
            done = false;
            while (!done)
            {
                client_socket = listen_socket.accept();

                ((SocketProxyAgent) myAgent)
                    .log("\nNew Connection with "
                         + client_socket.getInetAddress().toString()
                         + " on remote port " + client_socket.getPort());

                c = new Connection(client_socket, myAgent, myOnlyReceivers);
            } // END while (!done)
        }
	catch (IOException e)
	{
            // If the done flag is still false, then we had an unexpected
            // IOException.
            if (!done) {
                ((SocketProxyAgent) myAgent).
                     log(getName() + " IOException: " + e);
                myAgent.doDelete();
            }
        }
        finally {
            finalize();
        }
    } // END run()

    protected void closeDown()
    {
        done = true;
         try {
            if (listen_socket != null) {
                listen_socket.close();
                listen_socket = null;
            }
        }
	catch (Exception e) {
            // Do nothing
        }
    }
    
    protected void finalize() {
        try {
            if (listen_socket != null) {
                listen_socket.close();
                listen_socket = null;
            }
        }
	catch (Exception e) {}

        try
	{
            if (client_socket != null) {
                client_socket.close();
                client_socket = null;
            }
        }
	catch (Exception e) {}

        try
	{
            if (c != null) {
                if (c.isAlive()) {
                    c.closeDown();
                }
                c.join(1000);
                c = null;
            }
        }
	catch (Exception e) {}
    }
} // END class Server

class Connection extends Thread {
    private Agent myAgent;
    private Socket client;
    private DataInputStream in;
    private PrintStream out;
    private boolean done = false;

    /** Name of the agents who intend to receive any message from this agent */
    private Vector myOnlyReceivers;

    Connection(Socket client_socket, Agent a, Vector receivers)
    {
        myAgent = a;
        String threadName = myAgent.getLocalName() + "-ClientConnection-" + getName();
        // The thread name must not contain any spaces.  It get used as the
        // value for the reply-with field in the ACL message.
        threadName = threadName.trim().replace(' ', '_');
        setName(threadName);
        myAgent = a;
        client = client_socket;
        myOnlyReceivers = receivers;

        try {
            in = new DataInputStream(client.getInputStream());
            out = new PrintStream(client.getOutputStream(), true);
        } catch (IOException e) {
            try {
                client.close();
            } catch (IOException e2) {}

            e.printStackTrace();

            return;
        }

        start();
    }

    private boolean myOnlyReceiversContains(Iterator aids) {
        List l = new ArrayList();

        while (aids.hasNext()) {

            //if the name of the receiver has not the hap, it's local and the hap is added.
            String name = ((AID) aids.next()).getName();
            int atPos = name.lastIndexOf('@');

            if (atPos == -1) {
                name = name + "@" + myAgent.getHap();
            }
            ((SocketProxyAgent) myAgent).log("Requested addressee:" + name);
            l.add(name);
        }

        for (int i = 0; i < myOnlyReceivers.size(); i++) {
            String allow = (String)myOnlyReceivers.elementAt(i);
            if ( (allow.equals("*")) || (l.contains(allow)) ) {
                ((SocketProxyAgent) myAgent).log("Valid addressee:" + allow);
                return true;
            }
        }
        ((SocketProxyAgent) myAgent).log("No valid addressee, message delivery will be refused.");
        return false;
    }

    /**
     * Method run
     *
     *
     */
    public void run()
    {
        String line;

        try {
            ACLParser parser = new ACLParser(in);
            ACLMessage msg;

            done = false;
            while (!done)
            {
				if (parser.token.kind==ACLParserConstants.EOF)
					break;
                msg = parser.Message();
                ((SocketProxyAgent) myAgent).log(getName() + ": Received message:" + msg);

                if (myOnlyReceiversContains(msg.getAllReceiver())) {
                    msg.setSender(myAgent.getAID());

                    if ((msg.getReplyWith() == null)
                            || (msg.getReplyWith().length() < 1)) {
                        msg.setReplyWith(getName() + "."
                                         + java.lang.System.currentTimeMillis());
                    }
                    
                    if ( msg.getInReplyTo() == null ) {
                        msg.setInReplyTo( "noValue" );
                    }

                    myAgent.send(msg);
                    ((SocketProxyAgent) myAgent).log("Sent message to jade, awaiting reply for "
                                                         + msg.getReplyWith()
                                                    );
                    myAgent.addBehaviour(new WaitAnswersBehaviour(myAgent, msg, out));
                }
                else
                {
                    ((SocketProxyAgent) myAgent).log("About to send refusal");
                    out.println("(refuse :content unauthorised)");
                    out.flush();//Added by Sebastien_Siva@hp.com
                    done = true;
                    close(null);
                    return;
                }
            } // END while (!done)
        }
        catch (Throwable any) {
            close(any);
            return;
        }
    } // END run()

    protected void closeDown()
    {
        done = true;
        this.interrupt();
        close(null);
        try {
            this.join(1000);
        }
        catch (InterruptedException ignore) {}
    }
    
    void close(Throwable e) {
        done = true;
        try {
            client.close();
            client = null;
        } catch (IOException e2) {}

        if (e != null) {
            e.printStackTrace();
        }
    }

    protected void finalize() {
        try {
            if (client != null) {
                client.close();
                client = null;
            }
        } catch (Exception e) {}

        try {
            if (in != null) {
                in.close();
                in = null;
            }
        } catch (Exception e) {}

        try {
            if (out != null) {
                out.close();
                out = null;
            }
        } catch (Exception e) {}
    }
}

class WaitAnswersBehaviour extends SimpleBehaviour {
    ACLMessage msg;
    PrintStream out;
    long timeout;
    final static long DEFAULT_TIMEOUT = 10000;    // 10 seconds
    boolean finished;
    MessageTemplate mt;
    Agent myAgent=null;
    
    WaitAnswersBehaviour(Agent a, ACLMessage m, PrintStream o) {
        super(a);
        myAgent = a;
        out = o;

        try {
            mt = MessageTemplate
                .and(MessageTemplate
                    .MatchSender((AID) m.getAllReceiver()
                    .next()), MessageTemplate
                        .MatchInReplyTo(m.getReplyWith()));
        } catch (Exception e) {
            mt = MessageTemplate.MatchInReplyTo(m.getReplyWith());
        }

        Date d = m.getReplyByDate();

        if (d != null) 
        {
            timeout = d.getTime() - (new Date()).getTime();
            if (timeout <= 1000)
                timeout = 1000;
        }
        else
            timeout = DEFAULT_TIMEOUT;

        finished = false;
    }

    /**
     * Method action
     *
     *
     */
    public void action()
    {
        ((SocketProxyAgent) myAgent).log(Thread.currentThread().getName()
                                            + ": About to block, waiting for reply..."
                                        );
        msg = myAgent.blockingReceive(mt, timeout);
       
        ((SocketProxyAgent) myAgent).log(Thread.currentThread().getName()
                                            + ": No longer blocked"
                                        );

        if (msg == null) {
            ((SocketProxyAgent) myAgent).log(Thread.currentThread().getName()
                                            + ": Reply was null"
                                        );
            msg = new ACLMessage(ACLMessage.FAILURE);
            msg.setContent("( \"Timed-out waiting for response from agent\" )");
        }
        ((SocketProxyAgent) myAgent).log(Thread.currentThread().getName()
                                            + ": Wrote reply:" + msg.toString()
                                        );

        out.print(msg.toString());
        out.flush();//Added by Sebastien_Siva@hp.com


	if(msg.getPerformative() == ACLMessage.AGREE)
	    finished = false;
	else
	    finished = true;
    }

    /**
     * Method done
     *
     *
     * @return boolean indicating if done or not.
     *
     */
    public boolean done() {
        return finished;
    }
}


/*--- Formatted following HP STL Java Convention Style on Thu, Feb 1, '01 ---*/
