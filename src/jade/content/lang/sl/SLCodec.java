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
package jade.content.lang.sl;

import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.abs.*;
import jade.content.schema.ObjectSchema;
import jade.content.lang.StringCodec;
//#MIDP_EXCLUDE_BEGIN
import jade.lang.acl.ISO8601;
import jade.util.leap.Iterator;
import jade.domain.FIPANames;
import jade.core.CaseInsensitiveString;

import java.util.Date;
import java.util.Vector;
import java.io.StringReader;

import java.io.BufferedReader; // only for debugging purposes in the main
import java.io.InputStreamReader; // only for debugging purposes in the main
//#MIDP_EXCLUDE_END

/**  
 * The codec class for the <b><i>FIPA-SL</i>n</b> languages. This class
 * implements the <code>Codec</code> interface and allows converting
 * back and forth between strings and frames, according to the SL
 * grammar.
 * By default the class implements full SL grammar, otherwise the proper
 * value must be used in the constructor.
 * @author Fabio Bellifemine - TILAB 
 * @author Nicolas Lhuillier - Motorola (added support for byte[] primitive)
 * @version $Date$ $Revision$
 */
/*#MIDP_INCLUDE_BEGIN
public class SLCodec extends SimpleSLCodec {
#MIDP_INCLUDE_END*/
//#MIDP_EXCLUDE_BEGIN
public class SLCodec extends StringCodec {

    private transient SLParser parser;
    private SL0Ontology slOnto; // ontology of the content language
    private Ontology domainOnto = null; // application ontology

    /**
     * Construct a Codec object for the full SL-language (FIPA-SL).
     */
    public SLCodec() {
    	this(3);
    }
    
    /**
     * Construct a Codec object for the given profile of SL-language.
     * @param slType specify 0 for FIPA-SL0, 1 for FIPA-SL1, 2 for FIPA-SL2, any other value can be used for full FIPA-SL
     */
    public SLCodec(int slType) {
	super((slType==0 ? FIPANames.ContentLanguage.FIPA_SL0 :
	       (slType==1 ? FIPANames.ContentLanguage.FIPA_SL1 :
		(slType==2 ? FIPANames.ContentLanguage.FIPA_SL2 :
		 FIPANames.ContentLanguage.FIPA_SL ))));
	if ((slType < 0) || (slType > 2)) // if outside range, set to full SL
	    slType = 3;
	slOnto = (SL0Ontology) (slType == 0 ? SL0Ontology.getInstance() : 
		(slType == 1 ? SL1Ontology.getInstance() :
		(slType == 2 ? SL2Ontology.getInstance() : SLOntology.getInstance())));
	parser = new SLParser(new StringReader(""));
	parser.setSLType(slType); 
    }


    /**
     * Encodes a content into a String.
     * @param content the content as an abstract descriptor.
     * @return the content as a String.
     * @throws CodecException
     */
    public String encode(AbsContentElement content) throws CodecException {
	return encode(null, content);
    }

    /**
     * Encodes a content into a String.
     * @param ontology the ontology 
     * @param content the content as an abstract descriptor.
     * @return the content as a String.
     * @throws CodecException
     */
    public synchronized String encode(Ontology ontology, AbsContentElement content) throws CodecException {
	domainOnto = ontology;
	StringBuffer str = new StringBuffer("(");
	if (content instanceof AbsContentElementList) {
	    for (Iterator i=((AbsContentElementList)content).iterator(); i.hasNext(); ) {
		AbsObject o = (AbsObject)i.next();
		str.append(toString(o));
		str.append(" ");
	    }
	} else str.append(toString(content));
	str.append(")");
	/*try {
	    return str.toString().getBytes("US-ASCII");
	} catch (java.io.UnsupportedEncodingException e) {
	    e.printStackTrace();
	    return str.toString().getBytes();
	}*/
	return str.toString();
    }

  /** 
   * Take a java String and quote it to form a legal FIPA SL0 string.
   * Add quotation marks to the beginning/end and escape any 
   * quotation marks inside the string.
   * This must be the exact inverse of the procedure in
   * the parser (SLParser.jj) when it encounters a quoted string.
   *
  private String quotedString(String s)
  {
      // Make the stringBuffer a little larger than strictly
      // necessary in case we need to insert any additional
      // characters.  (If our size estimate is wrong, the
      // StringBuffer will automatically grow as needed).
      StringBuffer result = new StringBuffer(s.length()+20);
      result.append("\"");
      for( int i=0; i<s.length(); i++)
          if( s.charAt(i) == '"' ) 
              result.append("\\\"");
          else 
              result.append(s.charAt(i));
      result.append("\"");
      return result.toString();
  }*/


//private static String illegalFirstChar = new String("#0123456789:-?");

    /**
     * Test if the given string is a legal SL0 word using the FIPA XC00008D spec.
     * In addition to FIPA's restrictions, place the additional restriction 
     * that a Word can not contain a '\"', that would confuse the parser at
     * the other end.
     *
    private boolean isAWord( String s) {
	// This should permit strings of length 0 to be encoded.
	if( s==null || s.length()==0 )
	    return false; // words must have at least one character

	if ( illegalFirstChar.indexOf(s.charAt(0)) >= 0 )
	    return false;
      
	for( int i=0; i< s.length(); i++) {
	    char c = s.charAt(i);
	    if( c == '"' || c == '(' || 
		c == ')' || c <= 0x20 )
		return false;
	}
	return true;
    }*/


    /**
     * Encode a string, taking care of quoting separated words and
     * escaping strings, if necessary
     **/
    private String encode(String val) {
	// if the slotName is a String of words then quote it
	if (SimpleSLTokenizer.isAWord(val)) 
	    return val;
	else
	    return SimpleSLTokenizer.quoteString(val);
    }



    private String toString(AbsPredicate val) throws CodecException {
	String propositionSymbol = val.getTypeName();
	if (val.getCount() > 0) { // predicate with arguments
			String[] slotNames = getSlotNames(val);
	    StringBuffer str = new StringBuffer("(");
	    if (slOnto.isUnaryLogicalOp(propositionSymbol)) {
	    	// Unary logical operator of the SL language (NOT)
		str.append(propositionSymbol);
		str.append(" ");
		try {
		    str.append(toString((AbsPredicate)val.getAbsObject(slotNames[0])));
		} catch (RuntimeException e) {
		    throw new CodecException("A UnaryLogicalOp requires a formula argument",e);
		}
	    } else if (slOnto.isBinaryLogicalOp(propositionSymbol)) {
	    	// Bynary logical operator of the SL language (AND, OR)
		str.append(propositionSymbol);
		str.append(" ");
		try {
		    str.append(toString((AbsPredicate)val.getAbsObject(slotNames[0])));
		    str.append(" ");
		    str.append(toString((AbsPredicate)val.getAbsObject(slotNames[1])));
		} catch (RuntimeException e) {
		    throw new CodecException("A BinaryLogicalOp requires 2 formula arguments",e);
		}
	    } else if (slOnto.isQuantifier(propositionSymbol)) {
	    	// Quantifier operator of the SL language (EXISTS, FORALL)
		str.append(propositionSymbol);
		str.append(" ");
		try {
		    str.append(toString((AbsVariable)val.getAbsObject(slotNames[0]))); //FIXME. The hypothesis is that the first slot is the variable
		    str.append(" ");
		    str.append(toString((AbsPredicate)val.getAbsObject(slotNames[1])));
		} catch (RuntimeException e) {
		    throw new CodecException("A Quantifier requires a variable and a formula arguments",e);
		}
	    } else if (slOnto.isModalOp(propositionSymbol)) {
	    	// Modal operator of the SL language (B, I, U, PG)
		str.append(propositionSymbol);
		str.append(" ");
		try {
		    str.append(toString((AbsConcept)val.getAbsObject(slotNames[0])));
		    str.append(" ");
		    str.append(toString((AbsPredicate)val.getAbsObject(slotNames[1])));
		} catch (RuntimeException e) {
		    throw new CodecException("A ModalOp requires a concept and a formula arguments",e);
		}
	    } else if (slOnto.isActionOp(propositionSymbol)) {
	    	// Action operator of the SL language (DONE, FEASIBLE)
		str.append(propositionSymbol);
		str.append(" ");
		try {
		    str.append(toString((AbsTerm)val.getAbsObject(slotNames[0]))); //FIXME check it is an action expression
		    AbsPredicate ap = (AbsPredicate)val.getAbsObject(slotNames[1]);
		    if (ap != null) { // Second argument is optional
			str.append(" ");
			str.append(toString(ap));
		    }
		} catch (RuntimeException e) {
		    throw new CodecException("An ActionOp requires an actionexpression and (optionally) a formula arguments",e);
		}
	    } else if (slOnto.isBinaryTermOp(propositionSymbol)) {
	    	// Binary term operator of the SL language (RESULT, =)
		str.append(propositionSymbol);
		str.append(" ");
		try {
		    str.append(toString((AbsTerm)val.getAbsObject(slotNames[0])));
		    str.append(" ");
		    str.append(toString((AbsTerm)val.getAbsObject(slotNames[1])));
		} catch (RuntimeException e) {
		    throw new CodecException("A BinaryTermOp requires 2 term arguments",e);
		}
	    } else {
		str.append(encode(propositionSymbol));
	    	// Predicate in the ontology
		try {
			encodeSlotsByOrder(val, slotNames, str);
		} catch (RuntimeException e) {
		    throw new CodecException("SL allows predicates with term arguments only",e);
		}
	    }
	    str.append(")");
	    return str.toString();
	} else
			// Proposition
	    return encode(propositionSymbol);  
    }

    private String toString(AbsIRE val) throws CodecException {
	return "(" + encode(val.getTypeName()) + " " + toString(val.getVariable()) + " " + toString(val.getProposition()) + ")"; 
    }
 
    private String toString(AbsVariable val) throws CodecException {
	String var = val.getName();
	if (!var.startsWith("?"))
	    return "?"+encode(var);
	else
	    return "?"+encode(var.substring(1));
    }
    
    private String toString(AbsConcept val) throws CodecException {
	String functionSymbol = val.getTypeName();
	StringBuffer str = new StringBuffer("(");
	String[] slotNames = getSlotNames(val);
	if (slOnto.isSLFunctionWithoutSlotNames(functionSymbol)) { 
			// A Functional operator of the SL language (ACTION, + ...)
			// The form is: functionSymbol Term*
			str.append(functionSymbol);
	    try {
	    	encodeSlotsByOrder(val, slotNames, str);
	    } catch (RuntimeException e) {
		throw new CodecException("A FunctionalOperator requires 1 or 2 Term arguments",e);
	    }
	} else { 
			// A generic term in the ontology. The form can be both 
			// functionSymbol Parameter* or functionSymbol Term*. Get the 
			// preferred way from the ontology.
	    str.append(encode(functionSymbol));
	    try {
	    	// FIXME: To improve performances the two operations that imply
	    	// retrieving a schema from the ontology (getting slot names and
	    	// getting the preferred encoding type) should be carried out at 
	    	// the same time.
	    	if (getEncodingByOrder(val)) {
	    		encodeSlotsByOrder(val, slotNames, str);
	    	}
	    	else {
	    		encodeSlotsByName(val, slotNames, str);
	    	}
	    } catch (RuntimeException e) {
		throw new CodecException("A FunctionalTerm requires Terms arguments",e);
	    }
	}

	str.append(")");
	return str.toString();
    }


    private String toString(AbsAggregate val) throws CodecException {
    	StringBuffer str = new StringBuffer("(");
			str.append(encode(val.getTypeName()));
			for (Iterator i=val.iterator(); i.hasNext(); ) {
				str.append(" ");
				str.append(toString((AbsObject)i.next()));
			}
	    str.append(")");
	    return str.toString();
    }

/*
    private String toString(AbsAgentAction val) throws CodecException {
    	if ( CaseInsensitiveString.equalsIgnoreCase("action",val.getTypeName()) ||
      		 CaseInsensitiveString.equalsIgnoreCase("|",val.getTypeName()) ||
     		 	 CaseInsensitiveString.equalsIgnoreCase(";",val.getTypeName()))
 				return encode(val, false);
 			else
 				//throw new CodecException("SLEncoderRequiresTheSLActionOperator_insteadOf_"+val.getTypeName());
    }*/

    private String toString(AbsPrimitive val) throws CodecException {
	Object v = val.getObject();
	if (v instanceof Date)
	    return ISO8601.toString((Date)v);
	else if (v instanceof Number) 
		  return v.toString();
  else if (v instanceof byte[]) {
    // Note: Currently uses Java default charset, may need to use another one 
    byte[] b = (byte[]) v;
    return "#"+b.length+"\""+new String(b);
  }
  else if (v instanceof Boolean) 
			return v.toString();
	else {
			String vs = v.toString();
			if ( (CaseInsensitiveString.equalsIgnoreCase("true",vs)) ||
					 (CaseInsensitiveString.equalsIgnoreCase("false",vs)) )
					return '"' + vs + '"';  // quote true and false to avoid confusion with booleans
			else
					return encode(vs);
	}
    }

    private String toString(AbsObject val) throws CodecException { 
	if (val instanceof AbsPrimitive) return toString( (AbsPrimitive)val);
	if (val instanceof AbsPredicate) return toString( (AbsPredicate)val);
	if (val instanceof AbsIRE) return toString( (AbsIRE)val);
	if (val instanceof AbsVariable) return toString( (AbsVariable)val);
//	if (val instanceof AbsAgentAction) return toString( (AbsAgentAction)val);
	if (val instanceof AbsAggregate) return toString( (AbsAggregate)val);
	if (val instanceof AbsConcept) return toString( (AbsConcept)val);
	throw new CodecException("SLCodec cannot encode this object "+val);
    }




    /**
     * Decodes the content to an abstract description.
     * @param content the content as a String.
     * @return the content as an abstract description.
     * @throws CodecException
     */
    public AbsContentElement decode(String content) throws CodecException {
	return decode(null, content); 
    }

    /**
     * Decodes the content to an abstract description.
     * @param ontology the ontology.
     * @param content the content as a String.
     * @return the content as an abstract description.
     * @throws CodecException
     */
    public synchronized AbsContentElement decode(Ontology ontology, String content) throws CodecException {
	try {
	    return parser.parse(ontology,content);
	}  catch(Throwable e) { // both ParseException and TokenMgrError
	    throw new CodecException("Parse exception", e);
	}
    }


    public static void main(String[] args) {
	SLCodec codec = null;
	try {
	    codec = new SLCodec(Integer.parseInt(args[0]));
	} catch (Exception e) {
	    System.out.println("usage: SLCodec SLLevel\n  where SLLevel can be 0 for SL0, 1 for SL1, 2 for SL2, 3 or more for full SL");
	    System.exit(0);
	}

	while (true) {
	    try {
		System.out.println("insert an SL expression to parse (all the expression on a single line!): ");
		BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
		String str = buff.readLine();
		System.out.println("\n\n");
		//AbsContentElement result = codec.decode(str.getBytes("US-ASCII"));
		AbsContentElement result = codec.decode(str);
		System.out.println("DUMP OF THE DECODE OUTPUT:");
		System.out.println(result);
		System.out.println("\n\n");
		System.out.println("AFTER ENCODE:");
		//System.out.println(new String(codec.encode(result),"US-ASCII"));
		System.out.println(codec.encode(result));
		System.out.println("\n\n");
	    } catch(Exception pe) {
		pe.printStackTrace();
		//System.exit(0);
	    }
	}
    }

    /**
     * @return the ontology containing the schemas of the operator
     * defined in this language
     */
    public Ontology getInnerOntology() {
    	return slOnto;
    }
    
		private String[] getSlotNames(AbsObject abs) throws CodecException {
    	String[] slotNames = null;
    	String type = abs.getTypeName();
			if (domainOnto != null) {
				// If an ontology is specified, get the slot names from it 
				// (and not directly from the abstract descriptor val) to preserve 
				// the order
				try {
					ObjectSchema s = domainOnto.getSchema(type);
					if (s == null) {
						throw new CodecException("No schema found for symbol "+type);
					}
					slotNames = s.getNames();
				}
				catch (OntologyException oe) {
					throw new CodecException("Error getting schema for symbol "+type, oe);
				}
			}
			else {
	    	slotNames = abs.getNames();
			}
			return slotNames;
		}
    
		private boolean getEncodingByOrder(AbsObject abs) throws CodecException {
			if (domainOnto != null) {
    		String type = abs.getTypeName();
				try {
					ObjectSchema s = domainOnto.getSchema(type);
					return s.getEncodingByOrder();
				}
				catch (Exception e) {
					// Just ignore it
				}
			}
			return false;
		}
		
    /**
     * Encode the slots of an abstract descriptor by order, i.e. 
     * without writing the slot names. Also take into account that, in
     * order to ensure a correct parsing, empty slots can only occur at 
     * the end.
     */
    private void encodeSlotsByOrder(AbsObject val, String[] slotNames, StringBuffer str) throws CodecException {
			boolean lastSlotEmpty = false;
			for (int i=0; i<slotNames.length; i++) {
				AbsTerm t = (AbsTerm)val.getAbsObject(slotNames[i]);
				if (t != null) {
					if (lastSlotEmpty) {
						throw new CodecException("Non-empty slot "+slotNames[i]+" follows empty slot "+slotNames[i-1]);
					}
					str.append(" ");
					str.append(toString(t));
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
    private void encodeSlotsByName(AbsObject val, String[] slotNames, StringBuffer str) throws CodecException {
		  for (int i=0; i<slotNames.length; i++) {
				AbsTerm t = (AbsTerm)val.getAbsObject(slotNames[i]);
				if (t != null) {
					str.append(" :");
					str.append(encode(slotNames[i]));
					str.append(" ");
					str.append(toString(t));
				}
		  }
    }
//#MIDP_EXCLUDE_END
}

