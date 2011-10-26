package chat.client.agent;

public interface ChatClientInterface {
	public void handleSpoken(String s);
	public String[] getParticipantNames();
}
