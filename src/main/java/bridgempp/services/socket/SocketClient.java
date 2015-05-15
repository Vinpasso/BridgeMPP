package bridgempp.services.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.Endpoint;
import bridgempp.GroupManager;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.messageformat.MessageFormat;
import bridgempp.services.socket.SocketService.ProtoCarry;
import bridgempp.services.socketservice.protobuf.ProtoBuf;

class SocketClient implements Runnable {

	/**
	 * 
	 */
	private final SocketService socketService;
	Socket socket;
	Endpoint endpoint;
	int randomIdentifier;
	ProtoCarry protoCarry = ProtoCarry.Plain_Text;
	boolean running = true;

	public SocketClient(SocketService socketService, Socket socket, Endpoint endpoint) {
		this.socketService = socketService;
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
			while (running) {
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
					String messageLine = bufferedReader.readLine();
					if(messageLine == null)
					{
						disconnect();
						break;
					}
					CommandInterpreter.processMessage(new Message(endpoint, messageLine,
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
		running = false;
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		GroupManager.removeEndpointFromAllGroups(endpoint);
		this.socketService.pendingDeletion.add(randomIdentifier);
		ShadowManager.log(Level.INFO, "TCP client has disconnnected");
	}
}