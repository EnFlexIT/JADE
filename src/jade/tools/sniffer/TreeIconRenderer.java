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

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;
import java.awt.Component;


/** 
 * This is the renderer of the Tree. 
 * The method getTreeCellRendererComponent is messaged 
 * when OS repaints the Tree. In this class we describe as
 * a node of the tree must appear
 *
 * @author Gianluca Tanca
 * @version $Date$ $Revision$
 */
 
public class TreeIconRenderer extends JLabel implements TreeCellRenderer{ 

	public static final int WHITE_PAGES = 0;
	public static final int YELLOW_PAGES = 1;

	protected static int ShowType = WHITE_PAGES;

  public TreeIconRenderer(){
  	
		setOpaque(true);
	}

	/** 
	 * @return WHITE_PAGES or YELLOW_PAGES, depending
	 * on what the user has selected in AMSToolBar
	 */
	public static int getShowType (){
		
		return ShowType;
	}

	/**
	 * @param TypeP WHITE_PAGES or YELLOW_PAGES depending on what 
	 * we want to see in the AMSTree
	 */
	public static void setShowType (int TypeP){
		
		if (TypeP  == WHITE_PAGES || TypeP == YELLOW_PAGES) ShowType = TypeP;
	}

  public Component getTreeCellRendererComponent(JTree tree,
                            Object value,
                            boolean selected,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus)

    {
        TreeData data=null;
        
        setFont(tree.getFont());
        
        if(selected)
        {
            setForeground(tree.getBackground());
            setBackground(tree.getForeground());
		}
        else
        {
            setBackground(tree.getBackground());
            setForeground(tree.getForeground());
        }
          
	     data = (TreeData) value;

        if(data != null)
        {
			setToolTipText(data.getToolTipText());
			setIcon(data.getIcon()); 
			if (ShowType == YELLOW_PAGES) setText(data.getName());
			else setText(data.toString());
        }
        else
        {
            setIcon(null); 
            setText(value.toString());
        }
        
        return this;
    }
}