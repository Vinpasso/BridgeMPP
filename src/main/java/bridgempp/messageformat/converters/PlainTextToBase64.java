package bridgempp.messageformat.converters;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import bridgempp.messageformat.Converter;

public class PlainTextToBase64 extends Converter
{

	public PlainTextToBase64()
	{
		super(t -> Base64.getEncoder().encodeToString(t.getBytes(StandardCharsets.UTF_8)));
	}

}
