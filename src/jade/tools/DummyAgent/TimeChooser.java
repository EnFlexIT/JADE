///////////////////////////////////////////////////////////////
//   
//   /     /  ___/  ___/   / /_   _/ 
//  /  /--/___  /  ___/   /_/ / / /   
// /_____/_____/_____/_____/_/_/_/
// 
// -----------------------------------------------------------
// PROJECT:   DUMMY AGENT	
// FILE NAME: TimeChooser.java	
// CONTENT:   This file includes the definition of the TimeChooser class
//            that can be used to let the user choose a certain point in time
//            by means of a dialog window.
// AUTHORS:	  Giovanni Caire	
// RELEASE:	  4.0	
// MODIFIED:  28/04/1999	
// 
//////////////////////////////////////////////////////////////
package jade.tools.DummyAgent;
/**
 * The TimeChooser class can be used to let the user define a certain point 
 * in time by means of a dialog window.<p>
 * <p>
 */

// Import AWT and Swing classes to create the dialog window
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

// Import the java.util.Calendar and Date classes
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Date;

public class TimeChooser implements ActionListener
{
	private Date        date;
	private int         retVal;
	private JDialog     dlg;
	private JTextField  year, month, day, hour, min, sec;

	public static final int OK     = 1;
	public static final int CANCEL = 0;

	public TimeChooser()
	{
		retVal = CANCEL;
		date = null;
	}

	public TimeChooser(Date d)
	{
		retVal = CANCEL;
		date = d;
	}

	public int showEditTimeDlg(JFrame parent)
	{
		JLabel   l;
		Calendar cal;

		dlg = new JDialog(parent, "Edit time");
		dlg.getContentPane().setLayout(new BoxLayout(dlg.getContentPane(), BoxLayout.Y_AXIS));

		cal = new GregorianCalendar();
		if (date != null)
			cal.setTime(date);

		JPanel timePanel = new JPanel();
		timePanel.setLayout(new GridLayout(0,2));
		
		// YEAR
		l = new JLabel("Year: ");
		timePanel.add(l);
		year = new JTextField(4);
		year.setText(String.valueOf(cal.get(Calendar.YEAR)));
		timePanel.add(year);
		//MONTH
		l = new JLabel("Month: ");
		timePanel.add(l);
		month = new JTextField(2);
		month.setText(String.valueOf(cal.get(Calendar.MONTH)));
		timePanel.add(month);
		// DAY
		l = new JLabel("Day: ");
		timePanel.add(l);
		day = new JTextField(2);
		day.setText(String.valueOf(cal.get(Calendar.DATE)));
		timePanel.add(day);
		// HOUR
		l = new JLabel("Hour: ");
		timePanel.add(l);
		hour = new JTextField(2);
		hour.setText(String.valueOf(cal.get(Calendar.HOUR)));
		timePanel.add(hour);
		// MINUTES
		l = new JLabel("Min:");
		timePanel.add(l);
		min = new JTextField(2);
		min.setText(String.valueOf(cal.get(Calendar.MINUTE)));
		timePanel.add(min);
		// SECONDS
		l = new JLabel("Sec");
		timePanel.add(l);
		sec = new JTextField(2);
		sec.setText(String.valueOf(cal.get(Calendar.SECOND)));
		timePanel.add(sec);

		dlg.getContentPane().add(timePanel);

		dlg.getContentPane().add(Box.createVerticalStrut(5));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

		buttonPanel.add(Box.createHorizontalStrut(10));
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		buttonPanel.add(Box.createHorizontalStrut(10));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		buttonPanel.add(Box.createHorizontalStrut(10));

		dlg.getContentPane().add(buttonPanel);

		dlg.setModal(true);
		dlg.pack();
		dlg.show();

		return(retVal);
	}

	public void showViewTimeDlg(JFrame parent)
	{
		JLabel   l;
		Calendar cal;

		dlg = new JDialog(parent, "View Time");
		dlg.getContentPane().setLayout(new BoxLayout(dlg.getContentPane(), BoxLayout.Y_AXIS));

		if (date == null)
		{
			l = new JLabel("No time to view");
			dlg.getContentPane().add(l);
		}
		else
		{	
			cal = new GregorianCalendar();
			cal.setTime(date);

			JPanel timePanel = new JPanel();
			timePanel.setLayout(new GridLayout(0,2));
			
			// YEAR
			l = new JLabel("Year: ");
			timePanel.add(l);
			year = new JTextField(4);
			year.setText(String.valueOf(cal.get(Calendar.YEAR)));
			year.setEnabled(false);
			timePanel.add(year);
			//MONTH
			l = new JLabel("Month: ");
			timePanel.add(l);
			month = new JTextField(2);
			month.setText(String.valueOf(cal.get(Calendar.MONTH)));
			month.setEnabled(false);
			timePanel.add(month);
			// DAY
			l = new JLabel("Day: ");
			timePanel.add(l);
			day = new JTextField(2);
			day.setText(String.valueOf(cal.get(Calendar.DATE)));
			day.setEnabled(false);
			timePanel.add(day);
			// HOUR
			l = new JLabel("Hour: ");
			timePanel.add(l);
			hour = new JTextField(2);
			hour.setText(String.valueOf(cal.get(Calendar.HOUR)));
			hour.setEnabled(false);
			timePanel.add(hour);
			// MINUTES
			l = new JLabel("Min:");
			timePanel.add(l);
			min = new JTextField(2);
			min.setText(String.valueOf(cal.get(Calendar.MINUTE)));
			min.setEnabled(false);
			timePanel.add(min);
			// SECONDS
			l = new JLabel("Sec");
			timePanel.add(l);
			sec = new JTextField(2);
			sec.setText(String.valueOf(cal.get(Calendar.SECOND)));
			sec.setEnabled(false);
			timePanel.add(sec);

			dlg.getContentPane().add(timePanel);

			dlg.getContentPane().add(Box.createVerticalStrut(5));
		}

		JButton okButton = new JButton("Close");
		okButton.addActionListener(this);
		dlg.getContentPane().add(okButton);

		dlg.setModal(true);
		dlg.pack();
		dlg.show();
	}

	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		if (command == "OK")
		{
			Integer I;
			I = new Integer(year.getText());
			int YY = I.intValue();
			I = new Integer(month.getText());
			int MM = I.intValue();
			I = new Integer(day.getText());
			int DD = I.intValue();
			I = new Integer(hour.getText());
			int hh = I.intValue();
			I = new Integer(min.getText());
			int mm = I.intValue();
			I = new Integer(sec.getText());
			int ss = I.intValue();

			Calendar cal = new GregorianCalendar(YY,MM,DD,hh,mm,ss);
			date = cal.getTime();

			retVal = OK;
			dlg.dispose();
		}
		else if (command == "Cancel")
		{
			retVal = CANCEL;
			dlg.dispose();
		}
		else if (command == "Close")
		{
			dlg.dispose();
		}
	}

	public Date getDate()
	{
		return(date);
	}

	public void setDate(Date d)
	{
		date = d;
	}

}





		
