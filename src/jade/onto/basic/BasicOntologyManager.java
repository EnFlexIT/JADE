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


package jade.onto.basic;

import jade.core.AID;

import jade.onto.Frame;
import jade.onto.Ontology;
import jade.onto.DefaultOntology;
import jade.onto.SlotDescriptor;
import jade.onto.RoleEntityFactory;
import jade.onto.OntologyException;

/**
   Javadoc documentation for the file
   @author Giovanni Caire - CSELT S.p.A.
   @version $Date$ $Revision$
*/

public class BasicOntologyManager {

  /**
    A symbolic constant, containing the name of this ontology.
   */
  public static final String NAME = "basic-ontology";

  private static Ontology theInstance = new DefaultOntology();

  static {
    initInstance();
  }

  /**
     This method grants access to the unique instance of the
     basic ontology.
     @return An <code>Ontology</code> object, containing the concepts
     of the basic ontology.
  */
  public static Ontology instance() {
    return theInstance;
  }

  private BasicOntologyManager() {
  }

  private static void initInstance() {
    try {
			// Adds ACTION role
    	theInstance.addRole(
				BasicOntologyVocabulary.ACTION, 
				new SlotDescriptor[] {
	  			new SlotDescriptor(Ontology.FRAME_SLOT, BasicOntologyVocabulary.AGENTIDENTIFIER, Ontology.M),
	  			new SlotDescriptor(Ontology.FRAME_SLOT, Ontology.ANY_TYPE, Ontology.M)
				}, 
				new RoleEntityFactory() {
	     		public Object create(Frame f) { return new Action(); }
	     		public Class getClassForRole() { return Action.class; }
				}
			);

			// Adds AGENTIDENTIFIER role
			theInstance.addRole(
				BasicOntologyVocabulary.AGENTIDENTIFIER, 
				new SlotDescriptor[] {
	  			new SlotDescriptor("name", Ontology.PRIMITIVE_SLOT, Ontology.STRING_TYPE, Ontology.M),
	  			new SlotDescriptor("addresses", Ontology.SEQUENCE_SLOT, Ontology.STRING_TYPE, Ontology.O),
	  			new SlotDescriptor("resolvers", Ontology.SEQUENCE_SLOT, BasicOntologyVocabulary.AGENTIDENTIFIER, Ontology.O)
				}, 
				new RoleEntityFactory() {
	     		public Object create(Frame f) { return new AID(); }
	     		public Class getClassForRole() { return AID.class; }
				}
			);
	
			// Adds TRUE role
			theInstance.addRole(
				BasicOntologyVocabulary.TRUE, 
				new SlotDescriptor[]{
				}, 
				new RoleEntityFactory() {
					public Object create(Frame f) { return new TrueProposition(); } 
					public Class getClassForRole() { return TrueProposition.class; }
				}
			);

			// Adds FALSE role
			theInstance.addRole(
				BasicOntologyVocabulary.FALSE, 
				new SlotDescriptor[]{
				}, 
				new RoleEntityFactory() {
					public Object create(Frame f) { return new FalseProposition(); } 
					public Class getClassForRole() { return FalseProposition.class; }
				}
			);

			// Adds DONE role
			theInstance.addRole(
				BasicOntologyVocabulary.DONE, 
				new SlotDescriptor[] {
					new SlotDescriptor(Ontology.FRAME_SLOT, BasicOntologyVocabulary.ACTION, Ontology.M)
				}, 
				new RoleEntityFactory() {
					public Object create(Frame f) {return new DonePredicate(); }
					public Class getClassForRole() {return DonePredicate.class;}
				}
			);

			// Adds RESULT role
			theInstance.addRole(
				BasicOntologyVocabulary.RESULT, 
				new SlotDescriptor[] {
					new SlotDescriptor(Ontology.FRAME_SLOT, BasicOntologyVocabulary.ACTION, Ontology.M),
					new SlotDescriptor(Ontology.ANY_SLOT, Ontology.ANY_TYPE, Ontology.M)
				}, 
				new RoleEntityFactory() {
					public Object create(Frame f) {return new ResultPredicate(); }
					public Class getClassForRole() {return ResultPredicate.class;}
				}
			);
	   	   
			// DEBUG: PRINT VOCABULARY
	  	//List voc = theInstance.getVocabulary();
	  	//Iterator i = voc.iterator();
	  	//while (i.hasNext())
	  	//	System.out.println((String) (i.next()));
	  	
    }	// End of try
    catch(OntologyException oe) {
      oe.printStackTrace();
    }
  } //end of initInstance

}
