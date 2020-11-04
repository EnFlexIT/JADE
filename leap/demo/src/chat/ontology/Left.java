package chat.ontology;

import java.util.List;

import jade.content.Predicate;
import jade.core.AID;

//#J2ME_EXCLUDE_FILE

/**
 * Left predicate used by chat ontology.
 * 
 * @author Michele Izzo - Telecomitalia
 */

@SuppressWarnings("serial")
public class Left implements Predicate {

	private List<AID> _who;

	public void setWho(List<AID> who) {
		_who = who;
	}

	public List<AID> getWho() {
		return _who;
	}

}