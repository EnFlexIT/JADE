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

package jade.gui;

/**
@author Giovanni Caire - CSELT S.p.A
@version $Date$ $Revision$
*/
class StringParser
{
	// Returns the index of the first occurrence of one among nMatch characters
	// (specified in match[]) in the string s starting at position startAt
	// Returns -1 if does not find any matching character
	static int firstOccurrence(String s, int startAt, char[] match, int nMatch)
	{
		for (int i = startAt;i < s.length(); ++i)
		{
			char c = s.charAt(i);
			for (int j = 0;j < nMatch; ++j)
			{
				if (c == match[j])
					return (i);
			}
		}
		
		return (-1);
	}
	
	// Skips all consecutive occurrences of characters specified in skip[]
	// in the string s starting at position startAt.
	// Returns the number of sipped characters.
	static int skip(String s, int startAt, char[] skip, int nSkip)
	{
		for (int i = startAt;i < s.length(); ++i)
		{
			char c = s.charAt(i);
			boolean skipFlag = false;
			for (int j = 0;j < nSkip; ++j)
			{
				if (c == skip[j])
				{
					skipFlag = true;
					break;
				}
			}
			if (!skipFlag)
				return(i - startAt);
		}
		return(s.length() - startAt);
	}

}