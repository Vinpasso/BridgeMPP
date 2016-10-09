package bridgempp.message.formats;

import bridgempp.message.MessageBody;

public abstract class MarkupTextMessageBody extends MessageBody
{
	
	public MarkupTextMessageBody()
	{
		super();
	}

	public abstract String getText();
	
}
