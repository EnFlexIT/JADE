; Remind that the ACLMessage has been defined with the following template:
; (deftemplate ACLMessage 
;              (slot communicative-act) (slot sender) (multislot receiver) 
;              (slot reply-with) (slot in-reply-to) (slot envelope) 
;              (slot conversation-id) (slot protocol) 
;              (slot language) (slot ontology) (slot content) )
; refer to Fipa97 Part 2 (www.cselt.it/fipa) for the description of the 
; ACLMessage parameters.
;
; Remind that Jade has also asserted for you the fact 
; (MyAgent (name <agentname)) that is usefull to know the name of your agent
;
; Finally, remind that Jade has built a userfunction called send
; to send messages to other agents. There are two styles to call send:
; ?m <- (assert (ACLMessage (communicative-act inform) (receiver agent)))
; (send ?m)
; or, in alternative
; (send (assert (ACLMessage (communicative-act inform) (receiver agent))))
; The two following rules show the usage of both styles. One of the two
; rules can be used


(defrule proposal
 "When a 'cfp' message arrives from an agent ?s, this rule asserts a 
  'propose' message to the same sender and retract the just arrived message"
 ?m <- (ACLMessage (communicative-act cfp) (sender ?s) (content ?c) (receiver ?r))
 =>
;(send (assert (ACLMessage (communicative-act propose) (receiver ?s) (content ?c))))
 (assert (ACLMessage (communicative-act propose) (sender ?r) (receiver ?s) (content ?c)))
 (retract ?m)
)

(defrule send-a-message
 "When a message is asserted whose sender is this agent, the message is
  sent and then retracted from the knowledge base."
 (MyAgent (name ?n))
 ?m <- (ACLMessage (sender ?n))
 =>
 (send ?m)
 (retract ?m)
)

(watch facts)
(reset) 

(run)  
; if you put run here, Jess is run before waiting for a message arrival,
; if you do not put (run here, the agent waits before for the arrival of the 
; first message and then runs Jess.








