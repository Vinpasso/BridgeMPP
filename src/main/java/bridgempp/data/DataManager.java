package bridgempp.data;

import java.util.Collection;

import bridgempp.BridgeService;
import bridgempp.storage.PersistanceManager;

public class DataManager
{

	private static Endpoint registerEndpoint(BridgeService service, String identifier)
	{
		Endpoint endpoint = new Endpoint(service, identifier);
		PersistanceManager.getPersistanceManager().updateState(endpoint);
		return endpoint;
	}
	
	private static User registerUser(String identifier)
	{
		User user = new User(identifier);
		PersistanceManager.getPersistanceManager().updateState(user);
		return user;
	}
	
	/**
	 * Fetches a User for a given Identifier
	 * @param identifier The Primary Key to find the User from
	 * @return The User found from the Primary Key
	 */
	public static User getUserForIdentifier(String identifier)
	{
		return PersistanceManager.getPersistanceManager().getUserForIdentifier(identifier);
	}
	
	/**
	 * Fetches a User for a given Identifier. Creates a new User if it doesn't exist
	 * @param identifier The Primary Key to find the User from
	 * @return The User found from the Primary Key, or a new User
	 */
	public static User getOrNewUserForIdentifier(String identifier, Endpoint... endpoints)
	{
		User user = getUserForIdentifier(identifier);
		if(user == null)
		{
			user = registerUser(identifier);
		}
		for(Endpoint endpoint : endpoints)
		{
			endpoint.putUser(user);
		}
		return user;
	}
	
	/**
	 * Fetches an Endpoint for a given Identifier
	 * @param identifier The Primary Key to find the Endpoint from
	 * @return The Endpoint found from the Primary Key
	 */
	public static Endpoint getEndpointForIdentifier(String identifier)
	{
		return PersistanceManager.getPersistanceManager().getEndpointForIdentifier(identifier);
	}
	
	/**
	 * Fetches a Endpoint for a given Identifier. Creates a new Endpoint if it doesn't exist
	 * @param identifier The Primary Key to find the Endpoint from
	 * @return The Endpoint found from the Primary Key, or a new Endpoint
	 */
	public static Endpoint getOrNewEndpointForIdentifier(String identifier, BridgeService service)
	{
		Endpoint endpoint = getEndpointForIdentifier(identifier);
		if(endpoint == null)
		{
			endpoint = registerEndpoint(service, identifier);
		}
		return endpoint;
	}

	public static Collection<Endpoint> getAllEndpoints() {
		return PersistanceManager.getPersistanceManager().getEndpoints();
	}

	public static Group createGroup(String name) {
		Group group = new Group(name);
		PersistanceManager.getPersistanceManager().updateState(group);
		return group;
	}

	public static void removeGroup(Group group) {
		PersistanceManager.getPersistanceManager().removeState(group);
	}

	public static Group getGroup(String name) {
		return PersistanceManager.getPersistanceManager().getGroup(name);
	}

	public static Collection<Group> getAllGroups() {
		return PersistanceManager.getPersistanceManager().getGroups();
	}

	public static void createAccessKey(AccessKey accessKey) {
		PersistanceManager.getPersistanceManager().updateState(accessKey);
	}

	public static AccessKey getAccessKey(String key) {
		return PersistanceManager.getPersistanceManager().getFromPrimaryKey(AccessKey.class, key);
	}

	public static void removeAccessKey(AccessKey key) {
		PersistanceManager.getPersistanceManager().removeState(key);
	}

	public static Collection<AccessKey> getAllAccessKeys() {
		return PersistanceManager.getPersistanceManager().getQuery(AccessKey.class);
	}
}
