package bridgempp.service.stack.duplex;

import bridgempp.service.stack.ServiceStackElement;

public abstract class SingleMethodDuplexStackElement<T> extends ServiceStackElement<T, T>
{
	private boolean applyUpper;
	private boolean applyLower;
	private boolean passThroughOnNotApplied;
	
	protected SingleMethodDuplexStackElement(boolean applyUpper, boolean applyLower, boolean passThroughOnNotApplied)
	{
		this.applyUpper = applyUpper;
		this.applyLower = applyLower;
		this.passThroughOnNotApplied = passThroughOnNotApplied;
	}
	
	@Override
	protected void messageAscending(T input)
	{
		if(applyUpper)
		{
			T result = processMessage(input);
			if(result != null)
			{
				sendToUpper(result);
			}
		}
		else if(passThroughOnNotApplied)
		{
			sendToUpper(input);
		}
	}

	@Override
	protected void messageDescending(T input)
	{
		if(applyLower)
		{
			T result = processMessage(input);
			if(result != null)
			{
				sendToLower(result);
			}
		}
		else if(passThroughOnNotApplied)
		{
			sendToLower(input);
		}
	}

	protected abstract T processMessage(T input);
}
