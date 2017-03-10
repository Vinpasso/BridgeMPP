/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.BridgeMPP;
import bridgempp.PermissionsManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.PermissionsManager.Permissions;
import bridgempp.command.wrapper.CommandHelp;
import bridgempp.command.wrapper.CommandWrapper;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.log.Log;
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
			Log.logAndReply(Level.SEVERE, "Failed to get write permission to DOM", message, e);
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
		Log.log(Level.INFO, "Routing Message: " + message.toString());
		try
		{
			StatisticsManager.processMessage(message);
			if (message.isPlainTextMessage() && isCommand(message.getPlainTextMessageBody()))
			{
				interpretCommand(message);
			} else
			{

				if (message.getDestinations().size() == 0)
				{
					message.getOrigin().getGroups().forEach(group -> {
						message.addDestinationsFromGroupNoLoopback(group);
					});
				}

				message.deliver();

				if (message.getDestinations().size() == 0)
				{
					message.getOrigin().sendOperatorMessage("Message does not have a destination (user is subscribed to 0 groups). Send !help for a list of commands");
				}
			}
		} catch (Exception e)
		{
			Log.log(Level.WARNING, "Process Message failure due to " + e.getClass().getSimpleName() + " at " + e.getStackTrace()[0].toString());
		}
		Log.log(Level.INFO, "Routed Message: " + message.toString());
		BridgeMPP.readUnlock();
	}

	public static void loadCommands()
	{
		Log.log(Level.INFO, "Loading all Command Classes...");
		addCommandClass(CommandAliasOperations.class);
		addCommandClass(CommandGroupOperations.class);
		addCommandClass(CommandEndpointOperations.class);
		addCommandClass(CommandUserOperations.class);
		addCommandClass(CommandPermissionOperations.class);
		addCommandClass(CommandServerOperations.class);
		addCommandClass(CommandServiceOperations.class);
		addCommandClass(CommandStatisticsOperations.class);
		addCommandClass(CommandInfoOperations.class);
		addCommandClass(CommandBotOperations.class);
		addCommandClass(CommandHelp.class);
		Log.log(Level.INFO, "Loaded all Command Classes");
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
