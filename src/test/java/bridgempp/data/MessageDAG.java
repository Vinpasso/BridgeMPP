package bridgempp.data;

public class MessageDAG<H>
{
	private MessageNode<H> head;
	
	public void insertHead(MessageNode<H> head)
	{
		this.head = head;
	}
	
	public void send(H item)
	{
		head.process(item);
	}
	
}
