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

import jade.lang.acl.ACLMessage;
import jade.util.leap.*;
import jade.content.lang.Codec;
import jade.content.lang.StringCodec;
import jade.content.lang.ByteArrayCodec;
import jade.content.lang.Codec.CodecException;
import jade.content.abs.AbsContentElement;
import jade.content.schema.ObjectSchema;
import jade.content.onto.*;

/**
 * The content manager associated with an agent.
 * 
 * @author Federico Bergenti
 */
public class ContentManager implements Serializable {
    private Map languages = new HashMap();
    private Map ontologies = new HashMap();

    /**
     * Registers a codec <code>c</code> with its default name (i.e.
     * the name returned by its <code>getName()</code> method.
     * @param c the codec.
     */
    public void registerLanguage(Codec c) {
        registerLanguage(c, c.getName());
    }

    /**
     * Registers a codec <code>c</code> with a given name 
     * @param c the codec.
     * @param name the name.
     */
    public void registerLanguage(Codec c, String name) {
        languages.put(name, c);
    }

    /**
     * Registers an ontology <code>o</code> with its default name (i.e.
     * the name returned by its <code>getName()</code> method.
     * @param o the ontology.
     */
    public void registerOntology(Ontology o) {
        registerOntology(o, o.getName());
    }

    /**
     * Registers an ontology with a given <code>name</code>.
     * @param o the ontology.
     * @param name the name.
     */
    public void registerOntology(Ontology o, String name) {
        ontologies.put(name, o);
    }

    /**
     * Retrieves a codec with a given <code>name</code>.
     * @param name the name.
     * @return the codec.
     */
    public Codec lookupLanguage(String name) {
        return (Codec) languages.get(name);
    }

    /**
     * Retrieves an ontology with a given name.
     * @param name the name.
     * @return the ontology.
     */
    public Ontology lookupOntology(String name) {
        return (Ontology) ontologies.get(name);
    }

   /**
     * Fills the content of a message.
     * @param msg the message
     * @param content the content.
     * @throws CodecException
     * @throws OntologyException
     */
    public void fillContent(ACLMessage msg, AbsContentElement content) 
            throws CodecException, OntologyException {
        Codec    codec = lookupLanguage(msg.getLanguage());
        Ontology onto  = getMergedOntology(codec, lookupOntology(msg.getOntology()));

        // DEBUG
        //content.dump();
        
        // Validate the content against the ontology
    		ObjectSchema schema = onto.getSchema(content.getTypeName());
   			if (schema == null) {
  				throw new OntologyException("No schema found for type "+content.getTypeName());
  			}
    		schema.validate(content, onto);

    		if (codec instanceof ByteArrayCodec)
					msg.setByteSequenceContent(((ByteArrayCodec) codec).encode(onto, content));
				else if (codec instanceof StringCodec)
					msg.setContent(((StringCodec) codec).encode(onto, content));
				else
					throw new CodecException("UnsupportedTypeOfCodec");
    } 

    /**
     * Extracts an abstract descriptor of the content from a message.
     * @param msg the message
     * @return the content as an abstract descriptor.
     * @throws CodecException
     * @throws OntologyException
     * @see jade.content.Ontology
     */
    public AbsContentElement extractAbsContent(ACLMessage msg) 
            throws CodecException, OntologyException {
        Codec    codec = lookupLanguage(msg.getLanguage());
        Ontology onto  = getMergedOntology(codec, lookupOntology(msg.getOntology()));

        if (codec instanceof ByteArrayCodec)
					return ((ByteArrayCodec) codec).decode(onto, msg.getByteSequenceContent());
				else if (codec instanceof StringCodec)
					return ((StringCodec) codec).decode(onto, msg.getContent());
				else
					throw new CodecException("UnsupportedTypeOfCodec");
    } 

    /**
     * Fills the content of a message.
     * 
     * @param msg the message
     * @param content the content.
     * 
     * @throws CodecException
     * @throws OntologyException
     * 
     */
    public void fillContent(ACLMessage msg, ContentElement content) 
            throws CodecException, OntologyException {
        Codec    codec = lookupLanguage(msg.getLanguage());
        Ontology onto  = getMergedOntology(codec, lookupOntology(msg.getOntology()));

        AbsContentElement abs = (AbsContentElement) onto.fromObject(content);
       
        //DEBUG
        //abs.dump();
        
        // Validate the content against the ontology
    		ObjectSchema schema = onto.getSchema(abs.getTypeName());
   			if (schema == null) {
  				throw new OntologyException("No schema found for type "+abs.getTypeName());
  			}
       	schema.validate(abs, onto);
        
        if (codec instanceof ByteArrayCodec)
					msg.setByteSequenceContent(((ByteArrayCodec) codec).encode(onto, abs));
				else if (codec instanceof StringCodec)
					msg.setContent(((StringCodec) codec).encode(onto, abs));
				else
					throw new CodecException("UnsupportedTypeOfCodec");
    } 

    /**
     * Retrieves the content of a message as a concrete object.
     * @param msg the message
     * @return the content.
     * @throws CodecException
     * @throws OntologyException
     * @see jade.content.Ontology
     */
    public ContentElement extractContent(ACLMessage msg) 
            throws CodecException, UngroundedException, OntologyException {
        Codec    codec = lookupLanguage(msg.getLanguage());
        Ontology onto  = getMergedOntology(codec, lookupOntology(msg.getOntology()));
				AbsContentElement abs = null;
        if (codec instanceof ByteArrayCodec)
					abs=((ByteArrayCodec) codec).decode(onto, msg.getByteSequenceContent());
				else if (codec instanceof StringCodec)
					abs=((StringCodec) codec).decode(onto, msg.getContent());
				else
					throw new CodecException("UnsupportedTypeOfCodec");

				return (ContentElement) onto.toObject(abs);
    } 

		/**
		 * Merge the reference ontology with the inner ontology of the 
		 * content language
		 */
    private Ontology getMergedOntology(Codec c, Ontology o) {
				Ontology ontology = null;
				Ontology langOnto = c.getInnerOntology();
				if (langOnto == null) {
					ontology = o;
				}
				else {
					ontology = new Ontology(null, new Ontology[]{langOnto, o}, null);
				}
				return ontology;
    }
    
}

