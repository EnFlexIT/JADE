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

import jade.core.CaseInsensitiveString;
import jade.content.lang.*;
import jade.content.onto.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.util.leap.*;
import java.util.Vector;
import java.util.Date;
import java.io.*;

/**
 * @author Federico Bergenti - Universita` di Parma
 */
public class LEAPCodec extends ByteArrayCodec {
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
    private static final byte  DATE = 12;
    private static final byte  BYTE_SEQUENCE = 13;
    private static final byte  CONTENT_ELEMENT_LIST = 14;
    private static final byte  END_CONTENT_ELEMENT_LIST = 15;

    // LEAP Language operators
    public static final String INSTANCEOF = "INSTANCEOF";
    public static final String INSTANCEOF_ENTITY = "entity";
    public static final String INSTANCEOF_TYPE = "type";

    public static final String IOTA = "IOTA";
    
    /**
     * Construct a LEAPCodec object i.e. a Codec for the LEAP language
     */
    public LEAPCodec() {
        super(NAME);
    }

    /**
     * @return the ontology containing the schemas of the operator
     * defined i this language
     */
    public Ontology getInnerOntology() {
    	return LEAPOntology.getInstance();
    }
    
    /**
     * Encodes an abstract descriptor holding a content element
     * into a byte array.
     * @param content the content as an abstract descriptor.
     * @return the content as a byte array.
     * @throws CodecException
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
        catch (Throwable t) {
            throw new CodecException("Error encoding content", t);
        } 
    } 

    /**
     * Encodes a content into a byte array.
     * @param ontology the ontology 
     * @param content the content as an abstract descriptor.
     * @return the content as a byte array.
     * @throws CodecException
     */
    public byte[] encode(Ontology ontology, AbsContentElement content) throws CodecException {
        return encode(content);
    } 

    /**
     * Decodes the content to an abstract descriptor.
     * @param content the content as a byte array.
     * @return the content as an abstract description.
     * @throws CodecException
     */
    public AbsContentElement decode(byte[] content) throws CodecException {
        throw new CodecException("Not supported");
    } 

    /**
     * Decodes the content to an abstract description.
     * @param ontology the ontology.
     * @param content the content as a byte array.
     * @return the content as an abstract description.
     * @throws CodecException
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

            // TODO: check obj against ontology.getSchema(obj.getTypeName());
            stream.close();

            return (AbsContentElement) obj;
        } 
        catch (Throwable t) {
            throw new CodecException("Error decoding content", t);
        } 
    } 

    /**
     * Synchronized so that it can possibly be executed by different threads
     */
    private synchronized void write(DataOutputStream stream, 
                                    AbsObject abs) throws Throwable {
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
            		stream.writeUTF((String) obj);
            } 

            if (obj instanceof Boolean) {
                stream.writeByte(BOOLEAN);
            		stream.writeBoolean(((Boolean) obj).booleanValue());
            } 

            if (obj instanceof Long) {
                stream.writeByte(INTEGER);
            		stream.writeLong(((Long) obj).longValue());
            } 

            //__CLDC_UNSUPPORTED__BEGIN
            if (obj instanceof Double) {
                stream.writeByte(FLOAT);
            		stream.writeDouble(((Double) obj).doubleValue());
            } 
            //__CLDC_UNSUPPORTED__END

            if (obj instanceof Date) {
                stream.writeByte(DATE);
            		stream.writeLong(((Date) obj).getTime());
            } 

            if (obj instanceof byte[]) {
                stream.writeByte(BYTE_SEQUENCE);
                byte[] b = (byte[]) obj;
            		stream.writeInt(b.length);
            		stream.write(b, 0, b.length);
            } 

            return;
        } 

        if (abs instanceof AbsAggregate) {
            stream.writeByte(AGGREGATE);
            stream.writeUTF(abs.getTypeName());

            AbsAggregate aggregate = (AbsAggregate) abs;

            for (int i = 0; i < aggregate.size(); i++) {
                stream.writeByte(ELEMENT);
                write(stream, aggregate.get(i));
            } 

            stream.writeByte(END_AGGREGATE);

            return;
        } 

        if (abs instanceof AbsContentElementList) {
            stream.writeByte(CONTENT_ELEMENT_LIST);

            AbsContentElementList acel = (AbsContentElementList) abs;

            for (Iterator i = acel.iterator(); i.hasNext(); ) {
                stream.writeByte(ELEMENT);
                write(stream, (AbsObject) i.next());
            } 

            stream.writeByte(END_CONTENT_ELEMENT_LIST);

            return;
        } 

        references.addElement(abs);
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
     * Synchronized so that it can possibly be executed by different threads
     */
    private synchronized AbsObject read(DataInputStream stream, 
                                        Ontology ontology) throws Throwable {
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
            AbsPrimitive abs = null;

            if (type == STRING) {
            		String value = stream.readUTF();
                abs = AbsPrimitive.wrap(value);
            } 

            if (type == BOOLEAN) {
            		boolean value = stream.readBoolean();
                abs = AbsPrimitive.wrap(value);
            } 

            if (type == INTEGER) {
            		long value = stream.readLong();
                abs = AbsPrimitive.wrap(value);
            } 

            //__CLDC_UNSUPPORTED__BEGIN
            if (type == FLOAT) {
            		double value = stream.readDouble();
                abs = AbsPrimitive.wrap(value);
            } 
            //__CLDC_UNSUPPORTED__END

            if (type == DATE) {
                long value = stream.readLong();
                abs = AbsPrimitive.wrap(new Date(value));
            } 

            if (type == BYTE_SEQUENCE) {
                byte[] value = new byte[stream.readInt()];
                stream.read(value, 0, value.length);
                abs = AbsPrimitive.wrap(value);
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
                    		try {
                          abs.add((AbsTerm) elementValue);
                       	}
                        catch (ClassCastException cce) {
                        	throw new CodecException("Non term element in aggregate"); 
                        }
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
                      	try {
                            abs.add((AbsContentElement) elementValue);
                        }
                        catch (ClassCastException cce) {
                        		throw new CodecException("Non content-element element in content-element-list"); 
                        }
                    } 

                    marker = stream.readByte();
                } 
            } 
            while (marker != END_CONTENT_ELEMENT_LIST);

            return abs;
        } 

        String       typeName = stream.readUTF();
        // DEBUG System.out.println("Type is "+typeName);
        ObjectSchema schema = ontology.getSchema(typeName);
        // DEBUG System.out.println("Schema is "+schema);
        AbsObject    abs = schema.newInstance();

        references.addElement(abs);

        counter++;

        byte marker = stream.readByte();

        do {
            if (marker == ATTRIBUTE) {
                String    attributeName = stream.readUTF();
                AbsObject attributeValue = read(stream, ontology);

                if (attributeValue != null) {
                    AbsHelper.setAttribute(abs, attributeName, attributeValue);
                } 

                marker = stream.readByte();
            } 
        } 
        while (marker != END_ATTRIBUTES);

        return abs;
    } 
}

