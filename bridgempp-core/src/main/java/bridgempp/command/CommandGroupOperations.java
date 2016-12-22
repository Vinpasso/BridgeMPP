/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.GroupManager;
import bridgempp.ServiceManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.command.wrapper.CommandName;
import bridgempp.command.wrapper.CommandTrigger;
import bridgempp.command.wrapper.HelpTopic;
import bridgempp.command.wrapper.RequiredPermission;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.log.Log;
import bridgempp.message.Message;
import bridgempp.message.MessageBuilder;
import bridgempp.service.BridgeService;

/**
 *
 * @author Vinpasso
 */
public class CommandGroupOperations
{

	@CommandName("!removegroup: Close the group")
	@CommandTrigger("!removegroup")
	@HelpTopic("Closes the Group and disconnects all currently participating Users")
	@RequiredPermission(Permission.CREATE_REMOVE_GROUP)
	public static void cmdRemoveGroup(String name, Message message)
	{
		boolean success = removeGroup(name);
		if (success)
		{
			Log.logAndReply(Level.FINE, "Group has been removed: " + name, message);
		} else
		{
			message.getOrigin().sendOperatorMessage("Error: Group not found");
		}
	}

	@CommandName("!subscribegroup: Join a group")
	@CommandTrigger("!subscribegroup")
	@HelpTopic("Subscribe the Message's Sender to the specified Group with name")
	@RequiredPermission(Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)
	public static void cmdSubscribeGroup(String name, Message message)
	{
		Group group = subscribeGroup(name, message.getOrigin());
		if (group != null)
		{
			Log.log(Level.FINE, message.getOrigin().toString() + " has been subscribed: " + group.getName());
			new MessageBuilder(message.getSender(), message.getOrigin())
			.addDestinationsFromGroupNoLoopback(group)
			.addPlainTextBody("BridgeMPP: Endpoint: " + message.getOrigin().toString() + " has been added to Group: " + group.getName())
			.build().send();
			message.getOrigin().sendOperatorMessage("Group has been subscribed");
		} else
		{
			message.getOrigin().sendOperatorMessage("Error: Group not found");
		}
	}
	
	@CommandName("!remotesubscribegroup: Remote join a group")
	@CommandTrigger("!remotesubscribegroup")
	@HelpTopic("Subscribe the specified Endpoint to the specified Group with name. Requires SERVICE_ID, ENDPOINT_ID, USER_ID, GROUP_NAME")
	@RequiredPermission(Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)
	public static String cmdRemoteSubscribeGroup(int serviceID, String endpoint_id, String user_id, String name, Message message)
	{
		BridgeService service = ServiceManager.getServiceByServiceIdentifier(serviceID);
		if(service == null)
		{
			return "Service " + serviceID + "not found. Try obtaining a Service ID with !listservices";
		}
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(endpoint_id, service);
		DataManager.getOrNewUserForIdentifier(user_id, endpoint);
		Group group = subscribeGroup(name, endpoint);
		if (group != null)
		{
			Log.log(Level.FINE, message.getOrigin().toString() + " has been subscribed: " + group.getName());
			return "Group has been subscribed";
		} else
		{
			return "Error: Group not found";
		}
	}

	@CommandName("!remoteunsubscribegroup: Remote join a group")
	@CommandTrigger("!remoteunsubscribegroup")
	@HelpTopic("Unsubscribe the specified Endpoint from the specified Group with name. Requires SERVICE_ID, ENDPOINT_ID, USER_ID, GROUP_NAME")
	@RequiredPermission(Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)
	public static String cmdRemoteUnsubscribeGroup(int serviceID, String endpoint_id, String user_id, String name, Message message)
	{
		BridgeService service = ServiceManager.getServiceByServiceIdentifier(serviceID);
		if(service == null)
		{
			return "Service " + serviceID + "not found. Try obtaining a Service ID with !listservices";
		}
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(endpoint_id, service);
		DataManager.getOrNewUserForIdentifier(user_id, endpoint);
		Group group = unsubscribeGroup(name, endpoint);
		if (group != null)
		{
			Log.log(Level.FINE, message.getOrigin().toString() + " has been unsubscribed: " + group.getName());
			return "Group has been unsubscribed";
		} else
		{
			return "Error: Group not found";
		}
	}

	
	@CommandName("!listgroups: List all groups")
	@CommandTrigger("!listgroups")
	@HelpTopic("Lists all the groups currently registered on BridgeMPP")
	@RequiredPermission(Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)
	public static void cmdListGroups(Message message)
	{
		message.getOrigin().sendOperatorMessage("Listing Groups:\nBridgeMPP: " + GroupManager.listGroups().replaceAll("\n", "\nBridgeMPP: ") + "BridgeMPP: Finished listing Groups");
	}

	@CommandName("!unsubscribegroup: Leave the group")
	@CommandTrigger("!unsubscribegroup")
	@HelpTopic("Unsubscribe the Message's Sender from the specified Group with name")
	@RequiredPermission(Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)
	public static void cmdUnsubscribeGroup(String name, Message message)
	{
		Group group = unsubscribeGroup(name, message.getOrigin());
		if (group != null)
		{
			Log.logAndReply(Level.FINE, message.getOrigin().toString() + " has been unsubscribed: " + group.getName(), message);
			new MessageBuilder(message.getSender(), message.getOrigin())
			.addDestinationsFromGroupNoLoopback(group)
			.addPlainTextBody("BridgeMPP: Endpoint: " + message.getOrigin().toString() + " has been removed from Group: " + group.getName())
			.build().send();
		} else
		{
			message.getOrigin().sendOperatorMessage("Error: Group not found");
		}
	}

	@CommandName("!listmembers: List all the Members of a Group")
	@CommandTrigger("!listmembers")
	@HelpTopic("Prints all the Member names of the specified Group")
	@RequiredPermission(Permission.LIST_MEMBERS)
	public static void cmdListMembers(String name, Message message)
	{
		Group group = GroupManager.findGroup(name);
		if (group == null)
		{
			message.getOrigin().sendOperatorMessage("Error: No such group");
			return;
		}
		message.getOrigin().sendOperatorMessage("Listing Members:\nBridgeMPP: " + group.toString().replaceAll("\n", "\nBridgeMPP: ") + "Finished listing Members");
	}

	@CommandName("!creategroup: Create a new group")
	@CommandTrigger("!creategroup")
	@HelpTopic("Opens a new empty Group with the target name")
	@RequiredPermission(Permission.CREATE_REMOVE_GROUP)
	public static void cmdCreateGroup(String name, Message message)
	{
		Group group = createGroup(name);
		if (group == null)
		{
			message.getOrigin().sendOperatorMessage("Error: Group already exists");
		} else
		{
			Log.log(Level.FINE, "Group has been created: " + group.getName());
			message.getOrigin().sendOperatorMessage("Group has been created: " + group.getName());
		}
	}

	// Remove everyone from Group and destroy Group
	private static boolean removeGroup(String name)
	{
		Group group = GroupManager.findGroup(name);
		if (group == null)
		{
			return false;
		}
		GroupManager.removeGroup(group);
		return true;
	}

	// Create a Group with name
	private static Group createGroup(String name)
	{
		if (GroupManager.findGroup(name) != null)
		{
			return null;
		}
		Group group = GroupManager.newGroup(name);
		return group;
	}

	// Subscribe the Message's Sender to the specified Group with name
	protected static Group subscribeGroup(String message, Endpoint sender)
	{
		Group group = GroupManager.findGroup(message);
		if (group == null)
		{
			return null;
		}
		group.addEndpoint(sender);
		return group;
	}

	private static Group unsubscribeGroup(String name, Endpoint endpoint)
	{
		Group group = GroupManager.findGroup(name);
		if (group == null)
		{
			return null;
		}
		group.removeEndpoint(endpoint);
		return group;
	}

}
