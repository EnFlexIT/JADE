package jade.tools.sniffer;

import java.awt.event.ActionEvent;

/** 
 *  Opens the <em>Selection Agents</em> windows.
 *
 *	@see jade.Sniffer.MMAbstractAction
 *
 */
public class AddRemoveAgentAction extends MMAbstractAction{
	
	public AddRemoveAgentAction (){
    super ("AddRemoveAgentActionIcon","Add/Remove Agent");
	}

	/**
	 * Sets up the <em>Selection Agents</em> gui.
	 */
 	public void actionPerformed (ActionEvent evt){   
   	//super.setSelectionFrame();
		selFrame.getContentPane().add(selTree,"Center");
		selFrame.addWindowListener(new FrameCloser());
  	selFrame.setSize(300,300);
	  selFrame.setVisible(true);
	  selFrame.registerPopUp(selTree.getTree());
	}
}