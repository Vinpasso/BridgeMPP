package bridgempp.service.stack;

import java.util.Collections;
import java.util.List;

import bridgempp.Message;

public class ServiceStack
{

	//Index 0 is Top of Stack
	//Top of Stack is highest abstraction (CommandInterpreterSender)
	private List<ServiceStackLayer<?, ?>> stackElements;
	
	
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
		ServiceStackLayer<Message, ?> serviceStackElement = (ServiceStackLayer<Message, ?>) stackElements.get(0);
		serviceStackElement.messageDescending(message);
	}
	
	public List<ServiceStackLayer<?,?>> getStackLayers()
	{
		return Collections.unmodifiableList(stackElements);
	}
	
	public void addLayer(ServiceStackLayer<?, ?> layer)
	{
		stackElements.add(layer);
		if(stackElements.size() > 1)
		{
			stackElements.get(0).updateStackReferences(stackElements.listIterator(1));
		}
	}
}
