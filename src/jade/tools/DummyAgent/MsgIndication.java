package jade.tools.DummyAgent;
// Import the java.util.Vector class
import java.util.*;
import java.io.*;

// Import useful Jade classes
import jade.core.*;
import jade.lang.acl.*;

class MsgIndication
{
	private static final int TYPE_LEN = 20;
	public static final  int INCOMING = 0;
	public static final  int OUTGOING = 1;

	public ACLMessage msg;
	public int    direction;
	public Date   date;

	public MsgIndication(ACLMessage m, int dir, Date d)
	{
		msg = (ACLMessage) m.clone();
		direction = dir;
		date = d;
	}

	public String getIndication()
	{
		String tmpType = msg.getType();
		int blancCharCnt = TYPE_LEN - tmpType.length();
		while (blancCharCnt-- > 0)
			tmpType += " ";
		String tmpDir = (direction == OUTGOING ? "sent to  " : "recv from");
		String tmpPeer = (direction == OUTGOING ? msg.getDest() : msg.getSource());
		return(date.toString() + ":  " + tmpType + " " + tmpDir + "   " + tmpPeer);
	}

	public void setMessage(ACLMessage m)
	{
		msg = (ACLMessage) m.clone();
	}

	public ACLMessage getMessage()
	{
		return(msg);
	}

	public String toString()
	{
		return(date.toString() + "\n" + msg.toString());
	}

	public void toText(Writer w)
	{
		String tmp = toString() + "\n";
		try
		{
		    //			System.out.println(tmp);
			w.write(tmp, 0, tmp.length());
			w.flush();
		}
		catch(IOException e) 
		{
			System.out.println("IO Exception");
		} 
	}

}





