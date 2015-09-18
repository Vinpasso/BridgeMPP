package bridgempp.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.persistence.Entity;
import javax.persistence.OrderBy;

import bridgempp.data.DataManager;

@Entity(name = "PollToPushProcessor")
public class PollToPushProcessor<T>
{
	
	@OrderBy("expiryDate")
	private Collection<PollToPushEntity<T>> cachedMessages = new LinkedList<>();
	
	private BridgeService service;
	
	public boolean processMessage(T message)
	{
		processExpiredMessages();
		if(cachedMessages.contains())
		{
			return false;
		}
		cachedMessages.add(message);
		return true;
	}

	private void processExpiredMessages()
	{
		long currentTime = System.currentTimeMillis();
		Iterator<PollToPushEntity<T>> iterator = cachedMessages.iterator();
		while(iterator.hasNext())
		{
			PollToPushEntity<T> entity = iterator.next();
			if(entity.getExpiryDate() > currentTime)
			{
				break;
			}
			iterator.remove();
			DataManager.removeState(entity);
		}
	}
	
}
