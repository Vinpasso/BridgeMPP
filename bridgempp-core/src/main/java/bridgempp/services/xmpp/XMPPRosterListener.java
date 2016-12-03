package bridgempp.services.xmpp;

import java.util.logging.Level;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;

import bridgempp.ShadowManager;

class XMPPRosterListener implements StanzaListener {

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
	public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
		try {
			Presence presence = (Presence) packet;
			if(!presence.getType().equals(Presence.Type.subscribe))
			{
				return;
			}
			
			Presence subscribed = new Presence(Presence.Type.subscribed);
			subscribed.setTo(packet.getFrom());
			xmppService.connection.sendStanza(subscribed);
			Roster.getInstanceFor(xmppService.connection).createEntry(presence.getFrom(), presence.getFrom(), null);
			Presence subscribeRequest = new Presence(Presence.Type.subscribe);
			subscribeRequest.setTo(packet.getFrom());
			xmppService.connection.sendStanza(subscribeRequest);
		} catch (SmackException.NotLoggedInException | SmackException.NoResponseException
				| XMPPException.XMPPErrorException ex) {
			ShadowManager.log(Level.SEVERE, null, ex);
		}
	}
}