package jade.tools.logging.ontology;

//#J2ME_EXCLUDE_FILE

import jade.content.AgentAction;

public class SetLevel implements AgentAction {
	private int level;
	private String logger;
	
	public SetLevel() {
		this(1);
	}
	
	public SetLevel(int level) {
		setLevel(level);
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public int getLevel() {
		return level;
	}

	public String getLogger() {
		return logger;
	}

	public void setLogger(String logger) {
		this.logger = logger;
	}
	
}
