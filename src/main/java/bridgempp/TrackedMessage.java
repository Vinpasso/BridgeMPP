package bridgempp;

import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.message.Message;
import bridgempp.messageformat.MessageFormat;

public class TrackedMessage extends Message
{

	private long uniqueID;
	private long timestamp;
	
	public long getUniqueID()
	{
		return uniqueID;
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}
	
	public TrackedMessage(User sender, Endpoint origin, String message, MessageFormat messageFormat, long uniqueID)
	{
		super(sender, origin, message, messageFormat);
		this.uniqueID = uniqueID;
		this.timestamp = System.currentTimeMillis();
	}
	
}
