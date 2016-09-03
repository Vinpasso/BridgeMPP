package bridgempp.services.xmpp;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;

public class XMPPIQPacketListener implements StanzaListener {

	/**
	 * 
	 */
	private final XMPPService xmppService;

	/**
	 * @param xmppService
	 */
	XMPPIQPacketListener(XMPPService xmppService) {
		this.xmppService = xmppService;
	}

	@Override
	public void processPacket(Stanza packet) throws NotConnectedException {
		BOB.BOBIQ iq = (BOB.BOBIQ) packet;
		String hash = iq.hash;
		String data = this.xmppService.cachedObjects.get(hash);
		BOB.BOBIQ reply = new BOB.BOBIQ();
		reply.hash = hash;
		reply.data = data;
		reply.setType(IQ.Type.result);
		reply.setTo(iq.getFrom());
		reply.setStanzaId(iq.getStanzaId());
		reply.setFrom(iq.getTo());
		this.xmppService.connection.sendStanza(reply);
	}

}