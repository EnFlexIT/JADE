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
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsVariable extends AbsTerm {

    /**
     * Constructor
     *
     */
    public AbsVariable() {
        super(VariableSchema.BASE_NAME);
    }

    /**
     * Construct a variable with a <code>name</code> and a 
     * type of name <code>typeName</code>.
     *
     * @param name name of the variable.
     * @param typeName name of the type of the variable.
     *
     */
    public AbsVariable(String name, String typeName) {
        super(VariableSchema.BASE_NAME);

        setName(name);
        setType(typeName);
    }

    /**
     * Sets the name of the variable.
     *
     * @param name name of the variable.
     *
     */
    public void setName(String name) {
        setAttribute(BasicOntology.NAME, AbsPrimitive.wrap(name));
    } 

    /**
     * Sets the name of the type of the variable.
     *
     * @param name the name of the type of the variable.
     *
     */
    public void setType(String name) {
        setAttribute(BasicOntology.TYPE_NAME, AbsPrimitive.wrap(name));
    } 

    /**
     * Gets the name of the variable.
     *
     * @return the name of the variable.
     *
     */
    public String getName() {
        AbsPrimitive abs = (AbsPrimitive) getAttribute(BasicOntology.NAME);

        return abs.getStringValue();
    } 

    /**
     * Gets the name of the type of the variable.
     *
     * @return the name of the type.
     *
     */
    public String getType() {
        AbsPrimitive abs = 
            (AbsPrimitive) getAttribute(BasicOntology.TYPE_NAME);

        return abs.getStringValue();
    } 

}

