(REQUEST
 :sender (Agent-Identifier :name x@ior:002233)
 :receiver (set (Agent-Identifier :name y))
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content ((action AMS 
            (REGISTER 
             (Ams-agent-description
             :name (Agent-Identifier :name x@ior:002233)
             :ownership Test
             :state active
          ))))
)