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

import jade.content.Concept;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsConcept extends AbsTerm implements Concept {

    /**
     * Construct an Abstract descriptor to hold a concept of
     * the proper type (e.g. PERSON, ADDRESS...).
     * @param typeName The name of the type of the concept held by 
     * this abstract descriptor.
     */
    public AbsConcept(String typeName) {
        super(typeName);
    }

    /**
     * Sets an attribute of the concept held by this
     * abstract descriptor.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, AbsTerm value) {
        super.set(name, value);
    } 

    /**
     * Utility method that allows setting attributes of type
     * <code>String</code> without the need of wrapping the new value
     * into an <code>AbsTerm</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, String value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Utility method that allows setting attributes of type
     * <code>boolean</code> without the need of wrapping the new value
     * into an <code>AbsTerm</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, boolean value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    /**
     * Utility method that allows setting attributes of type
     * <code>int</code> without the need of wrapping the new value
     * into an <code>AbsTerm</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, int value) {
        set(name, AbsPrimitive.wrap(value));
    } 

    //__CLDC_UNSUPPORTED__BEGIN
    /**
     * Utility method that allows setting attributes of type
     * <code>float</code> without the need of wrapping the new value
     * into an <code>AbsTerm</code>.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    public void set(String name, float value) {
        set(name, AbsPrimitive.wrap(value));
    } 
    //__CLDC_UNSUPPORTED__END

    /**
     * Gets the value of an attribute of the concept 
     * held by this abstract descriptor.
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     */
    public AbsTerm getAbsTerm(String name) {
        return (AbsTerm)getAbsObject(name);
    }

   /**
     * Utility method that allows getting the value of attributes 
     * of type <code>String</code> directly as a <code>String</code>
     * i.e. not wrapped into an <code>AbsTerm/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public String getString(String name) {
        return ((AbsPrimitive)getAbsTerm(name)).getString();
    }

    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>boolean</code> directly as a <code>boolean</code>
     * i.e. not wrapped into an <code>AbsTerm/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public boolean getBoolean(String name) {
      	return ((AbsPrimitive)getAbsTerm(name)).getBoolean();
    }

    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>int</code> directly as an <code>int</code>
     * i.e. not wrapped into an <code>AbsTerm/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public int getInteger(String name) {
    	return ((AbsPrimitive)getAbsTerm(name)).getInteger();
    }

    //__CLDC_UNSUPPORTED__BEGIN
    /**
     * Utility method that allows getting the value of attributes 
     * of type <code>float</code> directly as a <code>float</code>
     * i.e. not wrapped into an <code>AbsTerm/code>.
     * @param name The name of the attribute to be retrieved.
     * @param value The value of the attribute.
     */
    public float getFloat(String name) {
    	return ((AbsPrimitive)getAbsTerm(name)).getFloat();
    }
    //__CLDC_UNSUPPORTED__END
}

