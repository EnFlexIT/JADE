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
import jade.content.lang.Codec.CodecException;
import jade.content.abs.*;
import jade.content.onto.*;

/**
 * The content manager associated with an agent.
 * 
 * @author Federico Bergenti
 */
public class ContentManager {
    private Map languages = new HashMap();
    private Map ontologies = new HashMap();

    /**
     * Registers a codec <code>c</code>.
     * 
     * @param c the codec.
     * 
     */
    public void registerLanguage(Codec c) {
        registerLanguage(c, c.getName());
    } 

    /**
     * Registers a language, i.e., a codec <code>c</code> plus 
     * a <code>name</code>.
     * 
     * @param c the codec.
     * @param name the name.
     * 
     */
    public void registerLanguage(Codec c, String name) {
        languages.put(name, c);
    } 

    /**
     * Registers an ontology.
     * 
     * @param o the ontology.
     * 
     */
    public void registerOntology(Ontology o) {
        registerOntology(o, o.getName());
    } 

    /**
     * Registers an ontology with a given <code>name</code>.
     * 
     * @param o the ontology.
     * @param name the name.
     * 
     */
    public void registerOntology(Ontology o, String name) {
        ontologies.put(name, o);
    } 

    /**
     * Retrieves a codec with a given <code>name</code>.
     * 
     * @param name the name.
     * 
     * @return the codec.
     * 
     */
    public Codec lookupLanguage(String name) {
        return (Codec) languages.get(name);
    } 

    /**
     * Retrieves an ontology with a given name.
     * 
     * @param name the name.
     * 
     * @return the ontology.
     * 
     */
    public Ontology lookupOntology(String name) {
        return (Ontology) ontologies.get(name);
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
        Ontology ontology = lookupOntology(msg.getOntology());

        msg.setContent(new String(encodeContent(codec, ontology, 
                                                (AbsContentElement) ontology.fromObject(content))));
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
    public void fillContent(ACLMessage msg, AbsContentElement content) 
            throws CodecException, OntologyException {
        Codec    codec = lookupLanguage(msg.getLanguage());
        Ontology ontology = lookupOntology(msg.getOntology());

        msg.setContent(new String(encodeContent(codec, ontology, content)));
    } 

    /**
     * Fills the content of a message.
     * 
     * @param msg the message
     * @param language the codec to use.
     * @param o the ontology.
     * @param content the content.
     * 
     * @throws CodecException
     * @throws OntologyException
     * 
     */
    public void fillContent(ACLMessage msg, Codec language, Ontology o, 
                            ContentElement content) throws CodecException, 
                            OntologyException {
        msg.setContent(new String(encodeContent(language, o, 
                                                (AbsContentElement) o.fromObject(content))));
    } 

    /**
     * Fills the content of a message.
     * 
     * @param msg the message
     * @param language the codec to use.
     * @param o the ontology.
     * @param content the content.
     * 
     * @throws CodecException
     * @throws OntologyException
     * 
     */
    public void fillContent(ACLMessage msg, Codec language, Ontology o, 
                            AbsContentElement content) throws CodecException, 
                            OntologyException {
        msg.setContent(new String(encodeContent(language, o, content)));
    } 

    /**
     * Fills the content of a message.
     * 
     * @param msg the message
     * @param language the name of the codec to use.
     * @param o the ontology.
     * @param content the content.
     * 
     * @throws CodecException
     * @throws OntologyException
     * 
     */
    public void fillContent(ACLMessage msg, String language, String onto, 
                            ContentElement content) throws CodecException, 
                            OntologyException {
        Codec    codec = lookupLanguage(language);
        Ontology ontology = lookupOntology(onto);

        msg.setContent(new String(encodeContent(codec, ontology, 
                                                (AbsContentElement) ontology.fromObject(content))));
    } 

    /**
     * Fills the content of a message.
     * 
     * @param msg the message
     * @param language the codec to use.
     * @param o the name of the ontology.
     * @param content the content.
     * 
     * @throws CodecException
     * @throws OntologyException
     * 
     */
    public void fillContent(ACLMessage msg, String language, String onto, 
                            AbsContentElement content) throws CodecException, 
                            OntologyException {
        Codec    codec = lookupLanguage(language);
        Ontology ontology = lookupOntology(onto);

        msg.setContent(new String(encodeContent(codec, ontology, content)));
    } 

    /**
     * Extracts an abstract descriptor of the content from a message.
     * 
     * @param msg the message
     * 
     * @return the content as an abstract descriptor.
     * 
     * @throws CodecException
     * @throws OntologyException
     * 
     * @see jade.content.Ontology
     */
    public AbsContentElement extractAbsContent(ACLMessage msg) 
            throws CodecException, OntologyException {
        Ontology onto = lookupOntology(msg.getOntology());
        Codec    codec = lookupLanguage(msg.getLanguage());

        return decodeContent(codec, onto, (msg.getContent()).getBytes());
    } 

    /**
     * Retrieves the content of a message as a concrete object.
     * 
     * @param msg the message
     * 
     * @return the content.
     * 
     * @throws CodecException
     * @throws OntologyException
     * 
     * @see jade.content.Ontology
     */
    public ContentElement extractContent(ACLMessage msg) 
            throws CodecException, OntologyException {
        Ontology onto = lookupOntology(msg.getOntology());
        Codec    codec = lookupLanguage(msg.getLanguage());

        return (ContentElement) onto.toObject(decodeContent(codec, onto, 
                (msg.getContent()).getBytes()));
    } 

    /**
     * Encodes a content.
     * 
     * @param language the language to use.
     * @param ontology the ontology to use.
     * @param content  the content to encode.
     * 
     * @return the encoded byte array.
     * 
     * @throws CodecException
     * 
     */
    public byte[] encodeContent(Codec language, Ontology ontology, 
                                AbsContentElement content) throws CodecException {
        return language.encode(ontology, content);
    } 

    /**
     * Decodes a content.
     * 
     * @param language the language to use.
     * @param ontology the ontology to use.
     * @param content  the content to decode.
     * 
     * @return the content as an abstract descriptor.
     * 
     * @throws CodecException
     * 
     */
    public AbsContentElement decodeContent(Codec language, Ontology ontology, 
                                           byte[] content) throws CodecException {
        return language.decode(ontology, content);
    } 

}

