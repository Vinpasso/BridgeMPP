package bridgempp.service.stack;

import java.util.Collections;
import java.util.List;

import bridgempp.Message;

public class ServiceStack
{

	//Index 0 is Top of Stack
	//Top of Stack is highest abstraction (CommandInterpreterSender)
	private List<ServiceStackElement> stackElements;
	
	
	public void sendMessage(Message message)
	{
		sendMessageFromTop(message);
	}

	protected void sendMessageFromTop(Message message)
	{
		if(stackElements.isEmpty())
		{
			return;
		}
		ServiceStackElement serviceStackElement = stackElements.get(0);
		serviceStackElement.messageDescending(message);
	}
	
	protected void sendMessageFromBottom(Message message)
	{
		if(stackElements.isEmpty())
		{
			return;
		}
		ServiceStackElement serviceStackElement = stackElements.get(stackElements.size() - 1);
		serviceStackElement.messageDescending(message);
	}
	
	public void insertAtTop(ServiceStackElement element)
	{
		stackElements.add(0, element);
	}
	
	public void insertAtBottom(ServiceStackElement element)
	{
		stackElements.add(element);
	}
	
	public void removeStackElement(ServiceStackElement element)
	{
		stackElements.remove(element);
	}
	
	public List<ServiceStackElement> getStackElements()
	{
		return Collections.unmodifiableList(stackElements);
	}
	
}
