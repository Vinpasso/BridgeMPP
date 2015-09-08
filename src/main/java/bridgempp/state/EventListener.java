package bridgempp.state;

public abstract class EventListener<T> {
	
	public abstract void onEvent(T eventMessage);
	
}
