/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * GNU Lesser General Public License
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jade.core;

import jade.util.leap.List;
import jade.util.leap.ArrayList;
import jade.util.leap.Iterator;

/**
 * This class represent a specifier and collects
 * a name, a className, and an array of arguments.
 * 
 * @author LEAP
 */
public class Specifier {
    private String   name = null;
    private String   className = null;
    private Object[] args = null;

    /**
     */
    public void setName(String n) {
        name = n;
    } 

    /**
     */
    public String getName() {
        return name;
    } 

    /**

     */
    public void setClassName(String cn) {
        className = cn;
    } 

    /**
     */
    public String getClassName() {
        return className;
    } 

    /**
     */
    public void setArgs(Object[] a) {
        args = a;
    } 

    /**
     */
    public Object[] getArgs() {
        return args;
    } 

    /**
     * This method is used by Boot, ProfileImpl, and RMA in order
     * to have a String representation of this Specifier according to the
     * format <code>name:className(arg1 arg2 argn)</code>
     **/
    public String toString() {
	// TAKE CARE: do not change this method otherwise Boot might fail
	StringBuffer tmp = new StringBuffer();
	if (name != null) {
	    tmp.append(name);
	    tmp.append(":");
	}
	if (className != null) {
	    tmp.append(className);
	}
	if (args != null) {
	    tmp.append("(");
	    for (int i=0; i<args.length; i++) {
		tmp.append(args[i]);
		if (i<args.length-1)
		    tmp.append(" ");
	    }
	    tmp.append(")");
	}
	return tmp.toString();
    }

  /**
   */
  public static List parseSpecifierList(String specsLine) throws Exception {
    ArrayList specs = new ArrayList();
    
    if (specsLine != null && !specsLine.equals("")) {
	    // Copy the string with the specifiers into an array of char
  	  char[] specsChars = new char[specsLine.length()];

    	specsLine.getChars(0, specsLine.length(), specsChars, 0);

    	// Create the StringBuffer to hold the first specifier
    	StringBuffer sbSpecifier = new StringBuffer();
    	int          i = 0;

    	while (i < specsChars.length) {
      	char c = specsChars[i];

      	if (c != ';') {
        	sbSpecifier.append(c);
      	} 
      	else {

        	// The specifier is terminated --> Convert it into a Specifier object
        	String tmp = sbSpecifier.toString().trim();

        	if (tmp.length() > 0) {
          	Specifier s = parseSpecifier(tmp, ',');

          	// Add the Specifier to the list
          	specs.add(s);
        	} 

        	// Create the StringBuffer to hold the next specifier
        	sbSpecifier = new StringBuffer();
      	} 

      	++i;
    	} 

    	// Handle the last specifier
    	String tmp = sbSpecifier.toString().trim();

    	if (tmp.length() > 0) {
      	Specifier s = parseSpecifier(tmp, ',');

      	// Add the Specifier to the list
      	specs.add(s);
    	} 
    }
  	return specs;
  } 

  /**
   * Utility method that parses a stringified object specifier in the form
   * name:class(arg1, arg2...) and returns
   * a Specifier object.
   * Both the name and the list of arguments are optional.
   * Concrete implementations can take advantage from this method to
   * implement the getSpecifiers() method.
   */
  public static Specifier parseSpecifier(String specString, char argsDelimiter) throws Exception {
    Specifier s = new Specifier();

    // NAME
    int       index1 = specString.indexOf(':');
    int       index2 = specString.indexOf('(');

    if (index2 < 0) {
      index2 = 99999;
    } 

    if (index1 > 0 && index1 < index2) {

      // The name exists, colon exists, and is followed by the class name
      s.setName(specString.substring(0, index1));

      // Skip colon
      index1++;
    } 
    else {

      // No name specified
      index1 = 0;
    } 

    // CLASS
    index2 = specString.indexOf('(', index1);

    if (index2 < 0) {

      // No arguments --> just add the class name
      s.setClassName(specString.substring(index1));
    } 
    else {

      // There are arguments --> add the class name and then parse the args
      s.setClassName(specString.substring(index1, index2));

      // ARGUMENTS
      if (!specString.endsWith(")")) {
        throw new Exception("Incorrect specifier \""+specString+"\". Missing final parenthesis");
      } 

      // Get everything is in between '(' and ')'
      String args = specString.substring(index2+1, specString.length()-1);

      s.setArgs(parseArguments(args, argsDelimiter));
    } 

    return s;
  } 

  /**
   */
  private static String[] parseArguments(String args, char argsDelimiter) {
    List argList = new ArrayList();
    int  argStart = 0;
    int  argEnd = args.indexOf(argsDelimiter);

    while (argEnd >= 0) {
      String arg = args.substring(argStart, argEnd);

      argList.add(arg.trim());

      argStart = argEnd+1;
      argEnd = args.indexOf(argsDelimiter, argStart);
    } 

    // Last argument
    String arg = args.substring(argStart, args.length());

    argList.add(arg.trim());

    // Convert the List into an Array
    String arguments[] = new String[argList.size()];
    int    i = 0;

    for (Iterator it = argList.iterator(); it.hasNext(); arguments[i++] = (String) it.next());

    return arguments;
  } 


}

