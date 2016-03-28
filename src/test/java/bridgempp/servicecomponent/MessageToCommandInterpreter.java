package bridgempp.servicecomponent;

import bridgempp.Message;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.MessageNode;

public class MessageToCommandInterpreter extends MessageNode<Message>
{

	@Override
	protected void process(Message input)
	{
		CommandInterpreter.processMessage(input);
	}

}
