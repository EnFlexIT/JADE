/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop multi-agent systems in compliance with the FIPA specifications.
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

package examples.jess;

import jade.core.*;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jess.*;
import java.io.*;
import java.util.*;

/**
Javadoc documentation for the file
@author Fabio Bellifemine - CSELT S.p.A
@version $Date$ $Revision$
*/

/**
 * This is the basic class that implements a behaviour of JADE that allows 
 * to embed a Jess engine inside the agent code. 
 * <p>
 * <a href="http://herzberg.ca.sandia.gov/jess">Jess</a> 
 * supports the development of rule-based expert systems. 
 * <p>
 * This JADE implementation has been tested only with version 4.3 of JESS.
 * <p>
 * The programmer can override this class.
 * In particular, its methods <code>ACL2JessString</code> and
 * <code>JessFact2ACL</code> to convert between ACLMessage JADE structure and
 * Jess facts. Also the method <code>ACLJessTemplate</code> might need to
 * be overide in order to change the deftemplate of the ACLMessage in Jess.
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
 * <li> calling the <code>ACL2JessString</code> method that returns the fact to be
 * asserted in Jess,
 * <li> asserting the fact in Jess,
 * <li> running Jess.
 * </ul>
 * <p>
 * Notice for programmers of the Jess .clp file:
 * <ul>
 * <li> the template of the ACLMessage contains the following slots:
<code>(deftemplate ACLMessage (slot communicative-act) (slot sender) (multislot receiver) (slot reply-with) (slot in-reply-to) (slot envelope) (slot conversation-id) (slot protocol) (slot language) (slot ontology) (slot content) (slot encoding) (multislot reply-to) (slot reply-by))</code>
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
 * Look at the sample file JadeAgent.clp that is shipped with this example.
 * <p>
 * WARNING:
 * FIPA2000 standard has specified an AgentIdentifier to have a 
 * template
 * composed of several slots, where the only mandatory one is the agent name, 
 * i.e. the globally unique identifier of the agent (GUID).
 * In order to reduce the porting of the JESS user code, this basic
 * behaviour automatically 
 * takes care of replacing the GUIDs with full-fledged AgentIdentifiers.
 */

public class BasicJessBehaviour extends CyclicBehaviour{
  
  /**
   * This class implements the Jess userfunction to send ACLMessages
   * directly from Jess.
   * It can be used by Jess by using the name <code>send</code>.
   */
  public class JessSend implements Userfunction {
    // data
    Agent my_agent;
    BasicJessBehaviour bjb;

    public JessSend(Agent a, BasicJessBehaviour b){ my_agent = a; bjb=b; }
    
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

      ACLMessage msg = bjb.JessFact2ACL(vv);
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
  Hashtable AIDCache;          // holds a local cache to map agent names to AID

  /**
   * Creates a <code>BasicJessBehaviour</code> instance
   *
   * @param agent the agent that adds the behaviour
   * @param jessFile the name of the Jess file to be executed
   */
    public BasicJessBehaviour(Agent agent, String jessFile){
      myAgent=agent;
      AIDCache = new Hashtable();
      // See info about the Display classes in Section 5 of Jess 4.1b6 Readme.htm
      NullDisplay nd = new NullDisplay();
      // Create a Jess engine
      jess = new Rete(nd);
      try {
	jess.addUserpackage((Userpackage)Class.forName("jess.MiscFunctions").newInstance());
      } catch (Throwable t) { System.out.println(t); }
      try { 
	// First I define the ACLMessage template
	jess.executeCommand(ACLJessTemplate());
        // Then I define the myagent template
	jess.executeCommand("(deftemplate MyAgent (slot name))");
	// Then I add the send function
	jess.addUserfunction(new JessSend(myAgent,this));
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
      assert(ACL2JessString(msg));
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
  

  private boolean isEmpty(String string) {
    return string == null || string.equals("");
  }
  
  /**
   * asserts a fact representing an ACLMessage in Jess. It is called after
   * the arrival of a message. 
   */
  private void assert(String fact) {
    try 		     { jess.executeCommand(fact); }
    catch (ReteException re) { re.printStackTrace((jess.display()).stderr()); }
  } 



  /*
   * replace a char in a String with a String
   * It is used to convert all the quotation marks in backslash quote
   * before asserting the content of a message in Jess.
   * @return the new String
   */
  private String stringReplace(String str, char oldChar, String s) {
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
  private String unquote(String str) {
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
  private String quote(java.lang.String str) {
    //replace all chars " in \ "
    return "\"" + stringReplace(str,'"',"\\\"") + "\""; 
  }


  /**
   * This method searches in the local cache for the full AID of the passed agentName.
   * If not found it creates a new AID where only the guid is set. 
  **/
  public AID getAIDFromCache(String agentName) {
    AID result;
    result = (AID)AIDCache.get(agentName);
    if (result == null) 
      result = new AID(agentName);
    return result;
  }

  /**
   * This method searches in the local cache for the full AID of the passed list of agent names.
   * @param list is a String of agent names separated by blanks
   * @return a List of AID
   */
  public List getAIDListFromCache(String list) {
    ArrayList l = new ArrayList();
    StringTokenizer st = new StringTokenizer(list);
    while (st.hasMoreTokens())  
      l.add(getAIDFromCache(st.nextToken()));
    return l;
  }

  /**
   * put a new AID in the local cache.
   * If one exists already with the same agentName, it is overwritten
   */ 
  public void putAIDInCache(AID aid) { 
    AIDCache.put(aid.getName(),aid);
  }

  /** @return a String with the deftemplate command to be executed in Jess **/
  public String ACLJessTemplate() {
    return "(deftemplate ACLMessage (slot communicative-act) (slot sender) (multislot receiver) (slot reply-with) (slot in-reply-to) (slot envelope) (slot conversation-id) (slot protocol) (slot language) (slot ontology) (slot content) (slot encoding) (multislot reply-to) (slot reply-by))";
  }

  /**
   * @return the ACLMessage representing the passed Jess Fact. This message 
   * will be then sent by the caller.
   */
  public ACLMessage JessFact2ACL(jess.ValueVector vv) throws jess.ReteException {
    // System.err.println("JessFact2ACL "+vv.toString());
    int perf = ACLMessage.getInteger(vv.get(3).stringValue());
    ACLMessage msg = new ACLMessage(perf);
    if (vv.get(4).stringValue() != "nil")
      msg.setSender(getAIDFromCache(vv.get(4).stringValue()));
    if (vv.get(5).toString() != "nil") {
      List l = getAIDListFromCache(vv.get(5).toString());
      for (int i=0; i<l.size(); i++)
	msg.addReceiver((AID)l.get(i));
    }
    if (vv.get(6).stringValue() != "nil")
      msg.setReplyWith(vv.get(6).stringValue());
    if (vv.get(7).stringValue() != "nil")
      msg.setInReplyTo(vv.get(7).stringValue());
    //if (vv.get(8).stringValue() != "nil")
    //  msg.setEnvelope(vv.get(8).stringValue());
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
    if (vv.get(14).stringValue() != "nil")
      msg.setEncoding(vv.get(14).stringValue());      
    //System.err.println("JessFact2ACL type is "+vv.get(15).type());
    if (vv.get(15).toString() != "nil") {
      List l = getAIDListFromCache(vv.get(15).toString());
      for (int i=0; i<l.size(); i++)
	msg.addReplyTo((AID)l.get(i)); 
    }
    if (vv.get(16).stringValue() != "nil")
      msg.setReplyBy(vv.get(16).stringValue());      
    return msg;
  }


  /**
   * @return the String representing the facts (even more than one fact is
   * allowed, but this method just returns one fact) 
   * to be asserted in Jess as a consequence of the receipt of
   * the passed ACL Message.
   * The messate content is quoted before asserting the Jess Fact.
   * It is unquoted by the JessFact2ACL function.
   */
   public String ACL2JessString(ACLMessage msg){ 
    String     fact;
    if (msg == null) return "";
    // I create a string that asserts the template fact
    fact = "(assert (ACLMessage (communicative-act " + ACLMessage.getPerformative(msg.getPerformative());     
    if (msg.getSender() != null) {        
      fact = fact + ") (sender " + msg.getSender().getName();
      putAIDInCache(msg.getSender());
    }
    Iterator i = msg.getAllReceiver();
    if (i.hasNext()) {
      fact = fact + ") (receiver ";
      while (i.hasNext()) {
	AID aid = (AID)i.next();
	putAIDInCache(aid);
	fact = fact + aid.getName();
      }
    }
    if (!isEmpty(msg.getReplyWith()))    fact=fact+") (reply-with " + msg.getReplyWith();
    if (!isEmpty(msg.getInReplyTo()))    fact=fact+") (in-reply-to " + msg.getInReplyTo();   
    //if (!isEmpty(msg.getEnvelope()))     fact=fact+") (envelope " + msg.getEnvelope();    
    if (!isEmpty(msg.getConversationId())) fact=fact+") (conversation-id " + msg.getConversationId(); 
    if (!isEmpty(msg.getProtocol()))     fact=fact+") (protocol " + msg.getProtocol();  
    if (!isEmpty(msg.getLanguage()))     fact=fact+") (language " + msg.getLanguage();    
    if (!isEmpty(msg.getOntology()))     fact=fact+") (ontology " + msg.getOntology();    
    if (msg.getContent() != null) 
      fact = fact + ") (content " + quote(msg.getContent());
    if (!isEmpty(msg.getEncoding()))     fact=fact+") (encoding " + msg.getEncoding();    
    i = msg.getAllReplyTo();
    if (i.hasNext()) {
      fact = fact + ") (reply-to ";
      while (i.hasNext()) {
	AID aid = (AID)i.next();
	putAIDInCache(aid);
	fact = fact + aid.getName();
      }
    }
    if (!isEmpty(msg.getReplyBy()))      fact=fact+") (reply-by " + msg.getReplyBy();    
    fact=fact+")))";
    return fact;
}

} // end JessBehaviour







