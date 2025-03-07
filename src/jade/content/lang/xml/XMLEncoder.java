package jade.content.lang.xml;

import java.util.Date;

import jade.content.abs.*;
import jade.content.schema.*;
import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SL0Vocabulary;
import jade.lang.acl.ISO8601;
import jade.util.leap.HashMap;
import jade.util.leap.Iterator;
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Map;

import org.apache.commons.codec.binary.Base64;

class XMLEncoder {	
	private static final char OPEN_ANG = '<';
	private static final char CLOSE_ANG = '>';
	
	private Ontology ontology;
	private StringBuffer buffer;
	private boolean indentEnabled = false;
	private int tabs = 0;
	private boolean preserveJavaTypes;
	
	public void init(Ontology onto, StringBuffer sb, boolean preserveJavaTypes) {
		ontology = onto;
		buffer = sb;
		this.preserveJavaTypes = preserveJavaTypes;
	}
	
	public void setIndentEnabled(boolean b) {
		indentEnabled = b;
	}
	
	public void encode(AbsObject abs) throws CodecException, OntologyException {
		if (abs != null) {
			if (isAggregate(abs)) {
				encodeAggregate((AbsAggregate) abs);
			}
			else if (isFrame(abs)) {
				encodeFrame((AbsPrimitiveSlotsHolder) abs);
			}
			else if (isPrimitive(abs)) {
				encodePrimitive((AbsPrimitive) abs);
			}
			else if (isContentElementList(abs)) {
				encodeContentElementList((AbsContentElementList) abs);
			}
			else {
				throw new CodecException("Unsupported Abstract decsriptor type "+abs);
			}
		}
	}
	
	private boolean isAggregate(AbsObject abs) {
		return abs instanceof AbsAggregate;
	}
	
	private boolean isFrame(AbsObject abs) {
		return abs instanceof AbsPrimitiveSlotsHolder;
	}
	
	private boolean isPrimitive(AbsObject abs) {
		return abs instanceof AbsPrimitive;
	}
	
	private boolean isContentElementList(AbsObject abs) {
		return abs instanceof AbsContentElementList;
	}
	
	private void encodeAggregate(AbsAggregate abs) throws CodecException, OntologyException {
		for (int i = 0; i < abs.size(); ++i) {
			encode(abs.get(i));
		}
	}
	
	private void encodeContentElementList(AbsContentElementList abs) throws CodecException, OntologyException {
		encodeOpenTag(abs.getTypeName(), null);
		tabs++;
		for (int i = 0; i < abs.size(); ++i) {
			encode(abs.get(i));
		}
		tabs--;
		encodeCloseTag(abs.getTypeName());
	}
	
	private void encodeFrame(AbsPrimitiveSlotsHolder abs) throws CodecException, OntologyException {
		ObjectSchema schema = ontology.getSchema(abs.getTypeName());
		if (schema != null) {
			boolean encodeByOrder = schema.getEncodingByOrder();
			String name = abs.getTypeName();
			
			insertIndent();
			buffer.append(OPEN_ANG);
			buffer.append(name);
			List tagSlotNames = encodeAttributes(abs, schema);
			if (tagSlotNames.isEmpty()) {
				buffer.append('/');
				buffer.append(CLOSE_ANG);
				insertNewline();
			}
			else {
				buffer.append(CLOSE_ANG);
				insertNewline();
				tabs++;
				
				// Encode slots that have not been encoded yet as attributes
				for (int i = 0; i < tagSlotNames.size(); ++i) {
					String slotName = (String) tagSlotNames.get(i);
					AbsObject slot = abs.getAbsObject(slotName);
					if (slot != null) {
						if (slot instanceof AbsPrimitive) {
							// This must be a String otherwise it would have been already encoded as an attribute
							encodeString(slotName, ((AbsPrimitive) slot).getString());
						}
						else {
							boolean closeSlotNameTag = false;
							if (slot instanceof AbsAggregate) {
								// Aggregate slots are always encoded by name. 
								Map aggregateModifyingAttributes = new HashMap();
								ObjectSchema slotSchema = schema.getSchema(slotName);
								if (!(slotSchema instanceof AggregateSchema)) {
									// The slot value is an aggregate, but this cannot be desumed from the slot schema -->
									// Insert an indication to enable correct decoding
									aggregateModifyingAttributes.put(XMLCodec.AGGREGATE_ATTR, "true");

									// Furthermore if the aggregate type is different than SEQUENCE, insert 
									// an indication to allow reconstructing it in the decoding phase
									if (!slot.getTypeName().equals(SL0Vocabulary.SEQUENCE)) {
										aggregateModifyingAttributes.put(XMLCodec.AGGREGATE_TYPE_ATTR, slot.getTypeName());
									}
								}
								encodeOpenTag(slotName, aggregateModifyingAttributes);
								closeSlotNameTag = true;
								tabs++;
							}
							else {
								// Frame slots are encoded according to the schema preference
								if (!encodeByOrder) {
									encodeOpenTag(slotName, null);
									closeSlotNameTag = true;
									tabs++;
								}
							}
							encode(slot);
							if (closeSlotNameTag) {
								tabs--;
								encodeCloseTag(slotName);
							}
						}
					}
				}
				tabs--;
				encodeCloseTag(name);
			}
		}
		else {
			throw new OntologyException("No schema found for type "+abs.getTypeName());
		}
	}
	
	/**
	 * Encode as attributes all slots with primitive values and return a list with the names of the slots that
	 * have not been encoded yet.
	 */
	private List encodeAttributes(AbsPrimitiveSlotsHolder abs, ObjectSchema schema) throws CodecException {
		List tagSlotNames = new ArrayList();
		try {
			String[] slotNames = schema.getNames();
			for (int i = 0; i < slotNames.length; ++i) {
				String slotName = slotNames[i];
				AbsObject slot = abs.getAbsObject(slotName);
				if (slot != null && slot instanceof AbsPrimitive) {
					Object obj = ((AbsPrimitive) slot).getObject();
					if (obj instanceof String && schema.getSchema(slotName) != BasicOntology.getInstance().getSchema(BasicOntology.STRING)) {
						// String primitives are encoded as tag
						tagSlotNames.add(slotName);
					}
					else {
						buffer.append(' ');
						buffer.append(slotName);
						buffer.append('=');
						buffer.append('"');
						buffer.append(getPrimitiveValue((AbsPrimitive) slot));
						buffer.append('"');
					}
				}
				else {
					tagSlotNames.add(slotName);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return tagSlotNames;
	}
	
	
	/**
	 * This is used only to encode primitive entities inside aggregates. 
	 * Such entities will appear as 
	 * <primitive type="BO_INTEGER" value="5"/>
	 * Normal primitive slots are encoded as XML attributes
	 */
	private void encodePrimitive(AbsPrimitive abs) throws CodecException {
		insertIndent();
		buffer.append(OPEN_ANG);
		buffer.append(XMLCodec.PRIMITIVE_TAG);
		buffer.append(' ');
		buffer.append(XMLCodec.TYPE_ATTR);
		buffer.append('=');
		buffer.append('"');
		buffer.append(XMLCodec.getPrimitiveTypeName(abs.getTypeName()));
		buffer.append('"');
		buffer.append(' ');
		buffer.append(XMLCodec.VALUE_ATTR);
		buffer.append('=');
		buffer.append('"');
		buffer.append(getPrimitiveValue(abs));
		buffer.append('"');
		buffer.append("/");
		buffer.append(CLOSE_ANG);
		insertNewline();
	}
	
	private void encodeString(String slotName, String slotValue) {
		insertIndent();
		buffer.append(OPEN_ANG);
		buffer.append(slotName);
		buffer.append(CLOSE_ANG);
		buffer.append(XMLCodec.toXML(slotValue));
		buffer.append(OPEN_ANG);
		buffer.append('/');
		buffer.append(slotName);
		buffer.append(CLOSE_ANG);
		insertNewline();
	}
	
	private void encodeOpenTag(String name, Map attrs) {
		insertIndent();
		buffer.append(OPEN_ANG);
		buffer.append(name);
		if (attrs != null) {
			Iterator it = attrs.keySet().iterator();
			while (it.hasNext()) {
				String attrName = (String) it.next();
				buffer.append(' ');
				buffer.append(attrName);
				buffer.append("=\"");
				buffer.append(attrs.get(attrName));
				buffer.append("\"");
			}
		}
		buffer.append(CLOSE_ANG);
		insertNewline();
	}
	
	/*private void encodeOpenAggregateTag(String name) {
		insertIndent();
		buffer.append(OPEN_ANG);
		buffer.append(name);
		buffer.append(' ');
		buffer.append(XMLCodec.AGGREGATE_ATTR);
		buffer.append("=\"true\"");
		buffer.append(CLOSE_ANG);
		insertNewline();
	}*/
	
	private void encodeCloseTag(String name) {
		insertIndent();
		buffer.append(OPEN_ANG);
		buffer.append('/');
		buffer.append(name);
		buffer.append(CLOSE_ANG);
		insertNewline();
	}
	
	private String getPrimitiveValue(AbsPrimitive abs) throws CodecException {
		Object obj = abs.getObject();
		if (obj instanceof Date) {
			return ISO8601.toString((Date) obj);
		}
		else if (preserveJavaTypes && obj instanceof Long) {
			return obj.toString()+'L';
		}
		else if (preserveJavaTypes && obj instanceof Float) {
			return obj.toString()+'F';
		}
		else if (obj instanceof byte[]) {
			try {
				String base64Str = new String(Base64.encodeBase64((byte[]) obj), "US-ASCII"); 
				StringBuffer sb = new StringBuffer(XMLCodec.BINARY_STARTER);
				sb.append(base64Str);
				return sb.toString();
			}
			catch (Exception e) {
				throw new CodecException("Error encoding binary value. ", e);
			}
		}
		else {
			// String -> encode text
			if (obj instanceof String) {
				return XMLCodec.toXML((String)obj);
			}
			
			// Boolean, Integer, Double, (Long, Float if not preserving Java types) 
			return obj.toString();
		}
	}
	
	private void insertNewline() {
		if (indentEnabled) {
			buffer.append('\n');
		}
	}
	
	private void insertIndent() {
		if (indentEnabled) {
			for (int i = 0; i < tabs; ++i) {
				buffer.append('\t');
			}
		}
	}
}
