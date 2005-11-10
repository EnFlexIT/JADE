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

package jade.core.faultRecovery;

//#J2ME_EXCLUDE_FILE

import jade.core.Profile;
import java.util.Map;

/**
   An implementation of this interface is used by the FaultRecoveryService 
   to save the information required to recover a platform after a fault
   of the Main Container.
   
   @author Giovanni Caire - TILAB
 */
public interface PersistentStorage {
	void init(Profile p) throws Exception;
	void close();
	void clear() throws Exception;
	
	void storeLocalAddress(String address) throws Exception;
	String getLocalAddress() throws Exception;
	
	void storeNode(String name, boolean isChild, byte[] nn) throws Exception;
	void removeNode(String name) throws Exception;
	Map getAllNodes(boolean children) throws Exception;

	void storeTool(String name, byte[] tt) throws Exception;
	void removeTool(String name) throws Exception;
	Map getAllTools() throws Exception;
}