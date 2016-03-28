package bridgempp.servicecomponent;

import java.io.OutputStream;
import java.io.PrintStream;

import bridgempp.data.MessageNode;

public class StringToPrintStream extends MessageNode<String>
{

	private PrintStream printStream;

	@Override
	protected void process(String input)
	{
		printStream.println(input);
	}

	public StringToPrintStream(OutputStream outputstream)
	{
		super();
		this.printStream = new PrintStream(outputstream, true);
	}

}
