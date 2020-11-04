package jade.android;

import java.io.Serializable;

class AsynchCommandInfo implements Serializable {
	private Object command;
	private RuntimeCallback<Void> callback;
	
	AsynchCommandInfo(Object command, RuntimeCallback<Void> callback) {
		this.command = command;
		this.callback = callback;
	}
	
	Object getCommand() {
		return command;
	}
	
	RuntimeCallback<Void> getCallback() {
		return callback;
	}
}
