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

/**
* This class models a search constraint.
* @see jade.domain.FIPAAgentManagement.FIPAAgentManagementOntology
* @author Fabio Bellifemine - CSELT S.p.A.
* @version $Date$ $Revision$
*/
public class SearchConstraints {

private Long max_depth = null; 

private Long max_results = null;

    private String search_id = null;

    /**
     * Constructor. Creates a new SearchConstraints by setting default value, as defined
     * by FIPA, for max_depth (i.e. 0 that corresponds to no propagation of the search
     * to the federated DFs) and max_results (i.e. 1 result only to be returned).
     * Furthermore, a new globally unique identifier is created for the value of
     * search_id.
     * WARNING: When the same object is reused for several searches, it is
     * recommended to call the method <code>renewSearchId</code> in order
     * to create a new globally unique identifier. Otherwise, the DF might reply
     * with a FAILURE having received already the same search.
     **/
    public SearchConstraints () {
        renewSearchId();
    }
    
    /** Regenerate the value of search_id as a globally unique identifier.
     * This call is recommended in order to reuse the same object for several
     * searches. Otherwise, the DF might reply with a FAILURe having received
     * already the same search.
     **/
    public void renewSearchId() {
        search_id = "s" + hashCode() + "_" + System.currentTimeMillis();
    }
    
    public void setSearchId(String searchId) {
	search_id = searchId;
    }
  /**
   * return null if it has not been set
   */
    public String getSearchId() {
	return search_id;
    }

public void setMaxDepth(Long l){
  max_depth=l;
}

  /**
   * return null if it has not been set
   */
public Long getMaxDepth() {
  return max_depth;
}

public void setMaxResults(Long l) {
  max_results = l;
}

  /**
   * return null if it has not been set
   */
  public Long getMaxResults(){
    return max_results;
  }
}
