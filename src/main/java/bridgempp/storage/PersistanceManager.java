package bridgempp.storage;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import bridgempp.BridgeService;
import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.data.ServiceConfiguration;
import bridgempp.data.User;

public class PersistanceManager
{
	private static PersistanceManager manager;

	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	public static PersistanceManager getPersistanceManager()
	{
		if (manager == null)
		{
			manager = new PersistanceManager();
		}
		return manager;
	}

	private PersistanceManager()
	{
		entityManagerFactory = Persistence.createEntityManagerFactory("bridgempp");
		entityManager = entityManagerFactory.createEntityManager();
	}

	public Collection<Group> loadGroups()
	{
		return entityManager.createQuery("SELECT e FROM Group e", Group.class).getResultList();
	}

	public void saveGroups(Collection<Group> groups)
	{
		EntityTransaction saveTransaction = entityManager.getTransaction();
		saveTransaction.begin();
		Iterator<Group> iterator = groups.iterator();
		while (iterator.hasNext())
		{
			Group group = iterator.next();
			if (entityManager.contains(group))
			{
				entityManager.merge(group);
			} else
			{
				entityManager.persist(group);
			}
		}
		saveTransaction.commit();
		entityManager.flush();
	}

	public User getUserForIdentifier(String identifier)
	{
		return entityManager.find(User.class, identifier);
	}

	public Endpoint getEndpointForIdentifier(String identifier)
	{
		return entityManager.find(Endpoint.class, identifier);
	}

	public List<Endpoint> getEndpointsForService(BridgeService service)
	{
		//TODO e.service no longer exists
 		return entityManager.createQuery("SELECT e FROM Endpoint e WHERE e.service = " + service.getName(), Endpoint.class).getResultList();
	}

	public Collection<ServiceConfiguration> getServiceConfigurations()
	{
		return entityManager.createQuery("SELECT s FROM SERVICE_CONFIGURATION s", ServiceConfiguration.class).getResultList();
	}
	
	public void shutdown()
	{
		entityManager.close();
		entityManagerFactory.close();
	}

}
