/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.storage.PersistanceManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 *
 * @author Vinpasso
 */
public class GroupManager {

    private static Collection<Group> groups;

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
    	Iterator<Group> iterator = groups.iterator();
        while(iterator.hasNext())
        {
        	Group group = iterator.next();
            if (group.getName().equalsIgnoreCase(name)) {
                return group;
            }
        }
        return null;
    }

    public static void sendMessageToAllSubscribedGroups(final Message message) {
        groups.forEach(new Consumer<Group>() {

			@Override
			public void accept(Group group)
			{
	            if (group.hasEndpoint(message.getOrigin())) {
	                group.sendMessage(message);
	            }
			}
		});
    }

    public static void sendMessageToAllSubscribedGroupsWithoutLoopback(final Message message) {
        groups.forEach(new Consumer<Group>() {

			@Override
			public void accept(Group group)
			{
	            if (group.hasEndpoint(message.getOrigin())) {
	                group.sendMessageWithoutLoopback(message);
	            }
			}
		});
    }

    public static String listGroups() {
        String listGroups = "";
        Iterator<Group> iterator = groups.iterator();
        while(iterator.hasNext())
        {
        	Group group = iterator.next();
            listGroups += "Group: " + group.getName() + "\n" + group.toString();
        }
        return listGroups;
    }

    public static void removeEndpointFromAllGroups(Endpoint endpoint) {
        Iterator<Group> iterator = groups.iterator();
        while(iterator.hasNext())
        {
        	iterator.next().removeEndpoint(endpoint);
        }
    }

	public static void loadAllGroups()
	{
		groups = PersistanceManager.getPersistanceManager().loadGroups();
	}

	public static void saveAllGroups()
	{
		PersistanceManager.getPersistanceManager().saveGroups(groups);
	}
}
