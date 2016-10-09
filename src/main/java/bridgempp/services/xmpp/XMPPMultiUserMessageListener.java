package bridgempp.services.xmpp;

import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.Occupant;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.User;

@Entity(name = "XMPPMULTIUSERCHAT")
@DiscriminatorValue("XMPPMultiUserChatHandle")
class XMPPMultiUserMessageListener extends XMPPHandle implements MessageListener
{

	/**
	 * 
	 */

	transient MultiUserChat multiUserChat;

	public XMPPMultiUserMessageListener(XMPPService xmppService, MultiUserChat multiUserChat)
	{
		super(DataManager.getOrNewEndpointForIdentifier(multiUserChat.getRoom(), xmppService), xmppService);
		this.multiUserChat = multiUserChat;
	}
	
	protected XMPPMultiUserMessageListener()
	{
		super();
	}
	
	// For resumed Chats
	public void onLoad()
	{
		ShadowManager.log(Level.INFO, "Resumed XMPP Multi User Chat from Handle: " + endpoint.getIdentifier());
		try
		{
			MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(service.connection);
			multiUserChat = manager.getMultiUserChat(endpoint.getIdentifier());
			DiscussionHistory discussionHistory = new DiscussionHistory();
			discussionHistory.setMaxStanzas(0);
			multiUserChat.join("BridgeMPP", "", discussionHistory, service.connection.getPacketReplyTimeout());
			multiUserChat.addMessageListener(this);
		} catch (XMPPException.XMPPErrorException | SmackException.NoResponseException | SmackException.NotConnectedException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}


	@Override
	public void sendXMPPMessage(Message message)
	{
		try
		{
			multiUserChat.sendMessage(message);
		} catch (SmackException.NotConnectedException ex)
		{
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void processMessage(Message message)
	{
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
		Occupant sender = multiUserChat.getOccupant(message.getFrom());
		if(sender.getNick() != null && !user.hasAlias())
		{
			user.setName(sender.getNick());
		}
		service.interpretXMPPMessage(user, endpoint, message);
	}
}