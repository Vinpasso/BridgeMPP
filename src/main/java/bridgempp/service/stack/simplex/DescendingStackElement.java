package bridgempp.service.stack.simplex;

import bridgempp.service.stack.ServiceStackElement;

public abstract class DescendingStackElement<T> extends ServiceStackElement<T, T>
{
	@Override
	protected void messageAscending(T message)
	{
		sendToUpper(message);
	}
}
