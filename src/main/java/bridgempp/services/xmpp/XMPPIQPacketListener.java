package bridgempp.services.xmpp;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;

public class XMPPIQPacketListener implements PacketListener {

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
	public void processPacket(Packet packet) throws NotConnectedException {
		BOB.BOBIQ iq = (BOB.BOBIQ) packet;
		String hash = iq.hash;
		String data = this.xmppService.cachedObjects.get(hash);
		BOB.BOBIQ reply = new BOB.BOBIQ();
		reply.hash = hash;
		reply.data = data;
		reply.setType(IQ.Type.RESULT);
		reply.setTo(iq.getFrom());
		reply.setPacketID(iq.getPacketID());
		reply.setFrom(iq.getTo());
		this.xmppService.connection.sendPacket(reply);
	}

}