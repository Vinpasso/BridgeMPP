package bridgempp.services.xmpp;

import java.util.logging.Level;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;

import bridgempp.Endpoint;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.messageformat.MessageFormat;

class XMPPMultiUserMessageListener implements XMPPMessageListener, PacketListener {

	/**
	 * 
	 */
	private final XMPPService xmppService;
	Endpoint endpoint;
	MultiUserChat multiUserChat;

	public XMPPMultiUserMessageListener(XMPPService xmppService, Chat chat) {
		this.xmppService = xmppService;
		throw new UnsupportedOperationException("Attempted to create multiuserchat from Single Chat");
	}

	// For resumed Chats
	public XMPPMultiUserMessageListener(XMPPService xmppService, Endpoint endpoint) {
		this.xmppService = xmppService;
		try {
			multiUserChat = new MultiUserChat(this.xmppService.connection, endpoint.getTarget());
			DiscussionHistory discussionHistory = new DiscussionHistory();
			discussionHistory.setMaxStanzas(0);
			multiUserChat.join("BridgeMPP", "", discussionHistory, this.xmppService.connection.getPacketReplyTimeout());
			this.endpoint = endpoint;
			this.xmppService.activeChats.put(endpoint.getTarget(), this);
		} catch (XMPPException.XMPPErrorException | SmackException.NoResponseException
				| SmackException.NotConnectedException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	public XMPPMultiUserMessageListener(XMPPService xmppService, MultiUserChat multiUserChat) {
		this.xmppService = xmppService;
		this.multiUserChat = multiUserChat;
		endpoint = new Endpoint(this.xmppService, multiUserChat.getRoom());
		this.xmppService.activeChats.put(endpoint.getTarget(), this);
	}

	@Override
	public void sendMessage(bridgempp.Message message) {
		try {
			Message sendMessage = new Message(multiUserChat.getRoom(), Message.Type.groupchat);
			if (message.chooseMessageFormat(XMPPService.supportedMessageFormats).equals(MessageFormat.XHTML)) {
				String messageContents = message.toSimpleString(XMPPService.supportedMessageFormats);
				messageContents = this.xmppService.cacheEmbeddedBase64Image(messageContents);
				XHTMLManager.addBody(sendMessage, "<body xmlns=\"http://www.w3.org/1999/xhtml\">" + messageContents
						+ "</body>");
			}
			sendMessage.addBody(null, message.toSimpleString(MessageFormat.PLAIN_TEXT));
			multiUserChat.sendMessage(sendMessage);
		} catch (XMPPException | SmackException.NotConnectedException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void processPacket(Packet packet) throws SmackException.NotConnectedException {
		Message message = (Message) packet;
		if (message.getType() != Message.Type.groupchat || message.getBody() == null
				|| !message.getFrom().contains(multiUserChat.getRoom() + "/")
				|| message.getFrom().contains(multiUserChat.getRoom() + "/" + multiUserChat.getNickname())) {
			return;
		}
		String jid = multiUserChat.getOccupant(message.getFrom()).getJid();
		if (jid != null) {
			endpoint.setExtra(jid.substring(0, jid.indexOf("/")));
		} else {
			endpoint.setExtra(message.getFrom().substring(message.getFrom().indexOf("/")));
		}
		MessageFormat messageFormat = MessageFormat.PLAIN_TEXT;
		if(XHTMLManager.getBodies(message) != null)
		{
			messageFormat = MessageFormat.XHTML;
		}
		CommandInterpreter.processMessage(new bridgempp.Message(endpoint, message.getBody(),
				messageFormat));
	}
}