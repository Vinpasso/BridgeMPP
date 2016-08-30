/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.Group;

import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Vinpasso
 */
public class GroupManager {

    //private static Collection<Group> groups;

    //Create an empty new group and add it to the list of Groups
    public static Group newGroup(String name) {
        return DataManager.createGroup(name);
    }

    //Remove Group and all its Users
    public static void removeGroup(Group group) {
        group.removeAllEndpoints();
        DataManager.removeGroup(group);
    }

    //Find Group, finds the First Group with name IGNORES CASE!
    public static Group findGroup(String name) {
        return DataManager.getGroup(name);
    }

    public static synchronized void sendMessageToAllSubscribedGroups(final Message message) {
        Endpoint endpoint = message.getOrigin();
        Collection<Group> groups = endpoint.getGroups();
        if(groups == null)
        {
        	return;
        }
        for(Group group : groups)
        {
        	group.sendMessage(message);
        }
    }

    public static synchronized void sendMessageToAllSubscribedGroupsWithoutLoopback(final Message message) {
        Endpoint endpoint = message.getOrigin();
        Collection<Group> groups = endpoint.getGroups();
        if(groups.size() == 0)
        {
        	endpoint.sendOperatorMessage("Message does not have a destination (user is subscribed to 0 groups). Send !help for a list of commands");
        }
		for(Group group : groups)
        {
        	group.sendMessageWithoutLoopback(message);
        }
    }

    public static String listGroups() {
        String listGroups = "";
        Iterator<Group> iterator = DataManager.getAllGroups().iterator();
        while(iterator.hasNext())
        {
        	Group group = iterator.next();
            listGroups += "Group: " + group.getName() + "\n" + group.toString();
        }
        return listGroups;
    }
}
