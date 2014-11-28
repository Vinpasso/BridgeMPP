/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp.command;

import bridgempp.Endpoint;
import bridgempp.GroupManager;
import bridgempp.Message;
import bridgempp.PermissionsManager;
import bridgempp.ShadowManager;
import bridgempp.PermissionsManager.Permission;
import bridgempp.PermissionsManager.Permissions;

/**
 *
 * @author Vinpasso
 */
public class CommandInterpreter {

    //Interpret a Message message with leading !
    public static void interpretCommand(Message message) {
        if (message.getMessage().charAt(0) != '!') {
            message.getSender().sendOperatorMessage("Internal Server Error! Command " + message.getMessage() + " not a message.getMessage() but recieved interpret request");
            throw new UnsupportedOperationException("Interpret Command: " + message.getMessage() + " Not a message.getMessage()");
        }
        String operator = message.getMessage().toLowerCase();
        if (operator.startsWith("!creategroup")) {
            CommandGroupOperations.cmdCreateGroup(message);
        } else if (operator.startsWith("!removegroup")) {
            CommandGroupOperations.cmdRemoveGroup(message);
        } else if (operator.startsWith("!subscribegroup")) {
            CommandGroupOperations.cmdSubscribeGroup(message);
        } else if (operator.startsWith("!unsubscribegroup")) {
            CommandGroupOperations.cmdUnsubscribeGroup(message);
        } else if (operator.startsWith("!listgroups")) {
            CommandGroupOperations.cmdListGroups(message);
        } else if (operator.startsWith("!listmembers")) {
            CommandGroupOperations.cmdListMembers(message);
        } else if (operator.startsWith("!addshadow")) {
            CommandShadowOperations.cmdAddShadow(message);
        } else if (operator.startsWith("!removeshadow")) {
            CommandShadowOperations.cmdRemoveShadow(message);
        } else if (operator.startsWith("!listshadows")) {
            CommandShadowOperations.cmdListShadows(message);
        } else if (operator.startsWith("!exit")) {
            CommandServerOperations.cmdExit(message);
        } else if (operator.startsWith("!generatepermanentkey")) {
            CommandPermissionOperations.cmdGeneratePermanentKey(message);
        } else if (operator.startsWith("!generateonetimekey")) {
            CommandPermissionOperations.cmdGenerateOneTimeKey(message);
        } else if (operator.startsWith("!removekey")) {
            CommandPermissionOperations.cmdRemoveKey(message);
        } else if (operator.startsWith("!usekey")) {
            CommandPermissionOperations.cmdUseKey(message);
        } else if (operator.startsWith("!listkey")) {
            CommandPermissionOperations.cmdListKeys(message);
        } else if (operator.startsWith("!printpermissions")) {
            CommandPermissionOperations.cmdPrintPermissions(message);
        } else if (operator.startsWith("!removepermissions")) {
            CommandPermissionOperations.cmdRemovePermissions(message);
        } else if (operator.startsWith("!createalias")) {
            CommandAliasOperations.cmdCreateAlias(message);
        } else if (operator.startsWith("!importalias")) {
            CommandAliasOperations.cmdImportAliasList(message);
        } else {
            message.getSender().sendOperatorMessage("Error: Command not found");
        }
    }

    //Process incomming messages and forward them to targets
    public static void processMessage(Message message) {
        if (message.getMessage() == null || message.getMessage().length() == 0) {
            return;
        }
        if (isCommand(message.getMessage())) {
            interpretCommand(message);
        } else {
            for (int i = 0; i < ShadowManager.shadowEndpoints.size(); i++) {
                ShadowManager.shadowEndpoints.get(i).sendMessage(message);
            }
            GroupManager.sendMessageToAllSubscribedGroupsWithoutLoopback(message);
        }
    }

    //Return Group name from argument
    public static String getStringFromArgument(String command) {
        if (!command.contains(" ")) {
            return "";
        }
        return command.substring(command.indexOf(" ") + 1).trim();
    }

    //Return Integer from argument
    public static int getIntegerFromArgument(String command) {
        if (!command.contains(" ")) {
            return -1;
        }
        try {
            return Integer.parseInt(command.substring(command.indexOf(" ") + 1).trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    //Test if input String is actually a message.getMessage()
    public static boolean isCommand(String command) {
        if (command.length() == 0) {
            return false;
        }
        return command.charAt(0) == '!';
    }

    //Check if sender has Access
    public static boolean checkPermission(Endpoint endpoint, Permission permission) {
        int permissions = Permissions.getPermission(permission);
        return PermissionsManager.hasPermissions(endpoint, permissions);
    }

}
