package bridgempp.data;

import java.util.List;

public abstract class MessageNodeIO<I, O> extends MessageNode<I>
{
	private List<MessageNode<O>> outputs;
	
	public void processInput(I input)
	{
		process(input);
		
		O result = processIO(input);
		
		processResult(result);
	}

	protected void processResult(O result)
	{
		if(result == null)
		{
			return;
		}
		
		outputs.forEach(e -> e.processInput(result));
	}

	public void addOutput(MessageNode<O> node)
	{
		outputs.add(node);
	}
	
	public void removeOutput(MessageNode<O> node)
	{
		outputs.remove(node);
	}
	
	protected O processIO(I input)
	{
		return null;
	}
	
	protected void process(I input)
	{
		
	}
}
