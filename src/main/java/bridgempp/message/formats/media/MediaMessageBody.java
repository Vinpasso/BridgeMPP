package bridgempp.message.formats.media;

import java.net.URL;

import javax.activation.MimeType;

import bridgempp.message.MessageBody;

public abstract class MediaMessageBody extends MessageBody
{
	
	public abstract URL getLocation();
	
	public abstract MimeType getMimeType();
	
}
