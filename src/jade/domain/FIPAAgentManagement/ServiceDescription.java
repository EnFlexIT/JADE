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
import java.util.*;

/** 
* This class models a service data type.
* @author Fabio Bellifemine - CSELT S.p.A.
* @version $Date$ $Revision$
* 
*/


  public class ServiceDescription {



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


    
  } 
