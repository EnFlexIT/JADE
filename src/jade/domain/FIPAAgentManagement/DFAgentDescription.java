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
    Models a DF agent descriptor.  This class provides platform-level
    support to <em>DF</em> agent, holding all informations needed by
    <code>DF-agent-description</code> objects in
    <code>fipa-agent-management</code> ontology.
  */
  public class DFAgentDescription implements Cloneable {



    private String name;
    private List services = new ArrayList();
    private List interactionProtocols = new ArrayList();
    private List ontology = new ArrayList();
    private List language = new ArrayList();


    public void setName(String n) {
      name = n;
    }

    public String getName() {
      return name;
    }

    public void addServices(ServiceDescription a) {
      services.add(a);
    }

    public boolean removeServices(ServiceDescription a) {
      return services.remove(a);
    }

public void clearAllServices(){
  services = new ArrayList();
}

public Iterator getAllServices(){
  return services.iterator();
}

    public void addProtocols(String ip) {
      interactionProtocols.add(ip);
    }
    public boolean removeProtocols(String ip) {
      return interactionProtocols.remove(ip);
    }
    public void clearAllProtocols(){
      interactionProtocols = new ArrayList();
    }
    public Iterator getAllProtocols() {
      return interactionProtocols.iterator();
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

    
  public Object clone()
  {
  	Object o = null;
  	try{
  		o = super.clone();
  	}catch(CloneNotSupportedException e){
  		System.out.println("DFAgentDescriptor not support clone");
  	}
  	return o;
  
  }  

  } // End of DFAgentDescriptor class
