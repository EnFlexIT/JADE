package jade.tools.sniffer;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Color;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;

/**
 * This class performs the <em>Sniffer</em> main-windows GUI setup. Also provides method for
 * asynchronous disposal at takedown.
 *	
 * @see	javax.swing.JFrame
 */
public class SnifferGUI extends JFrame {
	
	public static Sniffer sniffHandler; // by BENNY: da togliere quanto prima

		/** 
	 	 * Constructs the main window of the gui.
	 	 *
	 	 * @param	mySniffer handler to the low level agent creating the gui (for callback purpose)
	 	 */	
    public SnifferGUI(Sniffer mySniffer){
     
     super("Sniffer");
	   
	   sniffHandler = mySniffer; // by BENNY: fa schifo e lo tolgo ASAP
	   
	   MMPanel panel = new MMPanel(this);
	   
	   JScrollPane scrollPane = new JScrollPane(panel);
		 MMAbstractAction.setMMScrollAgent(scrollPane);
		 MMAbstractAction.setMMScrollMess(scrollPane);		 
	  
	   scrollPane.setAutoscrolls(false);
		 scrollPane.setOpaque(true);
	   
	   getContentPane().setLayout(new BorderLayout());
	   
		 MMMenuBar mbar = new MMMenuBar(this);
		 setJMenuBar(mbar);
	  
	   MMToolBar tbar = new MMToolBar();
	   getContentPane().add(tbar,"North");
	   	
	   getContentPane().add(scrollPane,"Center");

	
	   insertTextMessage();
	   
		 this.addWindowListener(new ProgramCloser());
	   
		 /*pack();
	   
	   setSize(new Dimension(500,500));
	   
   	 this.setVisible(true);
   	 toFront();*/
	   
	}
	
	
	public void ShowCorrect(){
		 pack();
	   setSize(new Dimension(500,500));
   	 this.setVisible(true);
   	 toFront();
	}
	
	
	/** 
	 * Tells the Agent Tree to add a container.
	 *
	 * @param	cont name of the container to be added
	 */
	public void addContainer(String cont) {	//by BENNY	
		MMTree myTree = MMAbstractAction.selTree;
		myTree.addContainer(cont);
	}

	/** 
	 * Tells the Agent Tree to remove a specified container.
	 *
	 * @param	cont name of the container to be removed
	 */
	public void removeContainer(String cont) { //by BENNY		
		MMTree myTree = MMAbstractAction.selTree;
		myTree.removeContainer(cont);
	}
	
	/** 
	 * Tells the Agent Tree to add an agent.
	 *
	 * @param	cont name of the container to contain the new agent
	 * @param name name of the agent to be created
	 * @param addr address of the agent to be created
	 * @param comm comment (usually type of the agent)
	 */
	public void addAgent(String cont, String name, String addr, String comm){ //by BENNY
		MMTree myTree = MMAbstractAction.selTree;
		myTree.addAgent(cont, name, addr, comm);		
	}

	/** 
	 * Tells the Agent Tree to remove a specified agent.
	 *
	 * @param	cont name of the container containing the agent
	 * @param name name of the agent to be removed
	 */
	public void removeAgent(String cont, String name){ //by BENNY
		MMTree myTree = MMAbstractAction.selTree;
		myTree.removeAgent(cont, name);				
	}
	
	/** 
	 * Displays a dialog box with the error string.
	 *
	 * @param	errMsg error message to print
	 */
	public void showError(String errMsg){
		JOptionPane.showMessageDialog(null, errMsg, "Error", JOptionPane.ERROR_MESSAGE); 
	}

	private void insertTextMessage()
	{
      //Text Component
	   MMTextMessage tm = new MMTextMessage();
     // E' un Handle all'oggetto
	   MMAbstractAction.setMMTextMessage(tm);
	   
	   getContentPane().add(tm,"South");  
	}
    
	public Dimension getPreferredSize()
	{
		return new Dimension(500,500);
	}
    

	private void setUI(String ui)
	{
	  try
        {
            UIManager.setLookAndFeel("javax.swing.plaf."+ui);
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

	/** 
	 * Provides async disposal of the gui to prevent deadlock when not running in
	 * awt event dispatcher
	 */
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