package bridgempp.util;

public class StringOperations
{

	public static String getPartOneIdentifier(String identifier)
	{
		if(!identifier.contains("@"))
		{
			return identifier;
		}
		return identifier.substring(0, identifier.indexOf("@"));
	}
	
}
