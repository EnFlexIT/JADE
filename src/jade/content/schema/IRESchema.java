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

import jade.content.onto.*;
import jade.content.abs.*;

/**
 * Note that an IRESchema should also be a TermSchema, but
 * this inheritance relation is cut as Java does not support
 * multiple inheritance. As a consequence in practice it will 
 * not be possible to define e.g. a ConceptSchema with a slot 
 * whose value must be instances of a certain type of IRE even if in theory 
 * this should be possible as a ConceptSchema can have slots 
 * of type term and an IRE is a term.
 * @author Federico Bergenti - Universita` di Parma
 */
public class IRESchema extends ContentElementSchema {
    public static final String BASE_NAME = "IRE";
    private static IRESchema   baseSchema = new IRESchema();
    
    public static final String VARIABLE = "Variable";
    public static final String PROPOSITION = "Proposition";

    /**
     * Construct a schema that vinculates an entity to be a generic
     * ire
     */
    private IRESchema() {
        super(BASE_NAME);
    }

    /**
     * Creates a <code>IRESchema</code> with a given type-name.
     * All ire-s have a variable and a proposition.
     *
     * @param typeName The name of this <code>IRESchema</code> 
     * (e.g. IOTA, ANY, ALL).
     */
    public IRESchema(String typeName) {
        super(typeName);

        // FIXME It should be possible to specify a set of variables
        add(VARIABLE, VariableSchema.getBaseSchema()); 
        add(PROPOSITION, PropositionSchema.getBaseSchema());
    }

    /**
     * Retrieve the generic base schema for all ire-s.
     *
     * @return the generic base schema for all ire-s.
     */
    public static ObjectSchema getBaseSchema() {
        return baseSchema;
    } 

    /**
     * Creates an Abstract descriptor to hold a ire of
     * the proper type.
     */
    public AbsObject newInstance() throws OntologyException {
        return new AbsIRE(getTypeName());
    } 

  	/**
  	   Return true if 
  	   - s is the base schema for the XXXSchema class this schema is
  	     an instance of (e.g. s is ConceptSchema.getBaseSchema() and this 
  	     schema is an instance of ConceptSchema)
  	   - s is the base schema for a super-class of the XXXSchema class
  	     this schema is an instance of (e.g. s is TermSchema.getBaseSchema()
  	     and this schema is an instance of ConceptSchema.
  	   Moreover, as IRESchema extends ContentElementSchema, but should
  	   also extend TermSchema (this is not possible in practice as
  	   Java does not support multiple inheritance), this method
  	   returns true also in the case that s is equals to, or is an
  	   ancestor of, TermSchema.getBaseSchema() (i.e. TermSchema.getBaseSchema()
  	   descends from s)
  	 */
  	protected boolean descendsFrom(ObjectSchema s) {
  		if (s.equals(getBaseSchema())) {
	  		return true;
  		}
  		if (super.descendsFrom(s)) {
  			return true;
  		}
  		return TermSchema.getBaseSchema().descendsFrom(s);
  	}
}
