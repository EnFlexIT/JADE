/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;

import java.util.List;
import java.util.ArrayList;

import jade.lang.acl.ACLMessage;
import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.lang.acl.ACLCodec;

/**
 * Light implementation of the ACC. This class simply requests the
 * default router to route the outgoing message.
 * @author Giovanni Caire - CSELT
 * @version $Date$ $Revision$
 */
class LightAcc implements acc {

    // This is the default router to which outgoing messages are passed
    private AgentContainer router;

    /**
     * Constructor declaration
     */
    public LightAcc() {}

    /**
     * @see acc#initialize()
     */
    public void initialize(AgentContainerImpl ac, Profile p) {
        try {
        	String routerName = p.getParameter("accRouter");
        	if (routerName == null) {
        		// Use the main container by default
        		routerName = "Main-Container";
        	}
        	ContainerID routerID = new ContainerID(routerName, null);
        	
          MainContainer main = p.getMain();
          router = main.lookup(routerID);
          main = null;
          routerID = null;
          routerName = null;
        } 
        catch (Exception e) {
            System.out.println("Warning: error initializing ACC router");
        } 
    } 

    /**
     * @see acc#forwardMessage()
     */
    public void forwardMessage(ACLMessage msg, AID receiver, 
                               String address) throws MTPException {
        try {
            router.routeOut(msg, receiver, address);
        } 
        catch (IMTPException imtpe) {
            throw new MTPException("Routing error: "+imtpe.getMessage());
        } 
    } 

    /**
     * @see acc#getProxy()
     */
    public AgentProxy getProxy(AID agentID) throws NotFoundException {
        return new ACCProxy(agentID, this);
    } 

    /**
     * @see acc#addMTP()
     */
    public String addMTP(String mtpClassName, 
                                   String address) throws MTPException {
        // Throws an exception as this operation is not supported in
        // this ACC implementation
        throw new MTPException("Unsupported operation");
    } 

    /**
     * @see acc#addMTP()
     */
    public void removeMTP(String address) throws MTPException {
    	// Just do nothing 
    } 

    /**
     * @see acc#addACLCodec()
     */
    public void addACLCodec(String codecClassName) throws jade.lang.acl.ACLCodec.CodecException {
        // Throws an exception as this operation is not supported in
        // this ACC implementation
        throw new jade.lang.acl.ACLCodec.CodecException("Unsupported operation", null);
    }
    
    /**
     * @see acc#getLocalAddresses()
     */
    public List getLocalAddresses() {
        // Return an empty list
        return new ArrayList();
    } 

    /**
     * @see acc#addRoute()
     */
    public void addRoute(String address, AgentContainer ac) {
        // Just do nothing
    } 

    /**
     * @see acc#removeRoute()
     */
    public void removeRoute(String address, AgentContainer ac) {
        // Just do nothing
    } 

    /**
     * @see acc#shutdown()
     */
    public void shutdown() {
        // Just do nothing
    } 

}

