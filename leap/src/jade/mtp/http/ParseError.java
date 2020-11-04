package jade.mtp.http;

public abstract class ParseError
{
	public abstract String getMessage();

	public abstract String getErrorId();
	
	public abstract String getPublicId();

	public abstract String getSystemId();

	public abstract int getLineNumber();

	public abstract int getColumnNumber();

	public abstract Exception getBaseException();

	public void Throw() throws ParseException
	{
		throw new ParseException(this);
	}
}

