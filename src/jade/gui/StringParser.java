
package jade.gui;

class StringParser
{
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