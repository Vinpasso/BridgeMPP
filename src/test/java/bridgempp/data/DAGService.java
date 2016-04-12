package bridgempp.data;

import bridgempp.Message;
import bridgempp.command.CommandInterpreter;
import bridgempp.service.BridgeService;

public abstract class DAGService extends BridgeService
{
	public EventTrigger<DAGService> onConnect;
	public EventTrigger<DAGService> onDisconnect;
	public EventTrigger<Message> onMessageReceived;
	public EventTrigger<Message> onMessageSending;

	public MessageDAG<Void> incommingDAG;
	public MessageDAG<Message> outgoingDAG;
	
	public void sendMessage(Message message)
	{
		onMessageSending.fire(message);
		outgoingDAG.send(message);
	}
	
	public void messageReceived(Message message)
	{
		onMessageReceived.fire(message);
		CommandInterpreter.processMessage(message);
	}
	
	public DAGService()
	{
		onConnect = new EventTrigger<>();
		onDisconnect = new EventTrigger<>();
		onMessageReceived = new EventTrigger<>();
		onMessageSending = new EventTrigger<>();
		incommingDAG = new MessageDAG<>();
		outgoingDAG = new MessageDAG<>();
	}
	
	public abstract void connect() throws Exception;
	public abstract void disconnect() throws Exception;
		
}
