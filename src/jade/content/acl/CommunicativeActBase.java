/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.content.acl;

import jade.content.*;
import jade.content.onto.*;

import jade.core.AID;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class CommunicativeActBase implements CommunicativeAct {
    private AID  sender = null;
    private List receivers = new ArrayList();

    /**
     * Constructor.
     *
     */
    public CommunicativeActBase() {}

    /**
     * Sets the <code>sender</code>.
     *
     * @param sender the sender.
     *
     */
    public void setSender(AID sender) {
        this.sender = sender;
    } 

    /**
     * Retrieves the <code>sender</code>.
     *
     * @return the sender.
     *
     */
    public AID getSender() {
        return sender;
    } 

    /**
     * Clears the receiver list.
     *
     * @param receivers the receivers.
     *
     */
    public void clearAllReceiver() {
        receivers.clear();
    } 

    /**
     * Retrieves the receivers.
     *
     * @return the receivers.
     *
     */
    public Iterator getAllReceiver() {
        return receivers.iterator();
    } 

    /**
     * Add a new receiver to the list.
     *
     * @param aid the AID of the receiver.
     *
     */
    public void addReceiver(AID aid) {
        receivers.add(aid);
    } 

}

