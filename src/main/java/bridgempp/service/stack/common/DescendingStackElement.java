package bridgempp.service.stack.common;

import bridgempp.Message;
import bridgempp.service.stack.ServiceStackElement;

public abstract class DescendingStackElement extends ServiceStackElement
{
	@Override
	protected void messageAscending(Message message)
	{
		sendToUpper(message);
	}
}
