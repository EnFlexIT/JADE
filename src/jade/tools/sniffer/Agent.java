/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/



package jade.tools.sniffer;

import java.awt.Color;
import java.io.Serializable;
import javax.swing.SwingUtilities;
/**

@author Gianluca Tanca
@version $Date$ $Revision$
*/
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