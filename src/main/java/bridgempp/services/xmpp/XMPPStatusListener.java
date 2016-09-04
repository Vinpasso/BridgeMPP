package bridgempp.services.xmpp;

import java.util.Collection;
import java.util.logging.Level;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;

public class XMPPStatusListener implements RosterListener
{

	protected XMPPService service;
	
	public XMPPStatusListener(XMPPService service)
	{
		this.service = service;
	}
	
	@Override
	public void entriesAdded(Collection<String> addresses)
	{
		ShadowManager.log(Level.INFO, "XMPP Roster entries added: " + addresses.toString());
	}

	@Override
	public void entriesUpdated(Collection<String> addresses)
	{
		ShadowManager.log(Level.INFO, "XMPP Roster entries updated: " + addresses.toString());
	}

	@Override
	public void entriesDeleted(Collection<String> addresses)
	{
		ShadowManager.log(Level.INFO, "XMPP Roster entries removed: " + addresses.toString());
	}

	@Override
	public void presenceChanged(Presence presence)
	{
		Endpoint endpoint = service.getXMPPPresenceEndpoint();
		User user = DataManager.getOrNewUserForIdentifier(presence.getFrom(), endpoint);
		Message message = new Message(user, endpoint, presence.getStatus(), MessageFormat.PLAIN_TEXT);
		service.receiveMessage(message);
	}

}
