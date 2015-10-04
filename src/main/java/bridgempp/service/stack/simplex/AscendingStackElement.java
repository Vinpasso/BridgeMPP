package bridgempp.service.stack.simplex;

import bridgempp.Message;
import bridgempp.service.stack.ServiceStackElement;

public abstract class AscendingStackElement<T> extends ServiceStackElement<T, T>
{	
	@Override
	protected void messageDescending(T message)
	{
		sendToLower(message);
	}

}
