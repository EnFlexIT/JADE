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
//import java.util.ArrayList;
import jade.lang.acl.ACLMessage;
//import jade.lang.acl.ACLCodec;
//import jade.mtp.MTP;
import jade.mtp.MTPException;
import jade.mtp.TransportAddress;

/**
 * Agent Communication Channel.
 * This class is the common interface for the lightweight,
 * and the full implementations of the Agent Communication Channel.
 * @author Giovanni Caire - TILAB
 * @author Nicolas Lhuillier - Motorola
 * @see jade.core.LightAcc, jade.core.fullAcc
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
    public String addMTP(String mtpClassName, String address) throws MTPException;
    
    public void removeMTP(String address) throws MTPException;

    public void addACLCodec(String codecClassName) throws jade.lang.acl.ACLCodec.CodecException;

    /**
     * Method declaration
     * 
     * @return
     * 
     * @see
     */
    public List getLocalAddresses();

    /**
     * Method declaration
     * 
     * @param address
     * @param ac
     */
    public void addRoute(String address, AgentContainer ac);

    /**
     * Method declaration
     * 
     * @param address
     * @param ac
     * 
     * @see
     */
    public void removeRoute(String address, AgentContainer ac);

    /**
     * Shut down this ACC
     */
    public void shutdown();


    public void dispatch(ACLMessage msg, AID receiverID) throws NotFoundException;

}

