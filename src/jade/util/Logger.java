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
#MIDP_INCLUDE_END*/

//#MIDP_EXCLUDE_BEGIN
import jade.util.leap.List;
import jade.util.leap.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//#MIDP_EXCLUDE_END

/**
   This class provides a uniform way to produce logging printouts
   in a device dependent way. More in details if you are running 
   JADE in a Java 2 standard edition environment or in a 
   PersonalJava environment (through the LEAP add-on) a call to 
   <code>Logger.println()</code> simply results in a call to 
   <code>System.out.println()</code>. On the other hand if you are
   running JADE in a MIDP environment (again through the LEAP add-on)
   printouts are redirected so that they can be later viewed by 
   means of the <code>jade.util.leap.OutputViewer</code> MIDlet 
   included in the LEAP add-on.
 */
public class Logger {

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
	#MIDP_INCLUDE_END*/

    /**
	Print a new line on the log stream.
    */
	public static void println() {
		println("");
	}
	
	/**
     Print a String in a device dependent way. 
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
       Create a logger object with the given name and verbosity level.
       @param id The name identifying the new logger.
       @param verbosity The verbosity level.
    */
	public Logger(String id, int verbosity) {
		this(id, verbosity, null, null);
	}
	
    /**
       Create a logger object with a custom log format.
       @param id The name identifying the new logger.
       @param verbosity The verbosity level.
       @param timeFormat The format for log timestamps.
       @param logFormat The format for log messages.
    */
	public Logger(String id, int verbosity, String timeFormat, String logFormat) {
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
	   Log a given message if the <code>level</code> is <= than the
	   verbosity level of this Logger object.
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
	   Define the log format of this Logger object.
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
       Establish a time format for this logger.
       @param format A string detailing the desired format for log
       messages.
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
	   Inner interface Printer.
	 */
	private interface Printer {
		void print(StringBuffer sb, int level, String msg);
	} // END of inner interface Printer
	
	/**
	   Inner class StringPrinter
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
	   Inner class TimePrinter
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
	   Inner class ThreadPrinter
	 */
	private class ThreadPrinter implements Printer {
		public void print(StringBuffer sb, int level, String msg) {
			sb.append(Thread.currentThread().getName());
		}
	} // END of inner class ThreadPrinter
	
	/**
	   Inner class LevelPrinter
	 */
	private class LevelPrinter implements Printer {
		public void print(StringBuffer sb, int level, String msg) {
			sb.append(level);
		}
	} // END of inner class LevelPrinter
	
	/**
	   Inner class MessagePrinter
	 */
	private class MessagePrinter implements Printer {
		public void print(StringBuffer sb, int level, String msg) {
			sb.append(msg);
		}
	}  // END of inner class MessagePrinter
	//#MIDP_EXCLUDE_END
}

    
 
