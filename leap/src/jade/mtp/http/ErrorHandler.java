package jade.mtp.http;

public interface ErrorHandler
{

	public void warning(ParseError error);

	public void error(ParseError error);

	public void fatalError(ParseError error) throws ParseException;
}
