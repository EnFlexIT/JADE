package jade.content.onto;

//#MIDP_EXCLUDE_FILE

import jade.content.abs.AbsAggregate;
import jade.content.abs.AbsHelper;
import jade.content.abs.AbsObject;
import jade.content.abs.AbsTerm;
import jade.content.lang.sl.SL0Vocabulary;
import jade.content.schema.ObjectSchema;

import java.util.*;

public class CFReflectiveIntrospector extends ReflectiveIntrospector {

	protected boolean isAggregateObject(Object slotValue) {
		return (slotValue instanceof List) || (slotValue instanceof Set);
	}
	
	protected void externaliseAndSetAggregateSlot(AbsObject abs, ObjectSchema schema, String slotName, Object slotValue, ObjectSchema slotSchema, Ontology referenceOnto) throws OntologyException {
		Collection c = (Collection) slotValue;
		if (!c.isEmpty() || schema.isMandatory(slotName)) {
			// Note that we ignore the aggregateType specified in the slot schema and we use SET for java.util.Set and SEQUENCE for java.util.List
			String aggregateType = null;
			if (slotValue instanceof List) {
				aggregateType = SL0Vocabulary.SEQUENCE;
			}
			else if (slotValue instanceof Set) {
				aggregateType = SL0Vocabulary.SET;
			}
			else {
				// This should never happen
				throw new OntologyException("Wrong class "+c.getClass().getName()+" for aggregate slot "+slotName+" of object "+abs.getTypeName());
			}
			AbsObject absSlotValue = externaliseCollection(c, referenceOnto, aggregateType); 
			AbsHelper.setAttribute(abs, slotName, absSlotValue);
		}
	}

	private AbsObject externaliseCollection(Collection c, Ontology referenceOnto, String aggregateType) throws OntologyException {
		AbsAggregate ret = new AbsAggregate(aggregateType);

		try {
			Iterator it = c.iterator();
			while (it.hasNext()) {
				ret.add((AbsTerm) (referenceOnto.fromObject(it.next())));
			}
		}
		catch (ClassCastException cce) {
			throw new OntologyException("Non term object in aggregate");
		}

		return ret;
	}

	protected Object internaliseAggregateSlot(AbsAggregate absAggregate, Ontology referenceOnto) throws OntologyException {
		Collection c = internaliseCollection(absAggregate, referenceOnto);
		// FIXME: Here we should check for Long --> Integer casting, but how?
		return c;
	}

	private Collection internaliseCollection(AbsAggregate absAggregate, Ontology referenceOnto) throws OntologyException {
		Collection ret = null;
		if (absAggregate.getTypeName().equals(SL0Vocabulary.SET)) {
			ret = new HashSet(absAggregate.size());
		}
		else {
			ret = new ArrayList(absAggregate.size());
		}

		for (int i = 0; i < absAggregate.size(); i++) {
			Object element = referenceOnto.toObject(absAggregate.get(i));
			// Check if the element is a Term, a primitive an AID or a List
			Ontology.checkIsTerm(element);
			ret.add(element);
		}

		return ret;
	}
}
