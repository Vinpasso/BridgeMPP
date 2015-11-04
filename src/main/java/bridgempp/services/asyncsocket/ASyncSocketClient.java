package bridgempp.services.asyncsocket;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import io.netty.channel.socket.SocketChannel;
import bridgempp.GroupManager;
import bridgempp.Message;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.MultiBridgeServiceHandle;
import bridgempp.services.socketservice.protobuf.ProtoBuf;

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

	protected void messageReceived(bridgempp.services.socketservice.protobuf.ProtoBuf.Message protoMessage)
	{
		if(protoMessage.getMessage().length() == 0)
		{
			return;
		}
		Message message = new Message();
		message.setMessageFormat(MessageFormat.parseMessageFormat(protoMessage.getMessageFormat()));
		message.setMessage(protoMessage.getMessage());
		message.setSender(user);
		message.setOrigin(endpoint);
		message.setGroup(GroupManager.findGroup(protoMessage.getGroup()));
		CommandInterpreter.processMessage(message);
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
	public void sendMessage(Message message)
	{
		ProtoBuf.Message.Builder protoMessageBuilder = ProtoBuf.Message.newBuilder();
		protoMessageBuilder.setMessageFormat(message.getMessageFormat().getName());
		protoMessageBuilder.setMessage(message.getMessage(message.getMessageFormat()));
		if (message.getGroup() != null)
		{
			protoMessageBuilder.setGroup(message.getGroup().getName());
		}
		if (message.getSender() != null)
		{
			protoMessageBuilder.setSender(message.getSender().toString());
		}
		if (message.getDestination() != null)
		{
			protoMessageBuilder.setTarget(message.getDestination().toString());
		}
		ProtoBuf.Message protoMessage = protoMessageBuilder.build();

		socketChannel.writeAndFlush(protoMessage);
	}

	public User getUser()
	{
		return user;
	}
}
