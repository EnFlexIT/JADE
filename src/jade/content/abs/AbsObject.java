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

import jade.util.leap.Serializable;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsObject implements Serializable {
    private Hashtable elements = new Hashtable();
    private String    typeName = null;

    /**
     * Construct an Abstract descriptor to hold an object of
     * the proper type.
     * @param typeName The name of the type of the object held by this
     * abstract descriptor.
     */
    protected AbsObject(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return The name of the type of the object held by this
     * abstract descriptor.
     */
    public String getTypeName() {
        return typeName;
    } 

    /**
     * Sets an attribute of the object held by this
     * abstract descriptor.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute.
     */
    protected void set(String name, AbsObject value) {
        elements.put(name.toUpperCase(), value);
    } 

    /**
     * Gets the value of an attribute of the object held by this
     * abstract descriptor.
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     */
    public AbsObject getAbsObject(String name) {
        return (AbsObject) elements.get(name.toUpperCase());
    } 

    /**
     * @return the name of all attributes.
     */
    public String[] getNames() {
        String[] names = new String[getCount()];
        int      count = 0;

        for (Enumeration e = elements.keys(); e.hasMoreElements(); ) {
            names[count++] = (String) e.nextElement();
        }

        return names;
    } 

    /**
     * Tests if the object is grounded, i.e., if no one of its attributes 
     * is associated with a variable
     * @return <code>true</code> if the object is grounded.
     */
    public boolean isGrounded() {

        // FIXME: Implement
        return true;
    } 

    /**
     * Gets the number of attributes.
     * @return the number of attributes.
     */
    public int getCount() {
        return elements.size();
    } 

    protected void dump(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        System.out.println(getTypeName());

        String[] names = getNames();

        for (int i = 0; i < getCount(); i++) {
            for (int j = 0; j < indent; j++) {
                System.out.print("  ");
            }

            System.out.println(":" + names[i]);

            AbsObject abs = getAbsObject(names[i]);

            abs.dump(indent + 1);
        } 
    }

    public void dump() {
        dump(0);
    } 

    public String toString() {
    	return getClass().getName()+"-"+getTypeName();
    }
}

