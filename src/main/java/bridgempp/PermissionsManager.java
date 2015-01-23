/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bridgempp;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vinpasso
 */
public class PermissionsManager {

    private static HashMap<String, AccessKey> accessKeys;

    public static String generateKey(int permissions, boolean useOnce) {
        SecureRandom random = new SecureRandom();
        byte[] byteKey = new byte[32];
        random.nextBytes(byteKey);
        String key = Base64.getEncoder().encodeToString(byteKey);
        accessKeys.put(key, new AccessKey(key, permissions, useOnce));
        return key;
    }

    public static boolean useKey(String key, Endpoint endpoint) {
        AccessKey accessKey = accessKeys.get(key);
        if (accessKey == null) {
            return false;
        }
        if (accessKey.isUseOnce() == true) {
            accessKeys.remove(key);
        }
        endpoint.addPermissions(accessKey.getPermissions());
        return true;
    }

    public static boolean hasPermissions(Endpoint endpoint, int permissions) {
        return (endpoint.getPermissions() & permissions) == permissions;
    }

    public static void loadAccessKeys() {
        ShadowManager.log(Level.INFO, "Loading all access keys...");

        accessKeys = new HashMap<>();
        int numberOfKeys = ConfigurationManager.permissionConfiguration.getRoot().getChild(0).getChildrenCount();
        for (int i = 0; i < numberOfKeys; i++) {
            AccessKey key = AccessKey.readAccessKey(ConfigurationManager.permissionConfiguration, "keys.key(" + i + ").");
            accessKeys.put(key.getKey(), key);
        }
        ShadowManager.log(Level.INFO, "Loaded all access keys...");
    }

    public static void saveAccessKeys() {
        ShadowManager.log(Level.INFO, "Saving all access keys...");
        try {
            ConfigurationManager.permissionConfiguration.clear();
            Iterator<AccessKey> iterator = accessKeys.values().iterator();
            while (iterator.hasNext()) {
                AccessKey key = iterator.next();
                AccessKey.writeAccessKey(ConfigurationManager.permissionConfiguration, key);
            }
            ConfigurationManager.permissionConfiguration.save();
        } catch (ConfigurationException ex) {
            Logger.getLogger(PermissionsManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ShadowManager.log(Level.INFO, "Saved all access keys...");
    }

    public static boolean removeKey(String keyName) {
        return accessKeys.remove(keyName) != null;
    }

    public static String listKeys() {
        String list = "";
        for (AccessKey key : accessKeys.values()) {
            list += key.toString() + "\n";
        }
        return list.substring(0, list.length() - 1);
    }

    static class AccessKey {

        private int permissions;
        private boolean useOnce;
        private String key;

        public AccessKey(String key, int permissions, boolean useOnce) {
            this.key = key;
            this.permissions = permissions;
            this.useOnce = useOnce;
        }

        @Override
        public String toString() {
            return "AccessKey: Permissions " + permissions + " useOnce " + useOnce + " Key " + key;
        }

        /**
         * @return the permissions
         */
        public int getPermissions() {
            return permissions;
        }

        /**
         * @return the useOnce
         */
        public boolean isUseOnce() {
            return useOnce;
        }

        /**
         * @return the Key
         */
        public String getKey() {
            return key;
        }

        public static void writeAccessKey(XMLConfiguration configuration, AccessKey key) {
            configuration.addProperty("keys.key(-1).key", key.getKey());
            configuration.addProperty("keys.key.permissions", key.getPermissions());
            configuration.addProperty("keys.key.useOnce", key.isUseOnce());
        }

        public static AccessKey readAccessKey(XMLConfiguration configuration, String prefix) {
            String key = configuration.getString(prefix + "key");
            int permissions = configuration.getInt(prefix + "permissions");
            boolean useOnce = configuration.getBoolean(prefix + "useOnce");
            return new AccessKey(key, permissions, useOnce);
        }

    }

    public static enum Permission {
    	NONE,
        DIRECT_MESSAGE,
        SUBSCRIBE_UNSUBSCRIBE_GROUP,
        CREATE_REMOVE_GROUP,
        LIST_MEMBERS,
        LIST_GROUPS,
        IMPORT_ALIAS,
        ADD_REMOVE_SHADOW,
        LIST_SHADOW,
        EXIT,
        GENERATE_ONETIME_KEYS,
        GENERATE_PERMANENT_KEYS,
        REMOVE_KEYS,
        LIST_KEYS, 
        BROADCAST
    }

    public static class Permissions {

        public static int getPermission(Permission permission) {
            return (int) Math.pow(2, permission.ordinal());
        }
        
        public static int getAdminPermissions()
        {
        	return (int) (Math.pow(2, Permission.values().length) - 1);
        }
    }
}
