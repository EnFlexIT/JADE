package jade.mtp.http;

import jade.domain.FIPAAgentManagement.Property;

public interface XmlReader
{

	EntityResolver getEntityResolver();

	void setEntityResolver(EntityResolver er);

	DtdHandler getDtdHandler();

	void setDtdHandler(DtdHandler dh);

	ContentHandler getContentHandler();

	void setContentHandler(ContentHandler ch);

	ErrorHandler getErrorHandler();

	void setErrorHandler(ErrorHandler eh);
	
	boolean getFeature(String name);

	void setFeature(String name, boolean value);

	Property getProperty(String name);

	void parse(InputSource input);

	void parse(String systemId);
}

