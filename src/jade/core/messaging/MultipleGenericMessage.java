package jade.core.messaging;

//#J2ME_EXCLUDE_FILE

import java.util.ArrayList;
import java.util.List;

public class MultipleGenericMessage extends GenericMessage {

	private static final long serialVersionUID = -7698819276642567423L;
	
	private List<GenericMessage> messages = new ArrayList<GenericMessage>(); 
	private int length; // Raw estimation of bytes taken by this MGM
	
	public MultipleGenericMessage(int length) {
		this.length = length;
	}
	
	public List<GenericMessage> getMessages() {
		return messages;
	}
	
	public void setMessages(List<GenericMessage> messages) {
		this.messages = messages;
		
		// --- Set the foreign receiver and envelope fields according to the messages in the list
		if (messages!=null && messages.size()>0) {
			this.setForeignReceiver(messages.get(0).hasForeignReceiver());
			this.setEnvelope(messages.get(0).getEnvelope());
		}
	}
	
	@Override
	public int getMessagesCnt() {
		return messages.size();
	}
	
	@Override
	public int length() {
		return length;
	}
}
