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
package examples.PingAgent;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import java.io.*;
import java.text.SimpleDateFormat;

/**
This class implements a Logger. It can be used to log the messages exchanged by agents.
The log message can be of the format:
 
A new log file is created every week.
The log maintain information using this format:
<ul>
<li> <b> GUID - - [Date]   "[TX/RX] Performative"</b> where: 
<ul> <li> GUID is the sender agent of the message received or the receiver of the message for a sent message. 
     <li> the Date when the log message has been write
     <li> TX or RX :TX for a transmitted, RX for a received message.
     <li> Perfomative: the performative received or sent. 
</ul>
<li> <b>  GUID - - [Date]   "TX/RX Performative" fullMsg </b> where the format is the same as the previous but it's possible to write the full message exchanged.
<li> <b> [Date] RX/TX ACLMessage/Perfomative </b> : if no Agent has to be specified.
<li><b>   [Date] text </b>: to write whatever log message.
</ul>

@author Tiziana Trucco - Tilab S.p.A.
@version  $Date$ $Revision$  
*/

public class Logger {

    //static variable to use for the log messages.
    /*
      Static variables to be used as constant for the TX/RX symbols
     */
    public static final String RECEIVED = "RX";
    /*
      Static variables to be used as constant for the TX/RX symbols
    */
    public static final String TRANSMITTED = "TX";

 
    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy_MM_dd");
    SimpleDateFormat logDateFormatter = new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss"); //FIXME: non ho trattato il time zone.
    PrintWriter writer;
    boolean justCreated;
    String fileName;
    Date fileCreationDate;

    /**
      Creates a new Logger. The name of the log file will be: fileName.log
      Pay attention that the extension .log is added at the end of the fileName.
    */
    public Logger(String fileName){
	try{
	    this.fileName = fileName + ".log";
	    fileCreationDate = new Date();
	    writer = new PrintWriter(new FileWriter(this.fileName,true));
	    justCreated = true;	
	}catch(IOException ex){
	    ex.printStackTrace();
	}
    }


    /**
       Use this method to write message into the log file with this format:
       GUID - - [Date]   "[TX/RX] Performative"
       where GUID is the sender for a RX message, or the receiver for a TX message. 
       Format of Apache logs: 163.162.130.24 - - [14/Jan/2002:17:07:11 +0100] "GET / HTTP/1.1" 200 1494
       It canalso be used to write the full message when it nto possible to know the performative.
     */
    public synchronized void log(String GUID, String kind,  String performative){

	// verify if create a new file.
	checkFileLog();
	String format = logDateFormatter.format(new Date());
	String logMsg = GUID + " - - " +"["+ format + "] " + "\"" + kind + " " + performative + "\"";
	writer.println(logMsg);  
	writer.flush();
    }

    /*
      Use this method when you want to write a more complete message in case of error.
      GUID - - [Date]   "TX/RX Performative" fullMsg
The same as previous <code>log</code> but the full message in this case is written.
     */
    public synchronized void log(String GUID, String kind,String performative, String fullMsg){
	// verify if create a new file.
	checkFileLog();
	String format = logDateFormatter.format(new Date());
	String logMsg = GUID + " - - " +"["+ format + "] " + "\"" + kind + " " + performative + "\" " + fullMsg;
	writer.println(logMsg);  
	writer.flush();
    }

    /**
       This method write a message using this format:
       [Date] RX/TX FullMessage/Perfomative.
       No sender o receiver specified
     */
    public synchronized void log(String kind,String message){
	checkFileLog();
	String format = logDateFormatter.format(new Date());
	String logMsg = "[" + format +"] " + kind + " " + message;
	writer.println(logMsg);
	writer.flush();
    }

    /**
       This method write a message using this format:
       [Date] text.
     */
    public synchronized void log(String text){
	checkFileLog();
	String format = logDateFormatter.format(new Date());
	String logMsg = "[" + format +"] " + text;
	writer.println(logMsg);
	writer.flush();
    }

   
    
    //verify if the log file is old so close the old one and rename it. create a new file every sunday.
    //note that if no message was written on sunday no new file will be created.
    private void checkFileLog(){
	
	Calendar today = Calendar.getInstance();
	if(today.get(Calendar.DAY_OF_WEEK) == 1){
	    if(!justCreated ){ //create a new file every SUNDAY only if not already created.
		//create a new file
		justCreated = true;
		writer.close();
		try{
		    //rename the file appends the current date.
		    File ff = new File(this.fileName);
		    ff.renameTo(new File(renameFile()));
		 
		    writer =  new PrintWriter(new FileWriter(this.fileName,true));
		   
		}catch(IOException ex){ 
		    ex.printStackTrace();
		}
	    }
	}else{
	    justCreated = false;
	}
    }
 
   

    //return the new name for archive the old log file. The is added as suffix to the previous name.
    private String renameFile(){
	String format = dateFormatter.format(fileCreationDate);
       
	int ext_index = fileName.indexOf(".log");

	String ext = ".log";
	String newName = fileName.substring(0,ext_index);	    
       
	newName += "_" + format + ext;
	fileCreationDate = new Date();
	return newName;
	    
    }
}
