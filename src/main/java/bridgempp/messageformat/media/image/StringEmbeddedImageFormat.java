package bridgempp.messageformat.media.image;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.Base64ToPlainText;
import bridgempp.messageformat.converters.EmbeddedToFileBacked;

public class StringEmbeddedImageFormat extends MessageFormat
{
	

	public StringEmbeddedImageFormat()
	{
		super();
		addConversion(BASE_64_IMAGE_FORMAT, new Base64ToPlainText());
		addConversion(FILE_BACKED_IMAGE_FORMAT, new EmbeddedToFileBacked());
	}

	@Override
	public String getName()
	{
		return "Image (Embedded)";
	}

}
