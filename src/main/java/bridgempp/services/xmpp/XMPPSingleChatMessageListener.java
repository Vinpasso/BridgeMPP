package bridgempp.services.xmpp;

import java.util.logging.Level;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;

import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;

class XMPPSingleChatMessageListener implements XMPPMessageListener, MessageListener {

	/**
	 * 
	 */
	private final XMPPService xmppService;
	Endpoint endpoint;
	Chat chat;

	// For resumed Chats
	public XMPPSingleChatMessageListener(XMPPService xmppService, Endpoint endpoint) {
		this.xmppService = xmppService;
		this.endpoint = endpoint;
		chat = xmppService.chatmanager.createChat(endpoint.getTarget(), this);
		xmppService.activeChats.put(endpoint.getTarget(), this);
	}

	public XMPPSingleChatMessageListener(XMPPService xmppService, Chat chat) {
		this.xmppService = xmppService;
		this.chat = chat;
		endpoint = new Endpoint(xmppService, chat.getParticipant());
		xmppService.activeChats.put(endpoint.getTarget(), this);
	}

	@Override
	public void processMessage(Chat chat, Message message) {
		xmppService.interpretXMPPMessage(endpoint, message);;
	}

	@Override
	public void sendMessage(bridgempp.Message message) {
		try {
			Message sendMessage = new Message();
			if (message.chooseMessageFormat(XMPPService.supportedMessageFormats).equals(MessageFormat.XHTML)) {
				String messageContents = message.toSimpleString(XMPPService.supportedMessageFormats);
				messageContents = xmppService.cacheEmbeddedBase64Image(messageContents);
				XHTMLManager.addBody(sendMessage, messageContents);
			}

			sendMessage.addBody(null, message.toSimpleString(MessageFormat.PLAIN_TEXT));
			chat.sendMessage(sendMessage);
		} catch (SmackException.NotConnectedException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}
}