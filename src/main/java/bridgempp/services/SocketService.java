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
	private boolean protoXMLCarry = false;

	private static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.HTML,
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
			for (SocketClient client : connectedSockets.values()) {
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
			out.write(((protoXMLCarry ? "<message>" : "") + message.toComplexString(getSupportedMessageFormats()) + (protoXMLCarry ? "</message>\n"
					: "\n")).getBytes("UTF-8"));
		} catch (IOException ex) {
			Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
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
		if (message.getPlainTextMessage().toLowerCase().startsWith("!protoxmlcarry")) {
			protoXMLCarry = true;
			return;
		}
		message.getSender().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
	}

	class ServerListener implements Runnable {

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
				}
			} catch (IOException ex) {
				Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
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
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(),
						"UTF-8"));
				while (true) {
					if (protoXMLCarry) {
						String buffer = "";
						do {
							String line = bufferedReader.readLine();
							if(line == null)
							{
								throw new IOException("End of Stream");
							}
							buffer += line + "\n";
						} while (bufferedReader.ready());
						buffer = buffer.trim();
						Matcher matcher = Pattern.compile("(?<=<message>).+?(?=<\\/message>)", Pattern.DOTALL).matcher(buffer);
						while (matcher.find()) {
							Message message = Message.parseMessage(matcher.group());
							message.setSender(endpoint);
							CommandInterpreter.processMessage(message);
						}
					} else {
						CommandInterpreter.processMessage(new Message(endpoint, bufferedReader.readLine(),
								MessageFormat.PLAIN_TEXT));
					}
				}

			} catch (IOException ex) {
				Logger.getLogger(SocketService.class.getName()).log(Level.SEVERE, null, ex);
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
}
