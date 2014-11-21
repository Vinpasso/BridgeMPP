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
public class CommandGroupOperations {

    //Unsubscribe the Message's Sender to the specified Group with name
    private static Group unsubscribeGroup(String name, Endpoint sender) {
        Group group = GroupManager.findGroup(name);
        if (group == null) {
            return null;
        }
        group.removeEndpoint(sender);
        return group;
    }

    static void cmdRemoveGroup(String command, Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.CREATE_REMOVE_GROUP)) {
            boolean success = removeGroup(CommandInterpreter.getStringFromArgument(command));
            if (success) {
                ShadowManager.log(Level.FINE, "Group has been removed: " + CommandInterpreter.getStringFromArgument(command));
                sender.sendMessage("BridgeMPP: Group has been removed");
            } else {
                sender.sendMessage("BridgeMPP: Error: Group not found");
            }
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    //Create a Group with name
    private static Group createGroup(String name) {
        if (GroupManager.findGroup(name) != null) {
            return null;
        }
        Group group = GroupManager.newGroup();
        group.setName(name);
        return group;
    }

    static void cmdSubscribeGroup(String command, Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)) {
            Group group = subscribeGroup(CommandInterpreter.getStringFromArgument(command), sender);
            if (group != null) {
                ShadowManager.log(Level.FINE, sender.toString() + " has been subscribed: " + group.getName());
                group.sendMessage("BridgeMPP: Endpoint: " + sender.toString() + " has been added to Group: " + group.getName());
                sender.sendMessage("BridgeMPP: Group has been subscribed");
            } else {
                sender.sendMessage("BridgeMPP: Error: Group not found");
            }
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdListGroups(Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.LIST_GROUPS)) {
            sender.sendMessage("BridgeMPP: Listing Groups:\nBridgeMPP: " + GroupManager.listGroups().replaceAll("\n", "\nBridgeMPP: ") + "BridgeMPP: Finished listing Groups");
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdUnsubscribeGroup(String command, Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)) {
            Group group = unsubscribeGroup(CommandInterpreter.getStringFromArgument(command), sender);
            if (group != null) {
                ShadowManager.log(Level.FINE, sender.toString() + " has been unsubscribed: " + group.getName());
                group.sendMessage("BridgeMPP: Endpoint: " + sender.toString() + " has been removed from Group: " + group.getName());
                sender.sendMessage("BridgeMPP: Group has been unsubscribed");
            } else {
                sender.sendMessage("BridgeMPP: Error: Group not found");
            }
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    //Subscribe the Message's Sender to the specified Group with name
    private static Group subscribeGroup(String name, Endpoint sender) {
        Group group = GroupManager.findGroup(name);
        if (group == null) {
            return null;
        }
        group.addEndpoint(sender);
        return group;
    }

    static void cmdListMembers(String command, Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.LIST_MEMBERS)) {
            Group group = GroupManager.findGroup(CommandInterpreter.getStringFromArgument(command));
            if (group == null) {
                sender.sendMessage("BridgeMPP: Error: No such group");
                return;
            }
            sender.sendMessage("BridgeMPP: Listing Members:\nBridgeMPP: " + group.toString().replaceAll("\n", "\nBridgeMPP: ") + "Finished listing Members");
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdCreateGroup(String command, Endpoint sender) {
        if (CommandInterpreter.checkPermission(sender, PermissionsManager.Permission.CREATE_REMOVE_GROUP)) {
            Group group = createGroup(CommandInterpreter.getStringFromArgument(command));
            if (group == null) {
                sender.sendMessage("BridgeMPP: Error: Group already exists");
            } else {
                ShadowManager.log(Level.FINE, "Group has been created: " + group.getName());
                sender.sendMessage("BridgeMPP: Group has been created: " + group.getName());
            }
        } else {
            sender.sendMessage("BridgeMPP: Access denied");
        }
    }

    //Remove everyone from Group and destroy Group
    private static boolean removeGroup(String name) {
        Group group = GroupManager.findGroup(name);
        if (group == null) {
            return false;
        }
        GroupManager.removeGroup(group);
        return true;
    }
    
}
