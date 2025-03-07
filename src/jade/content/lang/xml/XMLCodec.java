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

package jade.content.lang.xml;

import jade.content.lang.StringCodec;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Map;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.abs.AbsContentElement;
import jade.content.abs.AbsObject;


public class XMLCodec extends StringCodec {

	private static final long serialVersionUID = -711663402273632423L;

	public static final String NAME = "XML";

	// Tag and attribute for aggregates of primitives:
	// For instance a slot named foo that is a list of integers is encoded as below
	// ...
	// <foos>
	//   <primitive type="BO_INTEGER" value="1"/>
	//   <primitive type="BO_INTEGER" value="2"/>
	//   ...
	// </foos>
	// ...
	public static final String PRIMITIVE_TAG = "primitive";
	public static final String VALUE_ATTR = "value";
	public static final String TYPE_ATTR = "type";
	
	public static final String AGGREGATE_ATTR = "aggregate";
	public static final String AGGREGATE_TYPE_ATTR = "aggregate-type";
	public static final String BINARY_STARTER = "#";
	

	private boolean preserveJavaTypes;

	static final String STRING = "STRING";
	static final String INTEGER = "INTEGER";
	static final String BOOLEAN = "BOOLEAN";
	static final String DATE = "DATE";
	static final String BYTE_SEQUENCE = "BYTE_SEQUENCE";
	static final String FLOAT = "FLOAT";
	
	private static Map<String, String> primitiveTypeNames = new HashMap<String, String>();
	static {
		primitiveTypeNames.put(BasicOntology.STRING, STRING);
		primitiveTypeNames.put(BasicOntology.INTEGER, INTEGER);
		primitiveTypeNames.put(BasicOntology.BOOLEAN, BOOLEAN);
		primitiveTypeNames.put(BasicOntology.DATE, DATE);
		primitiveTypeNames.put(BasicOntology.BYTE_SEQUENCE, BYTE_SEQUENCE);
		primitiveTypeNames.put(BasicOntology.FLOAT, FLOAT);
	}
	
	static String getPrimitiveTypeName(String basicOntoPrimitiveTypeName) {
		return primitiveTypeNames.get(basicOntoPrimitiveTypeName);
	}
	

	/**
	 * Create an XMLCodec that preserves java primitive types (long, int, float, double).
	 * This is achieved by encoding long values as <numeric-value>L and float values as
	 * <numeric-valueF.
	 * This constructor is equivalent to <code>XMLCodec(null)</code> 
	 */
	public XMLCodec() {
		this(true);
	}

	/**
	 * Create an XMLCodec specifying whether or not java primitive types (long, int, float, double) must be
	 * preserved.
	 */
	public XMLCodec(boolean preserveJavaTypes) {
		super(NAME);
		this.preserveJavaTypes = preserveJavaTypes;
	}

	/**
	 * Encodes a content into a string.
	 * @param content the content as an abstract descriptor.
	 * @return the content as a string.
	 * @throws CodecException
	 */
	public String encode(AbsContentElement content) throws CodecException {
		throw new CodecException("Not supported");
	}

	/**
	 * Encodes a content into a string using a given ontology.
	 * @param ontology the ontology 
	 * @param content the content as an abstract descriptor.
	 * @return the content as a string.
	 * @throws CodecException
	 */
	public String encode(Ontology ontology, AbsContentElement content) throws CodecException {
		try {
			return encodeAbsObject(ontology, content, false);
		}
		catch (OntologyException oe) {
			throw new CodecException("Ontology error", oe);
		}
	}

	public String encodeAbsObject(Ontology ontology, AbsObject abs, boolean indent) throws CodecException, OntologyException {
		XMLEncoder encoder = new XMLEncoder();
		StringBuffer sb = new StringBuffer();
		encoder.init(ontology, sb, preserveJavaTypes);
		encoder.setIndentEnabled(indent);
		encoder.encode(abs);
		return sb.toString();
	}

	/**
	 * Encode a generic ontological entity in XML form
	 */
	public String encodeObject(Ontology ontology, Object obj, boolean indent) throws CodecException, OntologyException {
		AbsObject abs = ontology.fromObject(obj);
		return encodeAbsObject(ontology, abs, indent);
	}

	/**
	 * Decodes the content to an abstract description.
	 * @param content the content as a string.
	 * @return the content as an abstract description.
	 * @throws CodecException
	 */
	public AbsContentElement decode(String content) throws CodecException {
		throw new CodecException("Not supported");
	}

	/**.
	 * Decodes the content to an abstract description using a 
	 * given ontology.
	 * @param ontology the ontology.
	 * @param content the content as a string.
	 * @return the content as an abstract description.
	 * @throws CodecException
	 */
	public AbsContentElement decode(Ontology ontology, String content) throws CodecException {
		try {
			AbsObject abs = decodeAbsObject(ontology, content);
			if (abs instanceof AbsContentElement) {
				return (AbsContentElement) abs;
			}
			else {
				throw new CodecException(abs.getTypeName()+" is not a content element");
			}
		}
		catch (OntologyException oe) {
			throw new CodecException("Ontology error", oe);
		}
	}

	public AbsObject decodeAbsObject(Ontology ontology, String xml) throws CodecException, OntologyException {
		XMLDecoder decoder = new XMLDecoder();
		decoder.init(ontology, preserveJavaTypes);
		return decoder.decode(xml);
	}

	/**
	 * Decode a generic ontological entity from an XML form
	 */
	public Object decodeObject(Ontology ontology, String xml) throws CodecException, OntologyException {
		AbsObject abs = decodeAbsObject(ontology, xml);
		return ontology.toObject(abs);
	}
	
	
	static String toXML(String javaText){
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(javaText);
		char character =  iterator.current();
		while (character != CharacterIterator.DONE ){
			if (character == '<') {
				result.append("&lt;");
			}
			else if (character == '>') {
				result.append("&gt;");
			}
			else if (character == '\"') {
				result.append("&quot;");
			}
			else if (character == '\'') {
				result.append("&#039;");
			}
			else if (character == '&') {
				result.append("&amp;");
			}
			else if (character == '\n') {
				result.append("&#010;");
			}
			else if (character == '\r') {
				result.append("&#013;");
			}
			else {
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	static String fromXML(String xmlText){
		xmlText = xmlText.replace("&lt;", "<");
		xmlText = xmlText.replace("&gt;", ">");
		xmlText = xmlText.replace("&quot;", "\"");
		xmlText = xmlText.replace("&#039;", "\'");
		xmlText = xmlText.replace("&amp;", "&");
		xmlText = xmlText.replace("&#010;", "\n");
		xmlText = xmlText.replace("&#013;", "\r");
		return xmlText;
	}

}