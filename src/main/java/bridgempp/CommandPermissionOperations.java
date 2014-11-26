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

    static void cmdUseKey(Message message) {
        boolean success = PermissionsManager.useKey(CommandInterpreter.getStringFromArgument(message.getMessage()), message.getSender());
        if (success) {
            ShadowManager.log(Level.INFO, message.getSender().toString() + " has used key and now has " + message.getSender().getPermissions());
            message.getSender().sendOperatorMessage("BridgeMPP: Rights granted successfully. Your new rights are: " + message.getSender().getPermissions());
        } else {
            message.getSender().sendOperatorMessage("BridgeMPP: Key Failure. Incorrect Key");
        }
    }

    static void cmdPrintPermissions(Message message) {
        message.getSender().sendOperatorMessage("BridgeMPP: Your rights are " + message.getSender().getPermissions());
    }

    static void cmdGeneratePermanentKey(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.GENERATE_PERMANENT_KEYS)) {
            int permissions = CommandInterpreter.getIntegerFromArgument(message.getMessage()) & message.getSender().getPermissions();
            ShadowManager.log(Level.INFO, message.getSender().toString() + " has created permanent key with permissions " + permissions);
            message.getSender().sendOperatorMessage("BridgeMPP: Generated Permanent Key: " + PermissionsManager.generateKey(permissions, false) + " Permissions: " + permissions);
        } else {
            message.getSender().sendOperatorMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdRemovePermissions(Message message) {
        int permissions = CommandInterpreter.getIntegerFromArgument(message.getMessage());
        if (permissions < 0) {
            message.getSender().sendOperatorMessage("Invalid Argument: Required Integer: New Permissions");
            return;
        }
        message.getSender().removePermissions(permissions);
        message.getSender().sendOperatorMessage("BridgeMPP: Rights removed successfully. Your new rights are: " + message.getSender().getPermissions());
    }

    static void cmdGenerateOneTimeKey(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.GENERATE_ONETIME_KEYS)) {
            int permissions = CommandInterpreter.getIntegerFromArgument(message.getMessage()) & message.getSender().getPermissions();
            ShadowManager.log(Level.INFO, message.getSender().toString() + " has created temporary key with permissions " + permissions);
            message.getSender().sendOperatorMessage("BridgeMPP: Generated One Time Key: " + PermissionsManager.generateKey(permissions, true) + " Permissions: " + permissions);
        } else {
            message.getSender().sendOperatorMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdRemoveKey(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.REMOVE_KEYS)) {
            boolean success = PermissionsManager.removeKey(CommandInterpreter.getStringFromArgument(message.getMessage()));
            if (success) {
                ShadowManager.log(Level.INFO, message.getSender().toString() + " has removed key");
                message.getSender().sendOperatorMessage("BridgeMPP: Successfully removed key");
            } else {
                message.getSender().sendOperatorMessage("BridgeMPP: Error: Key not found");
            }
        } else {
            message.getSender().sendOperatorMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdListKeys(Message message)
    {
        if(CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.LIST_KEYS))
        {
            message.getSender().sendOperatorMessage("BridgeMPP: Key List: " + PermissionsManager.listKeys());
        }
        else
        {
            message.getSender().sendOperatorMessage("BridgeMPP: Access denied");
        }
    }
}