package jade.tools.sniffer;

import java.awt.Color;
import java.io.Serializable;
import javax.swing.SwingUtilities;

/**
 * Extends class TreeData and adds properties and methods for representing
 * agents on the Agent Canvas as rectangles.
 *
 * @see jade.tools.sniffer.TreeData 
 */
public class Agent extends TreeData implements Serializable{
	   
  protected static MMCanvas canvAgent = MMAbstractAction.getMMCanvasAgent();
   	
  public static int i = 0;
	public static final int DUMMY = 2;
	//public static final Color []  color = {Color.yellow,Color.red,Color.gray};
	public static final Color []  color = {Color.red,Color.red,Color.gray};
  public static final int hRet = 30;
	public static final int bRet = 50;
	public static final int yRet = 20;
	private int pos = 0;
	
	/**
	 * This flag is <em>true</em> for agents on canvas and <em>false</em> for agents
	 * out of the canvas.
	 */
	public boolean onCanv;

	private int x; 

  /** 
   * Constructor for any named agent to be put on the Agent Canvas
   */
	public Agent(String n){
  	super(n,(i++)%TreeData.AGENT);
   	onCanv = true;	
	}
	
  /** 
   * Constructor for a special agent called <em>Other</em> which represents every agent
   * not present on the Agent Canvas. It is displayed in color grey when every usual agent
   * is displayed in color red and is the first on the left.
   */
	public Agent(){
  	super("Other",(i++)%TreeData.AGENT);
    onCanv = false;	
	}
    
}