package bridgempp.services.xmpp;

import java.util.logging.Level;

import javax.persistence.PostLoad;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;

import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.MultiBridgeServiceHandle;

class XMPPSingleChatMessageListener extends MultiBridgeServiceHandle<XMPPService> implements MessageListener {

	/**
	 * 
	 */
	User user;
	Chat chat;

	public XMPPSingleChatMessageListener(XMPPService xmppService, Chat chat) {
		super(DataManager.getOrNewEndpointForIdentifier(chat.getParticipant(), xmppService), xmppService);
		this.chat = chat;
		user = DataManager.getOrNewUserForIdentifier(chat.getParticipant(), endpoint);
	}
	
	@PostLoad
	public void onLoad()
	{
		chat = service.chatmanager.createChat(endpoint.getIdentifier(), this);
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
				XHTMLManager.addBody(sendMessage, messageContents);
			}

			sendMessage.addBody(null, message.toSimpleString(MessageFormat.PLAIN_TEXT));
			chat.sendMessage(sendMessage);
		} catch (SmackException.NotConnectedException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}
}