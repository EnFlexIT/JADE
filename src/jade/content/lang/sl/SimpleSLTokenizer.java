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

package jade.content.lang.sl;

//#APIDOC_EXCLUDE_FILE

import jade.content.lang.Codec;

/**
   Simple utility class for hand-made SL parsing
   @author Giovanni Caire - TILAB
 */
public class SimpleSLTokenizer {
	private static final String msg = "Parse error: unexpected end of content at #";
	private String content;
	private int current = 0;
	
	/**
	   Construct a SimpleSLTokenizer that will act on the given String
	 */
	public SimpleSLTokenizer(String s) {
		content = s;
	}
	
	/**
	   Return the next SL token (i.e. '(', ')' or a generic element)
	   without advancing the pointer
	 */
	public String nextToken() throws Codec.CodecException {
		try {
			skipSpaces();
			char c = content.charAt(current);
			if (c == ')' || c == '(') {
				return String.valueOf(c);
			}
			int start = current;
			while (!isSpace(c) && c != ')') {
				c = content.charAt(++current);
			}
			String s = content.substring(start, current);
			current = start;
			return s;
		}
		catch (IndexOutOfBoundsException ioobe) {
			throw new Codec.CodecException(msg+current);
		}
	}
	
	/**
	   Check that the next character (after eventual spaces) is
	   'c' and advance the pointer to the character just after
	 */
	public void consumeChar(char c) throws Codec.CodecException {
		try {
			skipSpaces();
			if (content.charAt(current++) != c) {
				throw new Codec.CodecException("Parse error: position "+(current-1)+", found "+content.charAt(current-1)+" while "+c+" was expected ["+content.substring(0, current)+"]");
			}
		}
		catch (IndexOutOfBoundsException ioobe) {
			throw new Codec.CodecException(msg+current);
		}
	}
	
	/**
	   Return the next SL element (i.e. a word or a generic sequence 
	   of char enclosed into "") and advance the pointer to the character
	   just after.
	 */
	public String getElement() throws Codec.CodecException {
		try {
			String el = null;
			skipSpaces();
			if (content.charAt(current) == '"') {
				int start = current++;
				while (content.charAt(current) != '"') {
					if (content.charAt(current) == '\\') {
						current++;
					}
					current++;
				}
				current++;
				el = content.substring(start, current);
			}
			else {
				el = getWord();
			}
			return el;
		}
		catch (IndexOutOfBoundsException ioobe) {
			throw new Codec.CodecException(msg+current);
		}
	}
	
	private String getWord() {
		skipSpaces();
		int start = current;
		char c = content.charAt(current);
		// Automatically remove ':' in case this is a slot name.
		// Note that in SL slot values cannot start with ':'
		if (c == ':') {
			start++;
		}
		while (!isSpace(c) && c != ')') {
			c = content.charAt(++current);
		}
		String s = content.substring(start, current);
		return s;
	}
	
	private void skipSpaces() {
		while (isSpace(content.charAt(current))) {
			current++;
		}
	}
	
	private boolean isSpace(char c) {
		return (c == ' ' || c == '\t' || c == '\n');
	}
}
