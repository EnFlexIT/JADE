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
 * @author Govanni Caire - TILAB
 */
public class ContentManager implements Serializable {
    private Map languages = new HashMap();
    private Map ontologies = new HashMap();
    private boolean validationMode = true;

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
        // System.out.println("Filling content with "+content);
        
        validate(content, onto);

        encode(msg, content, codec, onto);
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
       
        // DEBUG
        // System.out.println("Filling content with "+abs);
        
        validate(abs, onto);
        
        encode(msg, abs, codec, onto);
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

        AbsContentElement content = decode(msg, codec, onto);
        
        validate(content, onto);
        
        return content;
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
        
        AbsContentElement content = decode(msg, codec, onto);
        
        validate(content, onto);
        
				return (ContentElement) onto.toObject(content);
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
    
		private void validate(AbsContentElement content, Ontology onto) throws OntologyException { 
    	if (validationMode) {
        // Validate the content against the ontology
    		ObjectSchema schema = onto.getSchema(content.getTypeName());
   			if (schema == null) {
  				throw new OntologyException("No schema found for type "+content.getTypeName());
  			}
    		schema.validate(content, onto);
      }
		}

		private void encode(ACLMessage msg, AbsContentElement content, Codec codec, Ontology onto) throws CodecException, OntologyException { 
      if (codec instanceof ByteArrayCodec)
				msg.setByteSequenceContent(((ByteArrayCodec) codec).encode(onto, content));
			else if (codec instanceof StringCodec)
				msg.setContent(((StringCodec) codec).encode(onto, content));
			else
				throw new CodecException("UnsupportedTypeOfCodec");
		}

		private AbsContentElement decode(ACLMessage msg, Codec codec, Ontology onto) throws CodecException, OntologyException { 
      if (codec instanceof ByteArrayCodec)
				return ((ByteArrayCodec) codec).decode(onto, msg.getByteSequenceContent());
			else if (codec instanceof StringCodec)
				return ((StringCodec) codec).decode(onto, msg.getContent());
			else
				throw new CodecException("UnsupportedTypeOfCodec");
		}
		
		/** 
		   Set the validation mode i.e. whether contents that are managed
		   by this content manager should be validated during 
		   message content filling/extraction.
		   Default value is <code>true</code>
		   @param mode the new validation mode 
		 */
		public void setValidationMode(boolean mode) {
			validationMode = mode;
		}
		
		/** 
		   Return the currently set validation mode i.e. whether 
		   contents that are managed by this content manager should 
		   be validated during message content filling/extraction.
		   Default value is <code>true</code>
		   @return the currently set validation mode 
		 */
		public boolean getValidationMode() {
			return validationMode;
		}
}

