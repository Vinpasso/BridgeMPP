package bridgempp.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

import bridgempp.ShadowManager;

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
	
	public static String urlEncode(String toBeEncoded)
	{
		try
		{
			return URLEncoder.encode(toBeEncoded, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e)
		{
			ShadowManager.log(Level.SEVERE, "UTF-8 Charset not found");
		}
		return null;
	}
	
}
