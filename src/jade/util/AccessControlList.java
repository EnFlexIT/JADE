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

package jade.util;

//#J2ME_EXCLUDE_FILE

import jade.util.leap.Properties;
import jade.util.Logger;
import java.io.*;
import java.util.regex.*;
import java.util.logging.*;
/**
 *
 *  An ACL object represents an Access Control List and 
 *  wraps a White list and a Black list, 
 *  Both lists make use of regular expressions for 
 *  allowing/denying access to a certain resource.
 * 
//#APIDOC_EXCLUDE_BEGIN
 * 
 * Example, a ACL object is initialized as follows:
 * <code>
 *     ACL myacl = new ACL();
 *     acl.setBlack( new File("black.txt") );
 *     acl.setWhite( new File("white.txt") );
 *     Properties client = new Property();
 *	   client.setProperty();
 *     boolean ok = acl.isAllowed();
 * </code>
 * 
 * and the two files look as follows: 
 * <strong>white.txt</strong>
 * <code>
 *  # this is a commant
 *  # the section 'user' (allowed usernames)
 * 	user:
 *     # all users are allowed
 *     .+?
 * 
 *  # the section 'msisdn' (allowed phone numbers)
 *  msisdn:
 * 	   # all business customers
 *     39335.+?
 * </code>
 * <strong>black.txt</strong>
 * <code>
 *  user:
 *     # specific users denied
 *     badboy
 *     goodgirl
 * 
 *  msisdn:
 *     # specific numbers denied
 *     3933555.+?
 *     39333444111
 * </code>
 * 
 * 
//#APIDOC_EXCLUDE_END
 * 
 * Into each file, multiple sections are allowed.
 *
 * A specific client is allowed if:
 * <ul><li>it is not present into the black list, AND </li>
 *     <li>it is present into the white list. </li>
 * </ul>
 *
 * More about regular expressions: <br>
 *  http://java.sun.com/j2se/1.4.2/docs/api/java/util/regex/Pattern.html
 *  http://java.sun.com/docs/books/tutorial/extra/regex/
 *
 *
 *
 * @author Giosue Vitaglione - Telecom Italia LAB
 *
 */
public class AccessControlList {


private static Logger logger = Logger.getMyLogger(AccessControlList.class.getName());

private String blackFileName=null;
private String whiteFileName=null;
public void setBlack( String blackFileName ){
	this.blackFileName=blackFileName;
}
public void setWhite( String whiteFileName ){
	this.whiteFileName=whiteFileName;
}


/**
 * A specific client is allowed if:
 * <ul><li>it is not present into the black list, AND </li>
 *     <li>it is present into the white list. </li>
 * </ul>
 *
 * <code>isAllow("section", value)</code> returns is the client 
 * having that specific value for that property is allowed.
 *
 */
public boolean isAllowed(String section, String value){
  boolean retVal=false;
  if(logger.isLoggable(Logger.FINER)) {
	      logger.log(Logger.FINER, 
	      "Current dir: "+System.getProperty("user.dir")+"\n" +
	      "\nChecking files:\n    black="+blackFileName+
	                       "\n    white="+whiteFileName+"\n"
  );}
  
  boolean isInBlack = isInList( blackFileName, section, value);
  boolean isInWhite = isInList( whiteFileName, section, value);

  if(logger.isLoggable(Logger.FINE)) {
	      logger.log(Logger.FINE, 
	      " isInBlack="+isInBlack+ 
	      " isInWhite="+isInWhite
  );}
  retVal = (!isInBlack) && (isInWhite);
  return retVal;
}

private boolean isInList(String fileName, String section, String value) {
  boolean retVal=false;
  // read the ACL file
  try {
         System.out.println("Opening: "+fileName);
         BufferedReader in = new BufferedReader(new FileReader(fileName));
         String str;
         String currSection="";
         while ((str = in.readLine()) != null) {
           str=str.trim();

           if (str.startsWith("#")) {
			 continue;
           }


		   // if encountered a section header
		   // change the current section 
		   if (str.endsWith(":")) {
		   	  currSection = str.substring(0, str.length()-1);
		   	  logger.log(Logger.FINER, "Encountered section named: '"+currSection +"'");
		   	  continue;
		   }

		   // is currSection the same as passed 'section' 
		   // that we are searching for into?
		   // If not, skip this line, looking for a new section
		   if (! currSection.equals( section ) ) {
			 continue;
		   }

		   // prepare pattern (from the pattern at this line)
           Pattern pattern = Pattern.compile(str);

	       // prapare matcher (from the passed value)
	       Matcher m = null;  // matcher is created from the pattern
           m = pattern.matcher( value );
		   if(logger.isLoggable(Logger.FINER)) {
				   logger.log(Logger.FINER, 
					"("+fileName+")  "+
					"  pattern="+str+ 
	       			"  matcher="+value+ 
	       			"\n"); 
		   }
		   // check the matching
           boolean b = m.matches();
           if(logger.isLoggable(Logger.FINER)) 
				   logger.log(Logger.FINER, "     " + value + "->" + b +"\n" );

           if (b) {
                 retVal = true;
           }

         } // end while
         in.close();
     } catch (IOException e) { 
     		logger.log(Logger.WARNING, "Exception while checking"+fileName, e );
     		retVal=false;
     }

  return retVal;
} // end isAllowed








/*
*
* The following code is only for class-level testing.
*
public static void main(String args[]){
	createTestACL();
	AccessControlList acl = new AccessControlList();
	acl.setBlack( blackFilename );
	acl.setWhite( whiteFilename );
	acl.setLogLevel( Level.FINE );

	testAndPrint( acl, "user",     "goodboy" );
	testAndPrint( acl, "user",     "sfogliatella9814" );
	testAndPrint( acl, "section2", "forbiddenvalue" );
	testAndPrint( acl, "section2", "goodvalue" );
	testAndPrint( acl, "sectionX", "anyvalue" );
	
}

private static void testAndPrint(AccessControlList acl, String section, String value){
	boolean ok = acl.isAllowed( section, value );
	System.out.println( "section="+section+"  value="+value+"    ok="+(ok+" ").toUpperCase()+"\n\n");
}

private static final String blackFilename="black.txt";
private static final String whiteFilename="white.txt";
private static void createTestACL(){
try {
 PrintWriter out;
 
 out = new PrintWriter(new BufferedWriter(new FileWriter(blackFilename)));
 out.println(
	"\n"
	+"user:\n"
	+"	sfogliatella.+?  \n"
	+"section2:\n"
	+"	forbiddenvalue.*  \n"
	+"section3:\n"
	+"	.+?  \n"
 );
 out.flush(); out.close();
 out = new PrintWriter(new BufferedWriter(new FileWriter(whiteFilename)));
 out.println(
	"\n"
	+"user:\n"
	+"	.+?  \n"
	+"section2:\n"
	+"	good.+?  \n"
	+"section3:\n"
	+"	.+?  \n"
 );
 out.flush(); out.close();

} catch (Exception e) { e.printStackTrace();}
}

private void setLogLevel(Level lev){
	logger.setLevel( lev );
	logger.getParent().setLevel( lev );

	//Set level for handlers associated to logger
	Handler[] pHandlers = logger.getParent().getHandlers();
	Handler[] handlers = logger.getHandlers();
	for (int i=0; i<pHandlers.length; i++){
		pHandlers[i].setLevel(lev);
	}
	for (int j=0; j<handlers.length; j++){
		handlers[j].setLevel(lev);
	}
}


*/


}// end class