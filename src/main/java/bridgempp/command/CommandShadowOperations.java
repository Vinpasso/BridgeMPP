/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

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
public class CommandShadowOperations
{

	@CommandName("!addshadow: Receive log messages")
	@CommandTrigger("!addshadow")
	@HelpTopic("Add the message's sender to the list of Shadows, which receive all BridgeMPP log messages")
	@RequiredPermission(Permission.ADD_REMOVE_SHADOW)
	public static void cmdAddShadow(Message message)
	{
		ShadowManager.shadowEndpoints.add(message.getOrigin());
		ShadowManager.logAndReply(Level.WARNING, "Shadow has been subscribed by " + message.getOrigin().toString(), message);
	}

	@CommandName("!listshadows: List log receivers")
	@CommandTrigger("!listshadows")
	@HelpTopic("List all the Endpoints currently receiving BridgeMPP log messages")
	@RequiredPermission(Permission.LIST_SHADOW)
	public static void cmdListShadows(Message message)
	{
		message.getOrigin().sendOperatorMessage("Listing shadows");
		for (int i = 0; i < ShadowManager.shadowEndpoints.size(); i++)
		{
			message.getOrigin().sendOperatorMessage("Shadow: " + ShadowManager.shadowEndpoints.get(i).toString());
		}
		message.getOrigin().sendOperatorMessage("Done listing shadows");
	}

	@CommandName("!removeshadow: No log messages")
	@CommandTrigger("!removeshadow")
	@HelpTopic("Remove the message's sender from the list of Shadows, which receive all BridgeMPP log messages")
	@RequiredPermission(Permission.ADD_REMOVE_SHADOW)
	public static void cmdRemoveShadow(Message message)
	{
		ShadowManager.logAndReply(Level.WARNING, "Shadow has been removed by " + message.getOrigin().toString(), message);
		ShadowManager.shadowEndpoints.remove(message.getOrigin());
	}

}
