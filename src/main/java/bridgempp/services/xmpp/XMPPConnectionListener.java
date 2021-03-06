package bridgempp.services.xmpp;

import java.util.logging.Level;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import bridgempp.ShadowManager;

final class XMPPConnectionListener implements ConnectionListener {
	@Override
	public void reconnectionSuccessful() {
		ShadowManager.info("XMPP Service has reconnected");
	}

	@Override
	public void reconnectionFailed(Exception e) {
		ShadowManager.fatal("XMPP Service could not reconnect", e);
	}

	@Override
	public void reconnectingIn(int seconds) {
		ShadowManager.info("XMPP Service will reconnect in " + seconds);
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		ShadowManager.fatal("The XMPP Connection has been disconnected: " + e.getMessage());
	}

	@Override
	public void connectionClosed() {
		ShadowManager.log(Level.WARNING, "XMPP Service has disconnected");
	}

	@Override
	public void connected(XMPPConnection connection) {
		ShadowManager.info("XMPP Service has connected");
	}

	@Override
	public void authenticated(XMPPConnection connection, boolean resumed) {
		ShadowManager.info("XMPP Service has logged in. " + (resumed?"Resumed connection.":""));
	}
}