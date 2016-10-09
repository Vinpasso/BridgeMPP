package bridgempp.message;

import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.formats.text.PlainTextMessageBody;

public class MessageBuilder
{
	private Message message;
	
	public MessageBuilder(User sender, Endpoint origin)
	{
		message = new Message(sender, origin);
	}
	
	
	public MessageBuilder addPlainTextBody(String body)
	{
		message.addMessageBody(new PlainTextMessageBody(body));
		return this;
	}
	
	public Message build()
	{
		return message;
	}


	public MessageBuilder addMessageBody(MessageBody messageBody)
	{
		message.addMessageBody(messageBody);
		return this;
	}
}
