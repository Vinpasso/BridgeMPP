package bridgempp.storage;

import java.util.Collection;
import java.util.logging.Level;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import bridgempp.BridgeMPP;
import bridgempp.ShadowManager;
import bridgempp.data.Endpoint;
import bridgempp.data.Group;
import bridgempp.data.User;
import bridgempp.service.BridgeService;
import bridgempp.statistics.StatisticStore;

public class PersistanceManager {
	private static PersistanceManager manager;

	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	public static synchronized PersistanceManager getPersistanceManager() {
		if (manager == null) {
			manager = new PersistanceManager();
		}
		return manager;
	}

	private PersistanceManager() {
		entityManagerFactory = Persistence
				.createEntityManagerFactory("bridgempp");
		entityManager = entityManagerFactory.createEntityManager();
	}

	public synchronized <T> T getFromPrimaryKey(Class<T> className,
			Object primaryKey) {
		return entityManager.find(className, primaryKey);
	}

	public synchronized <T> Collection<T> getQuery(Class<T> className) {
		Entity annotation = className.getAnnotation(Entity.class);
		if (annotation == null || annotation.name() == null
				|| annotation.name().length() == 0) {
			return null;
		}
		return entityManager.createQuery(
				"SELECT s FROM " + annotation.name() + " s", className)
				.getResultList();
	}

	public synchronized void updateState(Object... objects) {
		EntityTransaction saveTransaction = entityManager.getTransaction();
		try {
			saveTransaction.begin();
			for (Object object : objects) {

				if (entityManager.contains(object)) {
					entityManager.merge(object);
				} else {
					entityManager.persist(object);
				}

			}
			saveTransaction.commit();
		} catch (Exception e) {
			ShadowManager.log(Level.SEVERE, "Error while writing to database. Will attempt rollback.", e);
			transactionFailure();
			saveTransaction.rollback();
			ShadowManager.fatal("Error while writing to database. Successfully rolled back transaction", e);
		}
	}

	public synchronized void removeState(Object... objects) {
		executeRemoveState(objects);
	}

	public synchronized void executeRemoveState(Object... objects) {
		EntityTransaction transaction = entityManager.getTransaction();
		try {
			transaction.begin();
			for (Object object : objects) {
				entityManager.remove(object);
			}
			transaction.commit();
			transactionSuccess();
		} catch (Exception e) {
			ShadowManager.log(Level.SEVERE, "Error while deleting from database. Will attempt rollback.", e);
			transactionFailure();
			transaction.rollback();
			ShadowManager.fatal("Error while deleting from database. Successfully rolled back transaction", e);
		}
	}
	
	private int transactionFailures = 0;
	
	private void transactionFailure()
	{
		transactionFailures++;
		ShadowManager.log(Level.WARNING, "Transaction failure #" + transactionFailures);
		if(transactionFailures >= 3)
		{
			ShadowManager.log(Level.SEVERE, "Transaction failure reached critical level");
			BridgeMPP.exit();
			//This means we are already trying to shutdown but failing
			//Give up
			ShadowManager.log(Level.SEVERE, "Failed to terminate gracefully, giving up.");
			System.exit(0);
		}
	}
	
	private void transactionSuccess()
	{
		transactionFailures = 0;
	}
	

	public synchronized void shutdown() {
		entityManager.close();
		entityManagerFactory.close();
	}

	// Many convenience Methods

	public Collection<StatisticStore> getStatisticsStore() {
		return getQuery(StatisticStore.class);
	}

	public void saveStatisticsStore(StatisticStore statisticStore) {
		updateState(statisticStore);
	}

	public Collection<BridgeService> getServiceConfigurations() {
		return getQuery(BridgeService.class);
	}

	public User getUserForIdentifier(String identifier) {
		return getFromPrimaryKey(User.class, identifier);
	}

	public Endpoint getEndpointForIdentifier(String identifier) {
		return getFromPrimaryKey(Endpoint.class, identifier);
	}

	public Collection<Endpoint> getEndpoints() {
		return getQuery(Endpoint.class);
	}

	public Group getGroup(String name) {
		return getFromPrimaryKey(Group.class, name);
	}

	public Collection<Group> getGroups() {
		return getQuery(Group.class);
	}
}
