package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

import jade.content.AgentAction;

public class GetAllLoggers implements AgentAction {
	private String type;
	private String filter;
	
	public void setName(String type) {
		this.type = type;
	}
	
	public String getName() {
		return type;
	}
	
	public void setFilter(String filter) {
		this.filter = filter;
	}
	
	public String getFilter() {
		return filter;
	}	
}
