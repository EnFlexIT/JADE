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

/**
 * @author Paola Turci, Federico Bergenti - Universita` di Parma
 */
public class AbsIRE extends AbsProposition {

    /**
     * Constructor
     *
     */
    public AbsIRE() {
        super(IRESchema.BASE_NAME);
    }

    /**
     * Sets the kind of the IRE, i.e., the cardinality of the query.
     *
     * @param kind the kind of the IRE.
     *
     */
    public void setKind(String kind) {
        set(IRESchema.KIND, AbsPrimitive.wrap(kind));
    } 

    /**
     * Sets the variable.
     *
     * @param variable the variable.
     *
     */
    public void setVariable(AbsVariable variable) {
        set(IRESchema.VARIABLE, variable);
    } 

    /**
     * Sets the proposition.
     *
     * @param proposition the proposition.
     *
     */
    public void setProposition(AbsProposition proposition) {
        set(IRESchema.PROPOSITION, proposition);
    } 

    /**
     * Gets the kind of the IRE, i.e., the cardinality of the query.
     *
     * @return the kind of the IRE.
     *
     */
    public String getKind() {
        return ((AbsPrimitive) getAbsObject(IRESchema.KIND)).getString();
    } 

    /**
     * Gets the variable.
     *
     * @return the variable.
     *
     */
    public AbsAggregate getVariable() {
        return (AbsAggregate) getAbsObject(IRESchema.VARIABLE);
    } 

    /**
     * Gets the proposition.
     *
     * @return the proposition.
     *
     */
    public AbsProposition getProposition() {
        return (AbsProposition) getAbsObject(IRESchema.PROPOSITION);
    } 

}

