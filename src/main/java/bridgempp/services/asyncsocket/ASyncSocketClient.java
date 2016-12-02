package bridgempp.services.asyncsocket;


import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Future;
import bridgempp.ShadowManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
import bridgempp.service.MultiBridgeServiceHandle;
import bridgempp.services.socket.ProtoBufUtils;

@Entity(name = "ASYNC_SOCKET_HANDLE")
@DiscriminatorValue("ASYNC_SOCKET_HANDLE")
public class ASyncSocketClient extends MultiBridgeServiceHandle<ASyncSocketService, ASyncSocketClient>
{

	private transient SocketChannel socketChannel;

	private transient User user;

	protected ASyncSocketClient()
	{
	}

	public ASyncSocketClient(ASyncSocketService service, Endpoint endpoint, User user, SocketChannel socketChannel)
	{
		super(endpoint, service);
		this.user = user;
		this.socketChannel = socketChannel;

	}

	protected void messageReceived(bridgempp.services.socket.protobuf.Message protoMessage)
	{
		MessageBuilder messageBuilder = new MessageBuilder(user, endpoint);
		ProtoBufUtils.parseMessage(protoMessage, messageBuilder);
		service.receiveMessage(messageBuilder.build());
	}

	public void disconnect()
	{
		if(socketChannel != null)
		{
			socketChannel.close();
		}
		removeHandle();
	}

	@Override
	public void sendMessage(Message message, DeliveryGoal deliveryGoal)
	{
		if(socketChannel == null)
		{
			ShadowManager.log(Level.WARNING, "Attempted to send Message to non connected Socket Handle: " + identifier);
			scheduleRemoveHandle();
			return;
		}
		
		bridgempp.services.socket.protobuf.Message protoMessage = ProtoBufUtils.serializeMessage(message);
		Future<?> future = socketChannel.writeAndFlush(protoMessage);
		future.addListener((e) -> { if(e.isSuccess()) { deliveryGoal.setDelivered(); } });
	}

	public User getUser()
	{
		return user;
	}
}
