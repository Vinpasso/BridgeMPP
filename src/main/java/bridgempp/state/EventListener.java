package bridgempp.state;

import java.lang.reflect.ParameterizedType;

public abstract class EventListener<T> {
	
	public abstract void onEvent(T eventMessage);
	
	protected Class<?> getTypeOfEventMessage()
	{
		ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		return type.getActualTypeArguments()[0].getClass();
	}
}
