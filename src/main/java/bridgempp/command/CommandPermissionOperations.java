/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.PermissionsManager;
import bridgempp.ShadowManager;

/**
 *
 * @author Vinpasso
 */
public class CommandPermissionOperations {

    static void cmdUseKey(Message message) {
        boolean success = PermissionsManager.useKey(CommandInterpreter.getStringFromArgument(message.getPlainTextMessage()), message.getOrigin());
        if (success) {
            ShadowManager.log(Level.INFO, message.getOrigin().toString() + " has used key and now has " + message.getOrigin().getPermissions());
            message.getOrigin().sendOperatorMessage("Rights granted successfully. Your new rights are: " + message.getOrigin().getPermissions());
        } else {
            message.getOrigin().sendOperatorMessage("Key Failure. Incorrect Key");
        }
    }

    static void cmdPrintPermissions(Message message) {
        message.getOrigin().sendOperatorMessage("Your rights are " + message.getOrigin().getPermissions());
    }

    static void cmdGeneratePermanentKey(Message message) {
        if (CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.GENERATE_PERMANENT_KEYS)) {
            int permissions = CommandInterpreter.getIntegerFromArgument(message.getPlainTextMessage()) & message.getOrigin().getPermissions();
            ShadowManager.log(Level.INFO, message.getOrigin().toString() + " has created permanent key with permissions " + permissions);
            message.getOrigin().sendOperatorMessage("Generated Permanent Key: " + PermissionsManager.generateKey(permissions, false) + " Permissions: " + permissions);
        } else {
            message.getOrigin().sendOperatorMessage("Access denied");
        }
    }

    static void cmdRemovePermissions(Message message) {
        int permissions = CommandInterpreter.getIntegerFromArgument(message.getPlainTextMessage());
        if (permissions < 0) {
            message.getOrigin().sendOperatorMessage("Invalid Argument: Required Integer: New Permissions");
            return;
        }
        message.getOrigin().removePermissions(permissions);
        message.getOrigin().sendOperatorMessage("Rights removed successfully. Your new rights are: " + message.getOrigin().getPermissions());
    }

    static void cmdGenerateOneTimeKey(Message message) {
        if (CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.GENERATE_ONETIME_KEYS)) {
            int permissions = CommandInterpreter.getIntegerFromArgument(message.getPlainTextMessage()) & message.getOrigin().getPermissions();
            ShadowManager.log(Level.INFO, message.getOrigin().toString() + " has created temporary key with permissions " + permissions);
            message.getOrigin().sendOperatorMessage("Generated One Time Key: " + PermissionsManager.generateKey(permissions, true) + " Permissions: " + permissions);
        } else {
            message.getOrigin().sendOperatorMessage("Access denied");
        }
    }

    static void cmdRemoveKey(Message message) {
        if (CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.REMOVE_KEYS)) {
            PermissionsManager.removeKey(CommandInterpreter.getStringFromArgument(message.getPlainTextMessage()));
                ShadowManager.log(Level.INFO, message.getOrigin().toString() + " has removed key");
                message.getOrigin().sendOperatorMessage("Successfully removed key");
        } else {
            message.getOrigin().sendOperatorMessage("Access denied");
        }
    }

    static void cmdListKeys(Message message)
    {
        if(CommandInterpreter.checkPermission(message.getOrigin(), PermissionsManager.Permission.LIST_KEYS))
        {
            message.getOrigin().sendOperatorMessage("Key List: " + PermissionsManager.listKeys());
        }
        else
        {
            message.getOrigin().sendOperatorMessage("Access denied");
        }
    }
}