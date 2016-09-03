package bridgempp.services.xmpp;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;

class XMPPChatListener implements ChatManagerListener {

	/**
	 * 
	 */
	private final XMPPService xmppService;

	/**
	 * @param xmppService
	 */
	XMPPChatListener(XMPPService xmppService) {
		this.xmppService = xmppService;
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		if (!createdLocally) {
			chat.addMessageListener(new XMPPSingleChatMessageListener(xmppService, chat));
		}
	}
}