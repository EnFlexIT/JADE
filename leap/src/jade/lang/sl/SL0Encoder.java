
/**
 * ***************************************************************
 * The LEAP libraries, when combined with certain JADE platform components,
 * provide a run-time environment for enabling FIPA agents to execute on
 * lightweight devices running Java. LEAP and JADE teams have jointly
 * designed the API for ease of integration and hence to take advantage
 * of these dual developments and extensions so that users only see
 * one development platform and a
 * single homogeneous set of APIs. Enabling deployment to a wide range of
 * devices whilst still having access to the full development
 * environment and functionalities that JADE provides.
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


package jade.lang.sl;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.IOException;

import jade.util.leap.*;

import jade.onto.Frame;
import jade.onto.OntologyException;
//import jade.lang.Codec;
import jade.core.CaseInsensitiveString;

/**
  @author Steffen Rusitschka, Siemens AG, CT IC 6
 */
class SL0Encoder {

  public String encode(Frame f) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    OutputStreamWriter out = new OutputStreamWriter(baos);
    try {
      if (f.size()<=0) //if is a PropositionSymbol,then just write the symbol
	out.write(f.getName());
      else // else write the frame
	writeFrame(f, out);
      out.flush();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
    return new String(baos.toByteArray());
  }


  private void writeFrame(Frame f, Writer w) throws Exception {
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
                        else if (slotValue instanceof java.util.Date)
	  		    // if it is a Date then write a DateTimetoken
	  		    // I wanted to use an SLDate that extends Date but, if I did
	  		    // then the ontology would no more be language-independent!
	  		    w.write(jade.lang.acl.ISO8601.toString((java.util.Date)slotValue));
			else if (slotValue instanceof String) {
                            String stringifiedValue = slotValue.toString();
                            if( isAWord(stringifiedValue) && ! isAToken(stringifiedValue) )
                                w.write(stringifiedValue);
                            else
                                w.write(quotedString(stringifiedValue));
			}
                        else if (slotValue instanceof byte[])
	  		    throw new Exception("SL0 does not support binary fields");
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
      // This should permit strings of length 0 to be encoded.
      if( s==null || s.length()==0 )
          return false; // words must have at least one character

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
    return CaseInsensitiveString.equalsIgnoreCase(str, SL0Codec.NAME_OF_ACTION_FRAME);
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
     return f instanceof Frame;
   }
}
