/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.BridgeService;
import bridgempp.Endpoint;
import bridgempp.GroupManager;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.messageformat.MessageFormat;
import bridgempp.services.socketservice.protobuf.ProtoBuf;

/**
 *
 * @author Vinpasso
 */
public class SocketService implements BridgeService {

	private ServerSocket serverSocket;
	private int listenPort;
	private String listenAddress;
	private HashMap<Integer, SocketClient> connectedSockets;
	private ServerListener serverListener;
	private ProtoCarry protoCarry = ProtoCarry.Plain_Text;

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] {
			MessageFormat.HTML, MessageFormat.PLAIN_TEXT };

	@Override
	public void connect(String argString) {
		String[] args = argString.split("; ");
		if (args.length != 2) {
			throw new UnsupportedOperationException(
					"Incorrect options for Socket Service: " + argString);
		}
		ShadowManager.log(Level.INFO, "Loading TCP Server Socket Service...");

		listenPort = Integer.parseInt(args[1]);
		listenAddress = args[0];
		connectedSockets = new HashMap<>();
		serverListener = new ServerListener();
		new Thread(serverListener, "Socket Server Listener").start();
		ShadowManager.log(Level.INFO, "Loaded TCP Server Socket Service");
	}

	@Override
	public void disconnect() {
		try {
			serverSocket.close();
			for (SocketClient client : connectedSockets.values()) {
				client.socket.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE,
					null, ex);
		}

	}

	@Override
	public void sendMessage(Message message) {
		try {
			OutputStream out = connectedSockets.get(Integer.parseInt(message
					.getTarget().getTarget())).socket.getOutputStream();
			switch(protoCarry)
			{
			case Plain_Text:
				out.write((message.toComplexString(getSupportedMessageFormats()) + "\n").getBytes("UTF-8"));
				break;
			case XML_Embedded:
				out.write(("<message>" + message.toComplexString(getSupportedMessageFormats()) + "</message>\n").getBytes("UTF-8"));
				break;
			case ProtoBuf:
				ProtoBuf.Message protoMessage = ProtoBuf.Message.newBuilder().setMessageFormat(message.getMessageFormat().getName()).setMessage(message.getMessage(new MessageFormat[] {message.getMessageFormat()})).setGroup(message.getGroup().getName()).build();
				protoMessage.writeDelimitedTo(out);
				break;
			}
		} catch (IOException ex) {
			Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	@Override
	public String getName() {
		return "TCPSocket";
	}

	@Override
	public boolean isPersistent() {
		return false;
	}

	// Non Persistent Service. Adding an endpoint from save does nothing
	@Override
	public void addEndpoint(Endpoint endpoint) {
	}

	@Override
	public void interpretCommand(Message message) {
		String command = message.getPlainTextMessage().toLowerCase();
		if (command.contains(" ")) {
			command = command.substring(0, command.indexOf(" "));
		}
		switch (command) {
		case "!protocarry":
			try
			{
				protoCarry = ProtoCarry.valueOf(message.getPlainTextMessage().substring(command.length() + 1));
			}
			catch(Exception e)
			{
				message.getSender().sendOperatorMessage("Please specifiy a valid Protocol Carry");
			}
			break;
		case "!protoplaintextcarry":
			protoCarry = ProtoCarry.Plain_Text;
			break;
		case "!protoxmlcarry":
			protoCarry = ProtoCarry.XML_Embedded;
			break;
		case "!protoprotobufcarry":
			protoCarry = ProtoCarry.ProtoBuf;
			break;
		default:
			message.getSender().sendOperatorMessage(
					getClass().getSimpleName()
							+ ": No supported Protocol options");
			break;
		}
	}

	class ServerListener implements Runnable {

		@Override
		public void run() {
			ShadowManager
					.log(Level.INFO, "Starting TCP Server Socket Listener");

			try {
				serverSocket = new ServerSocket(listenPort, 10,
						InetAddress.getByName(listenAddress));
				serverSocket.setSoTimeout(5000);
				while (!serverSocket.isClosed()) {
					try {
						int randomIdentifier;
						do {
							randomIdentifier = new Random()
									.nextInt(Integer.MAX_VALUE);
						} while (connectedSockets.containsKey(randomIdentifier));

						Socket socket = serverSocket.accept();
						SocketClient socketClient = new SocketClient(socket,
								new Endpoint(SocketService.this,
										randomIdentifier + ""));
						socketClient.randomIdentifier = randomIdentifier;
						connectedSockets.put(randomIdentifier, socketClient);
						new Thread(socketClient, "Socket TCP Connection")
								.start();
					} catch (SocketTimeoutException e) {
					}
				}
			} catch (IOException ex) {
				Logger.getLogger(SocketService.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}

	}

	class SocketClient implements Runnable {

		Socket socket;
		Endpoint endpoint;
		int randomIdentifier;

		public SocketClient(Socket socket, Endpoint endpoint) {
			this.socket = socket;
			this.endpoint = endpoint;
		}

		@Override
		public void run() {
			ShadowManager.log(Level.INFO, "TCP client has connected");
			try {
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(socket.getInputStream(), "UTF-8"));
				while (true) {
					switch(protoCarry)
					{
					case ProtoBuf:
						ProtoBuf.Message protoMessage = ProtoBuf.Message.parseDelimitedFrom(socket.getInputStream());
						Message bridgeMessage = new Message(endpoint, protoMessage.getMessage(), MessageFormat.parseMessageFormat(protoMessage.getMessageFormat()));
						if(protoMessage.hasGroup())
						{
							bridgeMessage.setGroup(GroupManager.findGroup(protoMessage.getGroup()));
						}
						CommandInterpreter.processMessage(bridgeMessage);
						break;
					case XML_Embedded:
						String buffer = "";
						do {
							String line = bufferedReader.readLine();
							if (line == null) {
								throw new IOException("End of Stream");
							}
							buffer += line + "\n";
						} while (bufferedReader.ready());
						buffer = buffer.trim();
						Matcher matcher = Pattern.compile(
								"(?<=<message>).+?(?=<\\/message>)",
								Pattern.DOTALL).matcher(buffer);
						while (matcher.find()) {
							Message message = Message.parseMessage(matcher
									.group());
							message.setSender(endpoint);
							CommandInterpreter.processMessage(message);
						}
						break;
					case Plain_Text:
						CommandInterpreter.processMessage(new Message(endpoint,
								bufferedReader.readLine(),
								MessageFormat.PLAIN_TEXT));
						break;
					}
				}

			} catch (IOException ex) {
				Logger.getLogger(SocketService.class.getName()).log(
						Level.SEVERE, null, ex);
			}
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			GroupManager.removeEndpointFromAllGroups(endpoint);
			connectedSockets.remove(randomIdentifier);
			ShadowManager.log(Level.INFO, "TCP client has disconnnected");
		}
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}
	
	public enum ProtoCarry
	{
		Plain_Text,
		XML_Embedded,
		ProtoBuf
	}
}
