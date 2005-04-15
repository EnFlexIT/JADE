package jade.mtp.http;

public class ParseException extends System.Xml.XmlException
{
	private ParseError error;

	public ParseError getError()
	{
		return error;
	}


	public ParseException() {}

	public ParseException(String message)
	{
		super(message);
	}

	public ParseException(String message, Exception e)
	{
		super(message, e);
	}

	public ParseException(ParseError error)
	{
		super(error.getMessage(), error.getBaseException());
		this.error = error;
	}
}

