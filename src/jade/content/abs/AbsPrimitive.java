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
 * @author Paola Turci, Federico Bergenti - Universita` di Parma
 */
public class AbsPrimitive extends AbsTerm {
    private Object value = null;

    /**
     * Constructor.
     *
     * @param kind name of the type of the primitive.
     * @param value value of the primitive.
     *
     */
    public AbsPrimitive(String kind, Object value) {
        super(kind);

        this.value = value;
    }

    /**
     * Constructor.
     *
     * @param kind name of the type of the primitive.
     *
     */
    public AbsPrimitive(String kind) {
        super(kind);
    }

    public static AbsPrimitive wrap(String value) {
        AbsPrimitive ret = new AbsPrimitive(BasicOntology.STRING, value);

        return ret;
    } 

    public static AbsPrimitive wrap(boolean value) {
        AbsPrimitive ret = new AbsPrimitive(BasicOntology.BOOLEAN, 
                                            new Boolean(value));

        return ret;
    } 

    public static AbsPrimitive wrap(int value) {
        AbsPrimitive ret = new AbsPrimitive(BasicOntology.INTEGER, 
                                            new Integer(value));

        return ret;
    } 

    public static AbsPrimitive wrap(float value) {
        AbsPrimitive ret = new AbsPrimitive(BasicOntology.FLOAT, 
                                            new Float(value));

        return ret;
    } 

    public static Object toObject(AbsPrimitive abs) {
        return abs.value;
    } 

    /**
     * Sets a string value.
     *
     * @param value the value to set.
     *
     */
    public void set(String value) {
        this.value = value;
    } 

    /**
     * Sets a boolean value.
     *
     * @param value the value to set.
     *
     */
    public void set(boolean value) {
        this.value = new Boolean(value);
    } 

    /**
     * Sets an integer value.
     *
     * @param value the value to set.
     *
     */
    public void set(int value) {
        this.value = new Integer(value);
    } 

    /**
     * Sets a float value.
     *
     * @param value the value to set.
     *
     */
    public void set(float value) {
        this.value = new Float(value);
    } 

    /**
     * Gets a string value.
     *
     * @return the string.
     *
     */
    public String getString() {
        return (String) value;
    } 

    /**
     * Gets an integer value.
     *
     * @return the integer.
     *
     */
    public int getInteger() {
        return ((Integer) value).intValue();
    } 

    /**
     * Gets a float value.
     *
     * @return the float.
     *
     */
    public float getFloat() {
        return ((Float) value).floatValue();
    } 

    /**
     * Get a boolean value.
     *
     * @return the boolean.
     *
     */
    public boolean getBoolean() {
        return ((Boolean) value).booleanValue();
    } 

    public Object getObject() {
        return value;
    } 

    protected void dump(int indent) {
        for (int i = 0; i < indent; i++) {
            System.out.print("  ");
        }

        System.out.println(getObject());
    } 

}

