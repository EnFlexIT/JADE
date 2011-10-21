package chat.ontology;

import jade.content.Predicate;
import jade.core.AID;

//#MIDP_EXCLUDE_FILE

/**
 * Left predicate used by chat ontology.
 * 
 * @author Michele Izzo - Telecomitalia
 */

@SuppressWarnings("serial")
public class Left implements Predicate {

	private AID _who;

	public void setWho(AID who) {
		_who = who;
	}

	public AID getWho() {
		return _who;
	}

}