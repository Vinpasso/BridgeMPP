package bridgempp.message.formats.media;

import java.net.URL;

import javax.activation.MimeType;

import bridgempp.message.MessageBody;

public abstract class MediaMessageBody extends MessageBody
{
	public abstract String getIdentifier();
	public abstract URL getURL();
	
	public abstract MimeType getMimeType();
	
}
