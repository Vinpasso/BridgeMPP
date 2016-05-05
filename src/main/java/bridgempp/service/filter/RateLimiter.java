package bridgempp.service.filter;

import java.util.function.Function;
import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.ShadowManager;

public class RateLimiter implements Function<Message, Message>
{
	private long lastMessage = 0;
	private long messageDelay = 5000l;

	@Override
	public Message apply(Message t)
	{
		if(System.currentTimeMillis() < lastMessage + messageDelay)
		{
			try
			{
				Thread.sleep((lastMessage + messageDelay) - System.currentTimeMillis());
			} catch (InterruptedException e)
			{
				ShadowManager.log(Level.WARNING, "Deliverung message instantly due to interrupt");
			}
		}
		lastMessage = System.currentTimeMillis();
		return t;
	}
	
	public RateLimiter()
	{
		
	}
	
	public RateLimiter(long messageDelay)
	{
		this.messageDelay = messageDelay;
	}

	
	/**
	 * @return the messageDelay
	 */
	public long getMessageDelay()
	{
		return messageDelay;
	}

	/**
	 * @param messageDelay the messageDelay to set
	 */
	public void setMessageDelay(long messageDelay)
	{
		this.messageDelay = messageDelay;
	}
}
