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
public class AbsEquals extends AbsProposition {

    /**
     * Constructor
     *
     */
    public AbsEquals() {
        super(EqualsSchema.BASE_NAME);
    }

    /**
     * Sets the IRE describing the concept.
     *
     * @param ire the IRE describing the concept.
     *
     */
    public void setIRE(AbsIRE absIRE) {
        set(EqualsSchema.IRE, absIRE);
    } 

    /**
     * Sets the described concept.
     *
     * @param concept the concept.
     *
     */
    public void setConcept(AbsConcept concept) {
        set(EqualsSchema.CONCEPT, concept);
    } 

    /**
     * Gets the IRE describing the concept.
     *
     * @return the IRE.
     *
     */
    public AbsIRE getIRE() {
        return (AbsIRE) getAbsObject(EqualsSchema.IRE);
    } 

    /**
     * Gets the described concept.
     *
     * @return the described concept.
     *
     */
    public AbsConcept getConcept() {
        return (AbsConcept) getAbsObject(EqualsSchema.CONCEPT);
    }
}

