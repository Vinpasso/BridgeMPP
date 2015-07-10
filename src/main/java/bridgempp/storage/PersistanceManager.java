package bridgempp.storage;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import bridgempp.data.Group;

public class PersistanceManager
{
	private static PersistanceManager manager;
	
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
	
	public static PersistanceManager getPersistanceManager()
	{
		if(manager == null)
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
		//TODO
		return null;
	}
	
	public void saveGroups(Collection<Group> groups)
	{
		//TODO
		entityManager.flush();
	}
	
	public void shutdown()
	{
		entityManager.close();
		entityManagerFactory.close();
	}
}
