/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.PermissionsManager.Permission;
import bridgempp.PermissionsManager.Permissions;

/**
 *
 * @author Vinpasso
 */
public class CommandInterpreter {

    //Interpret a String command with leading !
    public static void interpretCommand(String command, Endpoint sender) {
        if (command.charAt(0) != '!') {
            sender.sendMessage("Internal Server Error! Command " + command + " not a command but recieved interpret request");
            throw new UnsupportedOperationException("Interpret Command: " + command + " Not a command");
        }
        String operator = command.toLowerCase();
        if (operator.startsWith("!creategroup")) {
            CommandGroupOperations.cmdCreateGroup(command, sender);
        } else if (operator.startsWith("!removegroup")) {
            CommandGroupOperations.cmdRemoveGroup(command, sender);
        } else if (operator.startsWith("!subscribegroup")) {
            CommandGroupOperations.cmdSubscribeGroup(command, sender);
        } else if (operator.startsWith("!unsubscribegroup")) {
            CommandGroupOperations.cmdUnsubscribeGroup(command, sender);
        } else if (operator.startsWith("!listgroups")) {
            CommandGroupOperations.cmdListGroups(sender);
        } else if (operator.startsWith("!listmembers")) {
            CommandGroupOperations.cmdListMembers(command, sender);
        } else if (operator.startsWith("!addshadow")) {
            CommandShadowOperations.cmdAddShadow(sender);
        } else if (operator.startsWith("!removeshadow")) {
            CommandShadowOperations.cmdRemoveShadow(sender);
        } else if (operator.startsWith("!listshadows")) {
            CommandShadowOperations.cmdListShadows(sender);
        } else if (operator.startsWith("!exit")) {
            CommandServerOperations.cmdExit(sender);
        } else if (operator.startsWith("!generatepermanentkey")) {
            CommandPermissionOperations.cmdGeneratePermanentKey(sender, command);
        } else if (operator.startsWith("!generateonetimekey")) {
            CommandPermissionOperations.cmdGenerateOneTimeKey(sender, command);
        } else if (operator.startsWith("!removekey")) {
            CommandPermissionOperations.cmdRemoveKey(sender, command);
        } else if (operator.startsWith("!usekey")) {
            CommandPermissionOperations.cmdUseKey(sender, command);
        } else if (operator.startsWith("!listkey")) {
            CommandPermissionOperations.cmdListKeys(sender, command);
        } else if (operator.startsWith("!printpermissions")) {
            CommandPermissionOperations.cmdPrintPermissions(sender, command);
        } else if (operator.startsWith("!removepermissions")) {
            CommandPermissionOperations.cmdRemovePermissions(sender, command);
        } else if (operator.startsWith("!createalias")) {
            CommandAliasOperations.cmdCreateAlias(sender, command);
        } else if (operator.startsWith("!importalias")) {
            CommandAliasOperations.cmdImportAliasList(sender, command);
        } else {
            sender.sendMessage("BridgeMPP: Error: Command not found");
        }
    }

    //Process incomming messages and forward them to targets
    public static void processMessage(String line, Endpoint sender) {
        if (line == null || line.length() == 0) {
            return;
        }
        if (isCommand(line)) {
            interpretCommand(line, sender);
        } else {
            for (int i = 0; i < ShadowManager.shadowEndpoints.size(); i++) {
                ShadowManager.shadowEndpoints.get(i).sendMessage("Shadow: " + sender.toString() + ": " + line);
            }
            GroupManager.sendMessageToAllSubscribedGroupsWithoutLoopback(sender.toString() + ": " + line, sender);
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

    //Test if input String is actually a command
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
