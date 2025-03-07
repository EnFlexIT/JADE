package jade.content.lang.xml;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.*;
import jade.content.*;
import jade.content.onto.*;
import jade.content.onto.basic.*;
import jade.content.abs.*;
import jade.util.leap.List;
import jade.util.leap.ArrayList;

import java.util.Vector;
import java.util.Date;

public class TestAgent extends Agent {

	private static final long serialVersionUID = -3099456336278223408L;

	protected void setup() {
		try {
			XMLCodec codec = new XMLCodec();
			Ontology onto = FIPAManagementOntology.getInstance();
			
			// A Concept
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			dfd.addProtocols("P1");
			dfd.addProtocols("P2");
			ServiceDescription sd = new ServiceDescription();
			sd.setName("service-name");
			sd.setType("service-type");
			sd.addLanguages("L1");
			sd.addProtocols("P3");
			sd.addProtocols("P4");
			sd.addProtocols("P5");
			Property p = new Property("propName", "propVal");
			sd.addProperties(p);
			p = new Property("intPropName", new Integer(25));
			sd.addProperties(p);
			p = new Property("boolPropName", new Boolean(true));
			sd.addProperties(p);
			p = new Property("datePropName", new Date());
			sd.addProperties(p);
			p = new Property("binPropName", new byte[]{1,2,3,4});
			sd.addProperties(p);
			p = new Property("serPropName", new Vector<Object>());
			sd.addProperties(p);
			dfd.addServices(sd);
			handle(dfd, codec, onto);
			
			// A Predicate
			Register reg = new Register();
			reg.setDescription(dfd);
			Action actExpr = new Action(getDefaultDF(), reg);
			Result r = new Result(actExpr, "Foo <= Bar");
			handle(r, codec, onto);

			// A ContentElementList
			List items = new ArrayList();
			items.add(dfd);
			r.setItems(items);
			ContentElementList ll = new ContentElementList();
			ll.add(r);
			ll.add(new TrueProposition());
			handle(ll, codec, onto);			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handle(Object val, XMLCodec codec, Ontology onto) throws Exception {
		String s = codec.encodeObject(onto, val, true);
		System.out.println(s);
		System.out.println("#####################################");
		AbsObject abs = codec.decodeAbsObject(onto, s);
		System.out.println(abs);	
		System.out.println("=====================================");
	}
}
