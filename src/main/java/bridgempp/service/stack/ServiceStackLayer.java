package bridgempp.service.stack;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import bridgempp.ShadowManager;

@Entity(name = "Service_Stack_Layer")
public class ServiceStackLayer<H, L>
{
	@OneToMany(mappedBy = "layer")
	private Collection<ServiceStackElement<H, L>> stackElements = new LinkedList<>();

	@ManyToOne(optional = false)
	private ServiceStack stack;
	
	private ServiceStackLayer<L, ?> lower;
	
	private ServiceStackLayer<?, H> upper;

	protected void messageDescending(H input)
	{
		Iterator<ServiceStackElement<H, L>> elementIterator = stackElements.iterator();
		while(elementIterator.hasNext())
		{
			try
			{
				elementIterator.next().messageDescending(input);
			}
			catch(Exception e)
			{
				ShadowManager.log(Level.WARNING, "Error occured while descending Stack", e);
			}
		}
	}
	
	protected void messageAscending(L input)
	{
		Iterator<ServiceStackElement<H, L>> elementIterator = stackElements.iterator();
		while(elementIterator.hasNext())
		{
			try
			{
				elementIterator.next().messageAscending(input);
			}
			catch(Exception e)
			{
				ShadowManager.log(Level.WARNING, "Error occured while ascending Stack", e);
			}
		}
	}
	
	protected void sendToLower(L element)
	{
		lower.messageDescending(element);
	}
	
	protected void sendToUpper(H element)
	{
		upper.messageAscending(element);
	}

	protected void updateStackReferences(ListIterator<ServiceStackLayer<?, ?>> iterator)
	{
		iterator.previous();
		if(iterator.hasPrevious())
		{
			upper = (ServiceStackLayer<?, H>) iterator.previous();
			iterator.next();
		}
		iterator.next();
		if(iterator.hasNext())
		{
			lower = (ServiceStackLayer<L, ?>) iterator.next();
			lower.updateStackReferences(iterator);
		}
	}

	public ServiceStack getStack()
	{
		return stack;
	}
	
}
