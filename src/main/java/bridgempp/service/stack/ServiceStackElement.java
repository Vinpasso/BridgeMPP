package bridgempp.service.stack;

import bridgempp.Message;

public abstract class ServiceStackElement
{

	protected abstract void messageAscending(Message message);
	
	protected abstract void messageDescending(Message message);
	
	void sendToLower(Message message)
	{
		
	}
	
	void sendToUpper(Message message)
	{
		
	}
}
