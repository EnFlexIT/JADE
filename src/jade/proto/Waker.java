package jade.proto;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import java.lang.InterruptedException;


/**
 * This class allows to implement timeout for agents.
 * When the timeout terminates, this class sends a message to the agent
 * that started the class itself. In this way, an agent blocked in a 
 * <code>receive</code> method, is waken up.
 * The class must be used as follows:
 * <ul>
 * <li> creates an ACLMessage object
 * with all the parameters set as wanted.
 * Remind to set the parameter with the same template of
 * ACLMessage passed as parameter to the <code>receive</code> method, otherwise
 * the agent will never be waken up. It is good practice, to set a parameter
 * in a way that allows you to understand that this is a wake-up message 
 * (e.g. the message content, or the conversation-id, ...)
 * <li> execute the code <code> (new Waker(myAgent,wakeMsg,timeout)).start();
 * </code>, where <code>myAgent</code> is the pointer to the agent,
 * <code>wakeMsg</code> is the ACLMessage to be sent, and
 * <code>timeout</code> is the timeout to be waited.
 * </ul> 
 */
public class Waker extends Thread {
    
    Agent myAgent;
    ACLMessage myMsg;
    long myTimeout;
    
    /**
    * class constructor
    * @param a is the agent to which the wake-up message must be sent
    * @param msg is the message to be sent
    * @param timeout is the sleeping time before sending the message
    */
    Waker(Agent a, ACLMessage msg, long timeout) {
        myAgent=a;
        myMsg = msg;
        myTimeout = timeout;
    }
    
    public void run() {
        System.err.println("Waker sleeping for " +myTimeout);
        try {
            sleep(myTimeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("Waker has been interrupted while sleeping");
        }
        myMsg.setDest(myAgent.getName());
        System.err.println("Waker wakes-up "+myAgent.getName()+" after "+myTimeout);
        myAgent.send(myMsg);
    }
}




