package bridgempp.services.xmpp;

import java.util.logging.Level;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import bridgempp.log.Log;

final class XMPPConnectionListener implements ConnectionListener {
	private XMPPService service;
	
	public XMPPConnectionListener(XMPPService service)
	{
		this.service = service;
	}
	
	@Override
	public void reconnectionSuccessful() {
		Log.info("XMPP Service has reconnected");
	}

	@Override
	public void reconnectionFailed(Exception e) {
		service.serviceError("XMPP Service could not reconnect", e);
	}

	@Override
	public void reconnectingIn(int seconds) {
		Log.info("XMPP Service will reconnect in " + seconds);
	}

	@Override
	public void connectionClosedOnError(Exception e) {
		service.serviceError("The XMPP Connection has been disconnected.", e);
	}

	@Override
	public void connectionClosed() {
		Log.log(Level.WARNING, "XMPP Service has disconnected");
	}

	@Override
	public void connected(XMPPConnection connection) {
		Log.info("XMPP Service has connected");
	}

	@Override
	public void authenticated(XMPPConnection connection, boolean resumed) {
		Log.info("XMPP Service has logged in. " + (resumed?"Resumed connection.":""));
	}
}