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
     * Constructor
     *
     * @param typeName name of the type of the object.
     *
     */
    protected AbsObject(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Returns the name of the type.
     *
     * @return the name of the type.
     *
     */
    public String getTypeName() {
        return typeName;
    } 

    /**
     * Sets an attribute of the object.
     *
     * @param name name of the attribute.
     * @param value value of the attribute.
     *
     */
    public void set(String name, AbsObject value) {
        elements.put(name.toUpperCase(), value);
    } 

    /**
     * Gets the value of an attribute of the object.
     *
     * @param name name of the attribute.
     *
     * @return value of the attribute.
     *
     */
    public AbsObject getAbsObject(String name) {
        return (AbsObject) elements.get(name.toUpperCase());
    } 

    /**
     * Retrieves the names of all attributes.
     *
     * @return the name of the attributes.
     *
     */
    public String[] getNames() {
        String[] ret = new String[getCount()];
        int      count = 0;

        for (Enumeration e = elements.keys(); e.hasMoreElements(); ) {
            ret[count++] = (String) e.nextElement();
        }

        return ret;
    } 

    /**
     * Tests if the object is grounded, i.e., if its attributes are not
     * associated with variables.
     *
     * @return if the object is grounded.
     *
     */
    public boolean isGrounded() {

        // TODO: Implement
        return true;
    } 

    /**
     * Gets the number of attributes.
     *
     * @return the number of attributes.
     *
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

}

