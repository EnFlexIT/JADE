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


import jade.content.*;
import jade.content.onto.*;
import jade.content.abs.*;
import jade.content.schema.*;
import jade.content.acl.*;
import jade.content.lang.*;
import jade.content.lang.j.*;

import jade.util.leap.List;
import jade.util.leap.Iterator;

import jade.core.AID;

/**
@author Federico Bergenti - Universita` di Parma
*/

public class PeopleOntology extends FullOntology {
	//A symbolic constant, containing the name of this ontology.
	public static final String ONTOLOGY_NAME = "PEOPLE_ONTOLOGY";

	// Concepts
	public static final String PERSON  = "PERSON";
	public static final String MAN     = "MAN";
	public static final String WOMAN   = "WOMAN";
	public static final String ADDRESS = "ADDRESS";

	// Slots
	public static final String NAME   = "NAME";
	public static final String STREET = "STREET";
	public static final String NUMBER = "NUMBER";
	public static final String CITY   = "CITY";
  
	// Predicates
	public static final String FATHER_OF = "FATHER_OF";
	public static final String MOTHER_OF = "MOTHER_OF";

	// Roles in predicates
	public static final String FATHER   = "FATHER";
	public static final String MOTHER   = "MOTHER";
	public static final String CHILDREN = "CHILDREN";

	// Actions
	public static final String MARRY = "MARRY";

	// Arguments in actions
	public static final String HUSBAND = "HUSBAND";
	public static final String WIFE    = "WIFE";

	private static PeopleOntology theInstance = new PeopleOntology(ACLOntology.getInstance());
	
	public static PeopleOntology getInstance() {
		return theInstance;
	}
	
	public PeopleOntology(Ontology base) {
		super(ONTOLOGY_NAME, base);

		try {
			PrimitiveSchema stringSchema  = (PrimitiveSchema)getSchema(BasicOntology.STRING);
			PrimitiveSchema integerSchema = (PrimitiveSchema)getSchema(BasicOntology.INTEGER);

			ConceptSchema addressSchema = new ConceptSchema(ADDRESS);
			addressSchema.add(STREET, stringSchema,  ObjectSchema.OPTIONAL);
			addressSchema.add(NUMBER, integerSchema, ObjectSchema.OPTIONAL);
			addressSchema.add(CITY,   stringSchema);

			ConceptSchema personSchema = new ConceptSchema(PERSON);
			personSchema.add(NAME,    stringSchema);
			personSchema.add(ADDRESS, addressSchema, ObjectSchema.OPTIONAL);

			ConceptSchema manSchema = new ConceptSchema(MAN);
			manSchema.addSuperSchema(personSchema);

			ConceptSchema womanSchema = new ConceptSchema(WOMAN);
			womanSchema.addSuperSchema(personSchema);

			add(personSchema, Person.class);
			add(manSchema, Man.class);
			add(womanSchema, Woman.class);
			add(addressSchema, Address.class);

			AggregateSchema childrenSchema = new AggregateSchema(BasicOntology.SET);

			PredicateSchema fatherOfSchema = new PredicateSchema(FATHER_OF);
			fatherOfSchema.add(FATHER,   manSchema);
			fatherOfSchema.add(CHILDREN, personSchema);

			PredicateSchema motherOfSchema = new PredicateSchema(MOTHER_OF);
			motherOfSchema.add(CHILDREN, personSchema);

			add(fatherOfSchema, FatherOf.class);
			add(motherOfSchema, MotherOf.class);

			AgentActionSchema marrySchema = new AgentActionSchema(MARRY);
			marrySchema.add(HUSBAND, manSchema);
			marrySchema.add(WIFE,    womanSchema);

			add(marrySchema);
		} catch(OntologyException oe) { oe.printStackTrace(); }
	}

	public static void main(String[] args) {
		try {
			PeopleOntology po = new PeopleOntology(ACLOntology.getInstance());

			AbsConcept absAddress = new AbsConcept(ADDRESS);
			absAddress.set(CITY, "London");

			AbsConcept absJohn = new AbsConcept(MAN);
			absJohn.set(NAME,    "John");
			absJohn.set(ADDRESS, absAddress);

			System.out.println("1 - abstract descriptor created:");
			absJohn.dump();

			Man john = (Man)po.toObject(absJohn);

			System.out.println("2 - Java object created.");

			absJohn = (AbsConcept)po.fromObject(john);

			System.out.println("3 - abstract descriptor re-created (take a look at NUMBER...):");
			absJohn.dump();

			Man ronnie = new Man();
			ronnie.setName("Ronnie");

			System.out.println("4 - Java object created.");

			AbsConcept absRonnie = (AbsConcept)po.fromObject(ronnie);

			System.out.println("5 - abstract descriptor created:");
			absRonnie.dump();

			AbsAggregate absChildren = new AbsAggregate(BasicOntology.SET);
			absChildren.add(absRonnie);

			System.out.println("6 - abstract descriptor created:");
			absChildren.dump();

			AbsPredicate absFatherOf = new AbsPredicate(FATHER_OF);
			absFatherOf.set(FATHER,   absJohn);
			absFatherOf.set(CHILDREN, absChildren);

			System.out.println("7 - abstract descriptor created:");
			absFatherOf.dump();

			AbsAID absSender   = new AbsAID("senderName", null, null);
			AbsAID absReceiver = new AbsAID("receiverName", null, null);

			AbsAggregate absReceivers = new AbsAggregate(BasicOntology.SET);
			absReceivers.add(absReceiver);

			AID  sender    = (AID)po.toObject(absSender);
			List receivers = (List)po.toObject(absReceivers);

			FatherOf fatherOf = (FatherOf)po.toObject(absFatherOf);

			Inform inform = new Inform();
			inform.setSender(sender);
			for (Iterator i=receivers.iterator(); i.hasNext(); )
			    inform.addReceiver((AID)i.next());
			inform.setProposition(fatherOf);

			Request request = new Request();
			request.setSender(sender);
			for (Iterator i=receivers.iterator(); i.hasNext(); )
			    request.addReceiver((AID)i.next());
			request.setAction(inform);
			
			System.out.println("8 - Java object created.");

			AbsCommunicativeAct absRequest = (AbsCommunicativeAct)po.fromObject(request);

			System.out.println("9 - abstract descriptor created:");
			absRequest.dump();

			Codec codec = new JCodec();

			byte[] data = codec.encode(po, absRequest);

			System.out.println("10- JCodec encoded");

			absRequest = (AbsCommunicativeAct)codec.decode(po, data);

			System.out.println("11- JCodec decoded: ");
			absRequest.dump();

			AbsVariable absX = new AbsVariable("X", BasicOntology.STRING);
			absRonnie.set(NAME, absX);

			System.out.println("12- abstract descriptor changed:");
			absFatherOf.dump();

			AbsIRE absIRE = new AbsIRE();
			absIRE.setVariable(absX);
			absIRE.setKind(ACLOntology.ANY);
			absIRE.setProposition(absFatherOf);

			AbsCommunicativeAct absQueryRef = new AbsCommunicativeAct(ACLOntology.QUERY_REF);
			absQueryRef.setSender(absSender);
			absQueryRef.setReceivers(absReceivers);
			absQueryRef.set(ACLOntology.IRE, absIRE);

			System.out.println("13- abstract descriptor created:");
			absQueryRef.dump();			
		} catch(OntologyException oe) {
			oe.printStackTrace();
		} catch(Codec.CodecException ce) {
			ce.printStackTrace();
		}
	}
}
