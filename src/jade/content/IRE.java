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
public class IRE implements Proposition {
    private Proposition proposition = null;
    private Variable    variable = null;
    private String      kind = null;

    /**
     * Constructor
     *
     */
    public IRE() {}

    /**
     * Sets the proposition that describes the object.
     *
     * @param proposition the describing proposition.
     *
     */
    public void setProposition(Proposition proposition) {
        this.proposition = proposition;
    } 

    /**
     * Retrieves the proposition that descripes the object.
     *
     * @return the describing proposition.
     *
     */
    public Proposition getProposition() {
        return proposition;
    } 

    /**
     * Sets the variable associated with the described object.
     *
     * @param variable the variable to use.
     *
     */
    public void setVariable(Variable variable) {
        this.variable = variable;
    } 

    /**
     * Retrieves the variable associated with the described object.
     *
     * @return the variable.
     *
     */
    public Variable getVariable() {
        return variable;
    } 

    /**
     * Sets the cardinality of the query, i.e., the kind of the variable.
     *
     * @param kind the kind of the variable.
     *
     */
    public void setKind(String kind) {
        this.kind = kind;
    } 

    /**
     * Gets the cardinality of the query, i.e., the kind of the variable.
     *
     * @return the kind of the variable.
     *
     */
    public String getKind() {
        return kind;
    } 

}

