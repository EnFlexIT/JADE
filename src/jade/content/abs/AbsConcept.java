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
    public void setSlot(String name, AbsTerm value) {
        setAttribute(name, value);
    } 

    /**
     * Sets a string slot.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void setSlot(String name, String value) {
        setAttribute(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Sets a boolean slot.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void setSlot(String name, boolean value) {
        setAttribute(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Sets an integer slot.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void setSlot(String name, int value) {
        setAttribute(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Sets a float slot.
     *
     * @param name name of the slot.
     * @param value value of the slot.
     *
     */
    public void setSlot(String name, float value) {
        setAttribute(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Retrieves the value of a slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public AbsTerm getAbsTermSlot(String name) {
        return (AbsTerm)getAttribute(name);
    }

   /**
     * Retrieves the value of a String slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public String getStringSlot(String name) {
        return ((AbsPrimitive)getAttribute(name)).getStringValue();
    }

    /**
     * Retrieves the value of a boolean slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public boolean getBooleanSlot(String name) {
      	return ((AbsPrimitive)getAttribute(name)).getBooleanValue();
    }

    /**
     * Retrieves the value of an integer slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public int getIntegerSlot(String name) {
    	return ((AbsPrimitive)getAttribute(name)).getIntegerValue();
    }

    /**
     * Retrieves the value of a float slot.
     *
     * @param name.
     * @return the value.
     *
     */
    public float getRealSlot(String name) {
    	return ((AbsPrimitive)getAttribute(name)).getRealValue();
    }
}

