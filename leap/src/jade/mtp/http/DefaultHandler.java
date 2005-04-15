package jade.mtp.http;

public abstract class DefaultHandler implements ContentHandler, DtdHandler, EntityResolver, ErrorHandler
{

	public void setDocumentLocator(Locator locator) {}

	public abstract void startDocument();

	public abstract void endDocument();

	public void startPrefixMapping(String prefix, String uri) {}

	public void endPrefixMapping(String prefix) {}

	public abstract void startElement(String uri, String localName, String qName, Attributes atts);

	public abstract void endElement(String uri, String localName, String qName);

	public abstract void characters(char[] ch, int start, int length);

	public void ignorableWhitespace(char[] ch, int start, int length) {}

	public void processingInstruction(String target, String data) {}

	public void skippedEntity(String name) {}

	public void notationDecl(String name, String publicId, String systemId) {}

	public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) {}

	public InputSource resolveEntity(String publicId, String systemId)
	{
		return null;
	}

	public void warning(ParseError error) {}

	public void error(ParseError error) {}

	public void fatalError(ParseError error) throws ParseException
	{
	   error.Throw();
	}
}
