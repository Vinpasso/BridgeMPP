package bridgempp.servicecomponent;

import bridgempp.Message;
import bridgempp.data.MessageNodeIO;

public class MessageToFormattedString extends MessageNodeIO<Message, String>
{
	private String formatString;
	
	public MessageToFormattedString(String formatString)
	{
		this.formatString = formatString;
	}
	
	public MessageToFormattedString()
	{
		this.formatString = "%s: %s";
	}
	
	public String processIO(Message input)
	{
		return String.format(formatString, input.getSender().toString(), input.getMessageRaw());
	}
}
