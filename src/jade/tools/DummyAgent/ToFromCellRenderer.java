package jade.tools.DummyAgent;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

class ToFromCellRenderer extends DefaultListCellRenderer
{
	ToFromCellRenderer() 
	{
		super();
	}

	public Component getListCellRendererComponent(
		JList list,
		Object value,
		int index,
		boolean isSelected,
		boolean cellHasFocus)
	{
		Font courier = new Font("Courier", Font.BOLD, 12);
		setFont(courier);
		setText(((MsgIndication)value).getIndication());
		setBackground(isSelected ? Color.black : Color.white);
		if (((MsgIndication) value).direction == MsgIndication.OUTGOING) 
			setForeground(isSelected ? Color.white : Color.blue);
		else                                        
			setForeground(isSelected ? Color.white : Color.red);
		return this;
	}
}
	

