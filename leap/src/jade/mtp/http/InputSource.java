package jade.mtp.http;

import System.Uri;

public class InputSource
{
	private String publicId;

	private String encoding;

	private Uri systemId;


	public String getEncoding()
	{
		return encoding;
	}

	public void setEncoding(String enc)
	{
		encoding = enc;	
	}

	public String getPublicId()
	{
		return publicId;
	}

	public void setPublicId(String pid)
	{
		publicId = pid;
	}

	public String getSystemId()
	{
		String str;

		if (systemId == null)
		{
			str = null;
		}
		else
		{
			str = systemId.get_AbsoluteUri();
		}
		return str;
	}

	public void setSystemId(String sid)
	{
		if (sid == null)
		{
			systemId = null;
		}
		else
		{
			systemId = new Uri(sid);
		}
	}

	public InputSource() {}

	public InputSource(String systemId)
	{
		setSystemId(systemId);
	}
}
