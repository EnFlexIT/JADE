package jade.tools.sniffer;

import java.io.Serializable;
import java.util.Vector;
import java.util.Enumeration;

/**
 * The List for the agents on the Agent Canvas. Implements Serializable for saving 
 * data to the binary snapshot file.
 */
public class AgentList implements Serializable{
	public Vector agentVector;
	
	/**
	 * Default constructor for the class <em>AgentList</em>
	 */
	public AgentList(){   
		agentVector = new Vector(50);
	    String n = "";
        
		/* First we put a dummy agent called "Other" */
		agentVector.addElement(new Agent());

	}
  
  /**
   * Add an agent to the list.
   *
   * @param agent the agent to add
   */  
	public void addAgent(Agent agent){
		agentVector.addElement(agent);
	}
	
	/**
	 * Removes an agent from the list
	 *
	 * @param agentName name of the agent to remove
	 */
  public void removeAgent(String agentName){
	
		for(Enumeration e=agentVector.elements() ; e.hasMoreElements();){
			Agent agent = (Agent)e.nextElement();
		    
			if( agentName.equals(agent.name) && agent.onCanv == true){
				agentVector.removeElement(agent);
			}
  	}
	}

  /**
   * Clears the agent list
   */
	public void removeAllAgents(){
		
		agentVector.removeAllElements();
	}

	
  /** 
   * Verifies if an agent is present on the canvas
   *
   * @param agName name of the agent to check for 
	 */
	public boolean isPresent (String agName){
		
		for(Enumeration e=agentVector.elements() ; e.hasMoreElements();){
			
			Agent agent = (Agent)e.nextElement();
		    
			if( agName.equals(agent.name) ){
				
				return true;
			}
		}
		return false;
	}
    

  /**
   * Gives back the position inside the agentVector
   *
   * @param agName name of the agent for its position to search 
   */
	public int getPos(String agName){
		int i = 0;
		
		int atPos = agName.indexOf("@");
		String onlyName = agName;
		
		if (atPos != -1) {
			onlyName = agName.substring(0,atPos);
		}
	
		for(Enumeration e=agentVector.elements() ; e.hasMoreElements();){
			
			Agent agent = (Agent)e.nextElement();
		   
			if( agName.equals(agent.name) || onlyName.equalsIgnoreCase(agent.name)){ 
				return i;
			}
			i = i + 1;
   	}
		/* 0 is the return value for an agent not present */
    return 0; 
   }
}