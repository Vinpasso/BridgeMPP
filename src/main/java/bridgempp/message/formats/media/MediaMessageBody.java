package bridgempp.message.formats.media;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;

import javax.activation.MimeType;

import org.apache.commons.io.IOUtils;

import bridgempp.message.MessageBody;

public abstract class MediaMessageBody extends MessageBody
{
	public abstract String getIdentifier();
	public abstract URL getURL();
	
	public abstract MimeType getMimeType();
	
	public byte[] getData() throws IOException
	{
		return IOUtils.toByteArray(getURL());
	}
	
	public String getDataAsBase64() throws IOException
	{
		return Base64.getEncoder().encodeToString(getData());
	}
}
