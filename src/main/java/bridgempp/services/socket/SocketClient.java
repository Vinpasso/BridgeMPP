package bridgempp.services.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.GroupManager;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.MultiBridgeServiceHandle;
import bridgempp.services.socket.SocketService.ProtoCarry;
import bridgempp.services.socketservice.protobuf.ProtoBuf;
import bridgempp.state.EventManager;
import bridgempp.state.EventManager.Event;

class SocketClient extends MultiBridgeServiceHandle<SocketService> implements Runnable
{

	/**
	 * 
	 */
	Socket socket;
	User user;
	Endpoint endpoint;
	String randomIdentifier;
	ProtoCarry protoCarry = ProtoCarry.None;
	boolean running = true;

	public SocketClient(SocketService socketService, Socket socket, User user, Endpoint endpoint)
	{
		super(endpoint, socketService);
		this.socket = socket;
		this.user = user;
	}

	@Override
	public void run()
	{
		ShadowManager.log(Level.INFO, "TCP client has connected");
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
			protoCarry = ProtoCarry.values()[initialProtocol];
			ShadowManager.log(Level.INFO, "TCP client is using Protocol: " + protoCarry.toString());
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
						ProtoBuf.Message protoMessage = ProtoBuf.Message.parseDelimitedFrom(socket.getInputStream());
						if (protoMessage == null)
						{
							throw new IOException("Failed to decode next ProtoBuf Message");
						}
						Message bridgeMessage = new Message(user, endpoint, protoMessage.getMessage(), MessageFormat.parseMessageFormat(protoMessage.getMessageFormat()));
						if (bridgeMessage.getMessageRaw() == null || bridgeMessage.getMessageRaw().length() == 0)
						{
							break;
						}
						if (protoMessage.hasGroup())
						{
							bridgeMessage.setGroup(GroupManager.findGroup(protoMessage.getGroup()));
						}
						if (bridgeMessage.getMessageFormat() == null)
						{
							bridgeMessage.setMessageFormat(MessageFormat.PLAIN_TEXT);
						}
						CommandInterpreter.processMessage(bridgeMessage);
						break;
					case XML_Embedded:
						String buffer = "";
						do
						{
							String line = bufferedReader.readLine();
							if (line == null)
							{
								throw new IOException("End of Stream");
							}
							buffer += line + "\n";
						} while (bufferedReader.ready());
						buffer = buffer.trim();
						Matcher matcher = Pattern.compile("(?<=<message>).+?(?=<\\/message>)", Pattern.DOTALL).matcher(buffer);
						while (matcher.find())
						{
							Message message = Message.parseMessage(matcher.group());
							message.setOrigin(endpoint);
							CommandInterpreter.processMessage(message);
						}
						break;
					case Plain_Text:
						String messageLine = bufferedReader.readLine();
						if (messageLine == null)
						{
							disconnect();
							break;
						}
						CommandInterpreter.processMessage(new Message(user, endpoint, messageLine, MessageFormat.PLAIN_TEXT));
						break;
					case None:
						ShadowManager.log(Level.SEVERE, "Established Socket has Protocol None, aborting");
						disconnect();
						break;
				}
			}

		} catch (IOException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
		disconnect();
	}

	public void disconnect()
	{
		running = false;
		try
		{
			socket.close();
		} catch (IOException e)
		{
			ShadowManager.log(Level.INFO, "Could not close Socket on disconnecting.");
		}
		EventManager.fireEvent(Event.ENDPOINT_DISCONNECTED, endpoint);
		DataManager.removeState(this);
		ShadowManager.log(Level.INFO, "TCP client has disconnected");
	}

	@Override
	public void sendMessage(Message message)
	{
		try
		{

			switch (protoCarry)
			{
				case Plain_Text:
					socket.getOutputStream().write((message.toComplexString(service.getSupportedMessageFormats()) + "\n").getBytes("UTF-8"));
					break;
				case XML_Embedded:
					socket.getOutputStream().write(("<message>" + message.toComplexString(service.getSupportedMessageFormats()) + "</message>\n").getBytes("UTF-8"));
					break;
				case ProtoBuf:
					ProtoBuf.Message.Builder protoMessageBuilder = ProtoBuf.Message.newBuilder();
					protoMessageBuilder.setMessageFormat(message.getMessageFormat().getName());
					protoMessageBuilder.setMessage(message.getMessage(message.getMessageFormat()));
					if (message.getGroup() != null)
					{
						protoMessageBuilder.setGroup(message.getGroup().getName());
					}
					if (message.getOrigin() != null)
					{
						protoMessageBuilder.setSender(message.getOrigin().toString());
					}
					if (message.getDestination() != null)
					{
						protoMessageBuilder.setTarget(message.getDestination().toString());
					}
					ProtoBuf.Message protoMessage = protoMessageBuilder.build();
					protoMessage.writeDelimitedTo(socket.getOutputStream());
					break;
				case None:
					ShadowManager.log(Level.WARNING, "Message not delivered due to Protocol None: " + message.toString());
					break;
			}
		} catch (IOException e)
		{
			ShadowManager.log(Level.SEVERE, null, e);
			disconnect();
		}
	}
}