package bridgempp.services.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.log.Log;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
import bridgempp.service.MultiBridgeServiceHandle;
import bridgempp.services.socket.SocketService.ProtoCarry;
import bridgempp.state.Event;
import bridgempp.state.EventManager;

@Entity(name = "SOCKET_HANDLE")
@DiscriminatorValue("SOCKET_HANDLE")
class SocketClient extends MultiBridgeServiceHandle<SocketService, SocketClient> implements Runnable
{

	transient User user;

	/**
	 * 
	 */
	transient Socket socket;
	transient ProtoCarry protoCarry = ProtoCarry.None;
	transient boolean running = true;
	transient Thread thread;

	public SocketClient(SocketService socketService, Socket socket, User user, Endpoint endpoint)
	{
		super(endpoint, socketService);
		this.socket = socket;
		this.user = user;
	}

	/**
	 * JPA Constructor
	 */
	@SuppressWarnings("unused")
	private SocketClient()
	{
		super();
	}

	public void connect()
	{
		thread = new Thread(this, "Socket TCP Connection " + endpoint.getIdentifier());
		thread.start();
	}

	@Override
	public void run()
	{
		Log.log(Level.INFO, "TCP client has connected");
		EventManager.fireEvent(Event.ENDPOINT_CONNECTED, endpoint);
		try
		{
			int initialProtocol = socket.getInputStream().read();
			if (initialProtocol >= 0x30)
			{
				initialProtocol -= 0x30;
			}
			if (initialProtocol >= ProtoCarry.values().length)
			{
				throw new IOException("Unknown Protocol");
			}
			if (initialProtocol < 0)
			{
				throw new IOException("Connection closed");
			}
			protoCarry = ProtoCarry.values()[initialProtocol];
			Log.log(Level.INFO, "TCP client is using Protocol: " + protoCarry.toString());
			BufferedReader bufferedReader = null;
			if (protoCarry == ProtoCarry.ProtoBuf)
			{

			} else
			{
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			}
			while (running)
			{
				switch (protoCarry)
				{
					case ProtoBuf:
						bridgempp.services.socket.protobuf.Message protoMessage = bridgempp.services.socket.protobuf.Message.parseDelimitedFrom(socket.getInputStream());
						if (protoMessage == null)
						{
							throw new IOException("Failed to decode next ProtoBuf Message");
						}
						MessageBuilder messageBuilder = new MessageBuilder(user, endpoint);

						ProtoBufUtils.parseMessage(protoMessage, messageBuilder);
						service.receiveMessage(messageBuilder.build());
						break;
					case Plain_Text:
						String messageLine = bufferedReader.readLine();
						if (messageLine == null)
						{
							disconnect();
							break;
						}
						CommandInterpreter.processMessage(new MessageBuilder(user, endpoint).addPlainTextBody(messageLine).build());
						break;
					case None:
						Log.log(Level.SEVERE, "Established Socket has Protocol None, aborting");
						disconnect();
						break;
				}
			}

		} catch (IOException ex)
		{
			Log.log(Level.SEVERE, null, ex);
		}
		disconnect();
	}

	public void disconnect()
	{
		running = false;
		if (socket != null)
		{
			try
			{
				socket.close();
			} catch (IOException e)
			{
				Log.log(Level.INFO, "Could not close Socket on disconnecting.");
			}
		}
		removeHandle();
		EventManager.fireEvent(Event.ENDPOINT_DISCONNECTED, endpoint);
		Log.log(Level.INFO, "TCP client has disconnected");
	}

	@Override
	public void sendMessage(Message message, DeliveryGoal deliveryGoal)
	{
		try
		{

			switch (protoCarry)
			{
				case Plain_Text:
					socket.getOutputStream().write((message.getPlainTextMessageBody() + "\n").getBytes("UTF-8"));
					break;
				case ProtoBuf:
					bridgempp.services.socket.protobuf.Message protoMessage = ProtoBufUtils.serializeMessage(message);
					protoMessage.writeDelimitedTo(socket.getOutputStream());
					break;
				case None:
					Log.log(Level.WARNING, "Message not delivered due to Protocol None: " + message.toString());
					if (thread == null || !thread.isAlive())
					{
						Log.log(Level.WARNING, "Found dead Socket Connection. Will disconnect.");
						disconnect();
					}
					break;
			}
			deliveryGoal.setDelivered();
		} catch (IOException e)
		{
			Log.log(Level.SEVERE, null, e);
			disconnect();
		}
	}

	Endpoint getEndpoint()
	{
		return endpoint;
	}
}