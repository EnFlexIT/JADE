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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
@author Giovanni Caire - CSELT S.p.A.
@version $Date$ $Revision$
*/

public class LayoutFacilitator
{
	private GridBagLayout lm;
	private GridBagConstraints constraint;
	private Container cont;
	private int leftBorder, rightBorder, topBorder, bottomBorder;
	private int xSpacing, ySpacing;
	private int gridNCol, gridNRow;
	private int colWidth[];

	public LayoutFacilitator(Container c)
	{
		lm = new GridBagLayout();
		constraint = new GridBagConstraints();
		cont = c;
		cont.setLayout(lm);
	}

	public void formatGrid(int nr, int nc, int lb, int rb, int tb, int bb, int xs, int ys)
	{
		gridNRow = nr;
		gridNCol = nc;
		colWidth = new int[nc];
		leftBorder = lb;
		rightBorder = rb;
		topBorder = tb;
		bottomBorder = bb;
		xSpacing = xs;
		ySpacing = ys;
	}

	public void setGridColumnWidth(int col, int width)
	{
		colWidth[col] = width;
	}

	public void put(JComponent c, int x, int y, int dx, int dy, boolean fill)
	{
	int leftMargin, rightMargin, topMargin, bottomMargin;
	int preferredWidth, preferredHeight;
		
		// System.out.println("x=" + x + " y=" + y);
		// System.out.println("dx=" + dx + " dy=" + dy);
		// System.out.println("fill=" + (fill ? "true" : "false"));
		// System.exit(0);
		constraint.gridx = x;
		constraint.gridy = y;
		constraint.gridwidth = dx;
		constraint.gridheight = dy;
		constraint.anchor = GridBagConstraints.WEST;
		if (fill)
			constraint.fill = GridBagConstraints.BOTH;
		else
			constraint.fill = GridBagConstraints.VERTICAL;

		leftMargin =   (x == 0 ? leftBorder : 0);
		rightMargin =  (x+dx == gridNCol ? rightBorder : xSpacing);
		topMargin =    (y == 0 ? topBorder : 0);
		bottomMargin = (y+dy == gridNRow ? bottomBorder : ySpacing);

		int i;
		preferredWidth = 0; 
		for (i = 0; i < dx; ++i)
			preferredWidth += colWidth[x+i] + xSpacing;
		preferredWidth -= xSpacing;
	  preferredHeight = c.getPreferredSize().height;
		c.setPreferredSize(new Dimension(preferredWidth, preferredHeight));

		constraint.insets = new Insets(topMargin, leftMargin, bottomMargin, rightMargin);
		lm.setConstraints(c,constraint); 
		cont.add(c);
	}

}
