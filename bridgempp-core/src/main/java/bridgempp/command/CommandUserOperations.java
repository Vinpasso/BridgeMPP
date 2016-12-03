package bridgempp.command;

import bridgempp.PermissionsManager.Permission;
import bridgempp.command.wrapper.CommandName;
import bridgempp.command.wrapper.CommandTrigger;
import bridgempp.command.wrapper.HelpTopic;
import bridgempp.command.wrapper.RequiredPermission;
import bridgempp.data.DataManager;
import bridgempp.data.User;
import bridgempp.message.Message;

public class CommandUserOperations {
	@CommandName("!removeuser: Remove a User")
	@CommandTrigger("!removeuser")
	@HelpTopic("Removes a user and the associations from the Database. Requires USER_IDENTIFIER")
	@RequiredPermission(Permission.INJECT_ENDPOINT)
	public static String cmdRemoveUser(String name, Message message)
	{
		User user = DataManager.getUserForIdentifier(name);
		if (user == null)
		{
			return "User not found by primary key";
		} else
		{
			DataManager.deregisterUser(user);
			return "Deleted User: " + user.getIdentifier() + ": " + user.toString();
		}
	}
}
