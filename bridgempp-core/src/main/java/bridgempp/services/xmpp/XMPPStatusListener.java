package bridgempp.services.xmpp;

import java.util.Collection;
import java.util.logging.Level;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;

import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.log.Log;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;

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
		Log.log(Level.INFO, "XMPP Roster entries added: " + addresses.toString());
	}

	@Override
	public void entriesUpdated(Collection<String> addresses)
	{
		Log.log(Level.INFO, "XMPP Roster entries updated: " + addresses.toString());
	}

	@Override
	public void entriesDeleted(Collection<String> addresses)
	{
		Log.log(Level.INFO, "XMPP Roster entries removed: " + addresses.toString());
	}

	@Override
	public void presenceChanged(Presence presence)
	{
		Endpoint endpoint = service.getXMPPPresenceEndpoint();
		User user = DataManager.getOrNewUserForIdentifier(presence.getFrom(), endpoint);
		Message message = new MessageBuilder(user, endpoint).addPlainTextBody(presence.getStatus()).build();
		service.receiveMessage(message);
	}

}
