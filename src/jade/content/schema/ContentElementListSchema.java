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
package jade.content.schema;

import jade.content.abs.*;
import jade.content.onto.OntologyException;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class ContentElementListSchema extends ContentElementSchema {
    public static final String BASE_NAME = "ContentElementList";
    private static ContentElementListSchema baseSchema = new ContentElementListSchema();

    /**
     * Construct a schema that vinculates an entity to be a content element 
     * list. Note that there are no different types of content element 
     * list as it 
     * happens for concepts (e.g. Person, Address...), IREs (e.g. IOTA,
     * ANY, ALL...) and the like. Therefore there is no ContentElementListSchema
     * constructor that takes a String parameter.
     */
    private ContentElementListSchema() {
        super(BASE_NAME);
    }

    /**
     * Retrieve the generic base schema for all content element lists.
     * @return the generic base schema for all content element lists.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Creates an Abstract descriptor to hold a content element list 
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsContentElementList();
    } 
}
