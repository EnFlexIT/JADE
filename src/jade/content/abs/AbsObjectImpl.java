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

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Base class for all abstract descriptor classes.
 * @author Federico Bergenti - Universita` di Parma
 */
public class AbsObjectImpl implements AbsObject {
    private Hashtable elements = new Hashtable();
    private String    typeName = null;

    /**
     * Construct an Abstract descriptor to hold an object of
     * the proper type.
     * @param typeName The name of the type of the object held by this
     * abstract descriptor.
     */
    protected AbsObjectImpl(String typeName) {
        this.typeName = typeName;
    }

    /**
     * @return The name of the type of the object held by this
     * abstract descriptor.
     * @see AbsObject#getTypeName()
     */
    public String getTypeName() {
        return typeName;
    } 

    /**
     * Sets an attribute of the object held by this
     * abstract descriptor.
     * @param name The name of the attribute to be set.
     * @param value The new value of the attribute. If <code>value</code>
     * is null the current mapping with <code>name</code> (if any) is
     * removed.
     */
    protected void set(String name, AbsObject value) {
    		if (value == null) {
    			elements.remove(name);
    		}
    		else {
	        elements.put(name.toUpperCase(), value);
    		}
    } 

    /**
     * Gets the value of an attribute of the object held by this
     * abstract descriptor.
     * @param name The name of the attribute.
     * @return value The value of the attribute.
     * @see AbsObject#getAbsObject()
     */
    public AbsObject getAbsObject(String name) {
        return (AbsObject) elements.get(name.toUpperCase());
    } 

    /**
     * @return the name of all attributes.
     * @see AbsObject#getNames()
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
     * @see AbsObject#isGrounded()
     */
    public boolean isGrounded() {
			Enumeration e = elements.elements();
			while (e.hasMoreElements()) {
				AbsObject abs = (AbsObject) e.nextElement();
				if (!abs.isGrounded()) {
					return false;
				}
			}
			return true;
    } 

    /**
     * Gets the number of attributes.
     * @return the number of attributes.
     * @see AbsObject#getCount()
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

            AbsObjectImpl abs = (AbsObjectImpl) getAbsObject(names[i]);

            abs.dump(indent + 1);
        } 
    }

    /**
     * @see AbsObject#dump()
     */
    public void dump() {
        dump(0);
    } 

    public String toString() {
    	return getClass().getName()+"-"+getTypeName();
    }
}

