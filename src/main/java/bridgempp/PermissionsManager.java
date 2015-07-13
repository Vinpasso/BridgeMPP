/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import bridgempp.data.AccessKey;
import bridgempp.data.Endpoint;
import bridgempp.storage.PersistanceManager;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

/**
 *
 * @author Vinpasso
 */
public class PermissionsManager
{

	private static Collection<AccessKey> keys;

	public static String generateKey(int permissions, boolean useOnce)
	{
		SecureRandom random = new SecureRandom();
		byte[] byteKey = new byte[32];
		random.nextBytes(byteKey);
		String key = Base64.getEncoder().encodeToString(byteKey);
		keys.add(new AccessKey(key, permissions, useOnce));
		saveAccessKeys();
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
		return PersistanceManager.getPersistanceManager().getAccessKeyForIdentifier(key);
	}

	private static void removeAccessKey(AccessKey key)
	{
		keys.remove(key);
		PersistanceManager.getPersistanceManager().removeAccessKey(key);
	}

	public static boolean hasPermissions(Endpoint endpoint, int permissions)
	{
		return (endpoint.getPermissions() & permissions) == permissions;
	}

	public static void loadAccessKeys()
	{
		ShadowManager.log(Level.INFO, "Loading all access keys...");
		keys = PersistanceManager.getPersistanceManager().loadAccessKeys();
		Iterator<AccessKey> iterator = keys.iterator();
		while (iterator.hasNext())
		{
			ShadowManager.log(Level.INFO, "Loaded Access Key for Permissions: " + iterator.next().getPermissions());
		}
		ShadowManager.log(Level.INFO, "Loaded all access keys...");
	}

	public static void saveAccessKeys()
	{
		ShadowManager.log(Level.INFO, "Saving all access keys...");
		PersistanceManager.getPersistanceManager().saveAccessKeys(keys);
		ShadowManager.log(Level.INFO, "Saved all access keys...");
	}

	public static void removeKey(String keyName)
	{
		removeAccessKey(getAccessKey(keyName));
	}

	public static String listKeys()
	{
		String list = "";
		Iterator<AccessKey> iterator = keys.iterator();
		while (iterator.hasNext())
		{
			AccessKey key = iterator.next();
			list += key.toString() + "\n";
		}
		return list.substring(0, list.length() - 1);
	}

	public static enum Permission
	{

		DIRECT_MESSAGE, SUBSCRIBE_UNSUBSCRIBE_GROUP, CREATE_REMOVE_GROUP, LIST_MEMBERS, LIST_GROUPS, IMPORT_ALIAS, ADD_REMOVE_SHADOW, LIST_SHADOW, EXIT, GENERATE_ONETIME_KEYS, GENERATE_PERMANENT_KEYS, REMOVE_KEYS, LIST_KEYS, LIST_SERVICES, ADD_REMOVE_SERVICE
	}

	public static class Permissions
	{

		public static int getPermission(Permission permission)
		{
			return (int) Math.pow(2, permission.ordinal());
		}
	}
}
