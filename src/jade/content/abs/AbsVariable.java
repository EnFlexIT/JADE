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
     * Construct an Abstract descriptor to hold a variable
     */
    public AbsVariable() {
        super(VariableSchema.BASE_NAME);
    }

    /**
     * Construct an AbsVariable with the given name and value type 
     * @param name The name of the variable.
     * @param valueType The type of values that can be assigned to 
     * this variable.
     *
     */
    public AbsVariable(String name, String valueType) {
        super(VariableSchema.BASE_NAME);

        setName(name);
        setType(valueType);
    }

    /**
     * Sets the name of this variable.
     * @param name The new name of this variable.
     */
    public void setName(String name) {
        set(VariableSchema.NAME, AbsPrimitive.wrap(name));
    } 

    /**
     * Sets the value type of this variable.
     * @param valueType The type of values that can be assigned to 
     * this variable.
     */
    public void setType(String valueType) {
        set(VariableSchema.VALUE_TYPE, AbsPrimitive.wrap(valueType));
    } 

    /**
     * Gets the name of this variable.
     * @return The name of this variable.
     */
    public String getName() {
        AbsPrimitive abs = (AbsPrimitive) getAbsObject(VariableSchema.NAME);
        if (abs != null) {
        	return abs.getString();
        }
        else {
        	return null;
        }
    } 

    /**
     * Gets the value type of this variable.
     * @return The type of values that can be assigned to 
     * this variable.
     */
    public String getType() {
        AbsPrimitive abs = (AbsPrimitive) getAbsObject(VariableSchema.VALUE_TYPE);
        if (abs != null) {
        	return abs.getString();
        }
        else {
        	return null;
        }
    } 

}

