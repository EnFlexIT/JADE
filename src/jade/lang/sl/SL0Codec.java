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

import java.io.StringReader;

import jade.lang.Codec;

import jade.onto.Frame;
import jade.onto.Ontology;

/**

  The codec class for the <b><i>SL0</i></b> language. This class
  implements the <code>Codec</code> interface and allows converting
  back and forth between strings and frames, according to the SL0
  grammar.

  @author Giovanni Rimassa - Universita` di Parma
  @version $Date$ $Revision$

 */
public class SL0Codec implements Codec {

  /**
   A symbolic constant, containing the name of this language.
   */
  public static final String NAME = "SL0";

  private SL0Parser parser = new SL0Parser(new StringReader(""));
  private SL0Encoder encoder = new SL0Encoder();

  public String encode(Frame f, Ontology o) {
    return encoder.encode(f, o);
  }

  public Frame decode(String s, Ontology o) throws Codec.CodecException {
    try {
     return parser.parse(s, o);
    }
    catch(ParseException pe) {
      throw new Codec.CodecException("Parse exception", pe);
    }
    catch(TokenMgrError tme) {
      throw new Codec.CodecException("Token Manager error", tme);
    }
  }

}
