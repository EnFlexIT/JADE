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
    static long numIterazioni = 0;

    void roundTripTime() {
        send(msg);
        ACLMessage msg2 = blockingReceive();
    }


    static int THR_UP = 100; // percentage of agents that must have been created before starting to count roundtrip
    static int THR_LOW = 0;
    private static int n_agenti = 0;
    private static int terminatedAgents = 0;
    private static boolean resultPrinted = false;
    synchronized static void increaseNumAgents(int numCoppie) {
	if (n_agenti == 0) {
	    THR_UP = Math.round(THR_UP * numCoppie / 100.0f);
	    System.out.println("The roundtrippers will measure time when at least "+THR_UP+" agents have been created and "+THR_LOW+" agents are still working.");
	}
	n_agenti++;
    }
    synchronized static void decreaseNumAgents() {
        terminatedAgents++;
    }

    static Vector tempi = new Vector();
    synchronized static void updateResults(long tempoIniziale, long tempoFinale, long numIterazioni) {
	long tempoTotale = tempoFinale - tempoIniziale;
	double avg = (double)tempoTotale/(double)numIterazioni;
	tempi.add(new Double(avg));
    }
    
    synchronized static double printResults() {
	double totalTime = 0;
	for (int i=0; i<tempi.size(); i++)
	    totalTime += ((Double)tempi.elementAt(i)).doubleValue();
	    double rtt = totalTime/( (double)tempi.size());
	System.out.println("RTT=" + rtt + " per " + numIterazioni ); 
	return rtt;
    }

    synchronized static boolean startMeasuring() {
	return ( n_agenti >= THR_UP);
    }

    synchronized static boolean stopTripping(int numCoppie) {
	return ((numCoppie - terminatedAgents) <= THR_LOW);
    }


    AID receiver;
    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
    String ior = "";
    int numCoppie = 0;
		AID controller = null;
		
    public void setup() {
			Object[] args = getArguments();
	String receiverName = (String)(args[0]);
        numIterazioni = (new Long((String)(args[1]))).longValue();
        ior = (String)args[2];
	numCoppie = Integer.parseInt(((String)(args[3]))); 
	receiver = new AID(receiverName, ((ior.length() > 9) ? AID.ISGUID : AID.ISLOCALNAME));
        if(ior.length() > 9){ //is GUID
            receiver.addAddresses(ior);
	}
        msg.addReceiver(receiver);
        if (args.length == 5) {
        	controller = new AID((String) args[4], AID.ISLOCALNAME);
        }
	//System.out.println(receiver);

	increaseNumAgents(numCoppie);

        addBehaviour( new CyclicBehaviour(this) {
		int counter = 0;

		int measuring=0;
		public void action() {
		    switch (measuring) {
		    case 0:
			if (startMeasuring()) {
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
			    if (stopTripping(numCoppie)) {
			    	if (!resultPrinted) {
			    		resultPrinted = true;
				    	double rtt = printResults();
				    	if (controller != null) {
				    		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				    		msg.addReceiver(controller);
			  	  		msg.setContent(String.valueOf(rtt));
			    			myAgent.send(msg);
			    		}
			    		else {
			    			System.exit(0);
			    		}
			    	}
			    }
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
