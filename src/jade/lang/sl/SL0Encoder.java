/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package jade.lang.sl;

import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;

import java.util.*;

import jade.onto.Frame;
import jade.onto.OntologyException;
import jade.lang.Codec;

/**
  
  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$
 * Modified by:
 * @author Craig Sayers, HP Labs, Palo Alto, California
 */
class SL0Encoder {

  public String encode(Frame f) {
    StringWriter out = new StringWriter();
    try {
      if (f.size()<=0) //if is a PropositionSymbol,then just write the symbol
	out.write(f.getName());
      else // else write the frame
	writeFrame(f, out);
      out.flush();
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
    }
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
    catch(Codec.CodecException ce) {
      ce.printStackTrace();
    }
    return out.toString();
  }


  private void writeFrame(Frame f, Writer w) throws IOException, OntologyException, Codec.CodecException {
  	w.write("(" + f.getName() + " ");
		for (int i=0; i<f.size(); i++ ) {
			String slotName = f.getSlotName(i); 
			Object slotValue = f.getSlot(i);
			if (!slotName.startsWith(Frame.UNNAMEDPREFIX)) {
	  		// if the slotName does not start with ':' then add ':'
	  		if (!slotName.startsWith(":"))
	    		w.write(":");
	  		// if the slotName is a String of words then quote it
	  		if (isAWord(slotName)) 
	    		    w.write(slotName);
	  		else
	    		    w.write(quotedString(slotName));
	  		w.write(" ");
			}
			if (isFrame(slotValue))
	  		    writeFrame((Frame)slotValue, w);
                        else if (slotValue.getClass().equals(java.util.Date.class))
	  		    // if it is a Date then write a DateTimetoken
	  		    // I wanted to use an SLDate that extends Date but, if I did
	  		    // then the ontology would no more be language-independent!
	  		    w.write(jade.lang.acl.ISO8601.toString((java.util.Date)slotValue));
			else if (slotValue.getClass().equals(java.lang.String.class)) {
                            String stringifiedValue = slotValue.toString();
                            if( isAWord(stringifiedValue) && ! isAToken(stringifiedValue) )
                                w.write(stringifiedValue);
                            else
                                w.write(quotedString(stringifiedValue));
			}
                        else if (slotValue.getClass().equals(java.lang.Byte[].class)) 
	  		    throw new Codec.CodecException("SL0 does not support binary fields", null);
                        else {
                            // Its not date, string, or byte, so the regular toString will work
                            w.write(slotValue.toString());
                        }
			w.write(" ");
		}
    w.write(")");
  }

  /**
   * Test if the given string is a legal SL0 word using the FIPA XC00008D spec.
   * In addition to FIPA's restrictions, place the additional restriction 
   * that a Word can not contain a '\"', that would confuse the parser at
   * the other end.
   */
  private boolean isAWord( String s)
  {
      String illegalFirstChar = new String("#0123456789:-?");
     
      if ( illegalFirstChar.indexOf(s.charAt(0)) >= 0 )
          return false;
      
      for( int i=0; i< s.length(); i++)
          if( s.charAt(i) == '"' || s.charAt(i) == '(' || 
              s.charAt(i) == ')' || s.charAt(i) <= 0x20 )
            return false;
      return true;
  }

  /**
    If this is a token of the language, then it must be quoted. 
    **/
  private boolean isAToken(String str) {
    return str.equalsIgnoreCase(SL0Codec.NAME_OF_ACTION_FRAME);  
  }

  /** 
   * Take a java String and quote it to form a legal FIPA SL0 string.
   * Add quotation marks to the beginning/end and escape any 
   * quotation marks inside the string.
   * This must be the exact inverse of the procedure in
   * the parser (SL0Parser.jj) when it encounters a quoted string.
   */
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
  }

   private boolean isFrame(Object f) {
     return (f.getClass()==Frame.class);
   }
}
