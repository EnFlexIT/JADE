package jade.tools.sniffer;

import java.awt.Insets;
import javax.swing.JToolBar;
import javax.swing.JButton;

/** 
 * Sets up the toolbar for the main Sniffer Gui
 *
 * @see javax.swing.JToolBar
 */
public class MMToolBar extends JToolBar{
    
	public MMToolBar(){
		
		super();
		
		addAction(new AddRemoveAgentAction());
    addSeparator();
	    
		addAction(new ClearCanvasAction());
    addSeparator();
       	
		addAction(new DisplayLogFileAction());
		addAction(new WriteLogFileAction());
		addAction(new WriteMessageListAction()); 
		
	}  
	
	private void addAction(MMAbstractAction act){
		
		JButton b = add(act);
		b.setToolTipText(act.getActionName());
		b.setText("");
    b.setRequestFocusEnabled(false);
    b.setMargin(new Insets(1,1,1,1));
  }
}