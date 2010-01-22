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

package test.bob.tests;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.schema.AgentActionSchema;
import jade.content.schema.Facet;
import jade.content.schema.ObjectSchema;
import jade.content.schema.facets.CardinalityFacet;
import jade.content.schema.facets.TypedAggregateFacet;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import test.bob.BeanOntologyBuilderTesterAgent;
import test.common.Test;
import test.common.TestException;

public abstract class AbstractCheckSendAndReceiveTest extends Test {
	private static final long serialVersionUID = 1L;

	static final String CHECKER_AGENT = "checker";

	protected BeanOntologyBuilderTesterAgent testerAgent;

	protected boolean compareFieldSets(String[] expectedSlotNames, Set<String> effectiveSlotNames) {
		boolean result = true;

		for (String expectedSlotName: expectedSlotNames) {
			if (!effectiveSlotNames.remove(expectedSlotName)) {
				log("missing field "+expectedSlotName);
				result = false;
			}
		}
		if (effectiveSlotNames.size() > 0) {
			result = false;
			for (String name: effectiveSlotNames) {
				log("unexpected field "+name);
			}
		}

		return result;
	}

	Codec getCodec() {
		return testerAgent.getCodec();  
	}

	protected abstract Ontology getOntology();
	protected abstract Concept getConcept();
	protected abstract boolean isConceptCorrectlyFilled(Concept c);
	protected abstract boolean isSchemaCorrect(ObjectSchema os);

	Set<String> getSlotNameSet(ObjectSchema os) {
		String[] names = os.getNames();
		Set<String> slotNames = new HashSet<String>();
		for (String name: names) {
			slotNames.add(name);
		}
		
		return slotNames;
	}

	void logStackTrace(Throwable t) {
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    t.printStackTrace(pw);
		log(sw.toString());
	}

	protected boolean verifySlotFacets(ObjectSchema os, String logPrefix, String slotName, int expectedCardMin, int expectedCardMax, String expectedAggregateTypeName) {
		Facet[] facets = os.getFacets(slotName);
		return verifyFacets(facets, "slot "+slotName, expectedCardMin, expectedCardMax, expectedAggregateTypeName);
	}

	protected boolean verifyResultFacets(AgentActionSchema aas, int expectedCardMin, int expectedCardMax, String expectedAggregateTypeName) {
		Facet[] facets = aas.getResultFacets();
		return verifyFacets(facets, "result", expectedCardMin, expectedCardMax, expectedAggregateTypeName);
	}

	private boolean verifyFacets(Facet[] facets, String logPrefix, int expectedCardMin, int expectedCardMax, String expectedAggregateTypeName) {
		boolean result = true;

		int cardMin = -1, cardMax = -1;
		String typeName = null;

		if (facets == null) {
			log(logPrefix+": no facets found");
			return false;
		}

		for (Facet facet: facets) {
			if (facet instanceof CardinalityFacet) {
				cardMin = ((CardinalityFacet)facet).getCardMin();
				cardMax = ((CardinalityFacet)facet).getCardMax();
			} else if (facet instanceof TypedAggregateFacet) {
				typeName = ((TypedAggregateFacet)facet).getType().getTypeName();
			}
		}
		if (cardMin != expectedCardMin) {
			result = false;
			if (cardMin >= 0) {
				log(logPrefix+": wrong minimum cardinality "+cardMin+" - expected was "+expectedCardMin);
			} else {
				log(logPrefix+": minimum cardinality not specified - expected was "+expectedCardMin);
			}
		}
		if (cardMax != expectedCardMax) {
			result = false;
			if (cardMax >= 0) {
				log(logPrefix+": wrong maximum cardinality "+cardMax+" - expected was "+expectedCardMax);
			} else {
				log(logPrefix+": maximum cardinality not specified - expected was "+expectedCardMax);
			}
		}
		if (expectedAggregateTypeName == null) {
			result &= typeName == null;
		} else {
			result &= expectedAggregateTypeName.equals(typeName);
		}
		return result;
	}

	@Override
	public Behaviour load(Agent a) throws TestException {
		try {
			testerAgent = (BeanOntologyBuilderTesterAgent)a;

			SequentialBehaviour srac = new SequentialBehaviour() {
				private static final long serialVersionUID = 1L;

				private boolean failed;

				@Override
				public void onStart() {
					super.onStart();

					addSubBehaviour(new OneShotBehaviour() {
						private static final long serialVersionUID = 1L;

						@Override
						public void action() {
							failed = true;
							Ontology ontology = getOntology();
							Concept concept = getConcept();
							try {
								log("=====>>>>> getting schema");
								ObjectSchema schema = ontology.getSchema(concept.getClass());
								failed = !isSchemaCorrect(schema);
								if (failed) {
									log("=====>>>>> schema is wrong");
									skipNext();
								} else {
									log("=====>>>>> schema is correct");
								}
							} catch (Exception e) {
								log("error sending message");
								//e.printStackTrace();
								skipNext();
							}
						}
					});

					addSubBehaviour(new OneShotBehaviour() {
						private static final long serialVersionUID = 1L;

						@Override
						public void action() {
							failed = true;
							ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
							msg.setLanguage(getCodec().getName());
							msg.setOntology(getOntology().getName());
							msg.addReceiver(new AID(testerAgent.getLocalName(), AID.ISLOCALNAME));
							Action action = new Action(myAgent.getAID(), getConcept());
							try {
								log("=====>>>>> sending message");
								myAgent.getContentManager().fillContent(msg, action);
								myAgent.send(msg);
								failed = false;
								log("=====>>>>> message sent");
							} catch (Exception e) {
								log("error sending message");
								logStackTrace(e);
								skipNext();
							}
						}
					});

					addSubBehaviour(new SimpleBehaviour() {
						private static final long serialVersionUID = 1L;
						private boolean done = false;

						@Override
						public void action() {
							ACLMessage msg = myAgent.receive();
							if (msg == null) {
								log("=====>>>>> message not received, blocking");
								block();
//								log("=====>>>>> message not received, blocked");
							} else {
								log("=====>>>>> message received");
								done = true;
								try {
//									System.out.println("}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}");
//									System.out.println(msg);
//									System.out.println("}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}");
									ContentElement extractedConcept = myAgent.getContentManager().extractContent(msg);
									Action action = (Action)extractedConcept;
									failed = !isConceptCorrectlyFilled(action.getAction());
									if (failed) {
										log("=====>>>>> message uncorrectly filled");
									} else {
										log("=====>>>>> message correctly filled");
									}
								} catch (Exception e) {
									log("error receiving message");
									logStackTrace(e);
									failed = true;
								}
							}
						}

						@Override
						public boolean done() {
							return done;
						}
					});
				}

				@Override
				public int onEnd() {
					if (failed) {
						failed("Test failed");
					} else {
						passed("Test passed");
					}
					return super.onEnd();
				}
			};

			return srac;
		} catch (Exception e) {
			throw new TestException("Error during test initialization", e);
		}
	}
}
