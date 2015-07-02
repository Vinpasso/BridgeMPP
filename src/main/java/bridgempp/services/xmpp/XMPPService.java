/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.xmpp;

import bridgempp.BridgeService;
import bridgempp.Endpoint;
import bridgempp.ShadowManager;
import bridgempp.messageformat.MessageFormat;
import bridgempp.services.xmpp.BOB.BOBIQ;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.spark.util.DummySSLSocketFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Vinpasso
 */
public class XMPPService implements BridgeService {

	XMPPTCPConnection connection;
	ChatManager chatmanager;
	HashMap<String, XMPPMessageListener> activeChats;
	HashMap<String, String> cachedObjects;

	static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.XHTML,
			MessageFormat.PLAIN_TEXT };

	public XMPPService() {
		activeChats = new HashMap<>();
		cachedObjects = new HashMap<>();
	}

	@Override
	// Arguments <server>; <port>; <domain>; <username>; <password>; <status>;
	// <oldStyleSSL>
	public void connect(String parameters) {
		try {
			ShadowManager.log(Level.INFO, "Starting XMPP Service...");
			String[] args = parameters.split("; ");
			if (args.length != 7) {
				throw new UnsupportedOperationException("XMPP Configuration Error: " + parameters);
			}
			ConnectionConfiguration configuration = new ConnectionConfiguration(args[0], Integer.parseInt(args[1]),
					args[2]);
			if (Boolean.parseBoolean(args[6])) {
				configuration.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
				configuration.setSocketFactory(new DummySSLSocketFactory());
			}
			connection = new XMPPTCPConnection(configuration);
			connection.connect();
			connection.login(args[3], args[4]);
			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus(args[5]);
			connection.sendPacket(presence);
			connection.addConnectionListener(new XMPPConnectionListener());
			connection.addPacketListener(new XMPPRosterListener(this), new PacketFilter() {

				@Override
				public boolean accept(Packet packet) {
					if (packet instanceof Presence) {
						if (((Presence) packet).getType().equals(Presence.Type.subscribe)) {
							return true;
						}
					}
					return false;
				}

			});
			connection.getRoster().setSubscriptionMode(Roster.SubscriptionMode.manual);
			chatmanager = ChatManager.getInstanceFor(connection);
			chatmanager.addChatListener(new XMPPChatListener(this));
			MultiUserChat.addInvitationListener(connection, new XMPPMultiUserChatListener(this));
			ShadowManager.log(Level.INFO, "Started XMPP Service");
			ProviderManager.addIQProvider("data", "urn:xmpp:bob", new BOB());
			connection.addPacketListener(new XMPPIQPacketListener(this), new PacketFilter() {

				@Override
				public boolean accept(Packet packet) {
					return packet instanceof BOBIQ;
				}
			});
		} catch (XMPPException | SmackException | IOException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}

	}

	@Override
	public void disconnect() {
		try {
			ShadowManager.log(Level.INFO, "Stopping XMPP Service...");
			connection.disconnect();
			// Prevent Executor services from idling in the background
			connection = null;
			ShadowManager.log(Level.INFO, "Stopped XMPP Service...");
		} catch (SmackException.NotConnectedException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void sendMessage(bridgempp.Message message) {
		activeChats.get(message.getTarget().getTarget()).sendMessage(message);
	}

	@Override
	public String getName() {
		return "XMPP";
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public void addEndpoint(Endpoint endpoint) {
		if (endpoint.getExtra().isEmpty()) {
			XMPPSingleChatMessageListener listener = new XMPPSingleChatMessageListener(this, endpoint);
			listener.chat.addMessageListener(listener);
		} else {
			XMPPMultiUserMessageListener listener = new XMPPMultiUserMessageListener(this, endpoint);
			listener.multiUserChat.addMessageListener(listener);
		}
	}

	@Override
	public void interpretCommand(bridgempp.Message message) {
		message.getSender().sendOperatorMessage(getClass().getSimpleName() + ": No supported Protocol options");
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}

	String cacheEmbeddedBase64Image(String messageContents) {
		Matcher matcher = Pattern.compile("<img src=\"data:image\\/jpeg;base64,(.*?)\".*?\\/>").matcher(
				messageContents);
		//cachedObjects.clear();
		for(String key : new HashSet<String>(cachedObjects.keySet()))
		{
			if(System.currentTimeMillis() - Long.parseLong(key.substring(0, key.indexOf('@'))) > 3600000l)
			{
				cachedObjects.remove(key);
			}
		}
		while (matcher.find()) {
			String data = matcher.group(1);
			String identifier = System.currentTimeMillis() + "@bob.xmpp.org";
			messageContents = messageContents
					.replace("data:image/jpeg;base64," + data, "cid:" + identifier);
			cachedObjects.put(identifier, data);
		}
		return messageContents;
	}

}
