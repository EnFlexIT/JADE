package jade.tools.sniffer;

import java.io.Serializable;
import jade.lang.acl.ACLMessage;
import javax.swing.SwingUtilities;


/**
 * A <em>Message</em> extends the meaning of an ACLMessage (thus extending 
 * jade.lang.acl.ACLMessage) providing an ACLMessage with the graphic part: the
 * arrow going from the sender of the message to the receiver of the message to
 * be drawn on the Message Canvas
 *
 * @see jade.lang.acl.ACLMessage
 */

public class Message extends jade.lang.acl.ACLMessage implements Serializable{
	
  protected static MMCanvas canvMess = MMAbstractAction.getMMCanvasMess();
	
	private int x1,x2,y;
	private int xCoords[] = new int[3];
	private int yCoords[] = new int[3];
	// private int xCoords[] = new int[6];
	// private int yCoords[] = new int[6];
	
  public static final int step = 80;
	public static final int offset = 45;
	public static final int r = 8;
	private int yDim = 0;
	private int xS = 0;
	private int xD = 0;

	
	public Message(String s, String r){
		
	  super("inform");  
	
		this.setSource(s);
		this.addDest(r);
	}

	public Message(ACLMessage msg) {
		
		super(msg.getType());
		this.addDest(msg.getFirstDest());
		this.setSource(msg.getSource());
		this.setContent(msg.getContent());
		this.setReplyWith(msg.getReplyWith());
		this.setReplyTo(msg.getReplyTo());
		this.setEnvelope(msg.getEnvelope());
		this.setLanguage(msg.getLanguage());
		this.setOntology(msg.getOntology());
		this.setReplyBy(msg.getReplyBy());
		this.setProtocol(msg.getProtocol());
		this.setConversationId(msg.getConversationId());
		
	}
	
		
	public int getInitSeg(int xS){
		x1 = xS * step + offset+4;
		return x1;
	}

	public int getEndSeg(int xD){
   	x2 = xD * step + offset;
    return x2;
	}

	public int getOrdSeg(int yDim){
    y = (yDim * 20) + step - 50;
		return y;
	}
	
}