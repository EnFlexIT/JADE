/**
 * @version $Id$
 *
 * Copyright (c) 1998 CSELT Centro Studi e Laboratori Telecomunicazioni S.p.A.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of 
 * CSELT Centro Studi e Laboratori Telecomunicazioni S.p.A. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the agreement you entered into with CSELT.
 *
 * @author Fabio Bellifemine - CSELT S.p.A.
 */

package examples.jess;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jess.*;
import java.io.*;
import java.util.Enumeration;

/**
 * This is a behaviour of JADE that allows to embed a Jess engine inside the agent code.
 * <p>
 * <a href="http://herzberg.ca.sandia.gov/jess">Jess</a> 
 * supports the development of rule-based expert systems. 
 * <p>
 * When this behaviour is added to the list of agent behaviours, 
 * it creates a Jess engine and initializes the engine by:
 * <ul>
 * <li> defining the template of an ACLMessage,
 * <li> defining the userfuntion "send" to send ACLMessages,
 * <li> asserting the fact <code>(myname nameofthisagent)</code>,
 * <li> parsing the Jess file passed as a parameter to the constructor.
 * </ul>
 * Then the behaviour loops infinitely by:
 * <ul>
 * <li> waiting that a message arrives,
 * <li> asserting the fact in Jess,
 * <li> running Jess.
 * </ul>
 * <p>
 * Notice for programmers of the Jess .clp file:
 * <ul>
 * <li> the template of the ACLMessage contains the following slots:
<code>(deftemplate ACLMessage (slot communicative-act) (slot sender) (slot receiver) (slot reply-with) (slot in-reply-to) (slot envelope) (slot conversation-id) (slot protocol) (slot language) (slot ontology) (slot content) )</code>
 * <li> match the fact <code>(myname nameofthisagent)</code> to know the name of your agent;
 * <li> use the userfunction <code>send</code> to send ACLMessages. 
 * The parameter of <code>send</code> must be a fact-id of type ACLMessage or
 * an ACLMessage itself; There are two styles of usage:
 * <p> <code>  ?m <- (ACLMessage (communicative-act cfp) (sender ?s))
 * <br>
 * (send ?m) </code>
 * <p> or, in alternative, 
 * <p> <code>(send (assert (ACLMessage (communicative-act cfp) 
 * (sender ?s))))</code>
 * <li> remember to load all the Jess Packages you need because, by default, 
 * Jess just loads the built-in functions
 * </ul>
 * <p>
 * Look at the sample file test.clp that is shipped with this example.
 */

public class JessBehaviour extends SimpleBehaviour {

  /**
   * This class implements the Jess userfunction to send ACLMessages
   * directly from Jess.
   * It can be used by Jess by using the name <code>send</code>.
   */
  public class JessSend implements Userfunction {
    // data
    Agent my_agent;
    
    public JessSend(Agent a){
      my_agent = a;
    }
    
    // The name method returns the name by which the function appears in Jess
    public String name() { return("send"); }
    
    public Value call (ValueVector vv, Context context) throws ReteException {
      //      for (int i=0; i<vv.size(); i++) {
      // System.out.println(" parameter " + i + "=" + vv.get(i).toString() +  
      // " type=" + vv.get(i).type());
      //}
      if (vv.get(1).type() != RU.FACT_ID)
        throw new ReteException(name(), "a fact with template ACLMessage must be passed to send","");
      vv=context.expandFact(findFactByID(context.engine(),vv.get(1).intValue())); 
      if (vv.get(0).toString() != "ACLMessage")
        throw new ReteException(name(), "a fact with template ACLMessage must be passed to send","");

      ACLMessage msg = new ACLMessage();
      msg.setType(vv.get(3).stringValue());
      if (vv.get(4).stringValue() != "nil")
        msg.setSource(vv.get(4).stringValue());
      if (vv.get(5).stringValue() != "nil")
        msg.setDest(vv.get(5).stringValue());
      if (vv.get(6).stringValue() != "nil")
        msg.setReplyWith(vv.get(6).stringValue());
      if (vv.get(7).stringValue() != "nil")
        msg.setReplyTo(vv.get(7).stringValue());
      if (vv.get(8).stringValue() != "nil")
        msg.setEnvelope(vv.get(8).stringValue());
      if (vv.get(9).stringValue() != "nil")
        msg.setConversationId(vv.get(9).stringValue());
      if (vv.get(10).stringValue() != "nil")
        msg.setProtocol(vv.get(10).stringValue());
      if (vv.get(11).stringValue() != "nil")
        msg.setLanguage(vv.get(11).stringValue());
      if (vv.get(12).stringValue() != "nil")
        msg.setOntology(vv.get(12).stringValue());      
      if (vv.get(13).stringValue() != "nil")
        msg.setContent(vv.get(13).stringValue());
      msg.dump();
      my_agent.send(msg);

      return Funcall.TRUE();
    }

    // Unfortunately, Jess has already this method but it is not defined public
    //  therefore I reimplemented it. It might suffer in future from changes to Jess.
    private ValueVector findFactByID(Rete r, int id) throws ReteException {
      for (Enumeration e=r.listFacts(); e.hasMoreElements();) {
        ValueVector f = (ValueVector) e.nextElement();
        if (f.get(RU.ID).factIDValue() == id)
          return  f;
      }
      return null;
    }

  } // end JessSend class 

  
  // class variables
  Rete  jess;                   // holds the pointer to jess
  Agent myAgent;                // holds the pointer to this agent
  int   m_maxJessPasses = 0;    // holds the maximum number of Jess passes for each run
  int executedPasses=-1;        // to count the number of Jess passes in the previous run
  
  /**
   * Creates a <code>JessBehaviour</code> instance
   *
   * @param agent the agent that adds the behaviour
   * @param jessFile the name of the Jess file to be executed
   */
    public JessBehaviour(Agent agent, String jessFile){
      myAgent=agent;
      // See info about the Display classes in Section 5 of Jess 4.1b6 Readme.htm
      NullDisplay nd = new NullDisplay();
      // Create a Jess engine
      jess = new Rete(nd);
      try { 
        // First I define the ACLMessage template
        jess.executeCommand("(deftemplate ACLMessage (slot communicative-act) (slot sender) (slot receiver) (slot reply-with) (slot in-reply-to) (slot envelope) (slot conversation-id) (slot protocol) (slot language) (slot ontology) (slot content) )");
        // Then I add the send function
        jess.addUserfunction(new JessSend(myAgent));
        // Then I assert the fact (myname <my-name>)
        jess.executeCommand("(assert (myname " + myAgent.getName() + "))");
        // Open the file test.clp
        FileInputStream fis = new FileInputStream(jessFile);
        // Create a parser for the file, telling it where to take input
        // from and which engine to send the results to
        Jesp j = new Jesp(fis, jess);
        // parse and execute one construct, without printing a prompt
        j.parse(false); 
      } catch (ReteException re)          { System.out.println(re); }
      catch (FileNotFoundException e) { System.out.println(e); }
    }
 
    /**
     * Creates a <code>JessBehaviour</code> instance that limits
     * the reasoning time of Jess before looking again for arrival of messages.
     *
     * @param agent the agent that adds the behaviour
     * @param jessFile the name of the Jess file to be executed
     * @param maxJessPasses the maximum number of passes that every run of Jess
     * can execute before giving again the control to this behaviour.
     */
     public JessBehaviour(Agent agent, String jessFile, int maxJessPasses){
     this(agent,jessFile);
     m_maxJessPasses = maxJessPasses;
     }
  /**
   * executes the behaviour
   */
  public void action() {
    ACLMessage msg;     // to keep the ACLMessage

    // wait a message
    if (executedPasses < m_maxJessPasses) {
      System.out.println(myAgent.getName()+ " is blocked to wait a message...");
      msg = myAgent.blockingReceive();
    } else {
      System.out.println(myAgent.getName()+ " is checking if there is a message...");
      msg = myAgent.receive();
    }
    // msg.dump();
    // assert the fact message in Jess
    assert(msg);
    // run jess 
    try {
      // jess.executeCommand("(facts)");
      System.out.println("Running Jess");
      if (m_maxJessPasses > 0) { 
        executedPasses=jess.run(m_maxJessPasses);
        System.out.println("Jess has executed "+executedPasses+" passes");
      }
      else jess.run();
    } catch (ReteException re) {
      re.printStackTrace((jess.display()).stderr());
    }
  }
  
  /**
   * returns <code>true</code> if the behaviour is ended and can be removed
   * from the queue of active behaviours.
   * <p>
   * in this implementation, this behaviour never ends.
   */
  public boolean done() {
    return false;
  }
  
  
  /**
   * asserts a fact representing an ACLMessage in Jess. It is called after
   * the arrival of a message.
   */
  private void assert(ACLMessage msg) {
    String     fact;
    
    if (msg == null) return;
    // I create a string that asserts the template fact
    fact = "(assert (ACLMessage (receiver " + msg.getDest() + ") (communicative-act " + msg.getType();     
    if (msg.getSource() != null)         fact = fact + ") (sender " + msg.getSource();   
    if (msg.getContent() != null)        fact = fact + ") (content " + msg.getContent();
    if (msg.getReplyWith() != null)      fact=fact+") (reply-with " + msg.getReplyWith();
    if (msg.getReplyTo() != null)        fact=fact+") (in-reply-to " + msg.getReplyTo();   
    if (msg.getEnvelope() != null)       fact=fact+") (envelope " + msg.getEnvelope();    
    if (msg.getLanguage() != null)       fact=fact+") (language " + msg.getLanguage();    
    if (msg.getOntology() != null)       fact=fact+") (ontology " + msg.getOntology();    
    if (msg.getReplyBy() != null)        fact=fact+") (reply-by " + msg.getReplyBy();    
    if (msg.getProtocol() != null)       fact=fact+") (protocol " + msg.getProtocol();  
    if (msg.getConversationId() != null) fact=fact+") (conversation-id " + msg.getConversationId(); 
    fact=fact+")))";
    // finally, I execute the Jess assert command
    try                         { jess.executeCommand(fact); }
    catch (ReteException re)    { re.printStackTrace((jess.display()).stderr()); }
  } // end assert
} // end JessBehaviour


