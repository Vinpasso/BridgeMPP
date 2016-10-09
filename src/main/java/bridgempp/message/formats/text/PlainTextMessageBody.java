package bridgempp.message.formats.text;

import bridgempp.message.MessageBody;

public class PlainTextMessageBody extends MessageBody
{
	
	private String text;
	
	public PlainTextMessageBody(String text)
	{
		this.text = text;
	}
	
	@Override
	public String getFormatName()
	{
		return "Plain Text";
	}

	/**
	 * @return the text
	 */
	public String getText()
	{
		return text;
	}

}
