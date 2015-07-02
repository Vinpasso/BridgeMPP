package bridgempp.services.xmpp;

import java.util.logging.Level;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import bridgempp.ShadowManager;

class XMPPRosterListener implements PacketListener {

	/**
	 * 
	 */
	private final XMPPService xmppService;

	/**
	 * @param xmppService
	 */
	XMPPRosterListener(XMPPService xmppService) {
		this.xmppService = xmppService;
	}

	@Override
	public void processPacket(Packet packet) throws SmackException.NotConnectedException {
		try {
			Presence presence = (Presence) packet;
			Presence subscribed = new Presence(Presence.Type.subscribed);
			subscribed.setTo(packet.getFrom());
			xmppService.connection.sendPacket(subscribed);
			xmppService.connection.getRoster().createEntry(presence.getFrom(), presence.getFrom(), null);
			Presence subscribeRequest = new Presence(Presence.Type.subscribe);
			subscribeRequest.setTo(packet.getFrom());
			xmppService.connection.sendPacket(subscribeRequest);
		} catch (SmackException.NotLoggedInException | SmackException.NoResponseException
				| XMPPException.XMPPErrorException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}
}