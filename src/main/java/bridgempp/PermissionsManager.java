/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.data.AccessKey;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Iterator;

/**
 *
 * @author Vinpasso
 */
public class PermissionsManager
{

	public static String generateKey(int permissions, boolean useOnce)
	{
		SecureRandom random = new SecureRandom();
		byte[] byteKey = new byte[42];
		random.nextBytes(byteKey);
		String key = Base64.getEncoder().encodeToString(byteKey);
		DataManager.createAccessKey(new AccessKey(key, permissions, useOnce));
		return key;
	}

	public static boolean useKey(String key, Endpoint endpoint)
	{
		AccessKey accessKey = getAccessKey(key);
		if (accessKey == null)
		{
			return false;
		}
		if (accessKey.isUseOnce() == true)
		{
			removeAccessKey(accessKey);
		}
		endpoint.addPermissions(accessKey.getPermissions());
		return true;
	}

	private static AccessKey getAccessKey(String key)
	{
		return DataManager.getAccessKey(key);
	}

	private static void removeAccessKey(AccessKey key)
	{
		DataManager.removeAccessKey(key);
	}

	public static boolean hasPermissions(Endpoint endpoint, int permissions)
	{
		return (endpoint.getPermissions() & permissions) == permissions;
	}

	public static void removeKey(String keyName)
	{
		removeAccessKey(getAccessKey(keyName));
	}

	public static String listKeys()
	{
		String list = "";
		Iterator<AccessKey> iterator = DataManager.getAllAccessKeys().iterator();
		while (iterator.hasNext())
		{
			AccessKey key = iterator.next();
			list += key.toString() + "\n";
		}
		return list.substring(0, list.length() - 1);
	}

	public static enum Permission
	{

		DIRECT_MESSAGE, SUBSCRIBE_UNSUBSCRIBE_GROUP, CREATE_REMOVE_GROUP, LIST_MEMBERS, LIST_GROUPS, IMPORT_ALIAS, ADD_REMOVE_SHADOW, LIST_SHADOW, EXIT, GENERATE_ONETIME_KEYS, GENERATE_PERMANENT_KEYS, REMOVE_KEYS, LIST_KEYS, LIST_SERVICES, ADD_REMOVE_SERVICE, INJECT_ENDPOINT, REMOTE_SEND_MESSAGE, LIST_ENDPOINTS
	}

	public static class Permissions
	{

		public static int getPermission(Permission permission)
		{
			return (int) Math.pow(2, permission.ordinal());
		}
	}
}
