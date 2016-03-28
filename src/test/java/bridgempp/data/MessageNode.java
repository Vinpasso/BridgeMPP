package bridgempp.data;

public abstract class MessageNode<I>
{
	public void processInput(I input)
	{
		process(input);
	}
	
	protected abstract void process(I input);
}
