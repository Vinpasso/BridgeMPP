/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.PermissionsManager;
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
public class CommandPermissionOperations
{

	@CommandName("!usekey: Use a BridgeMPP access key")
	@CommandTrigger("!usekey")
	@HelpTopic("Uses a BridgeMPP Access key to give the Sender of the message the key's rights. Use-Once access keys expire upon being used")
	public static void cmdUseKey(String key, Message message)
	{
		boolean success = PermissionsManager.useKey(key, message.getOrigin());
		if (success)
		{
			ShadowManager.log(Level.INFO, message.getOrigin().toString() + " has used key and now has " + message.getOrigin().getPermissions());
			message.getOrigin().sendOperatorMessage("Rights granted successfully. Your new rights are: " + message.getOrigin().getPermissions());
		} else
		{
			message.getOrigin().sendOperatorMessage("Key Failure. Incorrect Key");
		}
	}

	@CommandName("!printpermissions: List your Permissions")
	@CommandTrigger("!printpermissions")
	@HelpTopic("Prints your current Permission status")
	public static void cmdPrintPermissions(Message message)
	{
		message.getOrigin().sendOperatorMessage("Your rights are " + message.getOrigin().getPermissions());
	}

	@CommandName("!generatepermanentkey: Create a permanent BridgeMPP Key")
	@CommandTrigger("!generatepermanentkey")
	@HelpTopic("Generates a permanent key with the specified rights, which can not be greater than your current rights")
	@RequiredPermission(Permission.GENERATE_PERMANENT_KEYS)
	public static void cmdGeneratePermanentKey(int rights, Message message)
	{
		int permissions = rights & message.getOrigin().getPermissions();
		ShadowManager.log(Level.INFO, message.getOrigin().toString() + " has created permanent key with permissions " + permissions);
		message.getOrigin().sendOperatorMessage("Generated Permanent Key: " + PermissionsManager.generateKey(permissions, false) + " Permissions: " + permissions);
	}

	@CommandName("!generateonetimekey: Create a use once BridgeMPP Key")
	@CommandTrigger("!generateonetimekey")
	@HelpTopic("Generates a one time key with the specified rights, which can not be greater than your current rights. This key can be used once")
	@RequiredPermission(Permission.GENERATE_ONETIME_KEYS)
	public static void cmdGenerateOneTimeKey(int permissions, Message message)
	{
		if (CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.GENERATE_ONETIME_KEYS))
		{
			ShadowManager.log(Level.INFO, message.getOrigin().toString() + " has created temporary key with permissions " + permissions);
			message.getOrigin().sendOperatorMessage("Generated One Time Key: " + PermissionsManager.generateKey(permissions, true) + " Permissions: " + permissions);
		} else
		{
			message.getOrigin().sendOperatorMessage("Access denied");
		}
	}

	@CommandName("!removepermissions: Remove rights")
	@CommandTrigger("!removepermissions")
	@HelpTopic("Removes the specified permissions from the Sender's total permissions. This is done via subtraction.")
	public static void cmdRemovePermissions(int permissions, Message message)
	{
		if (permissions < 0)
		{
			message.getOrigin().sendOperatorMessage("Invalid Argument: Required Integer: New Permissions");
			return;
		}
		message.getOrigin().removePermissions(permissions);
		message.getOrigin().sendOperatorMessage("Rights removed successfully. Your new rights are: " + message.getOrigin().getPermissions());
	}

	@CommandName("!removekey: Remove a key")
	@CommandTrigger("!removekey")
	@HelpTopic("Removes a key from the BridgeMPP Access Keys. The key is no longer valid and can not be used to gain rights")
	@RequiredPermission(Permission.REMOVE_KEYS)
	public static void cmdRemoveKey(String key, Message message)
	{
		PermissionsManager.removeKey(key);
		ShadowManager.log(Level.INFO, message.getOrigin().toString() + " has removed key");
		message.getOrigin().sendOperatorMessage("Successfully removed key");
	}

	@CommandName("!listkeys: List all keys")
	@CommandTrigger("!listkeys")
	@HelpTopic("Lists all currently valid BridgeMPP Keys and their associated Permissions")
	@RequiredPermission(Permission.LIST_KEYS)
	public static void cmdListKeys(Message message)
	{
		message.getOrigin().sendOperatorMessage("Key List: " + PermissionsManager.listKeys());
	}
}