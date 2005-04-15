package jade.mtp.http;

public interface ContentHandler
{
	void setDocumentLocator(Locator locator);

	void startDocument();

	void endDocument();

	void startPrefixMapping(String prefix, String uri);

	void endPrefixMapping(String prefix);

	void startElement(String uri, String localName, String qName, Attributes atts);

	void endElement(String uri, String localName, String qName);

	void characters(char[] ch, int start, int length);

	void ignorableWhitespace(char[] ch, int start, int length);

	void processingInstruction(String target, String data);

	void skippedEntity(String name);
}
