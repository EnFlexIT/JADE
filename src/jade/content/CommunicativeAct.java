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
package jade.content;

import jade.content.onto.*;
import jade.core.AID;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class CommunicativeAct extends GenericAction {
    private AID  sender = null;
    private List receivers = new ArrayList();

    /**
     * Constructor.
     *
     */
    public CommunicativeAct() {}

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
     * Sets the receivers.
     *
     * @param receivers the receivers.
     *
     */
    public void setReceivers(List receivers) {
        this.receivers = receivers;
    } 

    /**
     * Retrieves the receivers.
     *
     * @return the receivers.
     *
     */
    public List getReceivers() {
        return receivers;
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

