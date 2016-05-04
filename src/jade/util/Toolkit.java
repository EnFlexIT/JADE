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

import java.util.Properties;

public class Toolkit {

	public static final String DELIM_START =  "{";
	public static final String DELIM_STOP = "}";
	
	// If a substitution parameter is not found
	public static final int MISSING_PARAM_POLICY_REMOVE = 0;   // Hello {x} World --> Hello World 
	public static final int MISSING_PARAM_POLICY_LEAVE = 1;   // Hello {x} World --> Hello {x} World  
	public static final int MISSING_PARAM_POLICY_FAIL = 2;     // Throw IllegalArgumentException
	
	public static String substituteParameters(String expression, Properties parameters) {
		return substituteParameters(expression, parameters, DELIM_START, DELIM_STOP);
	}
	
	public static String substituteParameters(String expression, Properties parameters, String startDelim, String stopDelim) {
		return substituteParameters(expression, parameters, startDelim, stopDelim, MISSING_PARAM_POLICY_REMOVE);
	}
	
	public static String substituteParameters(String expression, Properties parameters, String startDelim, String stopDelim, int missingParamPolicy) {
		if (expression == null || parameters == null || parameters.isEmpty()) {
			return expression;
		}
		
		// Expression is != null and parameters is NOT empty --> Do the substitution
		StringBuffer sbuf = new StringBuffer();
		int position = 0;
		while (true) {
			int paramStart = expression.indexOf(startDelim, position);
			if (paramStart == -1) {
				// No more parameters
				if (position == 0) { 
					// The expression does not contain any parameter --> return it as it is
					return expression;
				} 
				else { 
					// Add the tail string which contains no parameters and return the result.
					sbuf.append(expression.substring(position, expression.length()));
					return sbuf.toString();
				}
			} 
			else {
				// Parameter found. Append from current position to the char before the param
				sbuf.append(expression.substring(position, paramStart));
				// Then manage param substitution -> search the closing delimiter 
				// considering all potential sub-placeholder
				int openDelimCount = 0;
				int paramEnd = paramStart + 1;
				while (paramEnd < expression.length()) {
					String ch = Character.toString(expression.charAt(paramEnd));
					if (ch.equals(stopDelim)) {
						if (openDelimCount == 0) {
							break;
						}
						else {
							openDelimCount--;
						}
					}
					else if (ch.equals(startDelim)) {
						openDelimCount++;
					}
					paramEnd++;
				}
				if (paramEnd >= expression.length()) {
					throw new IllegalArgumentException('"' + expression + "\" has no closing brace. Opening brace at position " + paramStart + '.');
				} 
				else {
					String key = expression.substring(paramStart + startDelim.length(), paramEnd);
					// Manage sub-placeholder (eg. {A{B}})
					key = substituteParameters(key, parameters, startDelim, stopDelim, missingParamPolicy);
					
					// Try to substitute the key
					String replacement = parameters.getProperty(key, null);
					if (replacement != null) {
						// Do parameter substitution on the replacement string
						// such that we can solve "Hello {x2}" as "Hello p1" also where 
						// x2={x1}
						// x1=p1
						String recursiveReplacement = substituteParameters(replacement, parameters, startDelim, stopDelim, missingParamPolicy);
						sbuf.append(recursiveReplacement);
					}
					else {
						// Missing parameter
						if (missingParamPolicy == MISSING_PARAM_POLICY_LEAVE) {
							// Copy without substituting
							sbuf.append(startDelim).append(key).append(stopDelim);
						}
						else if (missingParamPolicy == MISSING_PARAM_POLICY_FAIL) {
							// Throws an exception
							throw new IllegalArgumentException("Missing substitution parameter "+key+" found at position " + paramStart + '.');
						}
						else {
							// Remove the param (default): just don't copy anything
						}
					}
					position = paramEnd + stopDelim.length();
				}
			}
		}
	}
	
	public static void main(String[] args) {
		String s = "Hello {x}...{y{d}}...{z} World";
		Properties pp = new Properties();
		pp.setProperty("x", "ciao");
		String s1 = substituteParameters(s, pp, DELIM_START, DELIM_STOP, MISSING_PARAM_POLICY_REMOVE);
		System.out.println(s1);
		String s2 = substituteParameters(s, pp, DELIM_START, DELIM_STOP, MISSING_PARAM_POLICY_LEAVE);
		System.out.println(s2);
	}
}
