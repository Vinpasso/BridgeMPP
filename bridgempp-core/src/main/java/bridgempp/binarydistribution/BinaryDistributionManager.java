package bridgempp.binarydistribution;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class BinaryDistributionManager
{
	
	private static List<DistributionMethod> distributionMethods;
	
	
	public static DistributionMethod getDefaultPublicDistributionMethod()
	{
		return distributionMethods.stream().findFirst().orElse(null);
	}


	public static URL defaultPublish(String identifier, File file) throws IOException
	{
		return getDefaultPublicDistributionMethod().publish(identifier, file);
	}


	public static URL defaultPublish(String identifier, InputStream inputStream) throws IOException
	{
		return getDefaultPublicDistributionMethod().publish(identifier, inputStream);
	}
	
	
}
