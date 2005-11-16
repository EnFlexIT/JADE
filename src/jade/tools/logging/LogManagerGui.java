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
package jade.tools.logging;

//Import required Java classes 
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.logging.*;
import java.util.logging.LogManager;
import java.util.Enumeration;
import java.util.Vector;
import jade.gui.JadeLogoButton;
import javax.swing.JOptionPane;


import jade.core.*;

/**
 * Utility class used to handle logging service.
 * @author Rosalba Bochicchio - TILAB
 */

public class LogManagerGui extends javax.swing.JFrame {
	
	private LogManagerAgent myAgent;
	
	private Vector loggers = new Vector();
	
	private static final int WIDTH 	= 700;
	private static final int HEIGHT = 500;
	
	private static int NAME_COLUMN  		= 0;
	private static int SET_LEVEL_COLUMN 	= 1;		
	private static int HANDLER_COLUMN 		= 2;		
	private static int FILE_HANDLER_COLUMN 	= 3;
	
	private String 	logo = "images/logger.gif";
	private String 	jadeLogo = "images/jadelogo.jpg";
	private AID     agentName;
	
	private static final String ErrorMessage = "A pattern consists of a string that includes the following special components that will be replaced at runtime:\n\n"+
	"/       "+" the local pathname separator"+"\n"+
	"%t     "+"the system temporary directory "+"\n"+
	"%h    "+"the value of the user.home system property "+"\n"+
	"%g    "+"the generation number to distinguish rotated logs "+"\n"+
	"%u    "+"a unique number to resolve conflicts "+"\n"+
	"%%    "+"translates to a single percent sign %"+"\n\n"+
	"Please refer to java.util.logging.FileHandler documentation for further informations.\n\n";
	private static final String ErrorPaneTitle = "Wrong  name for path specified ";
	
	
	
	LogManager logManager = LogManager.getLogManager();
	JComboBox levelCombo;
	
	public LogManagerGui(LogManagerAgent agent){
		this.myAgent = agent;
		agentName = myAgent.getAID();
		init();
	}
	
	public void init(){
		// set the icon for this frame
		setIconImage(getToolkit().getImage(getClass().getResource("/jade/gui/images/logosmall.jpg")));
		setTitle(myAgent.getLocalName()+" - Log Manager Agent");
		setSize(WIDTH,HEIGHT);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		TableModel model = new LogTable(getLoggers(),4);
		JTable table = new JTable(model);
		getContentPane().add(new JScrollPane(table),"Center");
		
		//Allows the modification of the value in the cell
		
		levelCombo = new JComboBox();
		levelCombo.addItem("SEVERE");
		levelCombo.addItem("WARNING");
		levelCombo.addItem("INFO");
		levelCombo.addItem("CONFIG");
		levelCombo.addItem("FINE");
		levelCombo.addItem("FINER");
		levelCombo.addItem("FINEST");
		levelCombo.addItem("ALL");
		levelCombo.addItem("OFF");
		
		TableCellEditor levelEditor = new DefaultCellEditor(levelCombo);	
		
		TableColumnModel columnModel = table.getColumnModel();
		TableColumn setLevelColumn = columnModel.getColumn(SET_LEVEL_COLUMN); 	
		
		setLevelColumn.setCellEditor(levelEditor);
		
		try{
			setTitle(agentName.getName() + " - LogManagerAgent");
		}catch(Exception e){setTitle("LogManagerAgent");}
		
		Image image = getToolkit().getImage(getClass().getResource(logo));
		setIconImage(image);
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.out.println("closing");
				dispose();
				myAgent.doDelete();
			}
		});
		
		/////////////////////////////////////////////////////
		// Add Toolbar to the NORTH part of the border layout 
		JToolBar bar = new JToolBar();
		
		bar.add(Box.createHorizontalGlue());
		JadeLogoButton logo = new JadeLogoButton();
		bar.add(logo);
		getContentPane().add("North", bar);
		
	}
	
	private int getLoggers(){
		for(Enumeration e = logManager.getLoggerNames();e.hasMoreElements();){
			String logName = (String)e.nextElement();
			LogElem logElem= new LogElem(logName," ", " ");
			// add the logger into the vector at the proper position, given the
			// lexical order
			// FIXME. This should be improved in performance
			int i;
			for (i=0; (i<loggers.size() 
					&& ( ((LogElem)loggers.elementAt(i)).getLogger().toString().compareTo(logName) <= 0)); i++) {
			}
			//System.out.println(logName+" "+i+" "+loggers.size());
			loggers.add(i, logElem);
		} 
		return loggers.size();
	}
	/**
	 * Utility method to show the GUI in the center of the screen.
	 */
	public void showCorrect() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int       centerX = (int) screenSize.getWidth()/2;
		int       centerY = (int) screenSize.getHeight()/2;
		setLocation(centerX-getWidth()/2, centerY-getHeight()/2);
		show();
	} 
	
	class LogElem{
		private String fileName ;
		private Object logger ;
		private String level;
		
		public LogElem (Object logger, String fileName, String level){
			this.logger   = logger;
			this.fileName = fileName;
			this.level = level;
		}
		
		public Object getLogger(){
			return this.logger;
		}
		
		public String getFileName(){
			return this.fileName;
		}
		
		public String getLevel(){
			return this.level;
		}
		public void setLevel (String level){
			this.level = level;
		}
		
		public void setFileName(String fileName){
			this.fileName = fileName;
		}
	}
	
	
	class LogTable extends AbstractTableModel{
		
		private int rows,columns;
		private String logFile = null;
		
		public LogTable(int rows, int columns){
			this.rows = rows;
			this.columns = columns;
		}
		
		public int getRowCount(){
			return rows;
		}
		
		public int getColumnCount(){
			return columns;
		}
		
		
		//Consente di editare le celle appartenenti alle colonne Level e Handler
		public boolean isCellEditable(int row, int column){
			return column == SET_LEVEL_COLUMN || column == FILE_HANDLER_COLUMN;
		}
		
		public Object getValueAt(int row, int column){
			Object result = null;
			String theLevel = null;
			String setLevel = null;			
			
			Logger theLogger = logManager.getLogger(logManager.getLogger((((LogElem)loggers.elementAt(row)).getLogger()).toString()).getName());
			Level level = theLogger.getLevel();
			
			//A level has been specified for this logger 
			if (level!=null)
				theLevel = level.getName();
			//If the result is null, this logger's effective level will be inherited from its parent. 
			//The value is the one specified for the property level in the configuration file 			
			else theLevel = logManager.getProperty(".level");
			
			//Handle the log on the file specified by the user 			
			String fileHandler = ((LogElem)loggers.elementAt(row)).getFileName();
			
			String handler = logManager.getProperty("handlers");
			// if handler==null, then set it to empty string
			handler = (handler == null ? "" : handler);
			// here we are sure that handler is never null
			
			boolean fhExists = (handler.indexOf("java.util.logging.FileHandler") > -1);
			
			Handler[] handlers = theLogger.getHandlers();
			
			//add the file handler specified by the user
			for (int i=0;i<handlers.length;i++){
				String temp = handlers[i].toString();
				if (!fhExists){
					String userHandler = (temp.indexOf('@') < 0 ? temp : temp.substring(0, temp.indexOf('@')));
					handler = ( handler.length() > 0 ? handler+", "+userHandler : userHandler);
				}
			}
			
			switch (column){
			case 0:result = ((LogElem)loggers.elementAt(row)).getLogger();	break;	
			case 1:result = theLevel.toString(); break;
			case 2:result =  handler;										break;
			case 3:result =	fileHandler;									break;
			}
			return result;
		}
		
		public void setValueAt(Object value, int row, int column){
			java.util.logging.Logger logger = logManager.getLogger(((LogElem)loggers.elementAt(row)).getLogger().toString());						
			switch(column){
			case 1:{
				Level level = Level.parse(value.toString());
				logger.setLevel(level);
				//Set level for handlers associated to logger
				Handler[] pHandlers = logger.getParent().getHandlers();
				Handler[] handlers = logger.getHandlers();
				for (int i=0; i<pHandlers.length; i++){
					pHandlers[i].setLevel(level);
				}
				for (int j=0; j<handlers.length; j++){
					handlers[j].setLevel(level);
				}
				((LogElem)loggers.elementAt(row)).setLevel(value.toString());
				//Updates the value in the table			
				getValueAt(row,column);
				System.out.println("Set log level for "+logger.getName() +" to: "+logger.getLevel().toString());
			}
			break;
			case 3:	{
				try{
					((LogElem)loggers.elementAt(row)).setFileName(value.toString());
					if (((LogElem)loggers.elementAt(row)).getFileName().length()>1)
					{
						System.out.println(value.toString());
						logger.addHandler(new FileHandler(value.toString()));
					}
				}catch (IOException e){
					JOptionPane.showMessageDialog(new JFrame(),ErrorMessage,ErrorPaneTitle,JOptionPane.ERROR_MESSAGE);
				}
				getValueAt(row,column); 
			}
			break;			
			}
		}
		
		public String getColumnName(int column){
			String columnName = null;
			switch (column){
			case 0: columnName = "Logger Name"; break;
			case 1: columnName = "Set Level";	break;												
			case 2: columnName = "Handlers";	break;				
			case 3: columnName = "Set log file";	break;	
			}
			return columnName;
		}
		
	}
	
}
