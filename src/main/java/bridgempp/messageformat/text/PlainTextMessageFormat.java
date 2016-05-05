package bridgempp.messageformat.text;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.PlainTextToBase64;

public class PlainTextMessageFormat extends MessageFormat {


	public PlainTextMessageFormat()
	{
		super();
		addConversion(BASE_64_PLAIN_TEXT, new PlainTextToBase64());
	}
	
	@Override
	public String getName() {
		return "Plain Text";
	}
}
