package bridgempp.data;

import java.util.Collection;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.service.BridgeService;
import bridgempp.state.EventManager;
import bridgempp.state.EventManager.Event;
import bridgempp.storage.PersistanceManager;

public class DataManager
{

	private static final PersistanceManager PERSISTANCE_MANAGER = PersistanceManager.getPersistanceManager();

	private static final int READ_PERMITS = Integer.MAX_VALUE;

	private static Semaphore domRead = new Semaphore(READ_PERMITS, true);
	private static Semaphore domWrite = new Semaphore(1, true);
	
	private static synchronized Endpoint registerEndpoint(BridgeService service, String identifier)
	{
		Endpoint endpoint = new Endpoint(service, identifier);
		PERSISTANCE_MANAGER.updateState(endpoint);
		EventManager.fireEvent(Event.ENDPOINT_CREATED, endpoint);
		return endpoint;
	}
	
	public static synchronized void deregisterEndpoint(Endpoint endpoint)
	{
		PERSISTANCE_MANAGER.removeState(endpoint);
		EventManager.fireEvent(Event.ENDPOINT_REMOVED, endpoint);
	}
	
	public static synchronized void deregisterUser(User user)
	{
		PERSISTANCE_MANAGER.removeState(user);
	}
	
	private static synchronized User registerUser(String identifier)
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
	public static synchronized User getUserForIdentifier(String identifier)
	{
		return PERSISTANCE_MANAGER.getUserForIdentifier(identifier);
	}
	
	/**
	 * Fetches a User for a given Identifier. Creates a new User if it doesn't exist
	 * @param identifier The Primary Key to find the User from
	 * @return The User found from the Primary Key, or a new User
	 */
	public static synchronized User getOrNewUserForIdentifier(String identifier, Endpoint... endpoints)
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
	public static synchronized Endpoint getEndpointForIdentifier(String identifier)
	{
		return PERSISTANCE_MANAGER.getEndpointForIdentifier(identifier);
	}
	
	/**
	 * Fetches a Endpoint for a given Identifier. Creates a new Endpoint if it doesn't exist
	 * @param identifier The Primary Key to find the Endpoint from
	 * @return The Endpoint found from the Primary Key, or a new Endpoint
	 */
	public static synchronized Endpoint getOrNewEndpointForIdentifier(String identifier, BridgeService service)
	{
		Endpoint endpoint = getEndpointForIdentifier(identifier);
		if(endpoint == null)
		{
			endpoint = registerEndpoint(service, identifier);
		}
		return endpoint;
	}

	public static synchronized Collection<Endpoint> getAllEndpoints() {
		return PERSISTANCE_MANAGER.getEndpoints();
	}

	public static synchronized Group createGroup(String name) {
		Group group = new Group(name);
		PERSISTANCE_MANAGER.updateState(group);
		return group;
	}

	public static synchronized void removeGroup(Group group) {
		PERSISTANCE_MANAGER.removeState(group);
	}

	public static synchronized Group getGroup(String name) {
		return PERSISTANCE_MANAGER.getGroup(name);
	}

	public static synchronized Collection<Group> getAllGroups() {
		return PERSISTANCE_MANAGER.getGroups();
	}

	public static synchronized void createAccessKey(AccessKey accessKey) {
		PERSISTANCE_MANAGER.updateState(accessKey);
	}

	public static synchronized AccessKey getAccessKey(String key) {
		return PERSISTANCE_MANAGER.getFromPrimaryKey(AccessKey.class, key);
	}

	public static synchronized void removeAccessKey(AccessKey key) {
		PERSISTANCE_MANAGER.removeState(key);
	}

	public static synchronized Collection<AccessKey> getAllAccessKeys() {
		return PERSISTANCE_MANAGER.getQuery(AccessKey.class);
	}

	public static synchronized Collection<User> getAllUsers() {
		return PERSISTANCE_MANAGER.getQuery(User.class);
	}

	public static synchronized <T> T getFromPrimaryKey(Class<T> class1, Object identifier)
	{
		return PERSISTANCE_MANAGER.getFromPrimaryKey(class1, identifier);
	}
	
	public static synchronized void updateState(Object... objects)
	{
		PERSISTANCE_MANAGER.updateState(objects);
	}

	public static synchronized void removeState(Object... objects)
	{
		PERSISTANCE_MANAGER.removeState(objects);
	}

	public static synchronized void deregisterEndpointAndUsers(Endpoint endpoint)
	{
		while(!endpoint.getUsers().isEmpty())
		{
			User user = endpoint.getUsers().iterator().next();
			DataManager.deregisterUser(user);
		}
		DataManager.deregisterEndpoint(endpoint);		
	}
	
	public static void acquireDOMWritePermission() throws InterruptedException
	{
		domWrite.acquire();
		domRead.acquire(READ_PERMITS);
	}
	
	public static void releaseDOMWritePermission()
	{
		domRead.release(READ_PERMITS);
		domWrite.release();
	}
}
