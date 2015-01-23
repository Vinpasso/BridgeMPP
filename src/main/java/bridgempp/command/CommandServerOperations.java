/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.BridgeMPP;
import bridgempp.BridgeService;
import bridgempp.Message;
import bridgempp.PermissionsManager.Permission;
import bridgempp.ServiceManager;
import bridgempp.ShadowManager;

/**
 *
 * @author Vinpasso
 */
public class CommandServerOperations extends CommandInterpreter {

	public static void cmdExit(Message message) {
		ShadowManager.log(Level.WARNING,
				"Server is being remotely shutdown by "
						+ message.getSender().toString());
		message.getSender().sendOperatorMessage("Shutting down");
		BridgeMPP.exit();
	}
	
	public static Permission permExit()
	{
		return Permission.EXIT;
	}
	
	public static String helpExit()
	{
		return formatHelp("Shuts the Server down with a limit of 60 seconds delay.");
	}

	public static void cmdBroadcastMessage(Message message) {
		String broadcast = CommandInterpreter.getStringFromArgument(message
				.getMessageRaw());
		for (BridgeService bridgeService : ServiceManager.listServices()) {
			bridgeService.broadcastMessage(new Message(message.getSender(),
					broadcast, message.getMessageFormat()));
		}
	}

	public static Permission permBroadcastMessage() {
		return Permission.BROADCAST;
	}
	
	public static String helpBroadcastMessage()
	{
		return formatHelp("Broadcast a message to all known endpoints.");
	}

}
