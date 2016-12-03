package bridgempp.binarydistribution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

public class HTTPDistributionMethod implements DistributionMethod
{
	private Path localDirectory;
	private URL remoteDirectory;
	
	public HTTPDistributionMethod(Path localDirectory, URL remoteDirectory)
	{
		this.localDirectory = localDirectory;
		this.remoteDirectory = remoteDirectory;
	}

	/**
	 * @return the localDirectory
	 */
	public Path getLocalDirectory()
	{
		return localDirectory;
	}

	/**
	 * @return the remoteDirectory
	 */
	public URL getRemoteDirectory()
	{
		return remoteDirectory;
	}

	@Override
	public URL publish(String identifier, InputStream inputStream) throws IOException
	{
		File output = getLocalDirectory().resolve(identifier).toFile();
		FileOutputStream outputStream = new FileOutputStream(output);
		IOUtils.copy(inputStream, outputStream);
		outputStream.close();
		return new URL(getRemoteDirectory(), identifier);
	}
	
	

}
