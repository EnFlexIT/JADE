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

package jade.domain.FIPAAgentManagement;
import jade.util.leap.*;
import jade.content.Concept;

/** 
* This class models a service data type.
  * <p>
  * <i>
  * FIPA2000 still uses singular names for some slots whose type
  * value is a set. In particular for "ontologies","languages","protocols".
  * Because of that, since JADE 2.4, both singular and plural names
  * can be used and are valid for those slots.
  * That might change as soon as FIPA takes a final decision on the
  * names of those slots.
  * </i>
* @author Fabio Bellifemine - CSELT S.p.A.
* @version $Date$ $Revision$
* 
*/
  public class ServiceDescription implements Concept {

    private String name;
    private String type;
    private String ownership;
    private List interactionProtocols = new ArrayList();
    private List ontology = new ArrayList();
    private List language = new ArrayList();
    private List properties = new ArrayList();

    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

public void setType(String t) {
  type = t;
}

public String getType() {
  return type;
}

    public void addProtocols(String ip) {
	if (!interactionProtocols.contains(ip)) //FIXME. This check is needed because addProtocol might be also been called
	    interactionProtocols.add(ip);
    }
    public boolean removeProtocols(String ip) {
      return interactionProtocols.remove(ip);
    }
    public void clearAllProtocols(){
      interactionProtocols.clear();
    }
    public Iterator getAllProtocols() {
      return interactionProtocols.iterator();
    }


    public void addLanguages(String ip) {
	if (!language.contains(ip)) //FIXME. This check is needed because addLanguage might be also been called
	    language.add(ip);
    }
    public boolean removeLanguages(String ip) {
      return language.remove(ip);
    }
    public void clearAllLanguages(){
      language.clear();
    }
    public Iterator getAllLanguages() {
      return language.iterator();
    }

    public void addOntologies(String ip) {
	if (!ontology.contains(ip)) //FIXME. This check is needed because addOntology might be also been called
	    ontology.add(ip);
    }
    public boolean removeOntologies(String ip) {
      return ontology.remove(ip);
    }
    public void clearAllOntologies(){
      ontology.clear();
    }
    public Iterator getAllOntologies() {
      return ontology.iterator();
    }


public void setOwnership(String o) {
  ownership = o;
}

public String getOwnership(){
  return ownership;
}

    public void addProperties(Property ip) {
      properties.add(ip);
    }
    public boolean removeProperties(Property ip) {
      return properties.remove(ip);
    }
    public void clearAllProperties(){
      properties.clear();
    }
    public Iterator getAllProperties() {
      return properties.iterator();
    }

      // FIXME. since JADE 2.4. In order to comply with FIPA 2000. 
      // see also the documentation of this class about singular and plural names
      public void addOntology(String ip) { addOntologies(ip); }
      public Iterator getAllOntology() { return getAllOntologies();}
      public void addProtocol(String ip) { addProtocols(ip); }
      public Iterator getAllProtocol() { return getAllProtocols();}
      public void addLanguage(String ip) { addLanguages(ip); }
      public Iterator getAllLanguage() { return getAllLanguages();}
    
  } 
