package bridgempp.message.messagebody;

import java.net.URL;

import bridgempp.message.MIMEProperty.MIMEType;

public abstract class MediaMessageBody extends MessageBody
{
	private URL url;
	private MIMEType mimeType;
	
	public URL getMediaURL()
	{
		return url;
	}
	
	public MIMEType getMIMEType()
	{
		return mimeType;
	}
	
}
