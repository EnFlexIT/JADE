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
import jade.content.Term;

/**
 * Note that an IRE is both a content element (as in the case of
 * a QUERY-REF communicative act) and a Term (as in the case of
 * (== (X) (iota ?x P(?x))
 * @author Paola Turci, Federico Bergenti - Universita` di Parma
 */
public class AbsIRE extends AbsContentElement implements Term {

    /**
     * Construct an Abstract descriptor to hold a IRE of
     * the proper type (e.g. ANY, IOTA, ALL...).
     * @param typeName The name of the type of the IRE held by 
     * this abstract descriptor.
     */
    public AbsIRE(String typeName) {
        super(typeName);
    }

    /**
     * Sets the variable of this IRE.
     * @param variable The abstract descriptor holding the variable.
     */
    public void setVariable(AbsVariable variable) {
        set(IRESchema.VARIABLE, variable);
    } 

    /**
     * Sets the proposition of this IRE.
     * @param proposition The abstract descriptor holding the proposition.
     */
    public void setProposition(AbsProposition proposition) {
        set(IRESchema.PROPOSITION, proposition);
    } 

    /**
     * Gets the variable of this IRE.
     * @return the abstract descriptor holding the variable of this IRE.
     */
    public AbsVariable getVariable() {
        return (AbsVariable) getAbsObject(IRESchema.VARIABLE);
    } 

    /**
     * Gets the proposition of this IRE.
     * @return the abstract descriptor holding the proposition of this IRE.
     */
    public AbsProposition getProposition() {
        return (AbsProposition) getAbsObject(IRESchema.PROPOSITION);
    } 

}

