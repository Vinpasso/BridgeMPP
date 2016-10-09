package bridgempp.messageformat.text;

import bridgempp.messageformat.converters.Base64ToPlainText;

public class Base64PlainTextFormat extends PlainTextMessageFormat
{


	public Base64PlainTextFormat()
	{
		super();
	}
	
	@Override
	public String getName()
	{
		return "Base64 Plain Text";
	}

	@Override
	public void registerConversions() {
		addConversion(PLAIN_TEXT, new Base64ToPlainText());
	}

}
