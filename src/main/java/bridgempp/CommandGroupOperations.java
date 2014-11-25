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
    private static Group unsubscribeGroup(String name, Endpoint endpoint) {
        Group group = GroupManager.findGroup(name);
        if (group == null) {
            return null;
        }
        group.removeEndpoint(endpoint);
        return group;
    }

    static void cmdRemoveGroup(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.CREATE_REMOVE_GROUP)) {
            boolean success = removeGroup(CommandInterpreter.getStringFromArgument(message.getMessage()));
            if (success) {
                ShadowManager.log(Level.FINE, "Group has been removed: " + CommandInterpreter.getStringFromArgument(message.getMessage()));
                message.getSender().sendMessage("BridgeMPP: Group has been removed");
            } else {
                message.getSender().sendMessage("BridgeMPP: Error: Group not found");
            }
        } else {
            message.getSender().sendMessage("BridgeMPP: Access denied");
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

    static void cmdSubscribeGroup(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)) {
            Group group = subscribeGroup(CommandInterpreter.getStringFromArgument(message.getMessage()), message.getSender());
            if (group != null) {
                ShadowManager.log(Level.FINE, message.getSender().toString() + " has been subscribed: " + group.getName());
                group.sendMessage("BridgeMPP: Endpoint: " + message.getSender().toString() + " has been added to Group: " + group.getName());
                message.getSender().sendMessage("BridgeMPP: Group has been subscribed");
            } else {
                message.getSender().sendMessage("BridgeMPP: Error: Group not found");
            }
        } else {
            message.getSender().sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdListGroups(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.LIST_GROUPS)) {
            message.getSender().sendMessage("BridgeMPP: Listing Groups:\nBridgeMPP: " + GroupManager.listGroups().replaceAll("\n", "\nBridgeMPP: ") + "BridgeMPP: Finished listing Groups");
        } else {
            message.getSender().sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdUnsubscribeGroup(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.SUBSCRIBE_UNSUBSCRIBE_GROUP)) {
            Group group = unsubscribeGroup(CommandInterpreter.getStringFromArgument(message.getMessage()), message.getSender());
            if (group != null) {
                ShadowManager.log(Level.FINE, message.getSender().toString() + " has been unsubscribed: " + group.getName());
                group.sendMessage("BridgeMPP: Endpoint: " + message.getSender().toString() + " has been removed from Group: " + group.getName());
                message.getSender().sendMessage("BridgeMPP: Group has been unsubscribed");
            } else {
                message.getSender().sendMessage("BridgeMPP: Error: Group not found");
            }
        } else {
            message.getSender().sendMessage("BridgeMPP: Access denied");
        }
    }

    //Subscribe the Message's Sender to the specified Group with name
    private static Group subscribeGroup(String message, Endpoint sender) {
        Group group = GroupManager.findGroup(message);
        if (group == null) {
            return null;
        }
        group.addEndpoint(sender);
        return group;
    }

    static void cmdListMembers(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.LIST_MEMBERS)) {
            Group group = GroupManager.findGroup(CommandInterpreter.getStringFromArgument(message.getMessage()));
            if (group == null) {
                message.getSender().sendMessage("BridgeMPP: Error: No such group");
                return;
            }
            message.getSender().sendMessage("BridgeMPP: Listing Members:\nBridgeMPP: " + group.toString().replaceAll("\n", "\nBridgeMPP: ") + "Finished listing Members");
        } else {
            message.getSender().sendMessage("BridgeMPP: Access denied");
        }
    }

    static void cmdCreateGroup(Message message) {
        if (CommandInterpreter.checkPermission(message.getSender(), PermissionsManager.Permission.CREATE_REMOVE_GROUP)) {
            Group group = createGroup(CommandInterpreter.getStringFromArgument(message.getMessage()));
            if (group == null) {
                message.getSender().sendMessage("BridgeMPP: Error: Group already exists");
            } else {
                ShadowManager.log(Level.FINE, "Group has been created: " + group.getName());
                message.getSender().sendMessage("BridgeMPP: Group has been created: " + group.getName());
            }
        } else {
            message.getSender().sendMessage("BridgeMPP: Access denied");
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
