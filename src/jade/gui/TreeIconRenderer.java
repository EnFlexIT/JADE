/*
  $Log$
  Revision 1.4  1999/02/04 14:47:33  rimassa
  Changed package specification for Swing: now it's 'javax.swing' and no more
  'com.sun.swing'.

  Revision 1.3  1998/10/10 19:37:30  rimassa
  Imported a newer version of JADE GUI from Fabio.

  Revision 1.2  1998/10/04 18:01:41  rimassa
  Added a 'Log:' field to every source file.
*/

package jade.gui;

import java.lang.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

/** 
 * This is the renderer of the Tree. 
 * The method getTreeCellRendererComponent is messaged 
 * when OS repaints the Tree. In this class we describe as
 * a node of the tree must appear
 */
public class TreeIconRenderer extends JLabel 
implements TreeCellRenderer
{ 

	public static final int WHITE_PAGES = 0;
	public static final int YELLOW_PAGES = 1;

	protected static int ShowType = WHITE_PAGES;


    public TreeIconRenderer()
    {
		setOpaque(true);
	}

	/** 
	 * @return WHITE_PAGES or YELLOW_PAGES, depending
	 * on what the user has selected in AMSToolBar
	 */
	public static int getShowType ()
	{
		return ShowType;
	}

	/**
	 * @param TypeP WHITE_PAGES or YELLOW_PAGES depending on what 
	 * we want to see in the AMSTree
	 */
	public static void setShowType (int TypeP)
	{
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

