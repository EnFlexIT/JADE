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
package jade.content.abs;

import jade.content.onto.*;
import jade.content.schema.*;
import java.util.Hashtable;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsCommunicativeAct extends AbsGenericAction {

    /**
     * Constructor
     *
     * @param name name of the communicative act.
     *
     */
    public AbsCommunicativeAct(String name) {
        super(name);
    }

    /**
     * Sets a parameter of the communicative act.
     *
     * @param name the name of the paramter.
     * @param value the value of the paramter.
     *
     * @throws UngroundedException
     *
     */
    public void set(String name, AbsContentElement value) 
            throws UngroundedException {
        if (!value.isGrounded()) {
            throw new UngroundedException();
        } 

        set(name, value);
    } 

    /**
     * Gets the value of a parameter.
     *
     * @param name the name of the parameter.
     *
     * @return the value of the parameter.
     *
     */
    public AbsContentElement getAbsContentElement(String name) {
        return (AbsContentElement) getAbsObject(name);
    } 

    /**
     * Sets the sender of the communicative act.
     *
     * @param sender the sender
     *
     */
    public void setSender(AbsAID sender) {
        set(ACLOntology.SENDER, sender);
    } 

    /**
     * Sets the receivers of the communicative act.
     *
     * @param receivers the list of receivers.
     *
     */
    public void setReceivers(AbsAggregate receivers) {
        set(ACLOntology.RECEIVERS, receivers);
    } 

    /**
     * Gets the sender of the communicative act.
     *
     * @return the sender.
     *
     */
    public AbsAID getSender() {
        return (AbsAID) getAbsObject(ACLOntology.SENDER);
    } 

    /**
     * Gets the receivers of the communicative act.
     *
     * @return the list of receivers.
     *
     */
    public AbsAggregate getReceivers() {
        return (AbsAggregate) getAbsObject(ACLOntology.RECEIVERS);
    } 

}

