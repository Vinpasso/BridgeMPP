/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.PermissionsManager.Permission;
import bridgempp.command.wrapper.CommandName;
import bridgempp.command.wrapper.CommandTrigger;
import bridgempp.command.wrapper.HelpTopic;
import bridgempp.command.wrapper.RequiredPermission;
import bridgempp.data.DataManager;
import bridgempp.data.User;
import bridgempp.log.Log;
import bridgempp.message.Message;

/**
 *
 * @author Vinpasso
 */
public class CommandAliasOperations
{

	@CommandName("!createalias: Set the current users alias")
	@CommandTrigger("!createalias")
	@HelpTopic("Sets the current users alias to the given parameter. This alias will be shown as the sender name instead of their id.")
	public static void cmdCreateAlias(Message message, String newAlias)
	{
		if (message.getSender().hasAlias())
		{
			message.getOrigin().sendOperatorMessage("You already have an Alias, overwriting your old Alias");
		}
		Log.log(Level.FINER, "Endpoint: " + message.getOrigin().toString() + " now has assigned Alias: " + newAlias);
		message.getSender().setName(newAlias);
		message.getOrigin().sendOperatorMessage("Alias successfully assigned");
	}

	@CommandName("!importalias: Import a list of aliases")
	@CommandTrigger("!importalias")
	@HelpTopic("Imports a list of aliases to be assigned to their Users. Requires USER_IDENTIFIER, SERVICE_IDENTIFIER, NEW_ALIAS")
	@RequiredPermission(Permission.IMPORT_ALIAS)
	public static void cmdImportAliasList(Message message, String userID, int service, String alias)
	{
		User user = DataManager.getUserForIdentifier(userID);
		if (user == null)
		{
			message.getOrigin().sendOperatorMessage("User: " + userID + " not found. Skipping...");
			return;
		}
		user.setName(rearrangeNameFormat(alias));
		message.getOrigin().sendOperatorMessage("Alias List successfully imported");
	}

	@CommandName("!removealias: Remove the current users alias")
	@CommandTrigger("!removealias")
	@HelpTopic("Removes the current users alias. The user id will be used when delivering messages if no alias is present.")
	public static void removeAlias(Message message)
	{
		if (!message.getSender().hasAlias())
		{
			message.getOrigin().sendOperatorMessage("No aliases found");
		}
		message.getSender().setName("");
		message.getOrigin().sendOperatorMessage("Aliases successfully deleted");
	}

	// If user is written in outlook Last Name, First Name format then rearrange
	// it to First Name Last Name
	private static String rearrangeNameFormat(String name)
	{
		if (name.contains(", "))
		{
			name = name.substring(name.indexOf(", ") + 2) + " " + name.substring(0, name.indexOf(", "));
		}
		return name;
	}

}
