package jade.gui;
	
	import javax.swing.JButton;
	import javax.swing.ImageIcon;
	import javax.swing.border.Border;
	import java.awt.event.ActionListener;
	import javax.swing.BorderFactory;
	import java.awt.event.ActionEvent;
	
	public class JadeLogoButton extends JButton
	{
		private static String logojade ="images/logosmall.jpg";
		
		public JadeLogoButton()
		{
			ImageIcon jadeicon = new ImageIcon(getClass().getResource(logojade));
    	//JButton logo = new JButton(jadeicon);
			setIcon(jadeicon);
    	Border raisedbevel = BorderFactory.createRaisedBevelBorder();
    	setBorder(raisedbevel);
    
      addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{						
					try{
						BrowserLauncher.openURL(BrowserLauncher.jadeURL);
					}catch(java.io.IOException ex){ex.printStackTrace();}
				}	
			} );
			}

	}