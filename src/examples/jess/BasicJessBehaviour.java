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
 *
 * $Log$
 * Revision 1.7  1999/04/06 00:08:07  rimassa
 * New version by Fabio.
 *
 * Revision 1.7  1999/03/31 20:54:49  rimassa
 * New version received from Fabio.
 *
 * Revision 1.13  1999/03/31 08:06:06  bellifemine
 * The ACLMessage content is quoted before Jess assertion and then unquoted before
 * Jess send.
 *
 * Revision 1.12  1999/02/25 17:26:05  bellifemine
 * Versione 0.975 inviata da Giovanni il 25/2/99 + FipaContractNetResponderBehaviour.java mio
 *
 * Revision 1.5  1999/02/04 13:09:04  rimassa
 * Fixed a bug and tested with Jess 4.3 .
 *
 * Revision 1.8  1999/02/04 09:41:21  bellifemine
 * <>
 *
 */

package examples.jess;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jess.*;
import java.io.*;
import java.util.Enumeration;

/**
 * This is the basic class that implements a behaviour of JADE that allows 
 * to embed a Jess engine inside the agent code.
 * <p>
 * As it is an abstract class, the programmer must override it.
 * In particular, its method <code>JessString</code> must be implemented.
 * <p>
 * <a href="http://herzberg.ca.sandia.gov/jess">Jess</a> 
 * supports the development of rule-based expert systems. 
 * <p>
 * When this behaviour is added to the list of agent behaviours, 
 * it creates a Jess engine and initializes the engine by:
 * <ul>
 * <li> defining the template of an ACLMessage,
 * <li> defining the userfuntion "send" to send ACLMessages,
 * <li> asserting the fact <code>(MyAgent (name nameofthisagent))</code>,
 * <li> parsing the Jess file passed as a parameter to the constructor.
 * </ul>
 * Then the behaviour loops infinitely by:
 * <ul>
 * <li> waiting that a message arrives,
 * <li> calling the <code>JessString</code> method that returns the fact to be
 * asserted in Jess,
 * <li> asserting the fact in Jess,
 * <li> running Jess.
 * </ul>
 * <p>
 * Notice for programmers of the Jess .clp file:
 * <ul>
 * <li> the template of the ACLMessage contains the following slots:
<code>(deftemplate ACLMessage (slot communicative-act) (slot sender) (multislot receiver) (slot reply-with) (slot in-reply-to) (slot envelope) (slot conversation-id) (slot protocol) (slot language) (slot ontology) (slot content) )</code>
 * <li> match the fact <code>(MyAgent (name nameofthisagent))</code> to know the name of your agent;
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

public abstract class BasicJessBehaviour extends CyclicBehaviour{
  
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
    
    public Value call(ValueVector vv, Context context) throws ReteException {
      //      for (int i=0; i<vv.size(); i++) {
      //System.out.println(" parameter " + i + "=" + vv.get(i).toString() +  
      // " type=" + vv.get(i).type());
      //}
      if (vv.get(1).type() != RU.FACT_ID) 
	throw new ReteException(name(), "a fact with template ACLMessage must be passed to send","");

      vv=context.expandFact(findFactByID(context.engine(),vv.get(1).intValue())); 
      //      System.err.println("vv.get(0)="+vv.get(0).toString());
      if (! vv.get(0).toString().equalsIgnoreCase("ACLMessage"))
	throw new ReteException(name(), "a fact with template ACLMessage must be passed to send","");

      //ACLMessage msg = new ACLMessage();
      //msg.setType(vv.get(3).stringValue());
      ACLMessage msg = new ACLMessage(vv.get(3).stringValue());
      if (vv.get(4).stringValue() != "nil")
	msg.setSource(vv.get(4).stringValue());
      if (vv.get(5).toString() != "nil")
	msg.setDest(vv.get(5).toString());
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
      if (vv.get(13).stringValue() != "nil") {
	//FIXME undo replace chars of JessBehaviour.java. Needs to be done better
	msg.setContent(unquote(vv.get(13).stringValue()));
      }

      //msg.dump();
      //msg.toText(new BufferedWriter(new OutputStreamWriter(System.out)));
      my_agent.send(msg);

      return Funcall.TRUE();
    }


    // Unfortunatelly, Jess has already this method but it is not defined public
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
  Rete  jess; 			// holds the pointer to jess
  Agent myAgent; 		// holds the pointer to this agent
  int   m_maxJessPasses = 0; 	// holds the maximum number of Jess passes for each run
  int executedPasses=-1; 	// to count the number of Jess passes in the previous run
  
  /**
   * Creates a <code>BasicJessBehaviour</code> instance
   *
   * @param agent the agent that adds the behaviour
   * @param jessFile the name of the Jess file to be executed
   */
    public BasicJessBehaviour(Agent agent, String jessFile){
      myAgent=agent;
      // See info about the Display classes in Section 5 of Jess 4.1b6 Readme.htm
      NullDisplay nd = new NullDisplay();
      // Create a Jess engine
      jess = new Rete(nd);
      try {
	jess.addUserpackage((Userpackage)Class.forName("jess.MiscFunctions").newInstance());
      } catch (Throwable t) { System.out.println(t); }
      try { 
	// First I define the ACLMessage template
	jess.executeCommand("(deftemplate ACLMessage (slot communicative-act) (slot sender) (multislot receiver) (slot reply-with) (slot in-reply-to) (slot envelope) (slot conversation-id) (slot protocol) (slot language) (slot ontology) (slot content) )");
        // Then I define the myagent template
	jess.executeCommand("(deftemplate MyAgent (slot name))");
	// Then I add the send function
	jess.addUserfunction(new JessSend(myAgent));
	// Then I assert the fact (Myagent (name <my-name>))
	jess.executeCommand("(deffacts MyAgent \"All facts about this agent\" (MyAgent (name " + myAgent.getName() + ")))");
	// Open the file test.clp
	FileInputStream fis = new FileInputStream(jessFile);
	// Create a parser for the file, telling it where to take input
	// from and which engine to send the results to
	Jesp j = new Jesp(fis, jess);
	// parse and execute one construct, without printing a prompt
	j.parse(false); 
      } catch (ReteException re) 	  { System.out.println(re); }
      catch (FileNotFoundException e) { System.out.println(e); }
    }
 
    /**
     * Creates a <code>BasicJessBehaviour</code> instance that limits
     * the reasoning time of Jess before looking again for arrival of messages.
     *
     * @param agent the agent that adds the behaviour
     * @param jessFile the name of the Jess file to be executed
     * @param maxJessPasses the maximum number of passes that every run of Jess
     * can execute before giving again the control to this behaviour;
     * put <code>0</code> if you do not ever want to stop Jess.
     */
     public BasicJessBehaviour(Agent agent, String jessFile, int maxJessPasses){
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
      // assert the fact message in Jess
      assert(JessString(msg));
    } else {
      System.out.println(myAgent.getName()+ " is checking if there is a message...");
      msg = myAgent.receive();
    }

    // run jess 
    try {
      // jess.executeCommand("(facts)");
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
   * returns the String representing the facts to be asserted in Jess
   * This is an abstract method and must be implemented by all subclasses
   */
  public abstract String JessString(ACLMessage msg);

  /**
   * asserts a fact representing an ACLMessage in Jess. It is called after
   * the arrival of a message. 
   */
  void assert(String fact) {
    try 		     { jess.executeCommand(fact); }
    catch (ReteException re) { re.printStackTrace((jess.display()).stderr()); }
  } 



  /*
   * replace a char in a String with a String
   * It is used to convert all the quotation marks in backslash quote
   * before asserting the content of a message in Jess.
   * @return the new String
   */
public String stringReplace(String str, char oldChar, String s) {
  int len = str.length();
  int i = 0; int j=0;  int k=0;
  char[] val = new char[len];
  str.getChars(0,len,val,0); // put chars into val
  char buf[] = new char[len*s.length()];

  while (i < len) {
    if (val[i] == oldChar) {
      s.getChars(0,s.length(),buf,j);
      j+=s.length();
    } else { 
      buf[j]=val[i];
      j++;
    }
    i++;
  }
  return new String(buf, 0, j);
}

  /**
   * Remove the first and the last character of the string 
   * (if it is a quotation mark) and convert all backslash quote in quote
   * It is used to convert a Jess content into an ACL message content.
   */
public String unquote(String str) {
  String t1= str.trim();
  if (t1.startsWith("\"")) 
    t1 = t1.substring(1);
  if (t1.endsWith("\"")) 
    t1 = t1.substring(0,t1.length()-1);
  int len = t1.length();
  int i = 0; int j=0;  int k=0;
  char[] val = new char[len];
  t1.getChars(0,len,val,0); // put chars into val
  char buf[] = new char[len];

  boolean maybe = false;
  while (i < len) {
    if (maybe) {
      if (val[i] == '\"') 
	j--;
      buf[j] = val[i];
      maybe=false;
      i++; 
      j++;
    } else {
      if (val[i] == '\\') {
	maybe=true;
      }
      buf[j] = val[i];
      i++; j++;
    }
  }
  return new String(buf, 0, j);
}

  /**
   * Insert the first and the last character of the string as a quotation mark
   * Replace all the quote characters into backslash quote.
   * It is used to convert an ACL message content into a Jess content.
   */
public String quote(java.lang.String str) {
  //replace all chars " in \ "
  return "\"" + stringReplace(str,'"',"\\\"") + "\""; 
}

} // end JessBehaviour







