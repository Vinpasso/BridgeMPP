package bridgempp.services.xmpp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

class XMPPMultiUserChatListener implements InvitationListener {

	/**
	 * 
	 */
	private final XMPPService xmppService;

	/**
	 * @param xmppService
	 */
	XMPPMultiUserChatListener(XMPPService xmppService) {
		this.xmppService = xmppService;
	}

	@Override
	public void invitationReceived(XMPPConnection conn, String room, String inviter, String reason,
			String password, Message message) {
		try {
			MultiUserChat multiUserChat = new MultiUserChat(conn, room);
			DiscussionHistory discussionHistory = new DiscussionHistory();
			discussionHistory.setMaxStanzas(0);
			multiUserChat.join("BridgeMPP", password, discussionHistory, conn.getPacketReplyTimeout());
			multiUserChat.addMessageListener(new XMPPMultiUserMessageListener(xmppService, multiUserChat));
		} catch (XMPPException.XMPPErrorException | SmackException.NoResponseException
				| SmackException.NotConnectedException ex) {
			Logger.getLogger(XMPPService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}