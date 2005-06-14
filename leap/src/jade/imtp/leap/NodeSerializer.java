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

package jade.imtp.leap;

//#MIDP_EXCLUDE_FILE

import jade.core.Node;

/**
   This class is used by the FaultRecoveryService to convert
   Node instances into/from sequences of bytes that can be saved in 
   the FaultRecoveryService persistent storage
   
   @author Giovanni Caire - TILAB
 */
public class NodeSerializer {
	private StubHelper stubHelper;
	
	public NodeSerializer() {
		stubHelper = CommandDispatcher.getDispatcher();
	}
		
	public byte[] serialize(Node n) throws Exception {
		DeliverableDataOutputStream ddos = new DeliverableDataOutputStream(stubHelper);
		ddos.serializeNode(n);
		return ddos.getSerializedByteArray();
	}
	
	public Node deserialize(byte[] bb) throws Exception {
		DeliverableDataInputStream ddis = new DeliverableDataInputStream(bb, stubHelper);
		return ddis.deserializeNode();
	}
}

