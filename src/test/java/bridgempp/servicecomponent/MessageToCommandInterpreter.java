package bridgempp.servicecomponent;

import bridgempp.command.CommandInterpreter;
import bridgempp.data.MessageNode;
import bridgempp.message.Message;

public class MessageToCommandInterpreter extends MessageNode<Message>
{

	@Override
	protected void process(Message input)
	{
		CommandInterpreter.processMessage(input);
	}

}
