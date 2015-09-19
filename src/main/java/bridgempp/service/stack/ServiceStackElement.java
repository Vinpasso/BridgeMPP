package bridgempp.service.stack;

import java.util.logging.Level;

import bridgempp.Message;
import bridgempp.ShadowManager;

public abstract class ServiceStackElement
{
	private ServiceStackElement above;
	private ServiceStackElement below;

	protected abstract void messageAscending(Message message);
	
	protected abstract void messageDescending(Message message);
	
	protected void sendToLower(Message message)
	{
		if(below == null)
		{
			ShadowManager.log(Level.WARNING, "Sent message below the Stack bottom");
			return;
		}
		below.messageDescending(message);
	}
	
	protected void sendToUpper(Message message)
	{
		if(above == null)
		{
			ShadowManager.log(Level.WARNING, "Sent message over the Stack top");
			return;
		}
		above.messageAscending(message);
	}
	
	protected void setAbove(ServiceStackElement above)
	{
		this.above = above;
	}
	
	protected void setBelow(ServiceStackElement below)
	{
		this.below = below;
	}
}
