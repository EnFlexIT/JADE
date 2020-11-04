package jade.mtp.http;

public interface EntityResolver
{
	InputSource resolveEntity(String publicId, String systemId);
}
