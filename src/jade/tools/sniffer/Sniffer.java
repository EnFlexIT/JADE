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

package jade.tools.sniffer;


import java.io.StringReader;

import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;

import jade.core.*;
import jade.core.behaviours.*;

import jade.domain.FIPAException;
import jade.domain.JADEAgentManagement.*;
import jade.domain.introspection.*;
import jade.domain.FIPAServiceCommunicator;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.ACLCodec;
import jade.lang.acl.StringACLCodec;

import jade.lang.sl.SL0Codec;

import jade.onto.basic.Action;

import jade.proto.FipaRequestInitiatorBehaviour;

import jade.tools.ToolAgent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.io.*;


/**
 *  This is the <em>Sniffer</em> agent.<br>
 *  This class implements the low level part of the Sniffer, interacting with Jade
 *  environment and with the sniffer GUI.<br>
 *  At startup, the sniffer subscribes itself as an rma to be informed every time
 *  an agent is born or dead, a container is created or deleted.<br>
 *  For more information see <a href="../../../../intro.htm" target="_top">Introduction to the Sniffer</a>
 * Javadoc documentation for the file
 * @author <a href="mailto:alessandro.beneventi@re.nettuno.it"> Alessandro Beneventi </a>(Developement) 
 * @author Gianluca Tanca (Concept & Early Version)
 * @version $Date$ $Revision$
 * 
 * Modified by:
 * @author Robert Kessler, University of Utah
 * Added the ability to have a configuration file (snifferagentname.inf) which allows
 * you to specify agents that should be sniffed as soon as they appear.  The file
 * contains a line by line list of agent names (if there is no @ in the name, it assumes
 * the current HAP for it).  Following the agent name is an optional list of performatives
 * that the sniffer will sniff.  If the list is not present, then the sniffer will
 * display all messages.  Otherwise, only those messages that have a matching
 * performative mentioned will be displayed.  A typical file might contain:
 * da0
 * da1 inform propose 
 * sniffer0@disjunior:1099/JADE
 *
 * Note - the file is looked for in the current directory, and if not found, it scans
 * for the file at the top level directory (/snifferagentname.inf)
 *
 * One other change was made to the system - that is the performative of the message
 * is now displayed above each message.  This gives additional information to the
 * user so they can see exactly what is happening dynamically.
 *
 * More changes - M. Griss changed the display to include information about the 
 * conversation id and other ids.  He also enhanced the one click information at the
 * bottom.  R. Kessler changed MainPanel, so the scrollPane has a column header, which
 * is the agent canvas list.  Now when it scrolls down, the agents stay on top.
 *
 * This functionality required changes to: Sniffer (read the file, when an agent is 
 * born - see if in the list and auto sniff, and when a message arrives - determine 
 * if it should be displayed or not); MainWindow (made the ActionProcessor local 
 * variable public so we can get a hold of the code in DoSniffAction); DoSniffAction
 * (modified so there is a new method doSniff - which takes an agent name and does
 * the actual sniffing - I broke this out from the doAction method); MMCanvas (added
 * the ability to display the performative name above the message).
 *
 * Some notes:
 * 1-if a message is one that is to be ignored, then it is dropped totally.  If you
 *   look at the sniffer dump of messages, it will not be there.  Might want to change
 *   this.
 * 2-Should develop a GUI to allow dynamically setting which messages are filtered instead
 *   of forcing them to be in the .inf file.
 * 3-Probably should allow one to turn on and off the display of the performative name.
 *   Although, it seems pretty nice to have this information and although one might
 *   consider that it clutters the display, it sure provides a lot of information with
 *   it.
 * 4-Since we can now sniff agents when they appear, you could imagine another option
 *   that would allow you to turn on a sniff all new agents flag.
 *
 */
public class Sniffer extends ToolAgent {

  public static final boolean SNIFF_ON = true;
  public static final boolean SNIFF_OFF = false;
  
  // Note - these two are parallel vectors, so we add at the same time to both.
  // Could use a better mechanism, like a vector of two element lists or something.
  private Vector preLoadedAgents; // The agents in the .inf file that we are to pre-sniff.
  private Vector preLoadedFilters; // Filtering array for the pre-sniffed agents.

  private LinkedList agentsUnderSniff = new LinkedList();


  // Sends requests to the AMS
  private class AMSClientBehaviour extends FipaRequestInitiatorBehaviour {

    private String actionName;

    public AMSClientBehaviour(String an, ACLMessage request) {
      super(Sniffer.this, request,
	    MessageTemplate.and(MessageTemplate.MatchOntology(JADEAgentManagementOntology.NAME),
				MessageTemplate.MatchLanguage(SL0Codec.NAME)
				)
	    );
      actionName = an;
    }

    protected void handleNotUnderstood(ACLMessage reply) {
      myGUI.showError("NOT-UNDERSTOOD received during " + actionName);
    }

    protected void handleRefuse(ACLMessage reply) {
      myGUI.showError("REFUSE received during " + actionName);
    }

    protected void handleAgree(ACLMessage reply) {
      // System.out.println("AGREE received");
    }

    protected void handleFailure(ACLMessage reply) {
      myGUI.showError("FAILURE received during " + actionName);
    }

    protected void handleInform(ACLMessage reply) {
      // System.out.println("INFORM received");
    }

  } // End of AMSClientBehaviour class


  private class SniffListenerBehaviour extends CyclicBehaviour {

    private MessageTemplate listenSniffTemplate;

    SniffListenerBehaviour() {
      listenSniffTemplate = MessageTemplate.MatchConversationId(getName() + "-event");
    }

    public void action() {

      ACLMessage current = receive(listenSniffTemplate);
      if(current != null) {
	try {
	  List l = extractContent(current);
	  Occurred o = (Occurred)l.get(0);
	  EventRecord er = o.get_0();
	  Event ev = er.getWhat();
	  String content;
	  if(ev instanceof SentMessage)
	    content = ((SentMessage)ev).getMessage().getPayload();
	  else if(ev instanceof PostedMessage)
	    content = ((PostedMessage)ev).getMessage().getPayload();
	  else return;

	  ACLCodec codec = new StringACLCodec();
	  ACLMessage tmp = codec.decode(content.getBytes());
	  Message msg = new Message(tmp);

	  // If this is a 'posted-message' event and the sender is
	  // currently under sniff, then the message was already
	  // displayed when the 'sent-message' event occurred. In that
	  // case, we simply skip this message.
	  if(ev instanceof PostedMessage) {
	    String nickname = msg.getSender().getName();
	    int index = nickname.indexOf("@");
	    if(index != -1)
	      nickname = nickname.substring(0, index);

	    Agent a = new Agent(nickname);
	    if(agentsUnderSniff.contains(a))
	      return;
	  }

          // If the message that we just got is one that should be filtered out
          // then drop that it on the floor.  WARNING - this means that the log file
          // that the sniffer might dump does not include the message!!!!
          boolean filters [];
          String agentName = msg.getSender().getName();
          if (preLoadedAgents.contains(agentName)) {
              filters = (boolean[])preLoadedFilters.elementAt(preLoadedAgents.indexOf(agentName));
              if ((msg.getPerformative() >= 0) && filters[msg.getPerformative()]) {
                myGUI.mainPanel.panelcan.canvMess.recMessage(msg);
              }
          } else {
            myGUI.mainPanel.panelcan.canvMess.recMessage(msg);
          }
	} 
	catch(Throwable e) {
	  //System.out.println("Serious problem Occurred");
	  myGUI.showError("An error occurred parsing the incoming message.\n" +
			  "          The message was lost.");
	  e.printStackTrace();
	}
      }
      else
      	block();
    }

  } // End of SniffListenerBehaviour


  private SequentialBehaviour AMSSubscribe = new SequentialBehaviour();

  /**
    @serial
  */
  private MainWindow myGUI;

  /**
    @serial
  */
  private String myContainerName;

  /**
   * ACLMessages for subscription and unsubscription as <em>rma</em> are created and
   * corresponding behaviours are set up.
   */
  public void toolSetup() {

    loadSnifferConfigurationFile();

    // Send 'subscribe' message to the AMS
    AMSSubscribe.addSubBehaviour(new SenderBehaviour(this, getSubscribe()));

    // Handle incoming 'inform' messages
    AMSSubscribe.addSubBehaviour(new AMSListenerBehaviour() {

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
	    // Here we check to see if the agent is one that we automatically will
	    // start sniffing.  If so, we invoke DoSnifferAction's doSniff and start
	    // the sniffing process.
	    if(preLoadedAgents.contains(agent.getName())) {
	      // System.out.println("Found one: " + agent.getName());
	      ActionProcessor ap = myGUI.actPro;
              DoSnifferAction sa = (DoSnifferAction)ap.actions.get(ap.DO_SNIFFER_ACTION);
              sa.doSniff(agent.getName());
	    } else {
              // System.out.println("Agent not in .inf: " + agent.getName());
	    }
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

      } // End of installHandlers() method

    });

    // Schedule Behaviours for execution
    addBehaviour(AMSSubscribe);
    addBehaviour(new SniffListenerBehaviour()); 

    // Show Graphical User Interface
    myGUI = new MainWindow(this);
    myGUI.ShowCorrect();

  }

  /* Private function to go out and grab the configuration file, fill the
   * preLoadedAgents vector with the agents that are to be automatically sniffed.
   * Also fill the parallel vector preLoadedFilters vector with special message
   * filtering.
   */
  private void loadSnifferConfigurationFile () {
      // See if there is a sniffer configuration file that lists the agents to
      // start sniffing.  Each line of the file lists an agent, optionally followed
      // by a set of messages to sniff.  If there are no tokens, then we assume
      // that means to sniff all.  The tokens are the name of the performative type
      // such as INFORM, QUERY, etc.
      // Look for the sniffer file locally and if not found, look at the top-level
      // directory.
      StringTokenizer st;
      preLoadedAgents = new Vector();
      preLoadedFilters = new Vector();
      String name;
      int atPos;
      List nameAndFilter;
      boolean filter[];
      BufferedReader in = null;
      try {
          // Hack - need to figure out where to put the sniffer .inf file.
          try {
              // First look locally
              in = new BufferedReader(new FileReader(getLocalName() + ".inf"));
          } catch (IOException nolocal) {
              try {
                  // Try at top-level
                  in = new BufferedReader(new FileReader("\\" + getLocalName() + ".inf"));
              } catch (IOException notop) {
              }
          }
          // Read each line.  Will toss an exception on EOF (I guess).
          while (true) {
              st = new StringTokenizer(in.readLine());
              name = st.nextToken();
              atPos = name.lastIndexOf('@');
              if(atPos == -1)
                  name = name + "@" + getHap();
              preLoadedAgents.add(name);
              
              filter = new boolean[22];
              boolean initVal;
              initVal = (st.hasMoreTokens() ? false : true);
              for (int i=0;i<22;i++) {
                  filter[i] = initVal;
              }
              while (st.hasMoreTokens())
              {
                  int perfIndex = ACLMessage.getInteger(st.nextToken());
                  if (perfIndex != -1) {
                      filter[perfIndex] = true;
                  }
              }
              preLoadedFilters.add(filter);
          }
      } catch(Exception e) {
          try { if (in != null) in.close(); } catch (IOException ee) {};
      }
  }

/**
   * Cleanup during agent shutdown. This method cleans things up when
   * <em>Sniffer</em> agent is destroyed, disconnecting from <em>AMS</em>
   * agent and closing down the Sniffer administration <em>GUI</em>.
   * Currently sniffed agents are also unsniffed to avoid errors.
   */
  protected void toolTakeDown() {

    List l = (List)(agentsUnderSniff.clone());
    ACLMessage request = getSniffMsg(l, SNIFF_OFF);

    // Start a FIPARequestProtocol to sniffOff all the agents since
    // the sniffer is shutting down
    try {
      if(request != null)
	FIPAServiceCommunicator.doFipaRequestClient(this,request);
    }
    catch(jade.domain.FIPAException e) {
      System.out.println(e.getMessage());
    }

    myGUI.mainPanel.panelcan.canvMess.ml.removeAllMessages();

    // Now we unsubscribe from the rma list
    send(getCancel());
    myGUI.setVisible(false);
    myGUI.disposeAsync();

  }

/**
 * This method add an AMSBehaviour the perform a request to the AMS for sniffing/unsniffing list of agents.
 **/
 public void sniffMsg(List agents, boolean onFlag) {
      ACLMessage request = getSniffMsg(agents,onFlag);
      if (request != null)
        addBehaviour(new AMSClientBehaviour((onFlag?"SniffAgentOn":"SniffAgentOff"),request));

 }
  /**
   * Creates the ACLMessage to be sent to the <em>Ams</em> with the list of the
   * agent to be sniffed/unsniffed. The internal list of sniffed agents is also 
   * updated.
   *
   * @param agentVect vector containing TreeData item representing the agents
   * @param onFlag can be:<ul>
   *			  <li> Sniffer.SNIFF_ON  to activate sniffer on an agent/group
   *			  <li> Sniffer.SNIFF_OFF to deactivate sniffer on an agent/group
   *		         </ul>
   */
  public ACLMessage getSniffMsg(List agents, boolean onFlag) {

    Iterator it = agents.iterator();

    if(onFlag) {
      SniffOn so = new SniffOn();
      so.setSniffer(getAID());
      boolean empty = true;
      while(it.hasNext()) {
	Agent a = (Agent)it.next();
	AID agentID = new AID();
	agentID.setName(a.agentName + '@' + getHap());
	if(!agentsUnderSniff.contains(a)) {
	  agentsUnderSniff.add(a);
	  so.addSniffedAgents(agentID);
	  empty = false;
	}
      }
      if(!empty) {
	try {
	  Action a = new Action();
	  a.set_0(getAMS());
	  a.set_1(so);
	  List l = new ArrayList(1);
	  l.add(a);

	  ACLMessage requestMsg = getRequest();
	  requestMsg.setOntology(JADEAgentManagementOntology.NAME);
	  fillContent(requestMsg, l);
          return requestMsg;
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
      }
    }

    else {
      SniffOff so = new SniffOff();
      so.setSniffer(getAID());
      boolean empty = true;
      while(it.hasNext()) {
	Agent a = (Agent)it.next();
	AID agentID = new AID();
	agentID.setName(a.agentName + '@' + getHap());
	if(agentsUnderSniff.contains(a)) {
	  agentsUnderSniff.remove(a);
	  so.addSniffedAgents(agentID);
	  empty = false;
	}
      }
      if(!empty) {
	try {
	  Action a = new Action();
	  a.set_0(getAMS());
	  a.set_1(so);
	  List l = new ArrayList(1);
	  l.add(a);

	  ACLMessage requestMsg = getRequest();
	  requestMsg.setOntology(JADEAgentManagementOntology.NAME);
	  fillContent(requestMsg, l);
          requestMsg.setReplyWith(getName()+ (new Date().getTime()));
          return requestMsg;
	}
	catch(FIPAException fe) {
	  fe.printStackTrace();
	}
      }
    }
    return null;
  }

}  // End of class Sniffer
