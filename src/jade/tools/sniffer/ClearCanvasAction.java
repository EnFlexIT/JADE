package jade.tools.sniffer;

import java.awt.event.ActionEvent;

/**
 * Clears the Message Canvas. This action cannot be undone 
 *
 * @see jade.Sniffer.MMAbstractAction
 */
public class ClearCanvasAction extends MMAbstractAction{
	
	public ClearCanvasAction(){
		
		super("ClearCanvasActionIcon","Clear Canvas");
	}

	public void actionPerformed (ActionEvent evt){
		
		canvasMess.removeAllMessages();
	}
}