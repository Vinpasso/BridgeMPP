package bridgempp.data;

import java.util.Collection;

import bridgempp.BridgeService;
import bridgempp.GroupManager;
import bridgempp.state.EndpointStateManager;
import bridgempp.storage.PersistanceManager;

public class DataManager
{

	private static final PersistanceManager PERSISTANCE_MANAGER = PersistanceManager.getPersistanceManager();

	private static Endpoint registerEndpoint(BridgeService service, String identifier)
	{
		Endpoint endpoint = new Endpoint(service, identifier);
		PERSISTANCE_MANAGER.updateState(endpoint);
		EndpointStateManager.created(endpoint);
		return endpoint;
	}
	
	public static void deregisterEndpoint(Endpoint endpoint)
	{
		PERSISTANCE_MANAGER.removeState(endpoint);
		EndpointStateManager.removed(endpoint);
	}
	
	public static void deregisterUser(User user)
	{
		PERSISTANCE_MANAGER.removeState(user);
	}
	
	private static User registerUser(String identifier)
	{
		User user = new User(identifier);
		PERSISTANCE_MANAGER.updateState(user);
		return user;
	}
	
	/**
	 * Fetches a User for a given Identifier
	 * @param identifier The Primary Key to find the User from
	 * @return The User found from the Primary Key
	 */
	public static User getUserForIdentifier(String identifier)
	{
		return PERSISTANCE_MANAGER.getUserForIdentifier(identifier);
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
		return PERSISTANCE_MANAGER.getEndpointForIdentifier(identifier);
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
		return PERSISTANCE_MANAGER.getEndpoints();
	}

	public static Group createGroup(String name) {
		Group group = new Group(name);
		PERSISTANCE_MANAGER.updateState(group);
		return group;
	}

	public static void removeGroup(Group group) {
		PERSISTANCE_MANAGER.removeState(group);
	}

	public static Group getGroup(String name) {
		return PERSISTANCE_MANAGER.getGroup(name);
	}

	public static Collection<Group> getAllGroups() {
		return PERSISTANCE_MANAGER.getGroups();
	}

	public static void createAccessKey(AccessKey accessKey) {
		PERSISTANCE_MANAGER.updateState(accessKey);
	}

	public static AccessKey getAccessKey(String key) {
		return PERSISTANCE_MANAGER.getFromPrimaryKey(AccessKey.class, key);
	}

	public static void removeAccessKey(AccessKey key) {
		PERSISTANCE_MANAGER.removeState(key);
	}

	public static Collection<AccessKey> getAllAccessKeys() {
		return PERSISTANCE_MANAGER.getQuery(AccessKey.class);
	}

	public static Collection<User> getAllUsers() {
		return PERSISTANCE_MANAGER.getQuery(User.class);
	}
}
