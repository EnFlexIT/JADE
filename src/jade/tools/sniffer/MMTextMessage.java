package jade.tools.sniffer;

import java.awt.Font;
import javax.swing.JTextField;

/**
 * Is the field in the bottom of the main frame. Shows the type of message
 *
 * @see javax.swing.JTextField
 */
public class MMTextMessage extends JTextField {
	
  Font font = new Font("SanSerif",Font.PLAIN,14); 
	
	public MMTextMessage(){
		super();
		setDoubleBuffered(true);
		setEditable(false);
		setFont(font);
		setText("No Info Message");
	}
}