package jade.gui;

import com.sun.java.swing.*;
import com.sun.java.swing.tree.*;
import com.sun.java.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

/**
 * The Dialog to set command line parameters
 * to start Agents. IT has many static attributes because
 * ONLY one Dialog of this kind can be shown at one moment
 */
public class StartDialog extends JDialog implements ActionListener
{
	protected static JTextField name;
	protected static JTextField host;
	protected static JTextField port;
	
	protected static JLabel NameL= new JLabel("Class Name");
	protected static JLabel HostL= new JLabel("Host");
	protected static JLabel PortL= new JLabel("Port");

	protected static JButton OKButton = new JButton ("OK");
	protected static JButton CancelButton = new JButton ("Cancel");

	protected static JFrame frame = new JFrame ("Insert Start Parameters");

	protected static String NameToolTip = "Class Name of the Agent"; 
	protected static String HostToolTip = "Host on which the Agent will start";
	protected static String PortToolTip = "TCP Port";
	
	protected static String result  = "";
	protected static int OK_BUTTON = 0;
	protected static int CANCEL_BUTTON = 1;
	protected static int choice = CANCEL_BUTTON;

	static
	{
		name = new JTextField ();
		name.setEditable(false);
		name.setToolTipText(NameToolTip);
		NameL.setToolTipText(NameToolTip);	

		host = new JTextField ("localhost");
		host.setEditable(true);
		host.setToolTipText(HostToolTip);
		HostL.setToolTipText(HostToolTip);

		port = new JTextField ("2020");
		port.setEditable(true);
		port.setToolTipText(PortToolTip);
		PortL.setToolTipText(PortToolTip);
	}

	protected StartDialog (String ClassName)
	{
		super(frame,"Insert Start Parameters",true);
		
		getContentPane().setLayout(new GridLayout(4,2));
		name.setText(ClassName);
		
		getContentPane().add(NameL);
		getContentPane().add(name);

		getContentPane().add(HostL);
		getContentPane().add(host);

		getContentPane().add(PortL);
		getContentPane().add(port);

		OKButton.addActionListener(this);
		CancelButton.addActionListener(this);

		getContentPane().add(OKButton);
		getContentPane().add(CancelButton);

		setSize(getPreferredSize());
		setVisible(true);
	}

	public Dimension getPreferredSize ()
	{
		return (new Dimension(250,300));
	}

	public void actionPerformed (ActionEvent evt)
	{
		choice = CANCEL_BUTTON;
		if (evt.getSource()==OKButton)
		{
			choice = OK_BUTTON;
		}
		dispose();
	}

	/**
	 * This method show a modal Dialog
	 * useful to set paramethers to start
	 * agents previously registered 
	 */
	public static int showStartDialog(String ClassName)
	{
		name.setEditable(false);
        StartDialog panel = new StartDialog(ClassName);
		return choice;
	}

	/**
	 * This method show a modal Dialog
	 * useful to set paramethers to start
	 * new agents  
	 */
	public static int showStartNewDialog ()
	{
        name.setEditable(true);
		StartDialog panel = new StartDialog("");
		return choice;
	}


	public static String getHost()
	{
		return host.getText();
	}

	public static String getPort ()
	{
		return port.getText();
	}

	public static String getClassName ()
	{
		return name.getText();
	}

	public static void setHost(String hostP) {host.setText(hostP);}
	public static void setPort(String portP) {port.setText(portP);}
	public static void setClassName(String nameP) {name.setText(nameP);}


}