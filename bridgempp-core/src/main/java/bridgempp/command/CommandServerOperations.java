/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.BridgeMPP;
import bridgempp.ShadowManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.command.wrapper.CommandName;
import bridgempp.command.wrapper.CommandTrigger;
import bridgempp.command.wrapper.HelpTopic;
import bridgempp.command.wrapper.RequiredPermission;
import bridgempp.message.Message;

/**
 *
 * @author Vinpasso
 */
public class CommandServerOperations
{

	@CommandName("!exit: Shutdown BridgeMPP")
	@CommandTrigger("!exit")
	@HelpTopic("Executes the BridgeMPP shutdown routine, disconnecting all clients and services")
	@RequiredPermission(Permission.EXIT)
	public static void cmdExit(Message message)
	{
		ShadowManager.logAndReply(Level.WARNING, "Server is being remotely shutdown by " + message.getOrigin().toString(), message);
		BridgeMPP.executeScheduledShutdown();
	}

}
