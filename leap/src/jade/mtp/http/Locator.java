package jade.mtp.http;

public interface Locator
{
	String getPublicID();
	int getLineNumber();
	int getColumnNumber();
}