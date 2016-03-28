package bridgempp.data;

import java.io.IOException;
import java.util.List;

public abstract class ExternalProcessService extends DAGService
{
	
	protected Process process;
	private List<String> command;

	public void connect() throws IOException
	{
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.redirectErrorStream(true);
		process = builder.start();
	}
	
	public void disconnect() throws IOException
	{
		process.getOutputStream().close();
		process.destroy();
	}
}
