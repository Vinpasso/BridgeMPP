package bridgempp.servicecomponent;

import bridgempp.Message;
import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.data.MessageNodeIO;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;

public class StringToMessageBodyNode extends MessageNodeIO<String, Message>
{
	private User sender;
	private Endpoint origin;
	private Endpoint target;
	private Group group;
	private MessageFormat messageFormat;

	public StringToMessageBodyNode(User sender, Endpoint origin, Endpoint target, Group group, MessageFormat messageFormat)
	{
		super();
		this.sender = sender;
		this.origin = origin;
		this.target = target;
		this.group = group;
		this.messageFormat = messageFormat;
	}
	
	@Override
	protected Message processIO(String input)
	{
		return new Message(sender, origin, target, group, input, messageFormat);
	}

}
