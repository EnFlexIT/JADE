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

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.Window;
import java.awt.EventQueue;
import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;
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
import javax.swing.ImageIcon;

/** 
 * This class is invoked the the user selects the AboutBox item on the menu. A dialog
 * box appears providing informations about the authors and program version.
 *  
 * @see jade.tools.sniffer.MMAbstractAction
 * @author <a href="mailto:alessandro.beneventi@re.nettuno.it"> Alessandro Beneventi </a>	
 * @version $Date$ $Revision$
 */

public class AboutBoxAction extends MMAbstractAction{
	
	/**
	@serial
	*/
	private SnifferGUI myGui; //by BENNY
	/**
	@serial
	*/
	private String labelText = "written by Alessandro Beneventi";
	/**
	@serial
	*/
	private JLabel label;
	
  /**
  @serial
  */
	String imageFile = "images/jadelogo.jpg";
	/**
	@serial
	*/
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
        
        final AboutFrame f = new AboutFrame(myGui,"About...");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                f.disposeAsync();
            }
        });

        Container theCont = f.getContentPane();
        
				theCont.setLayout(new BoxLayout(theCont,BoxLayout.Y_AXIS)); //simply remove
           
        
        label = new JLabel("The Sniffer for");
        //label.setForeground(Color.blue);
        theCont.add(label);
        theCont.setBackground(Color.black);
        
        ImageIcon jadelogo = new ImageIcon(getClass().getResource(imageFile));
        label = new JLabel(jadelogo);
        theCont.add(label);
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

} //End of AboutBoxAction class