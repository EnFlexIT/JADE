/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * LEAP license header to be added
 * SUN license header to be added (?)
 */
package jade.core;

import jade.lang.acl.ACLMessage;

/**
 * This class represents an agent proxy for a receiver agent that is living
 * in a remote container om the same platform.
 * 
 * @author Michael Watzke
 * @version 1.0, 09/11/2000
 */
public class RemoteContainerProxy implements RemoteProxy {

    /**
     * Class declaration
     * 
     * @author LEAP
     */
    public static class IMTPFailureException extends NotFoundException {

        /**
         */
        IMTPFailureException(String msg) {
            super(msg);
        }

    }

    private AgentContainer ref;
    private AID            receiver;

    /**
     * Constructor declaration
     */
    public RemoteContainerProxy() {}

    /**
     * Constructor declaration
     * 
     * @param ac
     * @param recv
     * 
     */
    public RemoteContainerProxy(AgentContainer ac, AID recv) {
        ref = ac;
        receiver = recv;
    }

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public AID getReceiver() {
        return receiver;
    } 

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public AgentContainer getRef() {
        return ref;
    } 

    /**
     * Method declaration
     * 
     * @param msg
     * 
     * @throws NotFoundException
     * 
     * @see
     */
    public void dispatch(ACLMessage msg) throws NotFoundException {
        try {
            ref.dispatch(msg, receiver);
        } 
        catch (IMTPException imtpe) {
            throw new IMTPFailureException("IMTP failure: [" 
                                           + imtpe.getMessage() + "]");
        } 
    } 

    /**
     * Method declaration
     * 
     * @throws UnreachableException
     * 
     * @see
     */
    public void ping() throws UnreachableException {
        try {
            ref.ping(false);
        } 
        catch (IMTPException imtpe) {
            throw new UnreachableException("Unreachable remote object");
        } 
    } 

    /**
     * Method declaration
     * 
     * @param cd
     * 
     * @see
     */

}

