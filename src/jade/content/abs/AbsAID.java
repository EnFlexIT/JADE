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
import java.util.Vector;
import java.util.Enumeration;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsAID extends AbsTerm {

    /**
     * Constructor
     *
     * @param name the name
     * @param addresses the list of addresses
     * @param resolvers the list of resolvers
     * @param userDefinedSlots list of user-defined slots.
     *
     */
    public AbsAID(String name, AbsAggregate addresses, 
                  AbsAggregate resolvers, AbsAggregate userDefinedSlots) {
        super(BasicOntology.AID);

        setAttribute(BasicOntology.NAME, AbsPrimitive.wrap(name));

        if (addresses != null) {
            setAttribute(BasicOntology.ADDRESSES, addresses);
        } 

        if (resolvers != null) {
            setAttribute(BasicOntology.RESOLVERS, resolvers);
        } 

        if (userDefinedSlots != null) {
            setAttribute(BasicOntology.USER_DEFINED_SLOTS, userDefinedSlots);
        } 
    }

}

