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
package jade.content.lang.j;

import jade.content.lang.*;
import jade.content.onto.*;
import jade.content.abs.*;
import java.io.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class JCodec extends Codec {
    public static final String NAME = "J";

    /**
     * Decorer.
     *
     * @param content the content to encode.
     *
     * @return decoded content.
     *
     * @throws CodecException
     *
     */
    public AbsContentElement decode(byte[] content) throws CodecException {
        try {
            ByteArrayInputStream buffer = new ByteArrayInputStream(content);
            ObjectInputStream    stream = new ObjectInputStream(buffer);
            Object               obj = stream.readObject();

            stream.close();

            return (AbsContentElement) obj;
        } 
        catch (IOException ioe) {
            throw new CodecException(ioe.getMessage());
        } 
        catch (ClassNotFoundException cnfe) {
            throw new CodecException(cnfe.getMessage());
        } 
    } 

    /**
     * Encoder.
     *
     * @param content the content to decode.
     *
     * @return encoded content.
     *
     * @throws CodecException
     *
     */
    public byte[] encode(AbsContentElement content) throws CodecException {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream    stream = new ObjectOutputStream(buffer);

            stream.writeObject(content);
            stream.close();

            return buffer.toByteArray();
        } 
        catch (IOException ioe) {
            throw new CodecException(ioe.getMessage());
        } 
    } 

    /**
     * Encoder.
     *
     * @param ontology the ontology to use for decoding.
     * @param content the content to encode.
     *
     * @return encoded content.
     *
     * @throws CodecException
     *
     */
    public byte[] encode(Ontology ontology, 
                         AbsContentElement content) throws CodecException {
        return encode(content);
    } 

    /**
     * Decoder.
     *
     * @param ontology the ontology to use for decoding.
     * @param content the content to decode.
     *
     * @return decoded content.
     *
     * @throws CodecException
     *
     */
    public AbsContentElement decode(Ontology ontology, 
                                    byte[] content) throws CodecException {
        return decode(content);
    } 

    /**
     * Constructor
     *
     */
    public JCodec() {
        super(NAME);
    }

}

