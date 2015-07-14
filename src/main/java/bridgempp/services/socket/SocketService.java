/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import bridgempp.BridgeService;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.messageformat.MessageFormat;
import bridgempp.services.socketservice.protobuf.ProtoBuf;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "SOCKET_SERVICE")
@DiscriminatorValue(value = "SOCKET_SERVICE")
public class SocketService extends BridgeService
{

	transient ServerSocket serverSocket;

	@Column(name = "Listen_Address", nullable = false, length = 50)
	String listenAddress;

	@Column(name = "List_Port", nullable = false)
	int listenPort;

	transient HashMap<String, SocketClient> connectedSockets;
	transient LinkedList<String> pendingDeletion;
	private transient ServerListener serverListener;
	protected transient boolean pendingShutdown = false;

	private transient static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.XHTML, MessageFormat.PLAIN_TEXT };

	@Override
	public void connect()
	{
		ShadowManager.log(Level.INFO, "Loading TCP Server Socket Service...");

		connectedSockets = new HashMap<>();
		pendingDeletion = new LinkedList<String>();
		serverListener = new ServerListener(this);
		new Thread(serverListener, "Socket Server Listener").start();
		ShadowManager.log(Level.INFO, "Loaded TCP Server Socket Service");
	}

	@Override
	public void disconnect()
	{
		try
		{
			pendingShutdown = true;
			@SuppressWarnings("unchecked")
			HashMap<Integer, SocketClient> tempConnected = (HashMap<Integer, SocketClient>) connectedSockets.clone();
			for (SocketClient client : tempConnected.values())
			{
				client.socket.close();
			}
		} catch (IOException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void sendMessage(Message message)
	{
		try
		{
			OutputStream out = connectedSockets.get(Integer.parseInt(message.getDestination().getIdentifier())).socket.getOutputStream();
			ProtoCarry protoCarry = connectedSockets.get(Integer.parseInt(message.getDestination().getIdentifier())).protoCarry;
			switch (protoCarry)
			{
				case Plain_Text:
					out.write((message.toComplexString(getSupportedMessageFormats()) + "\n").getBytes("UTF-8"));
					break;
				case XML_Embedded:
					out.write(("<message>" + message.toComplexString(getSupportedMessageFormats()) + "</message>\n").getBytes("UTF-8"));
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
					protoMessage.writeDelimitedTo(out);
					break;
			}
		} catch (IOException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
			connectedSockets.get(Integer.parseInt(message.getDestination().getIdentifier())).disconnect();
		}
	}

	@Override
	public String getName()
	{
		return "TCPSocket";
	}

	@Override
	public boolean isPersistent()
	{
		return false;
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return supportedMessageFormats;
	}

	public enum ProtoCarry
	{
		Plain_Text, XML_Embedded, ProtoBuf
	}

	public void configure(String listenAddress, int listenPort)
	{
		this.listenAddress = listenAddress;
		this.listenPort = listenPort;
	}
}
