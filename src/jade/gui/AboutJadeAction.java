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


package jade.gui;

// Import required Java classes 
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.AbstractAction;

/**
Javadoc documentation for the file
@author Tiziana Trucco - CSELT S.p.A
@version $Date$ $Revision$
*/

public class AboutJadeAction extends AbstractAction
{
	private JFrame gui;

  private JLabel label;
	
  String imageFile = "jade/gui/images/jadelogo.jpg";
  String imageLogoCselt = "jade/gui/images/LogoCselt.gif";
    
  Color dark_blue = new java.awt.Color(0,0,160);
 
	
	public AboutJadeAction(JFrame gui)
	{
		super ("About JADE");
		this.gui = gui;
		setEnabled(true);
	}
	
	public void actionPerformed(ActionEvent e) 
	{
		
		
		Image image = Toolkit.getDefaultToolkit().getImage(
                                        imageFile);
    ImagePanel imagePanel = new ImagePanel(image);
        
    
    Image imageLogo = Toolkit.getDefaultToolkit().getImage(
                                        imageLogoCselt);
    ImagePanel imagePanel2 = new ImagePanel(imageLogo);

        
    final AboutFrame f = new AboutFrame(gui,"About JADE");
    
    f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                f.disposeAsync();
            }
        });

        Container theCont = f.getContentPane();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        theCont.setLayout(gridbag);
        
        theCont.setBackground(Color.white);        
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx =0.5;
        c.gridwidth = 3;
        c.ipady = 100;
        c.gridx =0;
        c.gridy =0;
        gridbag.setConstraints(imagePanel,c);
        theCont.add(imagePanel);
       
        label = new JLabel("Version 1.3");
        label.setForeground(dark_blue);
				c.ipady = 1;
				c.gridwidth = 1;
				c.gridx = 1;
				c.gridy = 1;
        gridbag.setConstraints(label,c);
				theCont.add(label);
				
        label = new JLabel(" ");
       	c.gridwidth = 1;
				c.gridx = 0;
				c.gridy = 2;
        gridbag.setConstraints(label,c);
				theCont.add(label);

        label = new JLabel("Copyright (C) 2000 CSELT S.p.A.");
        label.setForeground(dark_blue);
				c.gridwidth = 3;
				c.gridx = 0;
				c.gridy = 3;
        gridbag.setConstraints(label,c);
				theCont.add(label);                
        
				label = new JLabel("Distributed under GNU LGPL");
        label.setForeground(dark_blue);
				c.gridwidth = 3;
				c.gridx = 0;
				c.gridy = 4;
        gridbag.setConstraints(label,c);
				theCont.add(label);
				
        label = new JLabel("http://sharon.cselt.it/projects/jade");
        label.setForeground(dark_blue);
				c.gridwidth = 3;
				c.gridx = 0;
				c.gridy = 5;
        gridbag.setConstraints(label,c);
				theCont.add(label);
        
				label = new JLabel(" ");
				c.gridwidth = 3;
				c.gridx =0;
				c.gridy=6;
				gridbag.setConstraints(label,c);
        theCont.add(label);

        c.weighty = 0.0;
        c.ipady = 50;
        c.gridx = 1;
        c.gridy = 7;
        gridbag.setConstraints(imagePanel2,c);
        theCont.add(imagePanel2); 
        
        f.setModal(true);
        f.setLocation(new Point(100,100));
        f.setSize(new Dimension(300,300));
        f.setVisible(true);
	}
	
	

private class AboutFrame extends JDialog {
	
	public AboutFrame(JFrame owner, String name){
		super(owner, name);
		//setModal(true);
		
	
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
				setBackground(Color.white);
        //Draw image at its natural size first.
        g.drawImage(image, 0, 0, this); //85x62 image
    }
	}

} //Ends AboutJadeAction class
	