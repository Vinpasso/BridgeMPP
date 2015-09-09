package bridgempp.services.xmpp;

import java.util.Collection;
import java.util.logging.Level;

import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.packet.Presence;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
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
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier("XMPP_Presence", service);
		User user = DataManager.getOrNewUserForIdentifier(presence.getFrom(), endpoint);
		Message message = new Message(user, endpoint, presence.getStatus(), MessageFormat.PLAIN_TEXT);
		CommandInterpreter.processMessage(message);
	}

}
