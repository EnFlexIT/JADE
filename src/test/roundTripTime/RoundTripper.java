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

import java.io.*;
import java.util.Iterator;
import java.util.Vector;

public class RoundTripper extends Agent {

    long tempoIniziale = 0;
    long tempoFinale = 0;
    long numIterazioni = 0;

    void roundTripTime() {
        send(msg);
        ACLMessage msg2 = blockingReceive();
    }


    int THR_UP = 0;
    int THR_LOW = 0;
    static int n_agenti = 0;
    synchronized void increaseNumAgents() {
	n_agenti++;
    }
    synchronized void decreaseNumAgents() {
	n_agenti--;
    }

    static Vector tempi = new Vector();
    synchronized void updateResults(long tempoIniziale, long tempoFinale, long numIterazioni) {
	long tempoTotale = tempoFinale - tempoIniziale;
	double avg = (double)tempoTotale/(double)numIterazioni;
	tempi.add(new Double(avg));
    }
    
    synchronized void printResults() {
	double totalTime = 0;
	for (int i=0; i<tempi.size(); i++)
	    totalTime += ((Double)tempi.elementAt(i)).doubleValue();
	System.out.println("RTT=" + totalTime/( (double)tempi.size()) + " per " + numIterazioni ); 
	System.exit(0);
    }

    AID receiver;
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    String ior = "";

    public void setup() {

	String receiverName = (String)(getArguments()[0]);
        numIterazioni = (new Long((String)(getArguments()[1]))).longValue();
        ior = (String)getArguments()[2];
	int numCoppie = Integer.parseInt(((String)(getArguments()[3]))); 

	receiver = new AID(receiverName, ((ior.length() > 9) ? AID.ISGUID : AID.ISLOCALNAME));
        if(ior.length() > 9){ //is GUID
            receiver.addAddresses(ior);
	}
        msg.addReceiver(receiver);
	//System.out.println(receiver);

	increaseNumAgents();

        addBehaviour( new CyclicBehaviour(this) {
		int counter = 0;

		int measuring=0;
		public void action() {
		    switch (measuring) {
		    case 0:
			if (n_agenti >= THR_UP) {
			    measuring=1;
			    //tempoIniziale = System.currentTimeMillis();
			    System.out.println(getLocalName()+" roundtripping with "+ receiver+" tempoIniziale= "+(tempoIniziale=System.currentTimeMillis()));
			    counter=0;
			}
			break;
		    case 1: 
			if (counter == numIterazioni) {
			    tempoFinale = System.currentTimeMillis();
			    System.out.println(getLocalName()+" tempoFinale= "+tempoFinale);
			    measuring=2;
			    decreaseNumAgents();
			    updateResults(tempoIniziale, tempoFinale, numIterazioni);
			    if (n_agenti <= THR_LOW) 
				printResults();
			}
			counter++;
			break;
		    default: 
		    }
		    roundTripTime();
		}
            });
    }


}
