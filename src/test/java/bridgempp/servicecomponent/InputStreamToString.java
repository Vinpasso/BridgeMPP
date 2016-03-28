package bridgempp.servicecomponent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.data.MessageNodeIO;

public class InputStreamToString extends MessageNodeIO<Void, String>
{

	public InputStreamToString(InputStream inputStream, String threadName)
	{
		Thread thread = new Thread(() -> {
			ShadowManager.log(Level.INFO, "Process Input Reader: " + threadName + " starting up");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			try
			{
			while (bufferedReader.ready())
			{
				processResult(bufferedReader.readLine());
			}
			}
			catch (IOException e)
			{
				ShadowManager.log(Level.SEVERE, "Error while trying to read Process:" + threadName, e);
			}
			ShadowManager.log(Level.INFO, "Process Input Reader: " + threadName + " shutting down");
		});
		thread.setName(threadName);
		thread.start();
	}
}
