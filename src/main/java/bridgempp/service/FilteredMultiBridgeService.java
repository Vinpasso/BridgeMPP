package bridgempp.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import bridgempp.Message;

public abstract class FilteredMultiBridgeService<S extends FilteredMultiBridgeService<S, H>, H extends MultiBridgeServiceHandle<S, H>> extends SingleToMultiBridgeService<S, H>
{
	private List<Predicate<Message>> filters = new ArrayList<>();
	
	public void receiveMessage(Message message)
	{
		Iterator<Predicate<Message>> iterator = filters.iterator();
		while(iterator.hasNext())
		{
			if(!iterator.next().test(message))
			{
				return;
			}
		}
		super.receiveMessage(message);
	}
	
	protected void addFilter(Predicate<Message> filter)
	{
		filters.add(filter);
	}
	
	protected void removeFilter(Predicate<Message> filter)
	{
		filters.remove(filter);
	}
	
}
