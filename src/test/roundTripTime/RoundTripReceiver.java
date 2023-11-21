package test.roundTripTime;

/**
 * Title:        RoundTrip tra piu' agenti
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author Elisabetta Cortese
 * @version 1.0
 */


import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;

public class RoundTripReceiver extends Agent {

    void roundTripTime() {
        ACLMessage msg = blockingReceive();
        msg = msg.createReply();
        send(msg);
    }

    public void setup() {

        addBehaviour( new CyclicBehaviour(this)
        {
            public void action() {
                roundTripTime();
            }
        });
    }
}
