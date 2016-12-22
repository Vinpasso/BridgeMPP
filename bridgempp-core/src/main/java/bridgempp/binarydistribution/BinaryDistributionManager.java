package bridgempp.binarydistribution;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import bridgempp.message.formats.media.ImageMessageBody;

public class BinaryDistributionManager
{
	
	private static List<DistributionMethod> distributionMethods;
	
	
	public static DistributionMethod getDefaultPublicDistributionMethod()
	{
		return distributionMethods.stream().findFirst().orElse(null);
	}


	public static URL defaultPublish(String identifier, File file) throws IOException
	{
		return getDefaultPublicDistributionMethod().publish(identifier, file);
	}


	public static URL defaultPublish(String identifier, InputStream inputStream) throws IOException
	{
		return getDefaultPublicDistributionMethod().publish(identifier, inputStream);
	}
	
	public ImageMessageBody createImageMessageBody(MimeType mimeType, File file) throws IOException
	{
		return createImageMessageBody(mimeType, file.getName(), new FileInputStream(file));
	}
	
	public ImageMessageBody createImageMessageBody(MimeType mimeType, String fileName, InputStream inputStream) throws IOException
	{
		String identifier = "image-" + System.currentTimeMillis() + "-" + fileName;
		URL url = BinaryDistributionManager.defaultPublish(identifier, inputStream);
		return new ImageMessageBody(identifier, mimeType, url);
	}
	
	public ImageMessageBody createImageMessageBody(URLConnection connection) throws IOException, MimeTypeParseException
	{
		return createImageMessageBody(new MimeType(connection.getContentType()), connection.getURL().getFile(), connection.getInputStream());
	}	
}
