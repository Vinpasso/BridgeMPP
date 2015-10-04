package bridgempp.service.stack.commandinterpreter;

import bridgempp.Message;
import bridgempp.command.CommandInterpreter;
import bridgempp.service.stack.simplex.AscendingStackElement;

public class CommandInterpreterProcessingStackElement extends AscendingStackElement<Message>
{

	@Override
	protected void messageAscending(Message message)
	{
		CommandInterpreter.processMessage(message);
	}

}
