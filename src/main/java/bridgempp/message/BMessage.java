package bridgempp.message;

import java.util.HashMap;

public class BMessage {
	
	private HashMap<Class<? extends MessageProperty>, MessageProperty> properties;
	
	public <T extends MessageProperty> T getProperty(Class<T> property)
	{
		return (T) properties.get(property);
	}
	
	public MIMEProperty getType()
	{
		return getProperty(MIMEProperty.class);
	}
	
	public StorageProperty getStorageLocation()
	{
		return getProperty(StorageProperty.class);
	}
	
	public 
}
