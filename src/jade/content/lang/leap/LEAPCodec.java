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
package jade.content.lang.leap;

import jade.content.lang.*;
import jade.content.onto.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.*;
import java.util.Vector;
import java.io.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class LEAPCodec extends Codec {
    public static final String NAME = "LEAP";
    private Vector             references = null;
    private int                counter = 0;
    private static final byte  REFERENCE = 0;
    private static final byte  OBJECT = 1;
    private static final byte  ATTRIBUTE = 2;
    private static final byte  END_ATTRIBUTES = 3;
    private static final byte  PRIMITIVE = 4;
    private static final byte  AGGREGATE = 5;
    private static final byte  ELEMENT = 6;
    private static final byte  END_AGGREGATE = 7;
    private static final byte  STRING = 8;
    private static final byte  BOOLEAN = 9;
    private static final byte  INTEGER = 10;
    private static final byte  FLOAT = 11;
    private static final byte  CONTENT_ELEMENT_LIST = 13;
    private static final byte  END_CONTENT_ELEMENT_LIST = 14;

    /**
     * Method declaration
     *
     * @param stream
     * @param abs
     *
     * @throws IOException
     *
     * @see
     */
    private synchronized void write(DataOutputStream stream, 
                                    AbsObject abs) throws IOException {
        int reference = references.indexOf(abs);

        if (reference != -1) {
            stream.writeByte(REFERENCE);
            stream.writeInt(reference);

            return;
        } 

        if (abs instanceof AbsPrimitive) {
            stream.writeByte(PRIMITIVE);

            Object obj = ((AbsPrimitive) abs).getObject();

            if (obj instanceof String) {
                stream.writeByte(STRING);
            } 

            if (obj instanceof Boolean) {
                stream.writeByte(BOOLEAN);
            } 

            if (obj instanceof Integer) {
                stream.writeByte(INTEGER);
            } 

            if (obj instanceof Float) {
                stream.writeByte(FLOAT);
            } 

            stream.writeUTF(obj.toString());

            return;
        } 

        if (abs instanceof AbsAggregate) {
            stream.writeByte(AGGREGATE);
            stream.writeUTF(abs.getTypeName());

            AbsAggregate aggregate = (AbsAggregate) abs;

            for (int i = 0; i < aggregate.getElementCount(); i++) {
                stream.writeByte(ELEMENT);
                write(stream, aggregate.getElement(i));
            } 

            stream.writeByte(END_AGGREGATE);

            return;
        } 

        if (abs instanceof AbsContentElementList) {
            stream.writeByte(CONTENT_ELEMENT_LIST);

            AbsContentElementList acel = (AbsContentElementList) abs;

            for (Iterator i = acel.getAll(); i.hasNext(); ) {
                stream.writeByte(ELEMENT);
                write(stream, (AbsObject) i.next());
            } 

            stream.writeByte(END_CONTENT_ELEMENT_LIST);

            return;
        } 

        references.add(abs);
        stream.writeByte(OBJECT);
        stream.writeUTF(abs.getTypeName());

        String[] names = abs.getNames();

        for (int i = 0; i < abs.getCount(); i++) {
            stream.writeByte(ATTRIBUTE);
            stream.writeUTF(names[i]);

            AbsObject child = abs.getAbsObject(names[i]);

            write(stream, child);
        } 

        stream.writeByte(END_ATTRIBUTES);
    } 

    /**
     * Method declaration
     *
     * @param stream
     * @param ontology
     *
     * @return
     *
     * @throws IOException
     * @throws OntologyException
     *
     * @see
     */
    private synchronized AbsObject read(DataInputStream stream, 
                                        Ontology onto) throws IOException, 
                                        OntologyException {
	FullOntology ontology = (FullOntology)onto;
        try {
            byte kind = stream.readByte();

            if (kind == REFERENCE) {
                int       reference = stream.readInt();
                AbsObject abs = (AbsObject) references.elementAt(reference);

                if (abs != null) {
                    return abs;
                } 
                else {
                    throw new IOException("Corrupted stream");
                }
            } 

            if (kind == PRIMITIVE) {
                byte         type = stream.readByte();
                String       value = stream.readUTF();
                AbsPrimitive abs = null;

                if (type == STRING) {
                    abs = new AbsPrimitive(BasicOntology.STRING, value);
                } 

                if (type == BOOLEAN) {
                    abs = new AbsPrimitive(BasicOntology.BOOLEAN, 
                                           new Boolean(value));
                } 

                if (type == INTEGER) {
                    abs = new AbsPrimitive(BasicOntology.INTEGER, 
                                           new Integer(value));
                } 

                if (type == FLOAT) {
                    abs = new AbsPrimitive(BasicOntology.FLOAT, 
                                           new Float(value));
                } 

                return abs;
            } 

            if (kind == AGGREGATE) {
                String       typeName = stream.readUTF();
                AbsAggregate abs = new AbsAggregate(typeName);
                byte         marker = stream.readByte();

                do {
                    if (marker == ELEMENT) {
                        AbsObject elementValue = read(stream, ontology);

                        if (elementValue != null) {
                            abs.add((AbsTerm) elementValue);
                        } 

                        marker = stream.readByte();
                    } 
                } 
                while (marker != END_AGGREGATE);

                return abs;
            } 

            if (kind == CONTENT_ELEMENT_LIST) {
                AbsContentElementList abs = new AbsContentElementList();
                byte                  marker = stream.readByte();

                do {
                    if (marker == ELEMENT) {
                        AbsObject elementValue = read(stream, ontology);

                        if (elementValue != null) {
                            abs.add((AbsContentElement) elementValue);
                        } 

                        marker = stream.readByte();
                    } 
                } 
                while (marker != END_CONTENT_ELEMENT_LIST);

                return abs;
            } 

            String       typeName = stream.readUTF();
            ObjectSchema schema = ontology.getSchema(typeName);
            AbsObject    abs = schema.newInstance();

            references.add(abs);

            counter++;

            byte marker = stream.readByte();

            do {
                if (marker == ATTRIBUTE) {
                    String    slotName = stream.readUTF();
                    AbsObject slotValue = read(stream, ontology);

                    if (slotValue != null) {
                        abs.set(slotName, slotValue);
                    } 

                    marker = stream.readByte();
                } 
            } 
            while (marker != END_ATTRIBUTES);

            return abs;
        } 
        catch (OntologyException oe) {
            throw oe;
        } 
        catch (IOException ioe) {
            throw ioe;
        } 
        catch (Throwable t) {
            throw new IOException("Corrupted stream");
        } 
    } 

    /**
     * Method declaration
     *
     * @param ontology
     * @param content
     *
     * @return
     *
     * @throws CodecException
     *
     * @see
     */
    public byte[] encode(Ontology ontology, 
                         AbsContentElement content) throws CodecException {

        // TODO: check content against ontology.getElementSchema(content.getTypeName())
        return encode(content);
    } 

    /**
     * Method declaration
     *
     * @param ontology
     * @param content
     *
     * @return
     *
     * @throws CodecException
     *
     * @see
     */
    public AbsContentElement decode(Ontology ontology, 
                                    byte[] content) throws CodecException {
        try {
            ByteArrayInputStream buffer = new ByteArrayInputStream(content);
            DataInputStream      stream = new DataInputStream(buffer);

            references = new Vector();
            counter = 0;

            if (content.length == 0) {
                return null;
            } 

            AbsObject obj = read(stream, ontology);

            // TODO: check obj against ontology.getElementSchema(obj.getTypeName());
            stream.close();

            return (AbsContentElement) obj;
        } 
        catch (OntologyException oe) {
            throw new CodecException(oe.getMessage());
        } 
        catch (IOException ioe) {
            throw new CodecException(ioe.getMessage());
        } 
        catch (ClassCastException cce) {
            throw new CodecException(cce.getMessage());
        } 
    } 

    /**
     * Method declaration
     *
     * @param content
     *
     * @return
     *
     * @throws CodecException
     *
     * @see
     */
    public AbsContentElement decode(byte[] content) throws CodecException {
        throw new CodecException("Not supported");
    } 

    /**
     * Method declaration
     *
     * @param content
     *
     * @return
     *
     * @throws CodecException
     *
     * @see
     */
    public byte[] encode(AbsContentElement content) throws CodecException {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            DataOutputStream      stream = new DataOutputStream(buffer);

            references = new Vector();
            counter = 0;

            write(stream, content);
            stream.close();

            return buffer.toByteArray();
        } 
        catch (IOException ioe) {
            throw new CodecException(ioe.getMessage());
        } 
    } 

    /**
     * Constructor declaration
     *
     */
    public LEAPCodec() {
        super(NAME);
    }

}

