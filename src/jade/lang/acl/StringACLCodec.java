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

package jade.lang.acl;

import java.io.*;

/**
  This class implements the FIPA String codec for ACLMessages.
 @author Fabio Bellifemine - CSELT S.p.A.
 @version $Date$ $Revision$
 **/
public class StringACLCodec implements ACLCodec {

  public static final String NAME = "fipa.acl.rep.string.std"; 

  ACLParser parser;
  Writer out;

  /**
   * constructor for the codec.
   * The standard input is used as an input stream of ACL messages.
   * The standard output is used to write encoded ACL messages.
   */
  public StringACLCodec() {
    parser = new ACLParser(System.in);
    out = new OutputStreamWriter(System.out);
  }


  /**
   * constructor for the codec.
   * @parameter r is the input stream for the ACL Parser (pass 
   * <code>new InputStreamReader(System.in)</code> 
   * if you want to use the standard input)
   * @parameter w is the writer to write encoded ACL messages (pass 
   * <code>new OutputStreamWriter(System.out)</code> if you want to 
   * use the standard output)
   */
  public StringACLCodec(Reader r, Writer w) {
    parser = new ACLParser(r);
    out = w;
  }

  /**
   * decode and parses the next message from the Reader passed in the 
   * constructor
   * @return the ACLMessage
   * @throws ACLCodec.CodecException if any Exception occurs during the 
   * parsing/reading operation
   */
  public ACLMessage decode() throws ACLCodec.CodecException {
    try {
      return parser.Message();
    } catch (jade.lang.acl.TokenMgrError e1) {
      throw new ACLCodec.CodecException(getName()+" ACLMessage decoding token exception",e1);
    } catch (Exception e) {
      throw new ACLCodec.CodecException(getName()+" ACLMessage decoding exception",e);
    }
  }

  /**
   * encodes the message and writes it into the Writer passed in the 
   * constructor.
   * Notice that this method does not call <code>flush</code> on the writer.
   @ param msg is the ACLMessage to encode and write into
   */
  public void write(ACLMessage msg) {
      try {
	  out.write(msg.toString());
      } catch (IOException ioe) {
	  ioe.printStackTrace();
      }
  }

  /**
   * @see ACLCodec#encode(ACLMessage msg)
   */
  public byte[] encode(ACLMessage msg) {
    try {
      String s = msg.toString();
      return s.getBytes("US-ASCII");
    }
    catch(IOException ioe) {
      ioe.printStackTrace();
      return new byte[0];
    }
  }

  /**
   * @see ACLCodec#decode(byte[] data)
   */
  public ACLMessage decode(byte[] data) throws ACLCodec.CodecException {
    try {
      return ACLParser.create().parse(new InputStreamReader(new ByteArrayInputStream(data)));
    } catch (jade.lang.acl.TokenMgrError e1) {
      throw new ACLCodec.CodecException(getName()+" ACLMessage decoding token exception",e1);
    } catch (Exception e2) {
      throw new ACLCodec.CodecException(getName()+" ACLMessage decoding exception",e2);
    }
  }

  /**
   * @return the name of this encoding according to the FIPA specifications
   */
  public String getName() {
    return NAME;
  }
}
