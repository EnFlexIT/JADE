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
package jade.content.lang.leap;

import jade.content.*;
import jade.content.abs.*;
import jade.content.onto.*;

public class InstanceOf implements Predicate, Introspectable {
	private Concept entity;
	private String type;
	
	public InstanceOf() {
		entity = null;
		type = null;
	}
	
	public InstanceOf(Concept c, String t) {
		setEntity(c);
		setType(t);
	}
	
	public Concept getEntity() {
		return entity;
	}
	
	public void setEntity(Concept c) {
		entity = c;
	}	
	
	public String getType() {
		return type;
	}
	
	public void setType(String t) {
		type = t;
	}
	
  public void externalise(AbsObject abs, Ontology onto) throws OntologyException {
  	try {
  		AbsPredicate absInstanceOf = (AbsPredicate) abs;
  		absInstanceOf.set(LEAPCodec.INSTANCEOF_ENTITY, (AbsTerm) onto.fromObject(getEntity()));
  		absInstanceOf.set(LEAPCodec.INSTANCEOF_TYPE, (AbsTerm) onto.fromObject(getType()));
  	}
  	catch (ClassCastException cce) {
  		throw new OntologyException("Error externalising InstanceOf");
  	}
  }

  public void internalise(AbsObject abs, Ontology onto) throws UngroundedException, OntologyException {
    try {
  		AbsPredicate absInstanceOf = (AbsPredicate) abs;
  		setEntity((Concept) onto.toObject(absInstanceOf.getAbsTerm(LEAPCodec.INSTANCEOF_ENTITY))); 
  		setType((String) onto.toObject(absInstanceOf.getAbsTerm(LEAPCodec.INSTANCEOF_TYPE))); 
  	}
  	catch (ClassCastException cce) {
  		throw new OntologyException("Error internalising InstanceOf");
  	}
  }
}
