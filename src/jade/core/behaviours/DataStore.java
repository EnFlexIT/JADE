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

package jade.core.behaviours;

import jade.util.leap.*;

/**
   @author Giovanni Caire - TILab S.p.A.
   @version $Date$ $Revision$
**/

public class DataStore extends HashMap {

    public DataStore() {
	super();
    }


    //#APIDOC_EXCLUDE_BEGIN

    //#MIDP_EXCLUDE_BEGIN


    // For persistence service
    private transient Long persistentID;

    // For persistence service
    private Long getPersistentID() {
	return persistentID;
    }

    // For persistence service
    private void setPersistentID(Long l) {
	persistentID = l;
    }



    // For persistence service: all mutator methods are extended to
    // keep the persistent data in sync


    public Object remove(Object o) {
	persistentData.remove(o);
	return super.remove(o);
    }

    public Object put(Object key, Object value) {
	persistentData.put(key, value);
	return super.put(key, value);
    }

    public void clear() {
	persistentData.clear();
	super.clear();
    }


    // FIXME: Implement keySet() and values() with wrappers...



    // For persistence service -- Hibernate needs java.util collections
    private java.util.Map persistentData = new java.util.HashMap();

    // For persistence service -- Hibernate needs java.util collections
    private java.util.Map getData() {
	return persistentData;
    }

    // For persistence service -- Hibernate needs java.util collections
    private void setData(java.util.Map data) {

	if(!persistentData.equals(data)) {
	    clear();

	    java.util.Iterator it = data.entrySet().iterator();
	    while(it.hasNext()) {
		java.util.Map.Entry e = (java.util.Map.Entry)it.next();
		put(e.getKey(), e.getValue());
	    }
	}

	persistentData = data;
    }



    //#MIDP_EXCLUDE_END

    //#APIDOC_EXCLUDE_END

}
