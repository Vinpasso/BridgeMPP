/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.services.xmpp;

import bridgempp.ServiceManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.log.Log;
import bridgempp.message.MessageBuilder;
import bridgempp.message.formats.media.ImageMessageBody;
import bridgempp.message.formats.text.XHTMLXMPPMessageBody;
import bridgempp.service.SingleToMultiBridgeService;
import bridgempp.state.EventManager;
import bridgempp.state.endpoint.XMPPPresenceEndpointRemover;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration.Builder;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.net.ssl.SSLSocketFactory;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 *
 * @author Vinpasso
 */
@Entity(name = "XMPP_SERVICE")
@DiscriminatorValue(value = "XMPP_SERVICE")
public class XMPPService extends SingleToMultiBridgeService<XMPPService, XMPPHandle>
{
	protected transient XMPPTCPConnection connection;
	protected transient ChatManager chatmanager;
	protected transient HashMap<String, String> cachedObjects;

	@Column(name = "HOST", nullable = false, length = 50)
	String host;

	@Column(name = "PORT", nullable = false)
	int port;

	@Column(name = "XMPP_DOMAIN", nullable = false, length = 50)
	String domain;

	@Column(name = "OLD_STYLE_SSL", nullable = false)
	boolean oldStyleSSL;

	@Column(name = "USERNAME", nullable = false, length = 50)
	String username;

	@Column(name = "PASSWORD", nullable = false, length = 50)
	String password;

	@Column(name = "STATUS_MESSAGE", nullable = false, length = 500)
	String statusMessage;

	public XMPPService()
	{
		cachedObjects = new HashMap<>();

	}

	@Override
	// Arguments <server>; <port>; <domain>; <username>; <password>; <status>;
	// <oldStyleSSL>
	public void connect() throws Exception
	{
		Log.log(Level.INFO, "Starting XMPP Service...");
		if (!handles.containsKey(getXMPPPresenceEndpoint().getIdentifier()))
		{
			addHandle(new XMPPNoOpHandle(getXMPPPresenceEndpoint(), this));
		}

		Builder configurationBuilder = XMPPTCPConnectionConfiguration.builder().setHost(host).setPort(port).setServiceName(domain);
		configurationBuilder.setHostnameVerifier(new DefaultHostnameVerifier());
		Log.log(Level.INFO, "Obtained XMPP Configuration: (" + host + ":" + port + ", " + domain + ", " + username + ")");
		if (oldStyleSSL)
		{
			Log.log(Level.INFO, "Connecting XMPP to " + host + " using old-style SSL");
			configurationBuilder.setSocketFactory(SSLSocketFactory.getDefault());
			// configurationBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.required).setSocketFactory(new
			// DummySSLSocketFactory());
		}
		connection = new XMPPTCPConnection(configurationBuilder.build());
		connection.connect();
		connection.login(username, password);
		sendPresenceUpdate();
		connection.addConnectionListener(new XMPPConnectionListener(this));
		connection.addAsyncStanzaListener(new XMPPRosterListener(this), new StanzaTypeFilter(Presence.class));

		Roster roster = Roster.getInstanceFor(connection);
		roster.setSubscriptionMode(Roster.SubscriptionMode.accept_all);
		roster.addRosterListener(new XMPPStatusListener(this));
		chatmanager = ChatManager.getInstanceFor(connection);
		chatmanager.addChatListener(new XMPPChatListener(this));

		MultiUserChatManager.getInstanceFor(connection).addInvitationListener(new XMPPMultiUserChatListener(this));
		Log.log(Level.INFO, "Started XMPP Service");
		BOB bobIQProcessor = new BOB(this);
		ProviderManager.addIQProvider("data", "urn:xmpp:bob", bobIQProcessor);
		connection.registerIQRequestHandler(bobIQProcessor);

		Iterator<XMPPHandle> iterator = handles.values().iterator();
		while (iterator.hasNext())
		{
			iterator.next().onLoad();
		}
		EventManager.loadEventListenerClass(new XMPPPresenceEndpointRemover());

	}

	public void sendPresenceUpdate() throws NotConnectedException
	{
		Presence presence = new Presence(Presence.Type.available);
		presence.setStatus(statusMessage);
		connection.sendStanza(presence);
	}

	@Override
	public void disconnect()
	{
		Log.log(Level.INFO, "Stopping XMPP Service...");
		connection.disconnect();
		// Prevent Executor services from idling in the background
		connection = null;
		Log.log(Level.INFO, "Stopped XMPP Service...");
	}

	@Override
	public String getName()
	{
		return "XMPP";
	}

	@Override
	public boolean isPersistent()
	{
		return true;
	}

	String cacheEmbeddedBase64Image(ImageMessageBody imageMessageBody)
	{
		Iterator<Entry<String, String>> iterator = cachedObjects.entrySet().iterator();
		while (iterator.hasNext())
		{
			String key = iterator.next().getKey();
			if (System.currentTimeMillis() - Long.parseLong(key.substring(0, key.indexOf('@'))) > 3600000l)
			{
				iterator.remove();
			}
		}

		try
		{
			String data = imageMessageBody.getDataAsBase64();
			String identifier = System.currentTimeMillis() + "@bob.xmpp.org";
			cachedObjects.put(identifier, data);
			return imageMessageBody.getCaption() + "<p/><img src=\"cid:" + identifier + "\"/>";

		} catch (IOException e)
		{
			return imageMessageBody.getCaption() + "<p/><img src=\"" + imageMessageBody.getURL() + "\"/>";
		}
	}

	void interpretXMPPMessage(User user, Endpoint endpoint, Message message)
	{
		if (XHTMLManager.isXHTMLMessage(message))
		{
			receiveMessage(new MessageBuilder(user, endpoint).addPlainTextBody(message.getBody()).addMessageBody(new XHTMLXMPPMessageBody(XHTMLManager.getBodies(message).get(0).toString())).build());
		} else
		{
			receiveMessage(new MessageBuilder(user, endpoint).addPlainTextBody(message.getBody()).build());
		}
	}

	// TEMPORARY DATABASE UPDATE CODE

	public void importFromEndpoint()
	{
		for (Endpoint endpoint : DataManager.getAllEndpoints())
		{
			if (!endpoint.getService().equals(this))
			{
				continue;
			}
			Log.log(Level.INFO, "Loading Endpoint: " + endpoint.toString());
			if (isSingleUserEndpoint(endpoint))
			{
			} else
			{
				MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(connection);
				XMPPMultiUserMessageListener listener = new XMPPMultiUserMessageListener(this, manager.getMultiUserChat(endpoint.getIdentifier()));
				listener.multiUserChat.addMessageListener(listener);
			}
			Log.log(Level.INFO, "Loaded Endpoint: " + endpoint.toString());
		}
	}

	private boolean isSingleUserEndpoint(Endpoint endpoint)
	{
		// Check if a user with Identical ID to Endpoint exists
		User user = DataManager.getUserForIdentifier(endpoint.getIdentifier());
		return user != null || endpoint.getUsers().contains(user);
	}

	public Endpoint getXMPPPresenceEndpoint()
	{
		return DataManager.getOrNewEndpointForIdentifier(host + "@XMPP_Presence", this);
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

	void serviceError(String message, Exception exception)
	{
		ServiceManager.onServiceError(this, message, exception);
	}

}
