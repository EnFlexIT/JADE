/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.lang.sl;

import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import jade.onto.Frame;
import jade.util.leap.List;

/**
 * The codec class for the <b><i>SL0</i></b> language. This class
 * implements the <code>Codec</code> interface and allows converting
 * back and forth between strings and frames, according to the SL0
 * grammar.
 * <p>
 * <b>WARNING:</b> When creating the Ontology, notice that this SL0Parser
 * returns a Java object of type <code>Long</code> everytime it parses an
 * integer, and
 * it returns a Java object of type <code>Double</code> everytime it parses a
 * float.
 * Therefore, the slots of the frames in the ontology must be of type,
 * respectively, <code>Ontology.LONG_TYPE</code> and
 * <code>Ontology.DOUBLE_TYPE</code>, otherwise you get an
 * <code>IllegalArgumentException</code>.
 * <p>
 * Notice also that the following convention is needed for all the frames representing actions:
 * the name of the frame must be <code>NAME_OF_ACTION_FRAME</code>,
 * and the name of the two slots representing actor and the action term
 * must be, respectively, <code>NAME_OF_ACTOR_SLOT</code> and
 * <code>NAME_OF_ACTION_SLOT</code>.
 * @author Giovanni Rimassa - Universita` di Parma
 * @author Nicolas Lhuillier - Motorola
 * @author Steffen Rusitschka - Siemens AG
 * @version $Date$ $Revision$
 */
public class SL0Codec {

  /**
   * Symbolic constant identifying a slot representing an actor
   */
  public static String       NAME_OF_ACTION_FRAME = "action";

  /**
   * Symbolic constant identifying a slot representing an actor
   */
  public static String       NAME_OF_ACTOR_SLOT = Frame.UNNAMEDPREFIX+".ACTION.actor";

  /**
   * Symbolic constant identifying a slot representing an action
   */
  public static String       NAME_OF_ACTION_SLOT = Frame.UNNAMEDPREFIX+".ACTION.action";

  /**
   * A symbolic constant, containing the name of this language.
   */
  public static final String NAME = "FIPA-SL0";
  private SL0Parser          parser =
    new SL0Parser(new InputStreamReader(new ByteArrayInputStream(new byte[0])));
  private SL0Encoder         encoder = new SL0Encoder();

  /**
   * Method declaration
   *
   * @param v
   *
   * @return
   *
   * @see
   */
  public synchronized String encode(List v) {
    StringBuffer s = new StringBuffer("(");

    for (int i = 0; i < v.size(); i++) {
      s.append(encoder.encode((Frame) v.get(i))+" ");
    }

    return s.append(")").toString();
  }

  /**
   * Method declaration
   *
   * @param s
   *
   * @return
   *
   * @see
   */
  public synchronized List decode(String s) /* throws Codec.CodecException */ {
    try {
      return parser.parse(s);
    }
    catch (ParseException pe) {
      pe.printStackTrace();
    }
    catch (TokenMgrError tme) {
      tme.printStackTrace();
    }

    return null;
  }


// jade/leap j2se compatiblity methods
  public String encode(List v, Object ontoDummy) {
    return encode(v);
  }

  public List decode(String s, Object ontoDummy) {
    return decode(s);
  }

}
