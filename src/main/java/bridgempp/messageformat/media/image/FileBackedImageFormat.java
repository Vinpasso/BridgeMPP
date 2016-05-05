package bridgempp.messageformat.media.image;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.FileBackedToEmbedded;

public class FileBackedImageFormat extends MessageFormat
{

	public FileBackedImageFormat()
	{
		super();
		addConversion(STRING_EMBEDDED_IMAGE_FORMAT, new FileBackedToEmbedded());
	}
	
	@Override
	public String getName()
	{
		return "Image (File Backed)";
	}

}
