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
 * This class provides all methods to manage the content languages 
 * and ontologies "known" by a given agent and to fill and extract the 
 * content of an ACL message according to a given content language and 
 * ontology.
 * Each agent has a <code>ContentManager</code> object accessible through
 * the <code>getContentManager()</code> method of the <code>Agent</code>
 * class.
 * 
 * @author Federico Bergenti
 * @author Govanni Caire - TILAB
 */
public class ContentManager implements Serializable {
    private Map languages = new HashMap();
    private Map ontologies = new HashMap();
    private boolean validationMode = true;

    /**
     * Registers a <code>Codec</code> for a given content language 
     * with its default name (i.e.
     * the name returned by its <code>getName()</code> method.
     * Since this operation is performed the agent that owns this 
     * <code>ContentManager</code> is able to "speak" the language
     * corresponding to the registered <code>Codec</code>.
     * @param c the <code>Codec</code> to be registered.
     */
    public void registerLanguage(Codec c) {
        registerLanguage(c, c.getName());
    }

    /**
     * Registers a <code>Codec</code> for a given content language 
     * with a given name.
     * @param c the <code>Codec</code> to be registered.
     * @param name the name associated to the registered codec.
     */
    public void registerLanguage(Codec c, String name) {
        languages.put(name, c);
    }

    /**
     * Registers an <code>Ontology</code> with its default name (i.e.
     * the name returned by its <code>getName()</code> method.
     * Since this operation is performed the agent that owns this 
     * <code>ContentManager</code> "knows" the registered 
     * <code>Ontology</code>.
     * @param o the <code>Ontology</code> to be registered.
     */
    public void registerOntology(Ontology o) {
        registerOntology(o, o.getName());
    }

    /**
     * Registers an <code>Ontology</code> with a given name.
     * @param o the <code>Ontology</code> to be registered.
     * @param name the name associated to the registered Ontology.
     */
    public void registerOntology(Ontology o, String name) {
        ontologies.put(name, o);
    }

    /**
     * Retrieves a previously registered <code>Codec</code> 
     * giving its <code>name</code>.
     * @param name the name associated to the <code>Codec</code> 
     * to be retrieved.
     * @return the <code>Codec</code> associated to 
     * <code>name</code> or <code>null</code> if no Codec was registered 
     * with the given name.
     */
    public Codec lookupLanguage(String name) {
        return (Codec) languages.get(name);
    }

    /**
     * Retrieves a previously registered <code>Ontology</code> 
     * giving its <code>name</code>.
     * @param name the name associated to the <code>Ontology</code> 
     * to be retrieved.
     * @return the <code>Ontology</code> associated to 
     * <code>name</code> or <code>null</code> if no Ontology was registered 
     * with the given name.
     */
    public Ontology lookupOntology(String name) {
        return (Ontology) ontologies.get(name);
    }

    /**
     * Fills the <code>:content</code> slot of an 
     * <code>ACLMessage msg</code> using the content language
     * and ontology indicated in the <code>:language</code> and 
     * <code>:ontology</code> fields of <code>msg</code>.
     * @param msg the message whose content has to be filled.
     * @param content the content of the message represented as an 
     * <code>AbsContentElement</code>.
     * @throws CodecException if <code>content</code> is not compliant
     * to the content language used for this operation.
     * @throws OntologyException if <code>content</code> is not compliant
     * to the ontology used for this operation.
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
     * Fills the <code>:content</code> slot of an 
     * <code>ACLMessage msg</code> using the content language
     * and ontology indicated in the <code>:language</code> and 
     * <code>:ontology</code> fields of <code>msg</code>.
     * @param msg the message whose content has to be filled.
     * @param content the content of the message represented as a 
     * <code>ContentElement</code>.
     * @throws CodecException if <code>content</code> is not compliant
     * to the content language used for this operation.
     * @throws OntologyException if <code>content</code> is not compliant
     * to the ontology used for this operation.
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
     * Translates the <code>:content</code> slot of an 
     * <code>ACLMessage msg</code> into an <code>AbsContentElement</code>
     * using the content language and ontology indicated in the 
     * <code>:language</code> and <code>:ontology</code> fields of <code>msg</code>.
     * @param msg the message whose content has to be extracted.
     * @return the content of the message represented as an 
     * <code>AbsContentElement</code>.
     * @throws CodecException if the content of the message is not compliant
     * to the content language used for this operation.
     * @throws OntologyException if the content of the message is not compliant
     * to the ontology used for this operation.
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
     * Translates the <code>:content</code> slot of an 
     * <code>ACLMessage msg</code> into a <code>ContentElement</code>
     * using the content language and ontology indicated in the 
     * <code>:language</code> and <code>:ontology</code> fields of <code>msg</code>.
     * @param msg the message whose content has to be extracted.
     * @return the content of the message represented as a 
     * <code>ContentElement</code>.
     * @throws CodecException if the content of the message is not compliant
     * to the content language used for this operation.
     * @throws OntologyException if the content of the message is not compliant
     * to the ontology used for this operation.
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
		
		/** 
		 */
		public Ontology getOntology(ACLMessage msg) {
      Codec    codec = lookupLanguage(msg.getLanguage());
      return getMergedOntology(codec, lookupOntology(msg.getOntology()));
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
		
}

