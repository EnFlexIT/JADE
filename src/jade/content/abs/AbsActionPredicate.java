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

import jade.content.ActionPredicate;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsActionPredicate extends AbsProposition implements ActionPredicate {

    /**
     * Construct an Abstract descriptor to hold an action predicate of
     * the proper type.
     * @param typeName The name of the type of the action predicate held by 
     * this abstract descriptor.
     */
    public AbsActionPredicate(String typeName) {
        super(typeName);
    }

    /**
     * Sets a generic action argument of the action predicate held by 
     * this abstract descriptor.
     * @param name The name of the argument to be set.
     * @param value The new value of the argument.
     */
    public void set(String name, AbsGenericAction value) {
        super.set(name, value);
    } 

    /**
     * Sets a term argument of the action predicate held by 
     * this abstract descriptor.
     * @param name The name of the argument to be set.
     * @param value The new value of the argument.
     */
    public void set(String name, AbsTerm value) {
        super.set(name, value);
    } 

    /**
     * Gets the value of a generic action argument of the action predicate 
     * held by this abstract descriptor.
     * @param name The name of the argument.
     * @return value The value of the argument.
     */
    public AbsGenericAction getAbsGenericAction(String name) {
        return (AbsGenericAction) getAbsObject(name);
    } 
    
    /**
     * Gets the value of a term argument of the action predicate 
     * held by this abstract descriptor.
     * @param name The name of the argument.
     * @return value The value of the argument.
     */
    public AbsTerm getAbsTerm(String name) {
        return (AbsTerm) getAbsObject(name);
    } 
}

