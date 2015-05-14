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

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.XHTML,
			MessageFormat.PLAIN_TEXT };

	@Override
	public void connect(String argString) {
		String[] args = argString.split("; ");
		if (args.length != 2) {
			throw new UnsupportedOperationException("Incorrect options for Socket Service: " + argString);
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
			@SuppressWarnings("unchecked")
			HashMap<Integer, SocketClient> tempConnected = (HashMap<Integer, SocketClient>) connectedSockets.clone();
			for (SocketClient client : tempConnected.values()) {
				client.socket.close();
			}
		} catch (IOException ex) {
			Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void sendMessage(Message message) {
		try {
			OutputStream out = connectedSockets.get(Integer.parseInt(message.getTarget().getTarget())).socket
					.getOutputStream();
			ProtoCarry protoCarry = connectedSockets.get(Integer.parseInt(message.getTarget().getTarget())).protoCarry;
			switch (protoCarry) {
			case Plain_Text:
				out.write((message.toComplexString(getSupportedMessageFormats()) + "\n").getBytes("UTF-8"));
				break;
			case XML_Embedded:
				out.write(("<message>" + message.toComplexString(getSupportedMessageFormats()) + "</message>\n")
						.getBytes("UTF-8"));
				break;
			case ProtoBuf:
				ProtoBuf.Message.Builder protoMessageBuilder = ProtoBuf.Message.newBuilder();
				protoMessageBuilder.setMessageFormat(message.getMessageFormat().getName());
				protoMessageBuilder.setMessage(message.getMessage(new MessageFormat[] { message.getMessageFormat() }));
				if (message.getGroup() != null) {
					protoMessageBuilder.setGroup(message.getGroup().getName());
				}
				if (message.getSender() != null) {
					protoMessageBuilder.setSender(message.getSender().toString());
				}
				if (message.getTarget() != null) {
					protoMessageBuilder.setTarget(message.getTarget().toString());
				}
				ProtoBuf.Message protoMessage = protoMessageBuilder.build();
				protoMessage.writeDelimitedTo(out);
				break;
			}
		} catch (IOException ex) {
			Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
			connectedSockets.get(Integer.parseInt(message.getTarget().getTarget())).disconnect();
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
			try {
				connectedSockets.get(Integer.parseInt(message.getSender().getTarget())).protoCarry = ProtoCarry
						.valueOf(message.getPlainTextMessage().substring(command.length() + 1));
			} catch (Exception e) {
				message.getSender().sendOperatorMessage("Please specifiy a valid Protocol Carry");
			}
			break;
		case "!protoplaintextcarry":
			connectedSockets.get(Integer.parseInt(message.getSender().getTarget())).protoCarry = ProtoCarry.Plain_Text;
			break;
		case "!protoxmlcarry":
			connectedSockets.get(Integer.parseInt(message.getSender().getTarget())).protoCarry = ProtoCarry.XML_Embedded;
			break;
		case "!protoprotobufcarry":
			connectedSockets.get(Integer.parseInt(message.getSender().getTarget())).protoCarry = ProtoCarry.ProtoBuf;
			break;
		default:
			message.getSender().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
			break;
		}
	}

	class ServerListener implements Runnable {
		private long lastKeepAlive = 0;

		@Override
		public void run() {
			ShadowManager.log(Level.INFO, "Starting TCP Server Socket Listener");

			try {
				serverSocket = new ServerSocket(listenPort, 10, InetAddress.getByName(listenAddress));
				serverSocket.setSoTimeout(5000);
				while (!serverSocket.isClosed()) {
					try {
						int randomIdentifier;
						do {
							randomIdentifier = new Random().nextInt(Integer.MAX_VALUE);
						} while (connectedSockets.containsKey(randomIdentifier));

						Socket socket = serverSocket.accept();
						SocketClient socketClient = new SocketClient(socket, new Endpoint(SocketService.this,
								randomIdentifier + ""));
						socketClient.randomIdentifier = randomIdentifier;
						connectedSockets.put(randomIdentifier, socketClient);
						new Thread(socketClient, "Socket TCP Connection").start();
					} catch (SocketTimeoutException e) {
					}
					if (System.currentTimeMillis() > lastKeepAlive + 60000) {
						sendKeepAliveMessages();
					}
				}
			} catch (IOException ex) {
				Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		private void sendKeepAliveMessages() {
			for (SocketClient client : connectedSockets.values()) {
				sendMessage(new Message(client.endpoint, client.endpoint, null, "", MessageFormat.PLAIN_TEXT));
			}
			lastKeepAlive = System.currentTimeMillis();
		}

	}

	class SocketClient implements Runnable {

		Socket socket;
		Endpoint endpoint;
		int randomIdentifier;
		ProtoCarry protoCarry = ProtoCarry.Plain_Text;

		public SocketClient(Socket socket, Endpoint endpoint) {
			this.socket = socket;
			this.endpoint = endpoint;
		}

		@Override
		public void run() {
			ShadowManager.log(Level.INFO, "TCP client has connected");
			try {
				int initialProtocol = socket.getInputStream().read();
				if (initialProtocol >= 0x30) {
					initialProtocol -= 0x30;
				}
				protoCarry = ProtoCarry.values()[initialProtocol];
				BufferedReader bufferedReader = null;
				if (protoCarry == ProtoCarry.ProtoBuf) {

				} else {
					bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				}
				while (true) {
					switch (protoCarry) {
					case ProtoBuf:
						ProtoBuf.Message protoMessage = ProtoBuf.Message.parseDelimitedFrom(socket.getInputStream());
						Message bridgeMessage = new Message(endpoint, protoMessage.getMessage(),
								MessageFormat.parseMessageFormat(protoMessage.getMessageFormat()));
						if (protoMessage.hasGroup()) {
							bridgeMessage.setGroup(GroupManager.findGroup(protoMessage.getGroup()));
						}
						if (bridgeMessage.getMessageFormat() == null) {
							bridgeMessage.setMessageFormat(MessageFormat.PLAIN_TEXT);
						}
						if (bridgeMessage.getMessageRaw() == null || bridgeMessage.getMessageRaw().length() == 0) {
							Logger.getLogger(SocketService.class.getName()).log(Level.INFO,
									"Received PING empty message from " + endpoint.toString(true));
							break;
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
						Matcher matcher = Pattern.compile("(?<=<message>).+?(?=<\\/message>)", Pattern.DOTALL).matcher(
								buffer);
						while (matcher.find()) {
							Message message = Message.parseMessage(matcher.group());
							message.setSender(endpoint);
							CommandInterpreter.processMessage(message);
						}
						break;
					case Plain_Text:
						CommandInterpreter.processMessage(new Message(endpoint, bufferedReader.readLine(),
								MessageFormat.PLAIN_TEXT));
						break;
					}
				}

			} catch (IOException ex) {
				Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
			}
			disconnect();
		}

		public void disconnect() {
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

	public enum ProtoCarry {
		Plain_Text, XML_Embedded, ProtoBuf
	}
}
