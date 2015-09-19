package bridgempp.service.stack.common;

import bridgempp.Message;
import bridgempp.service.stack.ServiceStackElement;

public abstract class AscendingStackElement extends ServiceStackElement
{	
	@Override
	protected void messageDescending(Message message)
	{
		sendToLower(message);
	}

}
