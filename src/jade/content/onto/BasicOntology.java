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
package jade.content.onto;

import jade.content.*;
import jade.content.schema.*;
import jade.core.AID;
import jade.util.leap.List;

/**
 * Ontology containing basic concepts.
 *
 * see jade.content.Ontology
 *
 * @author Federico Bergenti - Universita` di Parma
 */
public class BasicOntology extends FullOntology {
    public static final String         STRING = "String";
    public static final String         FLOAT = "Float";
    public static final String         INTEGER = "Integer";
    public static final String         BOOLEAN = "Boolean";
    public static final String         CONCEPT = "Concept";
    public static final String         TERM = "Term";
    public static final String         PROPOSITION = "Proposition";
    public static final String         GENERIC_ACTION = "GenericAction";
    public static final String         SEQUENCE = "Sequence";
    public static final String         SET = "Set";
    public static final String         KEY_VALUE_PAIR = "KeyValuePair";
    public static final String         KEY = "Key";
    public static final String         VALUE = "Value";
    public static final String         NAME = "Name";
    public static final String         TYPE_NAME = "TypeName";
    public static final String         EXISTS = "Exists";
    public static final String         FORALL = "Forall";
    public static final String         UNIQUE = "Unique";
    public static final String         QUANTIFIED_VARIABLE = 
        "QuantifiedVariable";
    public static final String         KIND = "Kind";
    public static final String         VARIABLE = "Variable";
    public static final String         AID = "AID";
    public static final String         ADDRESSES = "Addresses";
    public static final String         RESOLVERS = "Resolvers";
    public static final String         QUANTIFIER = "Quantifier";
    private static final BasicOntology theInstance = new BasicOntology();

    /**
     * Constructor
     *
     */
    private BasicOntology() {
        super("BASIC_ONTOLOGY");

        try {
            add(VariableSchema.getBaseSchema(), Variable.class);
            add(TermSchema.getBaseSchema(), Term.class);
            add(PropositionSchema.getBaseSchema(), Proposition.class);
            add(PrimitiveSchema.getBaseSchema());
            add(ConceptSchema.getBaseSchema(), Concept.class);
            add(AggregateSchema.getBaseSchema(), List.class);
            add(GenericActionSchema.getBaseSchema(), GenericAction.class);
            add(new PrimitiveSchema(STRING));
            add(new PrimitiveSchema(FLOAT));
            add(new PrimitiveSchema(INTEGER));
            add(new PrimitiveSchema(BOOLEAN));
            add(new AggregateSchema(SEQUENCE));
            add(new AggregateSchema(SET));

            ConceptSchema keyValuePairSchema = 
                new ConceptSchema(KEY_VALUE_PAIR);

            keyValuePairSchema.add(KEY, 
                                   (TermSchema) getSchema(STRING));
            keyValuePairSchema.add(VALUE, 
                                   (TermSchema) getSchema(STRING));
            add(keyValuePairSchema, KeyValuePair.class);

            ConceptSchema quantifiedVariableSchema = 
                new ConceptSchema(QUANTIFIED_VARIABLE);

            quantifiedVariableSchema.add(KIND, 
                                         (TermSchema) getSchema(STRING));
            quantifiedVariableSchema.add(VARIABLE, 
                                         VariableSchema.getBaseSchema());
            add(quantifiedVariableSchema, QuantifiedVariable.class);

            AggregateSchema setSchema = 
                (AggregateSchema) getSchema(SET);
            ConceptSchema   aidSchema = new ConceptSchema(AID);

            aidSchema.add(NAME, (TermSchema) getSchema(STRING));
            aidSchema.add(RESOLVERS, setSchema);
            aidSchema.add(ADDRESSES, setSchema);
            add(aidSchema, AID.class);

            HigherOrderPredicateSchema quantifierSchema = 
                new HigherOrderPredicateSchema(QUANTIFIER);

            quantifierSchema.add(KIND, 
                                 (TermSchema) getSchema(STRING));
            quantifierSchema.add(VARIABLE, 
                                 (TermSchema) getSchema(VARIABLE));
            quantifierSchema.add(PROPOSITION, 
                                 (PropositionSchema) getSchema(PROPOSITION));
            add(quantifierSchema, Quantifier.class);
        } 
        catch (OntologyException oe) {
            oe.printStackTrace();
        } 
    }

    /**
     * Returns the singleton instance of the <code>BasicOntology</code>.
     *
     * @return the <code>BasicOntology</code>
     *
     */
    public static BasicOntology getInstance() {
        return theInstance;
    } 
}
