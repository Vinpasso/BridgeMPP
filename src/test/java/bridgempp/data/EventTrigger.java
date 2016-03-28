package bridgempp.data;

import java.util.List;
import java.util.function.Consumer;

public class EventTrigger<T>
{

	private List<Consumer<T>> listeners;
	
	public void fire(T parameter)
	{
		listeners.forEach(e -> { e.accept(parameter); });
	}
	
	public void removeListener(Consumer<T> listener)
	{
		listeners.remove(listener);
	}
	
	public void addListener(Consumer<T> listener)
	{
		listeners.add(listener);
	}
	
}
