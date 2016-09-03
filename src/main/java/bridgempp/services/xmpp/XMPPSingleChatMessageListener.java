package bridgempp.services.xmpp;

import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;
import org.jivesoftware.smackx.xhtmlim.XHTMLText;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;

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
	public void sendMessage(bridgempp.Message message) {
		try {
			Message sendMessage = new Message();
			if (message.chooseMessageFormat(XMPPService.supportedMessageFormats).equals(MessageFormat.XHTML)) {
				String messageContents = message.toSimpleString(XMPPService.supportedMessageFormats);
				messageContents = service.cacheEmbeddedBase64Image(messageContents);
				XHTMLManager.addBody(sendMessage, new XHTMLText(messageContents, "en-us"));
			}

			sendMessage.addBody(null, message.toSimpleString(MessageFormat.PLAIN_TEXT));
			chat.sendMessage(sendMessage);
		} catch (SmackException.NotConnectedException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}
}