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
public class FullContentManager extends ContentManager implements Serializable {
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
                                                (AbsContentElement) ((FullOntology)ontology).fromObject(content))));
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
                                                (AbsContentElement) ((FullOntology)o).fromObject(content))));
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
                                                (AbsContentElement) ((FullOntology)ontology).fromObject(content))));
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

        return (ContentElement) ((FullOntology)onto).toObject(decodeContent(codec, onto, 
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
}

