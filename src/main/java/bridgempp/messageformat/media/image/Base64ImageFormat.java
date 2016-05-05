package bridgempp.messageformat.media.image;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.Base64ToPlainText;

public class Base64ImageFormat extends MessageFormat
{
	//contents
	//optional url
	


	public Base64ImageFormat()
	{
		super();
		addConversion(STRING_EMBEDDED_IMAGE_FORMAT, new Base64ToPlainText());
	}
	
	
	@Override
	public String getName()
	{
		return "Image (Base64)";
	}

}
