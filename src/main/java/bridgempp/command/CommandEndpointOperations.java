package bridgempp.command;

import java.util.Iterator;

import bridgempp.Message;
import bridgempp.ServiceManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.command.wrapper.CommandName;
import bridgempp.command.wrapper.CommandTrigger;
import bridgempp.command.wrapper.HelpTopic;
import bridgempp.command.wrapper.RequiredPermission;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.services.xmpp.XMPPService;

public class CommandEndpointOperations {
	@CommandName("!removenonpersistentendpoints: Remove all non persistent endpoints")
	@CommandTrigger("!removenonpersistentendpoints")
	@HelpTopic("Instantly removes all non persistent endpoints from the database and all groups. This also purges the endpoints users")
	@RequiredPermission(Permission.INJECT_ENDPOINT)
	public static void cmdRemoveNonPersistentEndpoints(Message message)
	{
		Iterator<Endpoint> endpoints = DataManager.getAllEndpoints().iterator();
		message.getOrigin().sendOperatorMessage("Removing all non persistent endpoints...");
		while(endpoints.hasNext())
		{
			Endpoint endpoint = endpoints.next();
			if(!endpoint.getService().isPersistent())
			{
				message.getOrigin().sendOperatorMessage("Removing: " + endpoint.toString());
				//TODO: Fix this
				DataManager.deregisterEndpoint(endpoint);
			}
		}
		message.getOrigin().sendOperatorMessage("Removed all non persistent endpoints...");
	}
	
	@CommandName("!upgradexmppendpoints: Upgrade Endpoints to new Handle Format")
	@CommandTrigger("!upgradexmppendpoints")
	@HelpTopic("Temporary Command")
	@RequiredPermission(Permission.INJECT_ENDPOINT)
	public static void cmdUpgradeXMPPEndpoint(Message message, int service)
	{
		message.getOrigin().sendOperatorMessage("Upgrading all XMPP persistent endpoints...");
		((XMPPService)ServiceManager.getServiceByServiceIdentifier(service)).importFromEndpoint();
		message.getOrigin().sendOperatorMessage("Upgraded all XMPP persistent endpoints...");
	}
	
}
