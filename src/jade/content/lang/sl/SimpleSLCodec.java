package jade.content.lang.sl;

import jade.content.*;
import jade.content.abs.*;
import jade.content.schema.ObjectSchema;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.BasicOntology;
import jade.content.lang.StringCodec;
import jade.content.lang.sl.SLVocabulary;
import jade.lang.acl.ISO8601;
import jade.util.leap.Iterator;
import java.util.Date;

class SimpleSLCodec extends StringCodec {
	private int indent = 0;
	
	public SimpleSLCodec() {
		super(jade.domain.FIPANames.ContentLanguage.FIPA_SL);
	}
	
  /**
   * Encodes a content into a string using a given ontology.
   * @param ontology the ontology 
   * @param content the content as an abstract descriptor.
   * @return the content as a string.
   * @throws CodecException
   */
  public String encode(Ontology ontology, AbsContentElement content) throws CodecException {
		StringBuffer str = new StringBuffer("(");
		if (content instanceof AbsContentElementList) {
	    for (Iterator i=((AbsContentElementList)content).iterator(); i.hasNext(); ) {
				AbsObject abs = (AbsObject) i.next();
				stringify(abs, ontology, str);
  			str.append(" ");
	    }
		} 
		else {
			stringify(content, ontology, str);
		}
		str.append(")");
		return str.toString();
	}

  private void stringify(AbsObject val, Ontology onto, StringBuffer str) throws CodecException {
		if (val instanceof AbsPrimitive) 
			stringifyPrimitive((AbsPrimitive) val, str);
		else if (val instanceof AbsVariable) 
			stringifyVariable((AbsVariable) val, str);
		else if (val instanceof AbsAggregate) 
			stringifyAggregate((AbsAggregate)val, onto, str);
		else 
			stringifyComplex(val, onto, str);
  }

  private void stringifyComplex(AbsObject val, Ontology onto, StringBuffer str) throws CodecException {
		str.append("(");
		str.append(val.getTypeName());
		ObjectSchema s = null;
  	try {
	  	s = onto.getSchema(val.getTypeName());
  	}
  	catch (OntologyException oe) {
  		throw new CodecException("Error getting the schema for element "+val, oe);
  	}
  	if (val instanceof AbsConcept && !s.getEncodingByOrder()) {
  		encodeSlotsByName(val, val.getNames(), onto, str);
  	}
  	else {
  		encodeSlotsByOrder(val, s.getNames(), onto, str);
  	}
  	str.append(")");
  }
	  			
  /**
   * Encode the slots of an abstract descriptor by order, i.e. 
   * without writing the slot names. Also take into account that, in
   * order to ensure a correct parsing, empty slots can only occur at 
   * the end.
   */
  private void encodeSlotsByOrder(AbsObject val, String[] slotNames, Ontology onto, StringBuffer str) throws CodecException {
		boolean lastSlotEmpty = false;
		for (int i=0; i<slotNames.length; i++) {
			AbsObject s = val.getAbsObject(slotNames[i]);
			if (s != null) {
				if (lastSlotEmpty) {
					throw new CodecException("Non-empty slot "+slotNames[i]+" follows empty slot "+slotNames[i-1]);
				}
				str.append(" ");
				stringify(s, onto, str);
			}
			else {
				lastSlotEmpty = true;
			}
		}
  }
    
  /**
   * Encode the slots of an abstract descriptor by name, i.e. 
   * writing for each non-empty slot the slot name followed by the
   * slot value.
   */
  private void encodeSlotsByName(AbsObject val, String[] slotNames, Ontology onto, StringBuffer str) throws CodecException {
  	for (int i=0; i<slotNames.length; i++) {
			AbsObject s = val.getAbsObject(slotNames[i]);
			if (s != null) {
				str.append(" :");
				str.append(slotNames[i]);
				str.append(" ");
				stringify(s, onto, str);
			}
		}
  }
  
  private void stringifyAggregate(AbsAggregate val, Ontology onto, StringBuffer str) throws CodecException {
		str.append("(");
		str.append(val.getTypeName());
		for (Iterator i=val.iterator(); i.hasNext(); ) {
			str.append(" ");
			stringify((AbsObject)i.next(), onto, str);
		}
	  str.append(")");
  }

  private void stringifyVariable(AbsVariable val, StringBuffer str) throws CodecException {
  	str.append("?");
  	str.append(val.getName());
  }
  
  private void stringifyPrimitive(AbsPrimitive val, StringBuffer str) throws CodecException {
		String type = val.getTypeName();
  	if (type.equals(BasicOntology.STRING)) {	
	   	String s = val.getString();
	   	if (isAWord(s)) {
	   		str.append(s);
	   	}
	   	else {
	   		str.append("\"");
	   		str.append(s);
	   		str.append("\"");
	   	}
  	}
  	else if (type.equals(BasicOntology.DATE))
	    str.append(ISO8601.toString(val.getDate()));
  	else if (type.equals(BasicOntology.BYTE_SEQUENCE))
  		throw new CodecException("SL_does_not_allow_encoding_sequencesOfBytes");
  	else 
  		str.append(val.getObject().toString());
  }

  /**
   * Test if the given string is a legal SL word using the FIPA XC00008D spec.
   * In addition to FIPA's restrictions, place the additional restriction 
   * that a Word can not contain a '\"', that would confuse the parser at
   * the other end.
   */
  private boolean isAWord( String s) {
		// This should permit strings of length 0 to be encoded.
		if( s==null || s.length()==0 ) {
	    return false; // words must have at least one character
		}
		
		String illegalFirstChar = new String("#0123456789:-?"); 
		if ( illegalFirstChar.indexOf(s.charAt(0)) >= 0 ) {
	    return false;
		}
		for( int i=0; i< s.length(); i++) {
	    if( s.charAt(i) == '"' || s.charAt(i) == '(' || 
					s.charAt(i) == ')' || s.charAt(i) <= 0x20 ) {
				return false;
			}
		}		
		return true;
  }

  /**
   * Decodes the content to an abstract description using a 
   * given ontology.
   * @param ontology the ontology.
   * @param content the content as a string.
   * @return the content as an abstract description.
   * @throws CodecException
   */
  public AbsContentElement decode(Ontology ontology, String content) throws CodecException {
  	Parser p = new Parser(content);
  	try {
	  	p.consumeChar('(');
  		AbsContentElement abs = (AbsContentElement) parse(p, ontology);
  		if (!p.next().equals(")")) {
  			AbsContentElementList l = new AbsContentElementList();
	  		l.add(abs);
  			do {
  				AbsContentElement abs1 = (AbsContentElement) parse(p, ontology);
  				l.add(abs1);
  			} while (!p.next().equals(")"));
  			abs = l;
  		}
  		p.consumeChar(')');
  		return abs;
  	}
  	catch (ClassCastException cce) {
  		throw new CodecException("Error converting to AbsContentElement", cce);
  	}
  }
  
  private AbsObject parse(Parser p, Ontology o) throws CodecException {
  	AbsObject abs = null;
  	if (p.next().startsWith("(")) {
  		abs = parseComplex(p, o);
  	}
  	else {
  		abs = parseSimple(p);
  	}
  	return abs;
  }

  private AbsObject parseComplex(Parser p, Ontology o) throws CodecException {
  	AbsObject abs = null;
  	p.consumeChar('(');
  	String name = p.consumeWord();
  	log("Parse complex descriptor: "+name);
  	++indent;
  	try {
	  	ObjectSchema s = o.getSchema(name);
  		abs = s.newInstance();
  		if (abs instanceof AbsAggregate) {
  			fillAggregate((AbsAggregate) abs, p, o);
  		}
  		else if (p.next().startsWith(":")) {
  			fillSlotsByName((AbsConcept) abs, p, o);
  		}
  		else {
	  		fillSlotsByOrder(abs, s, p, o);
  		}
  	}
  	catch (CodecException ce) {
  		throw ce;
  	}
  	catch (Throwable t) {
  		throw new CodecException("Unexpeceted error parsing "+name, t);
  	}
  	indent--;
  	p.consumeChar(')');
  	return abs;
  }
  		
  private void fillSlotsByOrder(AbsObject abs, ObjectSchema s, Parser p, Ontology o) throws CodecException {
  	String[] slotNames = s.getNames();
  	int i = 0;
  	while (!p.next().startsWith(")")) {
  		AbsObject val = parse(p, o);
  		try {
	  		AbsHelper.setAttribute(abs, slotNames[i], val);
	  		++i;
  		}
  		catch (OntologyException oe) {
  			throw new CodecException("Can't assign "+val+" to slot "+slotNames[i]+" of "+abs);
  		}
  	}
  }

  private void fillSlotsByName(AbsConcept abs, Parser p, Ontology o) throws CodecException {
  	while (!p.next().startsWith(")")) {
  		// No need to check again that the slot name starts with :
  		String slotName = p.consumeWord().substring(1);
  		try {
	  		AbsTerm val = (AbsTerm) parse(p, o);
	  		abs.set(slotName, val);
  		}
  		catch (ClassCastException cce) {
  			throw new CodecException("Non Term value for slot "+slotName+" of Concept "+abs);
  		}
  	}
  }

  private void fillAggregate(AbsAggregate abs, Parser p, Ontology o) throws CodecException {
  	int i = 0;
  	while (!p.next().startsWith(")")) {
  		try {
  			AbsTerm val = (AbsTerm) parse(p, o);
	  		abs.add(val);
	  		++i;
  		}
  		catch (ClassCastException cce) {
  			throw new CodecException("Non Term value for element "+i+" of Aggregate "+abs);
  		}
  	}
  }

  private AbsObject parseSimple(Parser p) throws CodecException {
  	String val = p.consumeElement();
  	log("Parse simple descriptor: "+val+". Next is "+p.next());
  	// Integer
  	try {
  		return AbsPrimitive.wrap(Long.parseLong(val));
  	}
  	catch (Exception e) {
  	}
//__CLDC_UNSUPPORTED__BEGIN
  	// Float
  	try {
  		// Note that Double.parseDouble() does not exist in PJava
  		return AbsPrimitive.wrap(Double.valueOf(val).doubleValue());
  	}
  	catch (Exception e) {
  	}
//__CLDC_UNSUPPORTED__END
  	// Date
  	try {
  		return AbsPrimitive.wrap(ISO8601.toDate(val));
  	}
  	catch (Exception e) {
  	}
  	// Boolean
  	if (val.equals("true")) {
  		return AbsPrimitive.wrap(true);
  	}
  	if (val.equals("false")) {
  		return AbsPrimitive.wrap(false);
  	}
  	// Variable
  	if (val.startsWith("?")) {
  		return new AbsVariable(val.substring(1, val.length()), null);
  	}
  	// String
  	if (val.startsWith("\"")) {
  		return AbsPrimitive.wrap(val.substring(1, val.length()-1));
  	}
  	else {
  		return AbsPrimitive.wrap(val);
  	}
  }
  	
  /**
   */
  public AbsContentElement decode(String content) throws CodecException {
    throw new CodecException("Unsupported operation");
  }

  /**
   */
  public String encode(AbsContentElement content) throws CodecException {
   	throw new CodecException("Unsupported operation");
  }

  /**
     Inner class Parser
   */
  class Parser {
  	private String content;
  	private int current = 0;
  	
  	Parser(String s) {
  		content = s;
  	}
  	
  	String next() {
  		skipSpaces();
  		char c = content.charAt(current);
  		if (c == ')' || c == '(') {
  			return String.valueOf(c);
  		}
  		int start = current;
  		while (!isSpace(c) && c != ')') {
  			c = content.charAt(++current);
  		}
  		String s = content.substring(start, current);
  		current = start;
  		return s;
  	}
  	
  	void consumeChar(char c) throws CodecException {
  		skipSpaces();
  		if (content.charAt(current++) != c) {
  			throw new CodecException("Parse error: found "+content.charAt(current-1)+" while "+c+" was expected");
  		}
  	}
  	
  	String consumeWord() {
  		skipSpaces();
  		int start = current;
  		char c = content.charAt(current);
  		while (!isSpace(c) && c != ')') {
  			c = content.charAt(++current);
  		}
  		String s = content.substring(start, current);
  		return s;
  	}
  	
  	String consumeElement() throws CodecException {
  		String el = null;
  		skipSpaces();
  		if (content.charAt(current) == '"') {
  			int start = current++;
  			while (content.charAt(current) != '"') {
  				current++;
  			}
  			current++;
  			el = content.substring(start, current);
  		}
  		else {
  			el = consumeWord();
  		}
  		return el;
  	}
  	
  	private void skipSpaces() {
  		while (isSpace(content.charAt(current))) {
  			current++;
  		}
  	}
  	
  	private boolean isSpace(char c) {
  		return (c == ' ' || c == '\t' || c == '\n');
  	}
  }
  
  private void log(String s) {
  	/*for (int i = 0; i < indent; ++i) {
  		System.out.print("  ");
  	}
  	System.out.println(s);*/
  }
  
  public Ontology getInnerOntology() {
    return SLOntology.getInstance();
  }

}