package fipa.lang.acl;

import java.util.Hashtable;

// Class representing an ACL message

public interface Message {

  public String getKind();
  public String getValue(String name);

}

class MessageImpl implements Message, MessageStructure {

  // Constant objects for representing the different kinds of
  // messages.
  private static final Object acceptProposalKind = new Object();
  private static final Object agreeKind = new Object();
  private static final Object cancelKind = new Object();
  private static final Object cfpKind = new Object();
  private static final Object confirmKind = new Object();
  private static final Object disconfirmKind = new Object();
  private static final Object failureKind = new Object();
  private static final Object informKind = new Object();
  private static final Object informIfKind = new Object();
  private static final Object informRefKind = new Object();
  private static final Object notUnderstoodKind = new Object();
  private static final Object proposeKind = new Object();
  private static final Object queryIfKind = new Object();
  private static final Object queryRefKind = new Object();
  private static final Object refuseKind = new Object();
  private static final Object rejectProposalKind = new Object();
  private static final Object requestKind = new Object();
  private static final Object requestWhenKind = new Object();
  private static final Object requestWheneverKind = new Object();
  private static final Object subscribeKind = new Object();


  // An hash table is used to associate the various message kinds
  // with their names. The hash table is initialized only once.

  // Association between names and codes for message kind
  private static Hashtable messageKinds;

  // A Double-Checked Locking is used to protect hash table
  // initialization: A first check is made in the constructor, then a
  // monitor is acquired on entry (since this method is synchronized),
  // and another check is made. Notice how 'parameters' is assigned
  // only at the end of the method code.
  private static synchronized void initKinds() {

    // Second check
    if(messageKinds == null) {
      // Create and fill a temporary hash table
      Hashtable temp = new Hashtable(20, 1.0f);
      temp.put("accept-proposal", acceptProposalKind);
      temp.put("agree", agreeKind);
      temp.put("cancel", cancelKind);
      temp.put("cfp", cfpKind);
      temp.put("confirm", confirmKind);
      temp.put("disconfirm", disconfirmKind);
      temp.put("failure", failureKind);
      temp.put("inform", informKind);
      temp.put("inform-if", informIfKind);
      temp.put("inform-ref", informRefKind);
      temp.put("not-understood", notUnderstoodKind);
      temp.put("propose", proposeKind);
      temp.put("query-if", queryIfKind);
      temp.put("query-ref", queryRefKind);
      temp.put("refuse", refuseKind);
      temp.put("reject-proposal", rejectProposalKind);
      temp.put("request", requestKind);
      temp.put("request-when", requestWhenKind);
      temp.put("request-whenever", requestWheneverKind);
      temp.put("subscribe", subscribeKind);

      // At the end, assign the temporary hash table to the static
      // variable.
      messageKinds = temp;
    }
  }

  // The constructor; it first checks whether the static hash table
  // has been created and creates it if not.
  public MessageImpl() {    
    if(messageKinds == null) {
      initKinds();
    }
  }


  // Message parameters, using keyword names as hash table keys
  private Hashtable parameters;

  public String getKind() {
    return null;
  }

  public String getValue(String name) {
    return (String)parameters.get(name);
  }

}


