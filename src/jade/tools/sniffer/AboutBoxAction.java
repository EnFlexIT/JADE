package jade.tools.sniffer;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.Window;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.BoxLayout;


/** 
 * This class is invoked the the user selects the AboutBox item on the menu. A dialog
 * box appears providing informations about the authors and program version.
 *  
 * @see jade.Sniffer.MMAbstractAction
 * @author <a href="mailto:alessandro.beneventi@re.nettuno.it"> Alessandro Beneventi </a>	
 */

public class AboutBoxAction extends MMAbstractAction{
	
	private SnifferGUI myGui; //by BENNY
	private String labelText = "written by Alessandro Beneventi";
	private JLabel label;
	
  String imageFile = "jade/tools/sniffer/images/jadelogo.jpg";
	private JButton ok;
	
	public AboutBoxAction (SnifferGUI snifferGui){
       super ("About...");
       myGui = snifferGui;
	}

  /**
   * A dialog box appears with the logo, informations about the authors of the sniffed
   * and program version.
   */
  public void actionPerformed (ActionEvent evt){   
        Image image = Toolkit.getDefaultToolkit().getImage(
                                        imageFile);
        ImagePanel imagePanel = new ImagePanel(image);

        final AboutFrame f = new AboutFrame(myGui,"About...");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                f.disposeAsync();
            }
        });

        Container theCont = f.getContentPane();
        
				theCont.setLayout(new BoxLayout(theCont,BoxLayout.Y_AXIS)); //simply remove
           
        
        //theCont.add(imagePanel, BorderLayout.CENTER);
        label = new JLabel("The Sniffer for");
        //label.setForeground(Color.blue);
        theCont.add(label);
        theCont.setBackground(Color.black);
        theCont.add(imagePanel);
        label = new JLabel("Concept & Early Version by Gianluca Tanca");
        // label.setForeground(Color.blue);
        theCont.add(label);
        label = new JLabel(" ");
        theCont.add(label);                
        label = new JLabel("Developed by Alessandro Beneventi");
        theCont.add(label);
        label = new JLabel("Universita' degli Studi di Parma");
        theCont.add(label);
        label = new JLabel("http://ce.unipr.it");
        theCont.add(label);
        label = new JLabel(" ");
        theCont.add(label);                
        ok = new JButton("Ok");
        ok.addActionListener(new ButtonListener(f));
        // theCont.add(ok, BorderLayout.SOUTH);
        theCont.add(ok);
        f.setModal(true);
        f.setLocation(new Point(100,100));
        f.setSize(new Dimension(310,280));
        f.setVisible(true);
   	}


private class ButtonListener implements ActionListener {
	
	private AboutFrame owner;
	
	public ButtonListener(AboutFrame dialog){
		owner = dialog;	
	}
	
	public void actionPerformed(ActionEvent e) {	
		owner.disposeAsync();
	}
} //End of ButtonListener inner-class

private class AboutFrame extends JDialog {
	
	public AboutFrame(JFrame owner, String name){
		super(owner, name);
		// setModal(true);
	}
	
  public void disposeAsync() {

    class disposeIt implements Runnable {
      private Window toDispose;

      public disposeIt(Window w) {
			toDispose = w;
      }

      public void run() {
			toDispose.dispose();
      }

    }
    
    EventQueue.invokeLater(new disposeIt(this));

  }
}


private class ImagePanel extends JPanel {
    Image image;

    public ImagePanel(Image image) {
        this.image = image;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g); //paint background
				setBackground(Color.black);
        //Draw image at its natural size first.
        g.drawImage(image, 0, 0, this); //85x62 image
    }
	}

} //End of AboutBoxAction class