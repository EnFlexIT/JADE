package jade.tools.sniffer;

import java.awt.Window;
import java.awt.event.WindowEvent;

/** 
 * Catch event for closing the main frame window
 *
 * @see jade.Sniffer.WindowCloser
 */
final class ProgramCloser extends WindowCloser{
        
	public void windowClosing(WindowEvent e){
		Window win = e.getWindow();
		win.setVisible(false);
		win.dispose();
	}
}