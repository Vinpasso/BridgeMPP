package bridgempp.messageformat.media.image;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.Base64ToPlainText;
import bridgempp.messageformat.converters.EmbeddedToFileBacked;

public class StringEmbeddedImageFormat extends MessageFormat
{
	

	public StringEmbeddedImageFormat()
	{
		super();
	}

	@Override
	public String getName()
	{
		return "Image (Embedded)";
	}

	@Override
	public void registerConversions() {
		addConversion(BASE_64_IMAGE_FORMAT, new Base64ToPlainText());
		addConversion(FILE_BACKED_IMAGE_FORMAT, new EmbeddedToFileBacked());
	}

}
