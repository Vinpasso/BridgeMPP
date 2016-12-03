/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.BridgeMPP;
import bridgempp.PermissionsManager;
import bridgempp.ShadowManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.PermissionsManager.Permissions;
import bridgempp.command.wrapper.CommandHelp;
import bridgempp.command.wrapper.CommandWrapper;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.message.Message;
import bridgempp.statistics.StatisticsManager;

/**
 *
 * @author Vinpasso
 */
public class CommandInterpreter
{

	// Interpret a Message message with leading !
	public static void interpretCommand(Message message)
	{
		try
		{
			DataManager.acquireDOMWritePermission();
			synchronized (DataManager.class)
			{
				CommandWrapper.executeCommand(message);
			}
			DataManager.releaseDOMWritePermission();
		} catch (InterruptedException e)
		{
			ShadowManager.logAndReply(Level.SEVERE, "Failed to get write permission to DOM", message, e);
		}
	}

	// Process incomming messages and forward them to targets
	public static synchronized void processMessage(Message message)
	{
		if (message.getMessageBodies().size() == 0)
		{
			return;
		}
		BridgeMPP.readLock();
		ShadowManager.log(Level.INFO, "Routing Message: " + message.toString());
		for(int delivery = 0; delivery < 3; delivery++)
		{
			try
			{
				StatisticsManager.processMessage(message);
				if (message.isPlainTextMessage() && isCommand(message.getPlainTextMessageBody()))
				{
					//Never repeat a Command
					delivery = 2;
					interpretCommand(message);
				} else
				{
					ShadowManager.shadowEndpoints.forEach(endpoint -> message.addDestinationEndpoint(endpoint));
					
					if(message.getDestinations().size() == 0)
					{
						message.getOrigin().getGroups().forEach(group -> {
							message.addDestinationsFromGroupNoLoopback(group);
						});
					}
					
					message.deliver();
					
					if(message.getDestinations().size() == 0)
					{
			        	message.getOrigin().sendOperatorMessage("Message does not have a destination (user is subscribed to 0 groups). Send !help for a list of commands");
					}
				}
				break;
			} catch (Exception e)
			{
				ShadowManager.log(Level.WARNING, "Process Message failure (" + (delivery + 1) + "/3) due to " + e.getClass().getSimpleName());
				if(delivery == 2)
				{
					ShadowManager.log(Level.SEVERE, "Process Message failure (This is final): ", e);
				}
			}
		}
		ShadowManager.log(Level.INFO, "Routed Message: " + message.toString());
		BridgeMPP.readUnlock();
	}

	public static void loadCommands()
	{
		ShadowManager.log(Level.INFO, "Loading all Command Classes...");
		addCommandClass(CommandAliasOperations.class);
		addCommandClass(CommandGroupOperations.class);
		addCommandClass(CommandEndpointOperations.class);
		addCommandClass(CommandUserOperations.class);
		addCommandClass(CommandPermissionOperations.class);
		addCommandClass(CommandServerOperations.class);
		addCommandClass(CommandShadowOperations.class);
		addCommandClass(CommandServiceOperations.class);
		addCommandClass(CommandStatisticsOperations.class);
		addCommandClass(CommandInfoOperations.class);
		addCommandClass(CommandBotOperations.class);
		addCommandClass(CommandHelp.class);
		ShadowManager.log(Level.INFO, "Loaded all Command Classes");
	}

	public static void addCommandClass(Class<?> commandClass)
	{
		CommandWrapper.loadCommandClass(commandClass);
	}

	// Test if input String is actually a message.getMessage()
	public static boolean isCommand(String command)
	{
		if (command.length() == 0)
		{
			return false;
		}
		return command.charAt(0) == '!';
	}

	// Check if sender has Access
	public static boolean checkPermission(Endpoint endpoint, Permission permission)
	{
		int permissions = Permissions.getPermission(permission);
		return PermissionsManager.hasPermissions(endpoint, permissions);
	}

}
