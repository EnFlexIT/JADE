(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name DF@IOR:001122)
            (SEARCH
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233))
             (search-constraints)
            )))
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)


STOP
(FAILURE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ( already-registered
            not-registered
            (internal-error "Nothing was possibile")
 )
 :X-comment "a bit of failure exceptions are here. Notice that a number is not a String and therefore you must quote it."
)



(REFUSE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ( unauthorised
            (unsupported-function PLUS) 
            (missing-argument ACTION)
            (unexpected-argument Agent-Identifier)
            unexpected-argument-count
            (missing-parameter Agent-Identifier name)
            (unexpected-parameter Agent-Identifier X-userdefined)
            (unrecognised-parameter-value Agent-Identifier "20")
 )
 :X-comment "a bit of refuse exceptions are here. Notice that a number is not a String and therefore you must quote it."
)


(NOT-UNDERSTOOD
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ( (unsupported-act INFORMA) 
            (unexpected-act PROPOSE)
            (unsupported-value content)
            (unrecognised-value ontology)

 )
 :X-comment "a bit of not-understood exceptions are here"
)



(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name AMS@IOR:001122)
            (QUIT (Agent-Identifier :name peppino))
          ))
 :X-comment "Request AMS to Quit the agent peppino"
)


(INFORM
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((done (action (Agent-Identifier :name AMS@IOR:001122)
            (QUIT (Agent-Identifier :name peppino)))
          ))
 :X-comment "Done Quit the agent peppino"
)







(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name DF@IOR:001122)
            (GET-DESCRIPTION)
          ))
 :X-comment ""
)

(AGREE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name DF@IOR:001122)
            (GET-DESCRIPTION)
          ) true)
 :X-comment ""
)

(INFORM
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content (( result (action (Agent-Identifier :name DF@IOR:001122)
                            (GET-DESCRIPTION))
           (AP-Description 
             :name JADE
             :dynamic true
             :mobility true
             :transport-profile 
              (AP-Transport-Description
               :available-mtps (set
                (mtp-description :profile alpha :mtp-name IIOP
                 :addresses (sequence iiop://cselt.it:50/acc)))))
          ))
 :X-comment "Inform of the result of the action"
)



(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name DF@IOR:001122)
            (MODIFY 
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233)
          ))) )
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)

(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name DF@IOR:001122)
            (DEREGISTER
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233 :x-dummy (AID gg))
          ))) )
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)





(REQUEST
 :sender (Agent-Identifier :name x@ior:002233)
 :receiver (set (Agent-Identifier :name y) 
                (Agent-Identifier :name z :addresses (sequence iiop://100.1/a))
                (Agent-Identifier :name z :resolvers (sequence (Agent-Identifier :name p))))
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name DF@IOR:001122)
            (REGISTER 
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233)
             :services (set
               (Service-Description
                :name Service1
                :type TestType
                :ontology (set O1 O2 O3)
                :protocols (set P1 P2 P3)
                :ownership CSELT
                :properties (set (Property :name X1 :value V1) 
          			 (Property :name X3 :value 12) 
          			 (Property :name X4 :value -12.23) 
          			 (Property :name X5 :value 20001211T100025100A)
          			 (Property :name X6 :value (Agent-Identifier :name x))
          			 (Property :name X7 :value (Dummy :name x))
                                 (Property :name X2 :value V2) )))
             :protocols (set FIPA-Request FIPA-Contract-Net)
             :ontology (set FIPA-Agent-Management Trading)
             :language (set FIPA-SL0)
          ))))
 :X-comment "Request to register a Description with the DF"
)

(AGREE
 :sender (Agent-Identifier :name x@ior:002233)
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name DF@IOR:001122)
            (REGISTER 
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233)
          ))) true )
 :X-comment "In the agree message you must put all the mandatory slots, otherwise you get an exception."
)

(INFORM
 :sender (Agent-Identifier :name x@ior:002233)
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((done (action (Agent-Identifier :name DF@IOR:001122)
            (REGISTER 
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233)
          ))) ))
 :X-comment "In the message you must put all the mandatory slots, otherwise you get an exception."
)


(REQUEST
 :sender (Agent-Identifier :name x@ior:002233)
 :receiver (set (Agent-Identifier :name y))
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name AMS)
            (REGISTER 
             (Ams-agent-description
             :name (Agent-Identifier :name x@ior:002233 :X-userdef ciao)
             :ownership Test
             :state active
          ))))
 :X-comment "Request the AMS to register an agent description"
)

(AGREE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action (Agent-Identifier :name AMS)
            (REGISTER 
             (Ams-agent-description
             :name (Agent-Identifier :name x@ior:002233)
             :ownership Test
             :state active
          ))) true)
 :X-mycomment Just_A_Test
 :X-myparam2 "0x23AF"
)

(INFORM
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content (
  (done (action (Agent-Identifier :name AMS)
            (REGISTER 
             (Ams-agent-description
             :name (Agent-Identifier :name x@ior:002233)
             :ownership Test
             :state active
          )))
 ))
 :X-mycomment Just_A_Test
 :X-myparam2 "0x23AF"
)

