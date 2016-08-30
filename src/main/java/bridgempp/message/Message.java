package bridgempp.message;

import java.util.HashMap;

public class Message {
	
	private HashMap<MessageFormat, MessageInstance> formats;
	private MessageInstance original;
	
	public MessageInstance getMessage(MessageFormat format)
	{
		if(formats.containsKey(format))
		{
			return formats.get(format);
		}
		
		MessageInstance converted = ConversionEngine.convert(formats, format);
		if(converted != null)
		{
			formats.put(format, converted);
		}
		return converted;
	}
}
