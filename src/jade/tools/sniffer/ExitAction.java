package jade.tools.sniffer;

import java.awt.event.ActionEvent;


/** 
 * Invokes the agent Sniffer to delete itself, closing the Gui and unregistering. 
 *
 * @see jade.Sniffer.MMAbstractAction
 */
public class ExitAction extends MMAbstractAction{
	
	private SnifferGUI myGui; 
	
	public ExitAction(SnifferGUI snifferGui){
		
		super ("ExitActionIcon","Exit");
		myGui = snifferGui; 
	}

	public void actionPerformed(ActionEvent evt) {
  	myGui.sniffHandler.doDelete(); 
  }
}