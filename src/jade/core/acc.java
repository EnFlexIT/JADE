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
import jade.lang.acl.ACLMessage;

import jade.mtp.MTPException;
import jade.mtp.TransportAddress;
import jade.mtp.MTPDescriptor;

/**
 * Agent Communication Channel.
 * This class is the common interface for the lightweight,
 * and the full implementations of the Agent Communication Channel.
 * @author Giovanni Caire - TILAB
 * @author Nicolas Lhuillier - Motorola
 * @see jade.core.LightAcc 
 * @see jade.core.FullAcc
 * @version $Date$ $Revision$
 */
interface acc {

    public static class UnknownACLEncodingException extends NotFoundException {
      UnknownACLEncodingException(String msg) {
	super(msg);
      }
    } // End of UnknownACLEncodingException class

    /**
     * Initialize the ACC object
     * 
     * @param ac the container
     * @param pm the profile to retrieve information
     */
    public void initialize(AgentContainerImpl ac, Profile p);

    /**
     * Method declaration
     * 
     * @param env
     * @param payload
     * @param address
     * 
     * @throws MTPException
     */
    public void forwardMessage(ACLMessage msg, AID receiver, 
                               String address) throws MTPException;

    /**
     * Method declaration
     * 
     * @param proto
     * @param address
     * 
     * @return
     * 
     * @throws MTPException
     */
    public MTPDescriptor addMTP(String mtpClassName, String address) throws MTPException;

    public MTPDescriptor removeMTP(String address) throws MTPException;

    public void addACLCodec(String codecClassName) throws jade.lang.acl.ACLCodec.CodecException;

    /**
     * Method declaration
     * 
     * @param mtp
     * @param ac
     */
    public void addRoute(MTPDescriptor mtp, AgentContainer ac);

    /**
     * Method declaration
     * 
     * @param mtp
     * @param ac
     * 
     * @see
     */
    public void removeRoute(MTPDescriptor mtp, AgentContainer ac);

    /**
     * Shut down this ACC
     */
    public void shutdown();


    public void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException;

  	/**
       Add all platform addresses to the given AID
   	*/
  	public void addPlatformAddresses(AID id);
}

