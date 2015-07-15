/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.xmpp;

import bridgempp.BridgeService;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.services.xmpp.BOB.BOBIQ;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;
import org.jivesoftware.spark.util.DummySSLSocketFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "XMPP_SERVICE")
@DiscriminatorValue(value = "XMPP_SERVICE")
public class XMPPService extends BridgeService {

	transient XMPPTCPConnection connection;
	transient ChatManager chatmanager;
	transient HashMap<String, XMPPMessageListener> activeChats;
	transient HashMap<String, String> cachedObjects;
	
	@Column(name = "HOST", nullable = false, length = 50)
	String host;
	
	@Column(name = "PORT", nullable = false)
	int port;
	
	@Column(name = "DOMAIN", nullable = false, length = 50)
	String domain;
	
	@Column(name = "OLD_STYLE_SSL", nullable = false)
	boolean oldStyleSSL;
	
	@Column(name = "USERNAME", nullable = false, length = 50)
	String username;
	
	@Column(name = "PASSWORD", nullable = false, length = 50)
	String password;
	
	@Column(name = "STATUS_MESSAGE", nullable = false, length = 50)
	String statusMessage;

	static MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.XHTML,
			MessageFormat.PLAIN_TEXT };

	public XMPPService() {
		activeChats = new HashMap<>();
		cachedObjects = new HashMap<>();
	}

	@Override
	// Arguments <server>; <port>; <domain>; <username>; <password>; <status>;
	// <oldStyleSSL>
	public void connect() {
		try {
			ShadowManager.log(Level.INFO, "Starting XMPP Service...");
			ConnectionConfiguration configuration = new ConnectionConfiguration(host, port, domain);
			if (oldStyleSSL) {
				configuration.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
				configuration.setSocketFactory(new DummySSLSocketFactory());
			}
			connection = new XMPPTCPConnection(configuration);
			connection.connect();
			connection.login(username, password);
			Presence presence = new Presence(Presence.Type.available);
			presence.setStatus(statusMessage);
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
		if(endpoints == null)
		{
			endpoints = new LinkedList<Endpoint>();
		}
		Iterator<Endpoint> iterator = endpoints.iterator();
		while(iterator.hasNext())
		{
			Endpoint endpoint = iterator.next();
			addEndpoint(endpoint);
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
		activeChats.get(message.getDestination().getIdentifier()).sendMessage(message);
	}

	@Override
	public String getName() {
		return "XMPP";
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	public void addEndpoint(Endpoint endpoint) {
		if (endpoint.getUsers().size() <= 1) {
			XMPPSingleChatMessageListener listener = new XMPPSingleChatMessageListener(this, endpoint);
			listener.chat.addMessageListener(listener);
		} else {
			XMPPMultiUserMessageListener listener = new XMPPMultiUserMessageListener(this, endpoint);
			listener.multiUserChat.addMessageListener(listener);
		}
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

	void interpretXMPPMessage(User user, Endpoint endpoint, Message message) {
		if (XHTMLManager.isXHTMLMessage(message)) {
			CommandInterpreter.processMessage(new bridgempp.Message(user, endpoint, XHTMLManager.getBodies(message).get(0),
					MessageFormat.XHTML));
		} else {
			CommandInterpreter.processMessage(new bridgempp.Message(user, endpoint, message.getBody(),
					MessageFormat.PLAIN_TEXT));
		}
	}

	public void configure(String host, int port, String domain, boolean oldStyleSSL, String username, String password, String statusMessage)
	{
		this.host = host;
		this.port = port;
		this.domain = domain;
		this.oldStyleSSL = oldStyleSSL;
		this.username = username;
		this.password = password;
		this.statusMessage = statusMessage;
	}

}
