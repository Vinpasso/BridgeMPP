/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class CommandPermissionOperations {

    static void cmdUseKey(Endpoint sender, String command) {
        boolean success = PermissionsManager.useKey(CommandInterpreter.getStringFromArgument(command), sender);
        if (success) {
            ShadowManager.log(Level.INFO, sender.toString() + " has used key and now has " + sender.getPermissions());
            sender.sendMessage("BridgeMPP: Rights granted successfully. Your new rights are: " + sender.getPermissions());
        } else {
            sender.sendMessage("BridgeMPP: Key Failure. Incorrect Key");
        }
    }

    static void cmdPrintPermissions(Endpoint sender, String command) {
        sender.sendMessage("BridgeMPP: Your rights are " + sender.getPermissions());
    }

    static void cmdGeneratePermanentKey(Endpoint sender, String command) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.GENERATE_PERMANENT_KEYS)) {
            int permissions = CommandInterpreter.getIntegerFromArgument(command) & sender.getPermissions();
            ShadowManager.log(Level.INFO, sender.toString() + " has created permanent key with permissions " + permissions);
            sender.sendMessage("BridgeMPP: Generated Permanent Key: " + PermissionsManager.generateKey(permissions, false) + " Permissions: " + permissions);
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdRemovePermissions(Endpoint sender, String command) {
        int permissions = CommandInterpreter.getIntegerFromArgument(command);
        if (permissions < 0) {
            sender.sendMessage("Invalid Argument: Required Integer: New Permissions");
            return;
        }
        sender.removePermissions(permissions);
        sender.sendMessage("BridgeMPP: Rights removed successfully. Your new rights are: " + sender.getPermissions());
    }

    static void cmdGenerateOneTimeKey(Endpoint sender, String command) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.GENERATE_ONETIME_KEYS)) {
            int permissions = CommandInterpreter.getIntegerFromArgument(command) & sender.getPermissions();
            ShadowManager.log(Level.INFO, sender.toString() + " has created temporary key with permissions " + permissions);
            sender.sendMessage("BridgeMPP: Generated One Time Key: " + PermissionsManager.generateKey(permissions, true) + " Permissions: " + permissions);
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdRemoveKey(Endpoint sender, String command) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.REMOVE_KEYS)) {
            boolean success = PermissionsManager.removeKey(CommandInterpreter.getStringFromArgument(command));
            if (success) {
                ShadowManager.log(Level.INFO, sender.toString() + " has removed key");
                sender.sendMessage("BridgeMPP: Successfully removed key");
            } else {
                sender.sendMessage("BridgeMPP: Error: Key not found");
            }
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdListKeys(Endpoint sender ,String command)
    {
        if(CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.LIST_KEYS))
        {
            sender.sendMessage("BridgeMPP: Key List: " + PermissionsManager.listKeys());
        }
        else
        {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }
}
