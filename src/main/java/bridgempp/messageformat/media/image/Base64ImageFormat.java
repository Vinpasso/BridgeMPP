package bridgempp.messageformat.media.image;

import bridgempp.messageformat.converters.Base64ToPlainText;

public class Base64ImageFormat extends StringEmbeddedImageFormat
{
	//contents
	//optional url
	


	public Base64ImageFormat()
	{
		super();
	}
	
	
	@Override
	public String getName()
	{
		return "Image (Base64)";
	}


	@Override
	public void registerConversions() {
		addConversion(STRING_EMBEDDED_IMAGE_FORMAT, new Base64ToPlainText());		
	}

}
