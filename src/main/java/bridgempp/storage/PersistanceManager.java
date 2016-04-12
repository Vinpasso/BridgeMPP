package bridgempp.storage;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import bridgempp.ShadowManager;
import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.data.User;
import bridgempp.data.processing.DataProcessor;
import bridgempp.service.BridgeService;
import bridgempp.statistics.StatisticStore;

public class PersistanceManager
{
	private static PersistanceManager manager;

	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	public static synchronized PersistanceManager getPersistanceManager()
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

	public synchronized <T> T getFromPrimaryKey(Class<T> className, Object primaryKey)
	{
		return entityManager.find(className, primaryKey);
	}

	public synchronized <T> Collection<T> getQuery(Class<T> className)
	{
		Entity annotation = className.getAnnotation(Entity.class);
		if (annotation == null || annotation.name() == null || annotation.name().length() == 0)
		{
			return null;
		}
		return entityManager.createQuery("SELECT s FROM " + annotation.name() + " s", className).getResultList();
	}

	public synchronized void updateState(Object... objects)
	{
		EntityTransaction saveTransaction = entityManager.getTransaction();
		saveTransaction.begin();
		for (Object object : objects)
		{
			try
			{
				if (entityManager.contains(object))
				{
					entityManager.merge(object);
				} else
				{
					entityManager.persist(object);
				}
			} catch (Exception e)
			{
				ShadowManager.log(Level.SEVERE, "Error while writing to Database", e);
			}
		}
		saveTransaction.commit();
	}
	
	public synchronized void removeState(Object... objects)
	{
		ShadowManager.log(Level.INFO, "Scheduled remove Operation on " + objects.length + " objects");

		DataProcessor.schedule(new Callable<Void>() {

			@Override
			public Void call() throws Exception
			{
				executeRemoveState(objects);
				ShadowManager.log(Level.INFO, "Executed remove Operation on " + objects.length + " objects");
				return null;
			}
			
		});
	}

	public synchronized void executeRemoveState(Object... objects)
	{
		EntityTransaction transaction = entityManager.getTransaction();
		transaction.begin();
		for (Object object : objects)
		{
			entityManager.remove(object);
		}
		transaction.commit();
	}

	public synchronized void shutdown()
	{
		entityManager.close();
		entityManagerFactory.close();
	}

	// Many convenience Methods

	public Collection<StatisticStore> getStatisticsStore()
	{
		return getQuery(StatisticStore.class);
	}

	public void saveStatisticsStore(StatisticStore statisticStore)
	{
		updateState(statisticStore);
	}

	public Collection<BridgeService> getServiceConfigurations()
	{
		return getQuery(BridgeService.class);
	}

	public User getUserForIdentifier(String identifier)
	{
		return getFromPrimaryKey(User.class, identifier);
	}

	public Endpoint getEndpointForIdentifier(String identifier)
	{
		return getFromPrimaryKey(Endpoint.class, identifier);
	}

	public Collection<Endpoint> getEndpoints()
	{
		return getQuery(Endpoint.class);
	}

	public Group getGroup(String name)
	{
		return getFromPrimaryKey(Group.class, name);
	}

	public Collection<Group> getGroups()
	{
		return getQuery(Group.class);
	}
}
