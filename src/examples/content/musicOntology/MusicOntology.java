/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package examples.content.musicOntology;

import jade.content.onto.*;
import jade.content.schema.*;
import examples.content.ecommerceOntology.*;

/**
 * Ontology containing music related concepts.
 * @author Giovanni Caire - TILAB
 */
public class MusicOntology extends Ontology {
	// NAME
  public static final String ONTOLOGY_NAME = "Music-ontology";
	
	// VOCABULARY
  public static final String CD = "CD";
  public static final String CD_TITLE = "title";
  public static final String CD_TRACKS = "tracks";

  public static final String TRACK = "TRACK";
  public static final String TRACK_NAME = "name";
  public static final String TRACK_DURATION = "duration";
  
  // The singleton instance of this ontology
	private static Ontology theInstance = new MusicOntology(ECommerceOntology.getInstance());
	
	public static Ontology getInstance() {
		return theInstance;
	}
	
  /**
   * Constructor
   */
  private MusicOntology(Ontology base) {
  	super(ONTOLOGY_NAME, base, new ReflectiveIntrospector());

    try {
    	ConceptSchema trackSchema = new ConceptSchema(TRACK);
    	trackSchema.add(TRACK_NAME, (TermSchema) getSchema(BasicOntology.STRING));
    	trackSchema.add(TRACK_DURATION, (TermSchema) getSchema(BasicOntology.INTEGER), ObjectSchema.OPTIONAL);
    	add(trackSchema, Track.class);
    	
    	ConceptSchema cdSchema = new ConceptSchema(CD);
    	cdSchema.addSuperSchema((ConceptSchema) getSchema(ECommerceOntology.ITEM));
    	cdSchema.add(CD_TITLE, (TermSchema) getSchema(BasicOntology.STRING));
    	cdSchema.add(CD_TRACKS, (TermSchema) getSchema(BasicOntology.SEQUENCE));
    	cdSchema.addFacet(CD_TRACKS, new TypedAggregateFacet(trackSchema)); 
    	add(cdSchema, CD.class);
    } 
    catch (OntologyException oe) {
    	oe.printStackTrace();
    } 
	}

}
