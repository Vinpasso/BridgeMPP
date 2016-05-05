package bridgempp.messageformat.text;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.Base64ToPlainText;

public class Base64PlainTextFormat extends MessageFormat
{

	public Base64PlainTextFormat()
	{
		super();
		addConversion(BASE_64_PLAIN_TEXT, new Base64ToPlainText());
	}
	
	@Override
	public String getName()
	{
		return "Base64 Plain Text";
	}

}
