package bridgempp.service;

public class PollToPushEntity<T>
{
	
	private T value;
	private long expireDate;
	
	public long getExpiryDate()
	{
		return expireDate;
	}
	
	public T getValue()
	{
		return value;
	}
	
}
