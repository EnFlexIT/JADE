/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.core.messaging;

import jade.core.Service;
import jade.core.AID;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.IMTPException;
import jade.core.ServiceException;
import jade.core.NotFoundException;
import jade.core.NameClashException;

import jade.lang.acl.ACLMessage;

import jade.mtp.MTPDescriptor;
import jade.mtp.MTPException;


/**
   The horizontal interface for the JADE kernel-level service managing
   the message passing subsystem installed in the platform.

   @author Giovanni Rimassa - FRAMeTech s.r.l.
*/
public interface MessagingSlice extends Service.Slice {

    // Constants for the names of horizontal commands associated to methods
    static final String H_DISPATCHLOCALLY = "1";
    static final String H_ROUTEOUT = "2";
    static final String H_GETAGENTLOCATION = "3";
    static final String H_INSTALLMTP = "4";
    static final String H_UNINSTALLMTP ="5";
    static final String H_NEWMTP = "6";
    static final String H_DEADMTP = "7";
    static final String H_ADDROUTE = "8";
    static final String H_REMOVEROUTE = "9";

    void dispatchLocally(ACLMessage msg, AID receiverID) throws IMTPException, NotFoundException;
    void routeOut(ACLMessage msg, AID receiverID, String address) throws IMTPException, MTPException;
    ContainerID getAgentLocation(AID agentID) throws IMTPException, NotFoundException;

    MTPDescriptor installMTP(String address, String className) throws IMTPException, ServiceException, MTPException;
    void uninstallMTP(String address) throws IMTPException, ServiceException, NotFoundException, MTPException;

    void newMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException;
    void deadMTP(MTPDescriptor mtp, ContainerID cid) throws IMTPException, ServiceException;

    void addRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException;
    void removeRoute(MTPDescriptor mtp, String sliceName) throws IMTPException, ServiceException;

}
