package bridgempp.storage;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import bridgempp.BridgeMPP;
import bridgempp.log.Log;

public class HibernatePersistanceManager extends PersistanceManager {

	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;

	public static void useHibernatePersistanceManager()
	{
		PersistanceManager.setPersistanceManager(new HibernatePersistanceManager());
	}
	
	private HibernatePersistanceManager() {
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
			Log.log(Level.SEVERE, "Error while writing to database. Will attempt rollback.", e);
			transactionFailure();
			saveTransaction.rollback();
			Log.log(Level.SEVERE, "Error while writing to database. Successfully rolled back transaction", e);
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
			Log.log(Level.SEVERE, "Error while deleting from database. Will attempt rollback.", e);
			transactionFailure();
			transaction.rollback();
			Log.log(Level.SEVERE, "Error while deleting from database. Successfully rolled back transaction", e);
		}
	}
	
	private int transactionFailures = 0;
	
	private void transactionFailure()
	{
		transactionFailures++;
		Log.log(Level.WARNING, "Transaction failure #" + transactionFailures);
		if(transactionFailures >= 1)
		{
			Log.log(Level.SEVERE, "Transaction failure reached critical level");
			BridgeMPP.exit();
			//This means we are already trying to shutdown but failing
			//Give up
			Log.log(Level.SEVERE, "Failed to terminate gracefully, giving up.");
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

	@Override
	public boolean hasState(Object... objects)
	{
		return Arrays.stream(objects).allMatch(o -> entityManager.contains(o));
	}
}
