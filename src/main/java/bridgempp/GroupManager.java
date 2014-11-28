/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import org.apache.commons.configuration.ConfigurationException;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vinpasso
 */
public class GroupManager {

    private static ArrayList<Group> groups;

    //Create an empty new group and add it to the list of Groups
    public static Group newGroup() {
        Group group = new Group();
        groups.add(group);
        return group;
    }

    //Remove Group and all its Users
    public static void removeGroup(Group group) {
        group.removeAllEndpoints();
        groups.remove(group);
    }

    //Find Group, finds the First Group with name IGNORES CASE!
    public static Group findGroup(String name) {
        for (int i = 0; i < groups.size(); i++) {
            String groupname = groups.get(i).getName();
            if (groupname != null && groupname.equalsIgnoreCase(name)) {
                return groups.get(i);
            }
        }
        return null;
    }

    public static void sendMessageToAllSubscribedGroups(Message message) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).hasEndpoint(message.getSender())) {
                groups.get(i).sendMessage(message);
            }
        }
    }

    public static void sendMessageToAllSubscribedGroupsWithoutLoopback(Message message) {
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).hasEndpoint(message.getSender())) {
                groups.get(i).sendMessageWithoutLoopback(message);
            }
        }
    }

    //Load all groups from File
    public static void loadAllGroups() {
        ShadowManager.log(Level.INFO, "Loading all groups...");

        groups = new ArrayList<>();
        int numGroups = ConfigurationManager.groupConfiguration.getRoot().getChild(0).getChildrenCount();
        for (int g = 0; g < numGroups; g++) {
            Group group = new Group();
            group.setName(ConfigurationManager.groupConfiguration.getString("groups.group(" + g + ").name"));
            int numEndpoints = ConfigurationManager.groupConfiguration.getRoot().getChild(0).getChild(g).getChildrenCount() - 1;
            for (int e = 0; e < numEndpoints; e++) {
                Endpoint endpoint = Endpoint.readEndpoint(ConfigurationManager.groupConfiguration, "groups.group(" + g + ").endpoint(" + e + ").");
                endpoint.getService().addEndpoint(endpoint);
                group.addEndpoint(endpoint);
            }
            groups.add(group);
        }
        ShadowManager.log(Level.INFO, "Loaded all groups");
    }

    //Save all groups to File
    public static void saveAllGroups() {
        ShadowManager.log(Level.INFO, "Saving all groups...");
        try {
            ConfigurationManager.groupConfiguration.clear();
            for (int g = 0; g < groups.size(); g++) {
                Group group = groups.get(g);
                ConfigurationManager.groupConfiguration.addProperty("groups.group(-1).name", group.getName());
                for (int e = 0; e < group.getEndpoints().size(); e++) {
                    Endpoint.writeEndpoint(group.getEndpoints().get(e), ConfigurationManager.groupConfiguration, "groups.group.");
                }
            }
            ConfigurationManager.groupConfiguration.save();
        } catch (ConfigurationException ex) {
            Logger.getLogger(GroupManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ShadowManager.log(Level.INFO, "Saved all groups");
    }

    public static String listGroups() {
        String listGroups = "";
        for (int i = 0; i < groups.size(); i++) {
            listGroups += "Group: " + groups.get(i).getName() + "\n" + groups.get(i).toString();
        }
        return listGroups;
    }

    public static void removeEndpointFromAllGroups(Endpoint endpoint) {
        for (int i = 0; i < groups.size(); i++) {
            groups.get(i).removeEndpoint(endpoint);
        }
    }
}
