/*
  $Log$
  Revision 1.2  1999/04/06 00:10:19  rimassa
  Documented public classes with Javadoc. Reduced access permissions wherever possible.

  Revision 1.1  1999/03/10 07:01:10  rimassa
  This file contains former FipaRequestClientBehaviour class of jade.domain
  package.

*/

package jade.proto;

import jade.core.*;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/** 
  This behaviour plays the <em>Initiator</em> role in
  <code>fipa-request</code> protocol. This is an abstract class,
  defining an abstract method for each message type expected from a
  <code>fipa-request</code> interaction.
  @see jade.proto.FipaRequestResponderBehaviour
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
*/
public abstract class FipaRequestInitiatorBehaviour extends SequentialBehaviour {

  private ComplexBehaviour firstReceive;
  private ComplexBehaviour secondReceive;

  /**
   Public constructor for this behaviour. Creates a
   <code>Behaviour</code> object that sends a <code>request</code> ACL
   message and calls user-defined methods to handle the different
   kinds of reply expected whithin a <code>fipa-request</code>
   interaction.
   @param client The agent this behaviour belongs to, that embodies
   <em>Initiator</em> role in this <code>fipa-request</code>
   interaction.
   @param request The <code>ACLMessage</code> object to be sent. When
   passed to this constructor, the message type is set to
   <code>request</code> and the <code>:protocol</code> slot is set to
   <code>fipa-request</code>, so there's no need to set them before
   caling the constructor. If present, <code>:conversation-id</code>
   and <code>:reply-with</code> slots are used for interaction
   labelling. Application programmer must ensure the following
   properties of <code>request</code> parameter when calling this
   constructor:
   <ol>
   <li> <code>request</code> has a valid <code>:receiver</code> slot value.
   <li> <code>request</code> has a valid <code>:content</code> slot value.
   <li> <code>request</code> has a valid <code>:language</code> slot value.
   <li> <code>request</code> has a valid <code>:ontology</code> slot value.
   </ol>
   However, only <code>:receiver</code> slot is actually used by this
   behaviour to send the message to the destination agent (only one
   receiver is supported by <code>fipa-request</code> protocol).
   @param template A <code>MessageTemplate</code> object used to match
   incoming replies. This behaviour automatically matches replies
   according to message type and <code>:protocol</code> slot value;
   also, <code>:conversation-id</code> and <code>:reply-to</code> slot
   values are matched when corresponding slot values are present in
   <code>request</code> parameter. This constructor argument can be
   used to match additional fields, such as <code>:language</code> and
   <code>:ontology</code> slots.
  */
  public FipaRequestInitiatorBehaviour(Agent client, ACLMessage request, MessageTemplate template) {
    super(client);

    // Set type and protocol for request
    request.setType("request");
    request.setProtocol("fipa-request");

    String destName = request.getFirstDest();

    // Create all necessary MessageTemplate objects
    MessageTemplate FipaRequestTemplate = MessageTemplate.and(
      MessageTemplate.MatchProtocol("fipa-request"),
      MessageTemplate.MatchSource(destName));
    String convID = request.getConversationId();
    String replyWith = request.getReplyWith();
    if(convID != null)
      FipaRequestTemplate = MessageTemplate.and(
        MessageTemplate.MatchConversationId(convID),
	FipaRequestTemplate);
    if(replyWith != null)
      FipaRequestTemplate = MessageTemplate.and(
        MessageTemplate.MatchReplyTo(replyWith),
	FipaRequestTemplate);
    FipaRequestTemplate = MessageTemplate.and(template, FipaRequestTemplate);

    final MessageTemplate NotUnderstoodTemplate = MessageTemplate.and(
      FipaRequestTemplate, MessageTemplate.MatchType("not-understood"));

    final MessageTemplate RefuseTemplate = MessageTemplate.and(
      FipaRequestTemplate, MessageTemplate.MatchType("refuse"));

    final MessageTemplate AgreeTemplate = MessageTemplate.and(
      FipaRequestTemplate, MessageTemplate.MatchType("agree"));

    final MessageTemplate FailureTemplate = MessageTemplate.and(
      FipaRequestTemplate, MessageTemplate.MatchType("failure"));

    final MessageTemplate InformTemplate = MessageTemplate.and(
      FipaRequestTemplate, MessageTemplate.MatchType("inform"));


    firstReceive = NonDeterministicBehaviour.createWhenAny(client);
    firstReceive.addSubBehaviour(new SimpleBehaviour(client) {

      private boolean finished = false;
      public void action() {
	// Receive 'not-understood'
	ACLMessage msg = myAgent.receive(NotUnderstoodTemplate);
	if(msg != null) {
	  handleNotUnderstood(msg);
	  finished = true;
	}
	else
	  block();
      }

      public boolean done() {
	return finished;
      }

      public void reset() {
	finished = false;
      }

    });
    firstReceive.addSubBehaviour(new SimpleBehaviour(client) {

      private boolean finished = false;
      public void action() {
	// Receive 'refuse'
	ACLMessage msg = myAgent.receive(RefuseTemplate);
	if(msg != null) {
	  handleRefuse(msg);
	  finished = true;
	}
	else 
	  block();
      }

      public boolean done() {
	return finished;
      }

      public void reset() {
	finished = false;
      }

    });
    firstReceive.addSubBehaviour(new SimpleBehaviour(client) {

      private boolean finished = false;
      public void action() {
	// Receive 'agree'
	ACLMessage msg = myAgent.receive(AgreeTemplate);
	if(msg != null) {
	  handleAgree(msg);
	  // Add a second NonDeterministicBehaviour to the main behaviour
	  FipaRequestInitiatorBehaviour.this.addSubBehaviour(secondReceive);
	  finished = true;
	}
	else
	  block();
      }

      public boolean done() {
	return finished;
      }

      public void reset() {
	if(finished)
	  FipaRequestInitiatorBehaviour.this.removeSubBehaviour(secondReceive);
	finished = false;
      }

    });

    secondReceive = NonDeterministicBehaviour.createWhenAny(client);
    secondReceive.addSubBehaviour(new SimpleBehaviour(client) {
      private boolean finished = false;
      public void action() {
	// Receive 'failure'
	ACLMessage msg = myAgent.receive(FailureTemplate);
	if(msg != null) {
	  handleFailure(msg);
	  finished = true;
	}
	else 
	  block();
      }

      public boolean done() {
	return finished;
      }

      public void reset() {
	finished = false;
      }

    });
    secondReceive.addSubBehaviour(new SimpleBehaviour(client) {
      private boolean finished = false;
      public void action() {
	// Receive 'inform'
	ACLMessage msg = myAgent.receive(InformTemplate);
	if(msg != null) {
	  handleInform(msg);
	  finished = true;
	}
	else
	  block();
      }

      public boolean done() {
	return finished;
      }

      public void reset() {
	finished = false;
      }

    });


    addSubBehaviour(new SenderBehaviour(client, request));
    addSubBehaviour(firstReceive);      

  }

  /**
    Abstract method to handle <code>not-understood</code>
    replies. This method must be implemented by
    <code>FipaRequestInitiatorBehaviour</code> subclasses to react to
    <code>not-understood</code> messages from the peer agent.
    @param reply The actual ACL message received. It is of
    <code>not-understood</code> type and matches the conversation
    template.
  */
  protected abstract void handleNotUnderstood(ACLMessage reply);

  /**
    Abstract method to handle <code>refuse</code> replies. This method
    must be implemented by <code>FipaRequestInitiatorBehaviour</code>
    subclasses to react to <code>refuse</code> messages from the peer
    agent.
    @param reply The actual ACL message received. It is of
    <code>refuse</code> type and matches the conversation
    template.
  */
  protected abstract void handleRefuse(ACLMessage reply);

  /**
    Abstract method to handle <code>agree</code> replies. This method
    must be implemented by <code>FipaRequestInitiatorBehaviour</code>
    subclasses to react to <code>agree</code> messages from the peer
    agent.
    @param reply The actual ACL message received. It is of
    <code>agree</code> type and matches the conversation
    template.
  */
  protected abstract void handleAgree(ACLMessage reply);

  /**
    Abstract method to handle <code>failure</code>
    replies. This method must be implemented by
    <code>FipaRequestInitiatorBehaviour</code> subclasses to react to
    <code>failure</code> messages from the peer agent.
    @param reply The actual ACL message received. It is of
    <code>failure</code> type and matches the conversation
    template.
  */
  protected abstract void handleFailure(ACLMessage reply);

  /**
    Abstract method to handle <code>inform</code> replies. This method
    must be implemented by <code>FipaRequestInitiatorBehaviour</code>
    subclasses to react to <code>inform</code> messages from the peer
    agent.
    @param reply The actual ACL message received. It is of
    <code>inform</code> type and matches the conversation
    template.
  */
  protected abstract void handleInform(ACLMessage reply);


}
