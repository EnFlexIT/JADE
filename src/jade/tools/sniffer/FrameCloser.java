package jade.tools.sniffer;

import java.awt.Window;
import java.awt.event.WindowEvent;

/** 
 * Catches event for closing frame.
 * @see jade.Sniffer.WindowCloser
 */

final class FrameCloser extends WindowCloser{
	
  public void windowClosing(WindowEvent e){
  	
		Window win = e.getWindow();
		win.setVisible(false);
		win.dispose();
	}
}