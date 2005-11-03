package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

import jade.content.AgentAction;

public class SetFile implements AgentAction {
	private String file;
	
	public SetFile() {
		
	}
	
	public SetFile(String file) {
		setFile(file);
	}
	
	public void setFile(String file) {
		this.file = file;
	}
	
	public String getFile() {
		return file;
	}
	

}
