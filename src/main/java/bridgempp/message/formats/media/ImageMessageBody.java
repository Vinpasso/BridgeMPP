package bridgempp.message.formats.media;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import bridgempp.binarydistribution.BinaryDistributionManager;

public class ImageMessageBody extends MediaMessageBody
{
	private String identifier;
	private MimeType mimeType;
	private URL url;
	private String caption;

	public ImageMessageBody(MimeType mimeType, File file) throws IOException
	{
		this(mimeType, file.getName(), new FileInputStream(file));
	}
	
	public ImageMessageBody(MimeType mimeType, String fileName, InputStream inputStream) throws IOException
	{
		this.mimeType = mimeType;
		identifier = "image-" + System.currentTimeMillis() + "-" + fileName;
		url = BinaryDistributionManager.defaultPublish(identifier, inputStream);
	}
	
	public ImageMessageBody(URLConnection connection) throws IOException, MimeTypeParseException
	{
		this(new MimeType(connection.getContentType()), connection.getURL().getFile(), connection.getInputStream());
	}

	@Override
	public String getIdentifier()
	{
		return identifier;
	}

	@Override
	public MimeType getMimeType()
	{
		return mimeType;
	}

	@Override
	public String getFormatName()
	{
		return "Image";
	}

	@Override
	public URL getURL()
	{
		return url;
	}

	/**
	 * @return the caption
	 */
	public String getCaption()
	{
		return caption;
	}

	/**
	 * @param caption the caption to set
	 */
	public void setCaption(String caption)
	{
		this.caption = caption;
	}
	
	public boolean hasCaption()
	{
		return caption != null;
	}

}
