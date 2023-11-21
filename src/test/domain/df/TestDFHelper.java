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

package test.domain.df;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.FIPAAgentManagement.*;

/**
   @author Giovanni Caire - TILAB
 */
public class TestDFHelper {
	private static final String TEST_LANGUAGE11 = "Test language 1.1"; // Test a string including  spaces
	private static final String TEST_LANGUAGE12 = "\"Test language 1.2\""; // Test a quoted string
	
	private static final String P_NAME_1 = "pname1";
	private static final String P_VALUE_1 = "pvalue)1"; // Test a string including a ')'
	private static final String P_NAME_2 = "pname2";
	private static final String P_VALUE_2 = "2pvalue"; // Test a string starting with a digit
	private static final String P_NAME_3 = "pname3";
	private static final String P_VALUE_3 = "p\"value\\\"3"; // Test a string including a " and an escaped "
	
	public static DFAgentDescription getSampleDFD(AID id) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(id);
		dfd.addOntologies("Test-ontology");
		dfd.addLanguages("Test-language");
		dfd.addProtocols("Test-protocol");
		
		// Services
		ServiceDescription sd1 = getSampleSD1();
		ServiceDescription sd2 = getSampleSD2();
		dfd.addServices(sd1);
		dfd.addServices(sd2);
		
		return dfd;
	}
	
	public static ServiceDescription getSampleSD1() {
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Test-service-1");
		sd.setType("Test-type-1");
		sd.addOntologies("Test-ontology-1.1");
		sd.addOntologies("Test-ontology-1.2");
		sd.addLanguages(TEST_LANGUAGE11);
		sd.addLanguages(TEST_LANGUAGE12);
		sd.addLanguages("Test-language-1.3");
		sd.addProtocols("Test-protocol-1");
		
		return sd;
	}
	
	public static ServiceDescription getSampleSD2() {
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Test-service-2");
		sd.setType("Test-type-2");
		sd.addOntologies("Test-ontology-2");
		sd.addLanguages("Test-language-2");
		sd.addProtocols("Test-protocol-2.1");
		sd.addProtocols("Test-protocol-2.2");
		Property p = new Property();
		p.setName(P_NAME_1);
		p.setValue(P_VALUE_1);
		sd.addProperties(p);
		p = new Property();
		p.setName(P_NAME_2);
		p.setValue(P_VALUE_2);
		sd.addProperties(p);
		
		return sd;
	}
	
	/**
	   Return a template that matches a DFD including SD1
	 */
	public static DFAgentDescription getSampleTemplate1() {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Test-type-1");
		sd.addLanguages(TEST_LANGUAGE12);
		sd.addLanguages("Test-language-1.3");
		dfd.addServices(sd);
		return dfd;
	}
	
	/**
	   Return a template that matches a DFD including SD2
	 */
	public static DFAgentDescription getSampleTemplate2() {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Test-type-2");
		Property p = new Property();
		p.setName(P_NAME_2);
		p.setValue(P_VALUE_2);
		sd.addProperties(p);
		dfd.addServices(sd);
		return dfd;
	}
	
	/**
	   Return a template that should not match a DFD including SD1
	 */
	public static DFAgentDescription getSampleTemplate3() {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Test-type-1");
		sd.addLanguages(TEST_LANGUAGE12);
		sd.addLanguages("Test-language-1.3");
		sd.addLanguages("Test-language-1.4");
		dfd.addServices(sd);
		return dfd;
	}

	/**
	   Return a template that should not match a DFD including SD2
	 */
	public static DFAgentDescription getSampleTemplate4() {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Test-type-2");
		Property p = new Property();
		p.setName(P_NAME_3);
		p.setValue(P_VALUE_2);
		sd.addProperties(p);
		p = new Property();
		p.setName(P_NAME_2);
		p.setValue(P_VALUE_3);
		sd.addProperties(p);
		dfd.addServices(sd);
		return dfd;
	}
	
	public static boolean compare(DFAgentDescription dfd1, DFAgentDescription dfd2) {
		return true;
	}
}
