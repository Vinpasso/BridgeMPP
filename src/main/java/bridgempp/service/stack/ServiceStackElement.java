package bridgempp.service.stack;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity(name = "Service_Stack_Element")
public abstract class ServiceStackElement<H, L>
{
	@ManyToOne(optional = false)
	private ServiceStackLayer<H, L> layer;

	protected abstract void messageAscending(L input);
	
	protected abstract void messageDescending(H input);
	
	protected void sendToLower(L element)
	{
		layer.sendToLower(element);
	}
	
	protected void sendToUpper(H element)
	{
		layer.sendToUpper(element);
	}
	
	protected ServiceStackLayer<H, L> getLayer()
	{
		return layer;
	}
}
