package bridgempp.messageformat;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import bridgempp.messageformat.media.image.Base64ImageFormat;
import bridgempp.messageformat.media.image.FileBackedImageFormat;
import bridgempp.messageformat.media.image.StringEmbeddedImageFormat;
import bridgempp.messageformat.text.Base64PlainTextFormat;
import bridgempp.messageformat.text.HTMLMessageFormat;
import bridgempp.messageformat.text.PlainTextMessageFormat;
import bridgempp.messageformat.text.XHTMLMessageFormat;

import java.util.Optional;
import java.util.Queue;

public abstract class MessageFormat {
		
	public abstract String getName();
	private HashMap<MessageFormat, Converter> conversions;


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
		return conversions.containsKey(other.getClass());
	}
	
	public final Entry<MessageFormat, String> convertToClosestFormat(String message, MessageFormat... others)
	{
		Collection<MessageFormat> otherCollection = Arrays.asList(others);
		if(others == null || others.length == 0)
		{
			return new AbstractMap.SimpleEntry<MessageFormat, String>(this, message);
		}
		Optional<Entry<MessageFormat, Converter>> closest = conversions.entrySet().stream().filter(e -> otherCollection.contains(e.getKey())).sorted().findFirst();
		if(closest.isPresent())
		{
			return new AbstractMap.SimpleEntry<MessageFormat, String>(closest.get().getKey(),closest.get().getValue().apply(message));
		}
		return new AbstractMap.SimpleEntry<MessageFormat, String>(this, message);
	}
	
	public void addConversion(MessageFormat target, Converter converter)
	{
		buildConversionMap(converter, target);
	}
	
	private void buildConversionMap(Converter converter, MessageFormat target)
	{
		Queue<Entry<MessageFormat, Converter>> pendingConversionChains = new LinkedList<>();
		pendingConversionChains.add(new AbstractMap.SimpleEntry<MessageFormat, Converter>(target, converter));
		
		while(!pendingConversionChains.isEmpty())
		{
			Entry<MessageFormat, Converter> entry = pendingConversionChains.poll();
			if(conversions.containsKey(entry.getKey()) && conversions.get(entry.getKey()).getNumConversions() <= entry.getValue().getNumConversions())
			{
				continue;
			}
			conversions.put(target, converter);
			target.conversions.forEach((k,v) -> {
				pendingConversionChains.add(new AbstractMap.SimpleEntry<MessageFormat, Converter>(k, converter.andThen(v)));
			});
		}
	}
		
	public MessageFormat()
	{
		conversions = new HashMap<>();
	}
	
	/**
	 * @return the conversions
	 */
	HashMap<MessageFormat, Converter> getConversions()
	{
		return conversions;
	}
	
	//CONSTANTS
	
	public static final PlainTextMessageFormat PLAIN_TEXT = new PlainTextMessageFormat();
	public static final HTMLMessageFormat HTML = new HTMLMessageFormat();
	public static final XHTMLMessageFormat XHTML = new XHTMLMessageFormat();
	public static final MessageFormat BASE_64_PLAIN_TEXT = new Base64PlainTextFormat();
	public static final MessageFormat BASE_64_IMAGE_FORMAT = new Base64ImageFormat();
	public static final MessageFormat STRING_EMBEDDED_IMAGE_FORMAT = new StringEmbeddedImageFormat();
	public static final FileBackedImageFormat FILE_BACKED_IMAGE_FORMAT = new FileBackedImageFormat();
	public static final MessageFormat[] PLAIN_TEXT_ONLY = new MessageFormat[] {PLAIN_TEXT};


	public static MessageFormat parseMessageFormat(String messageFormat)
	{
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
