package bridgempp.messageformat.text;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.PlainTextToBase64;

public class PlainTextMessageFormat extends MessageFormat {


	public PlainTextMessageFormat()
	{
		super();
	}
	
	@Override
	public String getName() {
		return "Plain Text";
	}

	@Override
	public void registerConversions() {
		addConversion(BASE_64_PLAIN_TEXT, new PlainTextToBase64());	
	}

	@Override
	public String encodeMetaInformationTag(String senderTag, String message)
	{
		return senderTag + ": " + message;
	}
}
