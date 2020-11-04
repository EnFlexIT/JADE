package jade.mtp.http;

public interface DtdHandler
{
	void notationDecl(String name, String publicId, String systemId);

	void unparsedEntityDecl(String name, String publicId, String systemId, String notationName);
}
