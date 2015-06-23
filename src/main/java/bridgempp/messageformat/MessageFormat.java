package bridgempp.messageformat;

public abstract class MessageFormat {
	public static MessageFormat PLAIN_TEXT = new PlainTextMessageFormat();
	public static MessageFormat HTML = new HTMLMessageFormat();
	public static MessageFormat XHTML = new XHTMLMessageFormat();
	public static MessageFormat[] PLAIN_TEXT_ONLY = new MessageFormat[] { PLAIN_TEXT };
	
	protected MessageFormat parentFormat;
	
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
		if(parentFormat == null)
		{
			return false;
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
	public static MessageFormat parseMessageFormat(String messageFormat) {
		if(PLAIN_TEXT.getName().equals(messageFormat))
		{
			return PLAIN_TEXT;
		}
		if(HTML.getName().equals(messageFormat))
		{
			return HTML;
		}
		if(XHTML.getName().equals(messageFormat))
		{
			return XHTML;
		}
		return null;
	}
	
}
