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
package jade.domain.FIPAAgentManagement;

import jade.util.leap.Iterator;
import jade.util.leap.List;

public class MultiValueProperty extends Property {

	public MultiValueProperty() {
		super();
	}
	
	public MultiValueProperty(String name, List value) {
		super(name, value);
	}

	public List getValues() {
		return (List) getValue();
	}
	
	public void setValues(List values) {
		setValue(values);
	}
	
	//#MIDP_EXCLUDE_BEGIN
	public boolean match(Property p) {
		boolean result = false;
		if (getName().equals(p.getName())) {
			// The property name matches. Check the value
			if (p.getValue() == null) {
				result = true;
			}
			else if (p.getValue() instanceof String) {
				// Loop into the list
				if (getValue() != null) {
					Iterator it = ((List) getValue()).iterator();
					while (it.hasNext()) {
						Object listValue = it.next();
						if (((String) p.getValue()).equalsIgnoreCase(listValue.toString())) {
							result = true;
							break;
						}
					}
				}
			}
			else {
				result = p.getValue().equals(getValue());
			}
		}
		return result;
	}
	//#MIDP_EXCLUDE_END
}
