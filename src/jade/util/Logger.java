/**
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A. 
 * Copyright (C) 2001,2002 TILab S.p.A. 
 *
 * GNU Lesser General Public License
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 */

package jade.util;

/*#MIDP_INCLUDE_BEGIN
import javax.microedition.rms.RecordStore;
import jade.util.leap.Properties;

#MIDP_INCLUDE_END*/

//#MIDP_EXCLUDE_BEGIN
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
//#MIDP_EXCLUDE_END

/**
 * This class provides a uniform way to produce logging printouts
 * in a device dependent way. The J2SE implementation is a pure
 * extension of the java Logger class and it provides the whole set of 
 * functionalities of java logging facility.<br>A call to the static method 
 * <code>getMyLogger()</code> results in a call to native method
 * <code>java.util.logging.Logger.getLogger()</code>. Then a
 * call to the method <code>log()</code> on the Logger object retrieved
 * logs the real message.<br>
 * Logging levels can be used to control logging output.<br>
 * The re-definition of logging levels has been implemented to allow the 
 * portability of code in PersonalJava e MIDP environments.<br>
 * If you want to create a log in a PersonalJava or MIDP environment you 
 * don't need to change anything in your code.<br><br>
 *
 * For example to log the warning message  "Attention!"the code should be<br><br>
 *
 * <code>Logger logger = Logger.getMyLogger(this.getClass().getName());</code><br>
 * <code>logger.log("Logger.WARNING","Attention!"); </code><br><br>
 * 
 *
 * The call to <code>log<code> method will result in the creation of an 
 * object of type java.util.logging.Logger if you're running in a
 * J2SE environment or in a <code>System.out.println()</code> if you're 
 * running in a PersonalJava environment (through the LEAP add-on).<br>On the 
 * other hand if you are running JADE in a MIDP environment (again through
 * the LEAP add-on) printouts are redirected so that they can be later viewed
 * by means of the <code>jade.util.leap.OutputViewer</code> MIDlet included 
 * in the LEAP add-on.<br><br>
 *
 * According to java logging philosophy a logging level can be set thus enabling
 * loggings at level higher than the one specified.<br><br>
 * The levels in descending order are: <br><br>
 * SEVERE (highest value) <br>
 * WARNING <br>
 * INFO <br>
 * CONFIG <br>
 * FINE <br> 
 * FINER <br>
 * FINEST (lowest value) <br><br><br>
 * In addition there is a level OFF that can be used to turn off logging, and a level ALL that can be used to enable logging of all messages. 
 * In J2SE environment the logging configuration can be initialized using a logging 
 * configuration file that will be read at startup. This file is in standard 
 * java.util.Properties format. The default logging configuration that ships with th JRE 
 * is only a default, and can be overridden with the java.util.logging.config.file <br>
 * system property. For example: <br><br>
 * java -Djava.util.logging.config.file=mylogging.properties jade.Boot <br><br><br>
 * 
 * If you're running in a MIDP environment the levels defined are the same as the J2SE
 * environment. The levels are mapped to int values.<br><br>
 * The levels in descending order are: <br><br>
 * SEVERE (highest value)  <br>
 * WARNING <br>            
 * INFO <br>			   
 * CONFIG <br>			   
 * FINE <br> 	           
 * FINER <br>
 * FINEST (lowest value) <br><br><br>
 * In addition there is a level OFF that can be used to turn off logging, and a level ALL that can be used to enable logging of all messages. 
 * The default level for logging is set to INFO. All messages of higher level will be logged.
 * To modify logging level you have to set the MIDlet-LEAP-level at the selected level
 * property in the manifest file of your MIDlet.<br><br>
 * Example:<br><br>
 * MIDlet-LEAP-log_level:warning
 * 
 * @author Rosalba Bochicchio - TILAB
 */
public class Logger
//#J2ME_EXCLUDE_BEGIN
		extends java.util.logging.Logger
//#J2ME_EXCLUDE_END
{ 
	
	//#J2ME_EXCLUDE_BEGIN
	/**
	 * SEVERE is a message level indicating a serious failure.
	 **/
	public static final Level SEVERE	=	Level.SEVERE;
	/**
	 * WARNING is a message level indicating a potential problem.
	 **/
	public static final Level WARNING	=	Level.WARNING;
	/**
	 * INFO is a message level for informational messages.
	 **/
	public static final Level INFO		=	Level.INFO;
	/**
	 * CONFIG is a message level for static configuration messages.
	 **/
	public static final Level CONFIG	=	Level.CONFIG;
	/**
	 * FINE is a message level providing tracing information.
	 **/
	public static final Level FINE		=	Level.FINE;
	/**
	 * FINER indicates a fairly detailed tracing message.
	 **/
	public static final Level FINER		=	Level.FINER;
	/**
	 * FINEST indicates a highly detailed tracing message
	 **/
	public static final Level FINEST	=	Level.FINEST;
	/**
	 *ALL indicates that all messages should be logged.
	 **/
	public static final Level ALL		=	Level.ALL;
	/**
	 * Special level to be used to turn off logging
	 **/
	public static final Level OFF		=	Level.OFF;



    /**
     * Private method to construct a logger for a named subsystem. 
     * @param name A name for the logger
     * @param resourceBundleName  Name of ResourceBundle to be used for localizing messages for this logger. May be null if none of the messages require localization. 
    */
	private Logger(String name,String resourceBundleName){
		super(name,resourceBundleName);		
	}
	//#J2ME_EXCLUDE_END

	/**
	 *  Find or create a logger for a named subsystem.
	 *	@param name The name of the logger.
	 */
	public static Logger getMyLogger(String name){	
		//#J2ME_EXCLUDE_BEGIN	
		Logger myLogger = new Logger(name, (String)null);
		java.util.logging.LogManager.getLogManager().addLogger(myLogger);
		//#J2ME_EXCLUDE_END
				
		/*#J2ME_INCLUDE_BEGIN
		Logger myLogger = new Logger(name);
		#J2ME_INCLUDE_END*/
		
		return myLogger;
	}


	/*#J2ME_INCLUDE_BEGIN
	  //SEVERE is a message level indicating a serious failure.
		public static final int SEVERE	=	1000;
	  //WARNING is a message level indicating a potential problem.
		public static final int WARNING	=	900;
	  //INFO is a message level for informational messages	
		public static final int INFO	=	800;
	  //CONFIG is a message level for static configuration messages.	
	  	public static final int CONFIG	=	700;
	  //FINE is a message level providing tracing information.
		public static final int FINE	=	500;
	  //FINER indicates a fairly detailed tracing message.
		public static final int FINER	=	400;
	  //FINEST indicates a highly detailed tracing message
		public static final int FINEST	=	300;
	  //ALL indicates that all messages should be logged.
		public static final int ALL		=	-2147483648;
	  //Special level to be used to turn off logging
		public static final int OFF		=	2147483647;
	
		private static int theLevel = 800;	
		private static String theName;
	
		//#J2ME_INCLUDE_END*/
		
		/*#MIDP_INCLUDE_BEGIN	
		private static final String OUTPUT = "OUTPUT";
				
		static {
			try {
				RecordStore.deleteRecordStore(OUTPUT);
			}
			catch (Exception e) {
				// The RS does not exist yet --> No need to reset it
			}
		}
				
		public Logger (String name){
			theName= name;
			try{
				Properties props = new Properties();
				props.load("jad");
												
				if(props.getProperty("log_level")!=null){		
				 	String tmp = props.getProperty("log_level");
				 	
				 	if (tmp.equals("severe"))
				 		theLevel = 1000;
				 	if (tmp.equals("warning"))
				 		theLevel = 900;
				 	if (tmp.equals("info"))
				 		theLevel = 800;
				 	if (tmp.equals("config"))
				 		theLevel = 700;
				 	if (tmp.equals("fine"))
				 		theLevel = 500;
				 	if (tmp.equals("finer"))
				 		theLevel = 400;
				 	if (tmp.equals("finest"))
				 		theLevel = 300;
				 	if (tmp.equals("all"))
				 		//Set the level to lowest possible one
				 		//so to log everything
				 		theLevel = -2147483648;
				 	else if (tmp.equals("off"))
						//set the level to highest possible one
						//so to log nothing				 	
				 		theLevel = 2147483647;
					}
				}
				catch (Exception e){
					Logger.println(e.getMessage());
					}
			}	

		// Log a message in MIDP environment.
		// The message is written in the RecorStore of the MIDP device
		public  synchronized  void log(int level, String msg) {		
			if(level >= theLevel){
			try{
				RecordStore rs =	RecordStore.openRecordStore(OUTPUT, true);
				String log = theName+": "+msg;
				byte[] bb = log.getBytes();
				rs.addRecord(bb,0,bb.length);
				rs.closeRecordStore();
			}
			catch (Throwable t){
				t.printStackTrace();
				}
			}
		}
	#MIDP_INCLUDE_END*/	
	
	 /*#PJAVA_INCLUDE_BEGIN
	  
	  		public Logger (String name){
				theName= name;
			}

		public  synchronized  void log(int level, String msg) {		
			if(level >= theLevel){
				System.out.println(theName+": "+msg);
			}
		}
		#PJAVA_INCLUDE_END*/



	//CODE FROM NOW ON IS JUST FOR  BACKWARD COMPATIBILITY -- TO BE REMOVED --
    /**
     *	
     *	Print a new line on the log stream.
     *
     *	@deprecated 	
     *
    */
	public static void println() {
		println("");
	}
	
	/**
	 *	Print a String in a device dependent way. 
	 *
     *	@deprecated 	
     *
   	*/
	public synchronized static void println(String s) {
		System.out.println(s);
		
		/*#MIDP_INCLUDE_BEGIN
		try{
			RecordStore rs =	RecordStore.openRecordStore(OUTPUT, true);
			byte[] bb = s.getBytes();
			rs.addRecord(bb,0,bb.length);
			rs.closeRecordStore();
		}
		catch (Throwable t){
			t.printStackTrace();
		}
		#MIDP_INCLUDE_END*/
	}
	
	//#MIDP_EXCLUDE_BEGIN
	private static final String DEFAULT_LOG_FORMAT = "%t [LVL-%l][%i][%h] %m";
	private static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

	private static final char TIME = 't';
	private static final char LEVEL = 'l';
	private static final char ID = 'i';
	private static final char THREAD = 'h';
	private static final char MESSAGE = 'm';
	
	private int verbosity;
	private String id = null;
	private StringBuffer sb = new StringBuffer();
	private DateFormat timeFormatter = null;
	private Printer[] myPrinters;

	/**
	 * 
	 * Create a logger object with the given name and verbosity level.
	 *	
	 * @deprecated
	 *
	 */	
	public Logger(String id, int verbosity) {
		this(id,verbosity,null,null);
	}
	
    /**
     *
     * Create a logger object with a custom log format.
     * @param id The name identifying the new logger.
     * @param verbosity The verbosity level.
     * @param timeFormat The format for log timestamps.
     * @param logFormat The format for log messages.
     *
     * @deprecated 
     *
    */
	public Logger(String id, int verbosity, String timeFormat, String logFormat){
	    //#PJAVA_EXCLUDE_BEGIN
	    super(id,timeFormat);
	    //#PJAVA_EXCLUDE_END
	    this.id = id;
		this.verbosity = verbosity;
		if (timeFormat == null) {
			timeFormat = DEFAULT_TIME_FORMAT;
		}
		if (logFormat == null) {
			logFormat = DEFAULT_LOG_FORMAT;
		}
		setTimeFormat(timeFormat);
		setLogFormat(logFormat);
	}
	
	/**
	 * 
	 * Log a given message if the <code>level</code> is <= than the
	 * verbosity level of this Logger object.
	 *
	 * @deprecated
	 */
	public synchronized void log(String msg, int level) {
		if (verbosity >= level) {
			for (int i = 0; i < myPrinters.length; ++i) {
				myPrinters[i].print(sb, level, msg);
			}
			
			println(sb.toString());
			sb.setLength(0);
		}
	}
	
	/**
	 * 
	 * Define the log format of this Logger object.
	 *
	 * @deprecated 
	 *
	 */
	public synchronized void setLogFormat(String format) {
		List l = new ArrayList();
		int index0 = 0;
		int index1 = format.indexOf('%');
		while (index1 >= 0) {
			if (index1-index0 > 0) {
				l.add(new StringPrinter(format.substring(index0, index1)));
			}
			char type = format.charAt(index1+1);
			switch (type) {
				case TIME:
					l.add(new TimePrinter());
					break;
				case LEVEL:
					l.add(new LevelPrinter());
					break;
				case ID:
					l.add(new StringPrinter(id));
					break;
				case THREAD:
					l.add(new ThreadPrinter());
					break;
				case MESSAGE:
					l.add(new MessagePrinter());
					break;
			}
			index0 = index1+2;
			index1 = format.indexOf('%', index0);
		}
		if (format.length()-index0 > 0) {
			l.add(new StringPrinter(format.substring(index0)));
		}
		// Fill the array of printers
		myPrinters = new Printer[l.size()];
		for (int i = 0; i < myPrinters.length; ++i) {
			myPrinters[i] = (Printer) l.get(i);
		}
	}
	
    /**
     * 
     * Establish a time format for this logger.
     * @param format A string detailing the desired format for log
     * messages.
     *
     * @deprecated
     *
    */
	public synchronized void setTimeFormat(String format) {
		if (format != null) {
			timeFormatter = new SimpleDateFormat(format);
		}
		else {
			timeFormatter = null;
		}
	}
	
	/**
	 *
	 * Inner interface Printer.
	 *
	 * @deprecated
	 *
	 */
	private interface Printer {
		void print(StringBuffer sb, int level, String msg);
	} // END of inner interface Printer
	
	/**
	 * Inner class StringPrinter
	 * 
	 * @deprecated
	 *
	 */
	private class StringPrinter implements Printer {
		private String myString;
		
		StringPrinter(String s) {
			myString = s;
		}
		
		public void print(StringBuffer sb, int level, String msg) {
			sb.append(myString);
		}
	} // END of inner class StringPrinter
	
	/**
	 * Inner class TimePrinter
	 * 
	 * @deprecated
	 *
	 */
	private class TimePrinter implements Printer {
		public void print(StringBuffer sb, int level, String msg) {
			if (timeFormatter != null) {
				sb.append(timeFormatter.format(new Date()));
			}
			else {
				sb.append(System.currentTimeMillis());
			}
		}
	} // END of inner class TimePrinter
	
	/**
	 * Inner class ThreadPrinter
	 *
	 * @deprecated
	 *
	 */
	private class ThreadPrinter implements Printer {
		public void print(StringBuffer sb, int level, String msg) {
			sb.append(Thread.currentThread().getName());
		}
	} // END of inner class ThreadPrinter
	
	/**
	 * Inner class LevelPrinter
	 *
	 * @deprecated
	 *
	 */
	private class LevelPrinter implements Printer {
		public void print(StringBuffer sb, int level, String msg) {
			sb.append(level);
		}
	} // END of inner class LevelPrinter
	
	/**
	 * Inner class MessagePrinter
	 *
	 * @deprecated
	 *
	 */
	private class MessagePrinter implements Printer {
		public void print(StringBuffer sb, int level, String msg) {
			sb.append(msg);
		}
	}  // END of inner class MessagePrinter
	//#MIDP_EXCLUDE_END

}

    
 
