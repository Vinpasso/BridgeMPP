package bridgempp.messageformat.media.image;

import bridgempp.messageformat.converters.FileBackedToEmbedded;

public class FileBackedImageFormat extends StringEmbeddedImageFormat
{

	public FileBackedImageFormat()
	{
		super();
	}
	
	@Override
	public String getName()
	{
		return "Image (File Backed)";
	}

	@Override
	public void registerConversions() {
		addConversion(STRING_EMBEDDED_IMAGE_FORMAT, new FileBackedToEmbedded());
	}

}
