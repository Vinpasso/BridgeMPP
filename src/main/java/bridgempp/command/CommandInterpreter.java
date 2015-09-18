/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.BridgeMPP;
import bridgempp.GroupManager;
import bridgempp.Message;
import bridgempp.PermissionsManager;
import bridgempp.ShadowManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.PermissionsManager.Permissions;
import bridgempp.command.wrapper.CommandHelp;
import bridgempp.command.wrapper.CommandWrapper;
import bridgempp.data.Endpoint;
import bridgempp.statistics.StatisticsManager;

/**
 *
 * @author Vinpasso
 */
public class CommandInterpreter {

	// Interpret a Message message with leading !
	public static void interpretCommand(Message message) {
		CommandWrapper.executeCommand(message);
	}

	// Process incomming messages and forward them to targets
	public static synchronized void processMessage(Message message) {
		if (message.getPlainTextMessage() == null || message.getMessageRaw().length() == 0) {
			return;
		}
		try {
			BridgeMPP.syncLockdown();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		StatisticsManager.processMessage(message);
		if (isCommand(message.getPlainTextMessage())) {
			interpretCommand(message);
		} else {
			ShadowManager.log(Level.INFO, "Routing Message: " + message.toString());
			for (int i = 0; i < ShadowManager.shadowEndpoints.size(); i++) {
				ShadowManager.shadowEndpoints.get(i).sendMessage(message);
			}
			if (message.getGroup() != null) {
				message.getGroup().sendMessageWithoutLoopback(message);
			} else {
				GroupManager.sendMessageToAllSubscribedGroupsWithoutLoopback(message);
			}
		}
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
		addCommandClass(CommandHelp.class);
		ShadowManager.log(Level.INFO, "Loaded all Command Classes");
	}
	
	public static void addCommandClass(Class<?> commandClass)
	{
		CommandWrapper.loadCommandClass(commandClass);
	}


	// Test if input String is actually a message.getMessage()
	public static boolean isCommand(String command) {
		if (command.length() == 0) {
			return false;
		}
		return command.charAt(0) == '!';
	}

	// Check if sender has Access
	public static boolean checkPermission(Endpoint endpoint, Permission permission) {
		int permissions = Permissions.getPermission(permission);
		return PermissionsManager.hasPermissions(endpoint, permissions);
	}

}
