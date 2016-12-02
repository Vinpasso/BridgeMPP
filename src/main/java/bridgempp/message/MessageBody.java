package bridgempp.message;

public abstract class MessageBody
{

	public abstract String getFormatName();
	
	public String toString()
	{
		return getFormatName();
	}
	
}
