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

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsConcept extends AbsTerm {

    /**
     * Constructor
     *
     * @param name the name of the concept.
     *
     */
    public AbsConcept(String name) {
        super(name);
    }

    /**
     * Sets a slot of the concept.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void set(String name, AbsTerm value) {
        super.set(name, value);
    } 

    /**
     * Sets a string slot.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void set(String name, String value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Sets a boolean slot.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void set(String name, boolean value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Sets an integer slot.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void set(String name, int value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Sets a float slot.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void set(String name, float value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Retrieves the value of a slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public AbsTerm getAbsTerm(String name) {
        return (AbsTerm)getAbsObject(name);
    }

   /**
     * Retrieves the value of a String slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public String getString(String name) {
        return ((AbsPrimitive)getAbsTerm(name)).getString();
    }

    /**
     * Retrieves the value of a boolean slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public boolean getBoolean(String name) {
      	return ((AbsPrimitive)getAbsTerm(name)).getBoolean();
    }

    /**
     * Retrieves the value of an integer slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public int getInteger(String name) {
    	return ((AbsPrimitive)getAbsTerm(name)).getInteger();
    }

    /**
     * Retrieves the value of a float slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public float getFloat(String name) {
    	return ((AbsPrimitive)getAbsTerm(name)).getFloat();
    }
}

