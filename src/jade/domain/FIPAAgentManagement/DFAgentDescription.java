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

import jade.core.AID;

  /**
  *  Models a DF agent descriptor.  This class provides platform-level
  *  support to <em>DF</em> agent, holding all informations needed by
  *  <code>DF-agent-description</code> objects in
  *  <code>fipa-agent-management</code> ontology.
  *  @see jade.domain.FIPAAgentManagement.FIPAAgentMangementOntology
  *  @author Fabio Bellifemine - CSELT S.p.A.
  *  @version $Date$ $Revision$
  *
  */
  public class DFAgentDescription {



    private AID name;
    private List services = new ArrayList();
    private List interactionProtocols = new ArrayList();
    private List ontology = new ArrayList();
    private List language = new ArrayList();


    public void setName(AID n) {
      name = n;
    }

    public AID getName() {
      return name;
    }

    public void addServices(ServiceDescription a) {
      services.add(a);
    }

    public boolean removeServices(ServiceDescription a) {
      return services.remove(a);
    }

public void clearAllServices(){
  services.clear();
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
      interactionProtocols.clear();
    }
    public Iterator getAllProtocols() {
      return interactionProtocols.iterator();
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

  } // End of DFAgentDescriptor class