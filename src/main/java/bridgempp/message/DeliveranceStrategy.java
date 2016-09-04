package bridgempp.message;

import java.util.function.Function;

import bridgempp.message.messagebody.MessageBody;
import bridgempp.message.messagebody.TextMessageBody;

public enum DeliveranceStrategy
{
	BASE64(e -> { return ((TextMessageBody)e).getData(); })
	
	;
	
	private Function<MessageBody, MessageBody> function;
	
	DeliveranceStrategy(Function<MessageBody, MessageBody> apply)
	{
		this.function = apply;
	}
	
	public MessageBody apply(MessageBody input)
	{
		return function.apply(input);
	}
	
	
}
