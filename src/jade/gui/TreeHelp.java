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

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreeSelectionModel;
import java.net.URL;
import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
/*
@author Tiziana Trucco - CSELT S.p.A.
@version $Date$ $ Revision:  $

 */

public class TreeHelp extends JFrame {
    private JEditorPane htmlPane;
    
    private URL helpURL;

    //Optionally play with line styles.  Possible values are
    //"Angled", "Horizontal", and "None" (the default).
    private boolean playWithLineStyle = false;
    private String lineStyle = "Angled"; 

    public TreeHelp() {
        super("JADE Help");
        
        // added for reply to window closing
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                disposeAsync();
            }
        });

        
        JPanel main = new JPanel();
        
        main.setLayout(new BorderLayout());
        
        //Create the HTML viewing pane.
        htmlPane = new JEditorPane();
        htmlPane.setEditable(false);
        htmlPane.setPreferredSize(new Dimension(500,300));
        JScrollPane htmlView = new JScrollPane(htmlPane);
	      

    		try {
            htmlPane.setPage(getClass().getResource("help/DFGUI.html"));
        } catch (IOException e) {
            System.err.println("Attempted to read a bad URL");
        }
    
        main.add(htmlView, BorderLayout.CENTER);
        //Add the split pane to this frame.
        getContentPane().add(main, BorderLayout.CENTER);
        
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