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

    public void addProtocol(String ip) {
      interactionProtocols.add(ip);
    }
    public boolean removeProtocol(String ip) {
      return interactionProtocols.remove(ip);
    }
    public void clearAllProtocol(){
      interactionProtocols = new ArrayList();
    }
    public Iterator getAllProtocol() {
      return interactionProtocols.iterator();
    }


    public void addLanguage(String ip) {
      language.add(ip);
    }
    public boolean removeLanguage(String ip) {
      return language.remove(ip);
    }
    public void clearAllLanguage(){
      language = new ArrayList();
    }
    public Iterator getAllLanguage() {
      return language.iterator();
    }

    public void addOntology(String ip) {
      ontology.add(ip);
    }
    public boolean removeOntology(String ip) {
      return ontology.remove(ip);
    }
    public void clearAllOntology(){
      ontology = new ArrayList();
    }
    public Iterator getAllOntology() {
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
    public void clearAllPropertis(){
      properties = new ArrayList();
    }
    public Iterator getAllProperties() {
      return properties.iterator();
    }


    
  } 
