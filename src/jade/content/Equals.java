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

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.content.onto.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class Equals implements Proposition {
    private IRE     ire     = null;
    private Concept concept = null;
   
    /**
     * Constructor
     *
     */
    public Equals() {}

    /**
     * Sets the IRE that describes the object.
     *
     * @param ire the describing IRE.
     *
     */
    public void setIRE(IRE ire) {
        this.ire = ire;
    } 

    /**
     * Retrieves the IRE that descripes the object.
     *
     * @return the describing IRE.
     *
     */
    public IRE getIRE() {
        return ire;
    } 

    /**
     * Sets the described concept.
     *
     * @param concept the described concept.
     *
     */
    public void setConcept(Concept concept) {
        this.concept = concept;
    } 

    /**
     * Retrieves the described concept.
     *
     * @return the described concept.
     *
     */
    public Concept getConcept() {
        return concept;
    } 
}

