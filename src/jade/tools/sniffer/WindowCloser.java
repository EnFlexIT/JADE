package jade.tools.sniffer;

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


/** 
 * Abstract class because doesn't implements windowClosing methos 
 *
 */
abstract class WindowCloser extends WindowAdapter{

	public void windowClosing(WindowEvent e) { }
}