package bridgempp.services.xmpp;

import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostLoad;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.MultiBridgeServiceHandle;

@Entity(name = "XMPPMultiUserChat")
class XMPPMultiUserMessageListener extends MultiBridgeServiceHandle<XMPPService> implements PacketListener
{

	/**
	 * 
	 */
	@Id
	@Column(name = "Identifier", nullable = false, length = 255)
	String multiUserIdentifier;

	@Column(name = "XMPPService", nullable = false)
	private XMPPService xmppService;

	@Column(name = "Endpoint", nullable = false)
	Endpoint endpoint;

	transient MultiUserChat multiUserChat;

	public XMPPMultiUserMessageListener(XMPPService xmppService, MultiUserChat multiUserChat)
	{
		super(DataManager.getOrNewEndpointForIdentifier(multiUserChat.getRoom(), xmppService), xmppService);
		this.multiUserChat = multiUserChat;
	}
	
	// For resumed Chats
	@PostLoad
	public void onLoad()
	{
		try
		{
			multiUserChat = new MultiUserChat(this.xmppService.connection, endpoint.getIdentifier());
			DiscussionHistory discussionHistory = new DiscussionHistory();
			discussionHistory.setMaxStanzas(0);
			multiUserChat.join("BridgeMPP", "", discussionHistory, this.xmppService.connection.getPacketReplyTimeout());
		} catch (XMPPException.XMPPErrorException | SmackException.NoResponseException | SmackException.NotConnectedException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}


	@Override
	public void sendMessage(bridgempp.Message message)
	{
		try
		{
			Message sendMessage = new Message(multiUserChat.getRoom(), Message.Type.groupchat);
			if (message.chooseMessageFormat(XMPPService.supportedMessageFormats).equals(MessageFormat.XHTML))
			{
				String messageContents = message.toSimpleString(XMPPService.supportedMessageFormats);
				messageContents = this.xmppService.cacheEmbeddedBase64Image(messageContents);
				XHTMLManager.addBody(sendMessage, "<body xmlns=\"http://www.w3.org/1999/xhtml\">" + messageContents + "</body>");
			}
			sendMessage.addBody(null, message.toSimpleString(MessageFormat.PLAIN_TEXT));
			multiUserChat.sendMessage(sendMessage);
		} catch (XMPPException | SmackException.NotConnectedException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void processPacket(Packet packet) throws SmackException.NotConnectedException
	{
		Message message = (Message) packet;
		if (message.getType() != Message.Type.groupchat || message.getBody() == null || !message.getFrom().contains(multiUserChat.getRoom() + "/")
				|| message.getFrom().contains(multiUserChat.getRoom() + "/" + multiUserChat.getNickname()))
		{
			return;
		}
		String jid = multiUserChat.getOccupant(message.getFrom()).getJid();
		User user;
		if (jid != null)
		{
			user = DataManager.getOrNewUserForIdentifier(jid.substring(0, jid.indexOf("/")), endpoint);
		} else
		{
			user = DataManager.getOrNewUserForIdentifier(message.getFrom().substring(message.getFrom().indexOf("/")), endpoint);
		}
		xmppService.interpretXMPPMessage(user, endpoint, message);
	}
}