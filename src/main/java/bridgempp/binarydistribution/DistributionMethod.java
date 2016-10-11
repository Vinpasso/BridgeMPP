package bridgempp.binarydistribution;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public interface DistributionMethod
{
	public URL publish(String identifier, InputStream inputStream) throws IOException;

	public default URL publish(String identifier, File file) throws IOException
	{
		return publish(identifier, new FileInputStream(file));
	}
}
