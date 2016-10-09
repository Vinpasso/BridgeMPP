package bridgempp.services.xmpp;

import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.User;

@Entity(name = "XMPPSINGLEUSERCHAT")
@DiscriminatorValue("XMPPSingleUserChatHandle")
class XMPPSingleChatMessageListener extends XMPPHandle implements ChatMessageListener {

	/**
	 * 
	 */
	transient User user;
	transient Chat chat;

	public XMPPSingleChatMessageListener(XMPPService xmppService, Chat chat) {
		super(DataManager.getOrNewEndpointForIdentifier(chat.getParticipant(), xmppService), xmppService);
		this.chat = chat;
		user = DataManager.getOrNewUserForIdentifier(chat.getParticipant(), endpoint);
	}
	
	protected XMPPSingleChatMessageListener()
	{
		super();
	}
	
	public void onLoad()
	{
		chat = service.chatmanager.createChat(endpoint.getIdentifier(), this);
		chat.addMessageListener(this);
	}

	@Override
	public void processMessage(Chat chat, Message message) {
		service.interpretXMPPMessage(user, endpoint, message);;
	}

	@Override
	public void sendXMPPMessage(Message message) {
		try {
			chat.sendMessage(message);
		} catch (SmackException.NotConnectedException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}
}