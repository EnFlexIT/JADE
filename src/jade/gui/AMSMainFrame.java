/*
  $Log$
  Revision 1.3  1998/10/10 19:37:03  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;

   /**
     * A class representing the main window of the Management System
	 * command line parameters are:
	 *				<p>-wi	 enables Windows Look & Feel (default)</P>
	 *				<p>-mo			 Motif Look & Feel</P>
	 *				<p>-me			 Metal Look & Feel</P>
	 *				<p>-mu			 Multi Look & Feel</P>
	 * <pre>
     *    java AMSMainFrame -mo
	 * </pre>
     *
     * @author  Gianstefano Monni
     * @version %I%, %G%
     * @see     com.sun.java.swing.JFrame
     */
public class AMSMainFrame extends JFrame 
{	
	public AMSMainFrame ()
	{
		super("Agent Management System GUI");
		setJMenuBar(new AMSMenu());

		AMSTree panel = new AMSTree();
		setForeground(Color.black);
		setBackground(Color.lightGray);
		addWindowListener(new WindowCloser());
		getContentPane().add(new AMSToolBar(panel),"North");
		
		getContentPane().add(panel,"Center");
	}
	/**
	   show the AMSMainfFrame packing and setting its size correctly
	*/
	public void ShowCorrect()
	{
		pack();
		setSize(400,400);
        setVisible(true);
	}

	private void setUI(String ui)
	{
	  try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf."+ui);
            SwingUtilities.updateComponentTreeUI(this);
			pack();
		}
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace(System.out);
        }
	}
	/**
		enables Motif L&F
	*/
	public void setUI2Motif()
    {
		setUI("motif.MotifLookAndFeel");   
    }
    
	/**
		enables Windows L&F
	*/
	public void setUI2Windows()
	{
		setUI("windows.WindowsLookAndFeel");   
    }

	/**
		enables Multi L&F
	*/
	public void setUI2Multi()
	{
		setUI("multi.MultiLookAndFeel");   
    }

	/**
		enables Metal L&F
	*/
	public void setUI2Metal()
	{
		setUI("metal.MetalLookAndFeel");   
    }

	public static void main (String[] argv)
	{
		AMSMainFrame jf = new AMSMainFrame();
		
		if (argv.length >= 1)
		{
			if (argv[0].equals("-me"))
				jf.setUI2Metal();
			else if (argv[0].equals("-mo"))
				jf.setUI2Motif();
			else if (argv[0].equals("-wi"))
				jf.setUI2Windows();
			else if (argv[0].equals("-mu"))
				jf.setUI2Multi();
			else if (argv[0].equals("-h"))
				System.out.println("Usage : java AMSMainFrame -[wi][mo][mu]");
		}
		jf.ShowCorrect();
	}
}
