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

// Import required Java classes 
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.logging.*;
import java.util.Enumeration;
import java.util.Vector;

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
	private static int LEVEL_COLUMN 		= 1;					
	private static int SET_LEVEL_COLUMN 	= 2;		
	private static int HANDLER_COLUMN 		= 3;		
	private static int FILE_HANDLER_COLUMN 	= 4;
	
	LogManager logManager = LogManager.getLogManager();
	
	public LogManagerGui(LogManagerAgent agent){
		this.myAgent = agent;
		init();
		}

	public void init(){
		setTitle("Logging service configuration");
		setSize(WIDTH,HEIGHT);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		
		TableModel model = new LogTable(getLoggers()-1,5);
		JTable table = new JTable(model);
		getContentPane().add(new JScrollPane(table),"Center");
		
		//Allows the modification of the value in the cell
		
		JComboBox levelCombo = new JComboBox();
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
		TableColumn setLevelColumn = columnModel.getColumn(2); 	
		
		setLevelColumn.setCellEditor(levelEditor);
		}
		
	private int getLoggers(){
		for(Enumeration e = logManager.getLoggerNames();e.hasMoreElements();){
			LogElem logElem= new LogElem(e.nextElement()," ", " ");
			loggers.add(logElem);
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
			String handler = null;
			String temp = null;
			boolean fhExists = false;
			
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
			
			handler = logManager.getProperty("handlers");

			String tmp = handler;
			while(tmp.indexOf(",")>0){
				String token = tmp.substring(0,handler.indexOf(","));		
				tmp = tmp.substring(tmp.indexOf(",")+1);
				if (token.equals("java.util.logging.FileHandler"))
					fhExists =true;
				}
				if (tmp.equals("java.util.logging.FileHandler"))
					fhExists =true;
			
			
			Handler[] handlers = theLogger.getHandlers();
			
			//add the file handler specified by the user
			for (int i=0;i<handlers.length;i++){
				temp = handlers[i].toString();
				if (!fhExists){
					if( handler !=null)
						handler = handler+", "+temp.substring(0,temp.indexOf("@"));
					else 
						handler = temp.substring(0,temp.indexOf("@"));
					}
				}

			switch (column){
				case 0:result = ((LogElem)loggers.elementAt(row)).getLogger();	break;	
				case 1:result = theLevel;										break;
				case 2:result = ((LogElem)loggers.elementAt(row)).getLevel();	break;
				case 3:result =  handler;										break;
				case 4:result =	fileHandler;									break;
				}
			return result;
			}
			
		public void setValueAt(Object value, int row, int column){
			java.util.logging.Logger logger = logManager.getLogger(((LogElem)loggers.elementAt(row)).getLogger().toString());						
			switch(column){
				case 2:{
					Level level = Level.parse(value.toString());
					logger.setLevel(level);
					((LogElem)loggers.elementAt(row)).setLevel(value.toString());
					//Updates the value in the table			
					getValueAt(row,column);
					System.out.println("Set log level for "+logger.getName() +" to: "+logger.getLevel().toString());
					}
				break;
				case 4:	{
					try{
						((LogElem)loggers.elementAt(row)).setFileName(value.toString());
						if (((LogElem)loggers.elementAt(row)).getFileName().length()>1)
							logger.addHandler(new FileHandler(value.toString()));
						}catch (Exception e){
							e.printStackTrace();
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
				case 1: columnName = "Level";		break;
				case 2: columnName = "Set Level";	break;												
				case 3: columnName = "Handlers";	break;				
				case 4: columnName = "Log to file";	break;	
				}
			return columnName;
			}

		}
		
	}
