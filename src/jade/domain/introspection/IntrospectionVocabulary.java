package jade.domain.introspection;

public interface IntrospectionVocabulary {
	
// Concepts

  public static final String APDESCRIPTION					= "ap-description";
  public static final String APDESCRIPTION_NAME				= "name";
  public static final String APDESCRIPTION_DYNAMIC			= "dynamic";
  public static final String APDESCRIPTION_MOBILITY			= "mobility";
  public static final String APDESCRIPTION_TRANSPORTPROFILE = "transport-profile";
  
  public static final String APTRANSPORTDESCRIPTION 			  = "ap-transport-description";
  public static final String APTRANSPORTDESCRIPTION_AVAILABLEMTPS = "available-mtps";

  public static final String MTPDESCRIPTION					= "mtp-description";
  public static final String MTPDESCRIPTION_PROFILE			= "profile";
  public static final String MTPDESCRIPTION_NAME			= "mtp-name";
  public static final String MTPDESCRIPTION_ADDRESSES	    = "addresses"; 

  public static final String EVENTRECORD = "event-record";
  public static final String EVENTRECORD_WHAT = "what";
  public static final String EVENTRECORD_WHEN = "when";
  public static final String EVENTRECORD_WHERE = "where";
  
  public static final String ADDEDCONTAINER = "added-container";
  public static final String ADDEDCONTAINER_CONTAINER = "container";
  public static final String ADDEDCONTAINER_OWNERSHIP = "ownership";

  public static final String REMOVEDCONTAINER = "removed-container";
  public static final String REMOVEDCONTAINER_CONTAINER = "container";
  
  public static final String ADDEDMTP = "added-mtp";
  public static final String ADDEDMTP_ADDRESS = "address";
  public static final String ADDEDMTP_WHERE = "where";
    
  public static final String REMOVEDMTP = "removed-mtp";
  public static final String REMOVEDMTP_ADDRESS = "address";
  public static final String REMOVEDMTP_WHERE = "where";
  
  public static final String BORNAGENT = "born-agent";
  public static final String BORNAGENT_AGENT = "agent";
  public static final String BORNAGENT_WHERE = "where";
  public static final String BORNAGENT_STATE = "state";
  public static final String BORNAGENT_OWNERSHIP = "ownership";
  
  public static final String DEADAGENT = "dead-agent";
  public static final String DEADAGENT_AGENT = "agent";
  public static final String DEADAGENT_WHERE = "where";

  public static final String SUSPENDEDAGENT = "suspended-agent";
  public static final String SUSPENDEDAGENT_AGENT = "agent";
  public static final String SUSPENDEDAGENT_WHERE = "where";

  public static final String RESUMEDAGENT = "resumed-agent";
  public static final String RESUMEDAGENT_AGENT = "agent";
  public static final String RESUMEDAGENT_WHERE = "where";

  public static final String CHANGEDAGENTOWNERSHIP = "changed-agent-ownership";
  public static final String CHANGEDAGENTOWNERSHIP_AGENT = "agent";
  public static final String CHANGEDAGENTOWNERSHIP_FROM = "from";
  public static final String CHANGEDAGENTOWNERSHIP_TO = "to";
  public static final String CHANGEDAGENTOWNERSHIP_WHERE = "where";
    
  public static final String MOVEDAGENT = "moved-agent";
  public static final String MOVEDAGENT_AGENT = "agent";
  public static final String MOVEDAGENT_TO = "to";
  public static final String MOVEDAGENT_FROM = "from";
  
  public static final String CHANGEDAGENTSTATE = "changed-agent-state";
  public static final String CHANGEDAGENTSTATE_AGENT = "agent";
  public static final String CHANGEDAGENTSTATE_FROM = "from";
  public static final String CHANGEDAGENTSTATE_TO = "to";

  
  public static final String ADDEDBEHAVIOUR = "added-behaviour";
  public static final String ADDEDBEHAVIOUR_AGENT = "agent";
  public static final String ADDEDBEHAVIOUR_BEHAVIOUR = "behaviour";
  
  public static final String REMOVEDBEHAVIOUR = "removed-behaviour";
  public static final String REMOVEDBEHAVIOUR_AGENT = "agent";
  public static final String REMOVEDBEHAVIOUR_BEHAVIOUR = "behaviour";
  
  public static final String CHANGEDBEHAVIOURSTATE = "changed-behaviour-state";
  public static final String CHANGEDBEHAVIOURSTATE_AGENT = "agent";
  public static final String CHANGEDBEHAVIOURSTATE_BEHAVIOUR = "behaviour";
  public static final String CHANGEDBEHAVIOURSTATE_FROM = "from";
  public static final String CHANGEDBEHAVIOURSTATE_TO = "to";
    
  public static final String SENTMESSAGE = "sent-message";
  public static final String SENTMESSAGE_SENDER = "sender";
  public static final String SENTMESSAGE_MESSAGE = "message";
  
  public static final String RECEIVEDMESSAGE = "received-message";
  public static final String RECEIVEDMESSAGE_RECEIVER = "receiver";
  public static final String RECEIVEDMESSAGE_MESSAGE = "message";
   
  public static final String POSTEDMESSAGE = "posted-message";
  public static final String POSTEDMESSAGE_RECEIVER = "receiver";
  public static final String POSTEDMESSAGE_MESSAGE = "message";
  
  public static final String ROUTEDMESSAGE = "routed-message";
  public static final String ROUTEDMESSAGE_FROM = "from";
  public static final String ROUTEDMESSAGE_TO = "to";
  public static final String ROUTEDMESSAGE_MESSAGE = "message";
  
  public static final String CONTAINERID = "container-ID";
  public static final String CONTAINERID_NAME = "name";
  public static final String CONTAINERID_ADDRESS = "address";
  
  public static final String AGENTSTATE = "agent-state";
  public static final String AGENTSTATE_NAME = "name";
  
  public static final String BEHAVIOURID = "behaviour-ID";
  public static final String BEHAVIOURID_NAME = "name";
  public static final String BEHAVIOURID_KIND = "kind";
  public static final String BEHAVIOURID_CHILDREN = "children";
  
  public static final String ACLMESSAGE = "acl-message";
  public static final String ACLMESSAGE_ENVELOPE = "envelope";
  public static final String ACLMESSAGE_PAYLOAD = "payload";
  public static final String ACLMESSAGE_ACLREPRESENTATION = "acl-representation";
    
  public static final String ENVELOPE = "envelope";
  public static final String ENVELOPE_TO = "to";
  public static final String ENVELOPE_FROM = "from";
  public static final String ENVELOPE_COMMENTS = "comments";
  public static final String ENVELOPE_ACLREPRESENTATION = "acl-representation";
  public static final String ENVELOPE_PAYLOADLENGTH = "payload-length";
  public static final String ENVELOPE_PAYLOADENCODING = "payload-encoding";
  public static final String ENVELOPE_DATE = "date";
  public static final String ENVELOPE_ENCRYPTED = "encrypted";
  public static final String ENVELOPE_INTENDEDRECEIVER = "intended-receiver";
  public static final String ENVELOPE_RECEIVED = "received";
  
  public static final String RECEIVEDOBJECT = "received-object";
  public static final String RECEIVEDOBJECT_BY = "by";
  public static final String RECEIVEDOBJECT_FROM = "from";
  public static final String RECEIVEDOBJECT_DATE = "date";
  public static final String RECEIVEDOBJECT_ID = "id";
  public static final String RECEIVEDOBJECT_VIA = "via";
  
  public static final String CHANNEL = "channel";
  public static final String CHANNEL_NAME = "name";
  public static final String CHANNEL_PROTOCOL = "protocol";
  public static final String CHANNEL_ADDRESS = "address";
  

  public static final String PLATFORMDESCRIPTION = "platform-description";
  public static final String PLATFORMDESCRIPTION_PLATFORM = "platform";
  
    // Actions
  public static final String STARTNOTIFY = "start-notify";
  public static final String STARTNOTIFY_OBSERVER = "observer";
  public static final String STARTNOTIFY_EVENTS = "events";
  
  public static final String STOPNOTIFY = "stop-notify";
  public static final String STOPNOTIFY_OBSERVER= "observer";
  public static final String STOPNOTIFY_EVENTS = "events";

  // Predicates
  public static final String OCCURRED = "occurred";
  public static final String OCCURRED_EVENTRECORD = "event-record";
	
}