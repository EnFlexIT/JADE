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
package jade.content;

import jade.content.onto.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class Variable extends Term {
    private String name = null;
    private String typeName = null;

    /**
     * Constructor
     *
     */
    public Variable() {}

    /**
     * Sets the name.
     *
     * @param name the name.
     *
     */
    public void setName(String name) {
        this.name = name;
    } 

    /**
     * Gets the name.
     *
     * @return the name.
     *
     */
    public String getName() {
        return name;
    } 

    /**
     * Sets the name of the type.
     *
     * @param name the name of the type.
     *
     */
    public void setTypeName(String name) {
        this.typeName = name;
    } 

    /**
     * Gets the name of the type.
     *
     * @return the name of the type.
     *
     */
    public String getTypeName() {
        return typeName;
    } 

}

