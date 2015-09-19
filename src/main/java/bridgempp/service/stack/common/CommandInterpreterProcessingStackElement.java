package bridgempp.service.stack.common;

import bridgempp.Message;
import bridgempp.command.CommandInterpreter;

public class CommandInterpreterProcessingStackElement extends AscendingStackElement
{

	@Override
	protected void messageAscending(Message message)
	{
		CommandInterpreter.processMessage(message);
	}

}
