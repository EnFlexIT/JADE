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

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

import jade.lang.acl.ACLMessage;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.mtp.MTPDescriptor;

/**
 * Light implementation of the ACC. This class simply requests the
 * default router to route the outgoing message.
 * @author Giovanni Caire - CSELT
 * @version $Date$ $Revision$
 */
class LightAcc implements acc {

    // This is the default router to which outgoing messages are passed
    private AgentContainer router;
    // The list of all platform addresses
    private List platformAddresses = new ArrayList();

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
        	
          router = p.getPlatform().lookup(routerID);
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
     * @see acc#addMTP()
     */
    public MTPDescriptor addMTP(String mtpClassName, 
                                   String address) throws MTPException {
        // Throws an exception as this operation is not supported in
        // this ACC implementation
        throw new MTPException("Unsupported operation");
    } 

    /**
     * @see acc#removeMTP()
     */
    public MTPDescriptor removeMTP(String address) throws MTPException {
      // Just do nothing 
      return null;
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
     * @see acc#addRoute()
     */
    public void addRoute(MTPDescriptor mtp, AgentContainer ac) {
      // Just update the list of platform addresses
    	synchronized (this) { // Mutual exclusion with addPlatformAddresses()
    		platformAddresses.add(mtp.getAddress());
    	}
    } 

    /**
     * @see acc#removeRoute()
     */
    public void removeRoute(MTPDescriptor mtp, AgentContainer ac) {
      // Just update the list of platform addresses
    	synchronized (this) { // Mutual exclusion with addPlatformAddresses()
    		platformAddresses.remove(mtp.getAddress());
    	}
    } 

  /**
     Add all platform addresses to the given AID
   */
  public void addPlatformAddresses(AID id) {
  	synchronized (this) { // Mutual exclusion with add/removeRoute()
  		Iterator it = platformAddresses.iterator();
    	while(it.hasNext()) {
      	String addr = (String)it.next();
      	id.addAddresses(addr);
    	}
  	}
  }
  
    /**
     * @see acc#shutdown()
     */
    public void shutdown() {
        // Just do nothing
    } 

    /**
     * @see acc#dispatch()
     */
    public void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException { 
    	Iterator addresses = receiverID.getAllAddresses();
    	while(addresses.hasNext()) {
      	String address = (String)addresses.next();
      	try {
					forwardMessage(msg, receiverID, address);
					return;
      	}
      	catch(MTPException mtpe) {
					System.out.println("Bad address [" + address + "]: trying the next one...");
      	}
    	}
    	throw new NotFoundException("No valid address contained within the AID.");
  	}

}

