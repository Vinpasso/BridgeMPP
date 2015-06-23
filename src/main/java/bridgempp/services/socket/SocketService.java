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
import java.util.logging.Logger;

import bridgempp.BridgeService;
import bridgempp.Endpoint;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.messageformat.MessageFormat;
import bridgempp.services.socketservice.protobuf.ProtoBuf;

/**
 *
 * @author Vinpasso
 */
public class SocketService implements BridgeService {

	ServerSocket serverSocket;
	int listenPort;
	String listenAddress;
	HashMap<Integer, SocketClient> connectedSockets;
	LinkedList<Integer> pendingDeletion;
	private ServerListener serverListener;
	protected boolean pendingShutdown = false;

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
		pendingDeletion = new LinkedList<Integer>();
		serverListener = new ServerListener(this);
		new Thread(serverListener, "Socket Server Listener").start();
		ShadowManager.log(Level.INFO, "Loaded TCP Server Socket Service");
	}

	@Override
	public void disconnect() {
		try {
			pendingShutdown  = true;
			@SuppressWarnings("unchecked")
			HashMap<Integer, SocketClient> tempConnected = (HashMap<Integer, SocketClient>) connectedSockets.clone();
			for (SocketClient client : tempConnected.values()) {
				client.socket.close();
			}
		} catch (IOException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
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
				protoMessageBuilder.setMessage(message.getMessage(message.getMessageFormat()));
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
			ShadowManager.log(Level.SEVERE, null, ex);
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

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}

	public enum ProtoCarry {
		Plain_Text, XML_Embedded, ProtoBuf
	}
}
