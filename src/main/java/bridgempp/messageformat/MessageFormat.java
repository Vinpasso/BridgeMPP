package bridgempp.messageformat;

public abstract class MessageFormat {
	public static MessageFormat PLAIN_TEXT = new PlainTextMessageFormat();
	public static MessageFormat HTML = new HTMLMessageFormat();
	
	private MessageFormat parentFormat;
	
	public abstract String getName();
	public abstract String convertToParent(String message);
	
	public final boolean canConvertToFormat(MessageFormat other)
	{
		if(other == null)
		{
			return false;
		}
		if(other.equals(this))
		{
			return true;
		}
		return parentFormat.canConvertToFormat(other);
	}
	
	public final String convertToFormat(String message, MessageFormat other)
	{
		if(!canConvertToFormat(other))
		{
			return null;
		}
		if(other.equals(this))
		{
			return message;
		}
		return parentFormat.convertToFormat(convertToParent(message), other);
	}
	
}
