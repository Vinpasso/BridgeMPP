package bridgempp.messageformat;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.logging.Level;

import bridgempp.ShadowManager;
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

	private static HashMap<String, MessageFormat> formats;

	public abstract String getName();

	public abstract void registerConversions();

	private HashMap<MessageFormat, Converter> conversions;

	public final boolean canConvertToFormat(MessageFormat other) {
		if (other == null) {
			return false;
		}
		if (other.equals(this)) {
			return true;
		}
		return conversions.containsKey(other.getClass());
	}

	public final Entry<MessageFormat, String> convertToClosestFormat(
			String message, MessageFormat... others) {
		try {
			if (message == null || message == "") {
				return new AbstractMap.SimpleEntry<MessageFormat, String>(this,
						message);
			}
			Collection<MessageFormat> otherCollection = Arrays.asList(others);
			if (others == null || others.length == 0) {
				return new AbstractMap.SimpleEntry<MessageFormat, String>(this,
						message);
			}
			Optional<Entry<MessageFormat, Converter>> closest = conversions
					.entrySet().stream()
					.filter(e -> otherCollection.contains(e.getKey())).sorted()
					.findFirst();
			if (closest.isPresent()) {
				return new AbstractMap.SimpleEntry<MessageFormat, String>(
						closest.get().getKey(), closest.get().getValue()
								.apply(message));
			}
		} catch (Exception e) {
			ShadowManager.log(Level.WARNING,
					"Failed to convert Message from Format: " + this.getName()
							+ "\nMessage: " + message, e);
		}
		return new AbstractMap.SimpleEntry<MessageFormat, String>(this, message);
	}

	public void addConversion(MessageFormat target, Converter converter) {
		conversions.put(target, converter);
	}

	private void buildConversionMap() {
		ShadowManager.log(Level.INFO, "Building conversion map for "
				+ getName());
		Queue<Entry<MessageFormat, Converter>> pendingConversionChains = new LinkedList<>();
		pendingConversionChains.addAll(conversions.entrySet());
		conversions.clear();

		while (!pendingConversionChains.isEmpty()) {
			Entry<MessageFormat, Converter> entry = pendingConversionChains
					.poll();
			if (conversions.containsKey(entry.getKey())
					&& conversions.get(entry.getKey()).getNumConversions() <= entry
							.getValue().getNumConversions()) {
				return;
			}
			conversions.put(entry.getKey(), entry.getValue());
			entry.getKey().conversions
					.forEach((k, v) -> {
						if (conversions.containsKey(k)
								&& conversions.get(k).getNumConversions() <= v
										.getNumConversions() + 1) {
							return;
						}
						if (k.equals(this)) {
							return;
						}
						if(pendingConversionChains.parallelStream().anyMatch(e -> e.getKey().equals(k)))
						{
							return;
						}
						pendingConversionChains
								.add(new AbstractMap.SimpleEntry<MessageFormat, Converter>(
										k, entry.getValue().andThen(v)));
					});
		}
		
		
		
		final StringBuilder builder = new StringBuilder();
		conversions.forEach((e, v) -> builder.append(e.getName() + " ("
				+ v.getNumConversions() + " hops)\n"));
		ShadowManager.log(Level.INFO, "The conversion map for " + getName()
				+ " has been built:\n" + builder.toString());
	}
	
	public MessageFormat() {
		conversions = new HashMap<>();
	}

	/**
	 * @return the conversions
	 */
	HashMap<MessageFormat, Converter> getConversions() {
		return conversions;
	}

	// CONSTANTS

	static {
		formats = new HashMap<>();

		PlainTextMessageFormat plainTextMessageFormat = new PlainTextMessageFormat();
		formats.put(plainTextMessageFormat.getName(), plainTextMessageFormat);
		PLAIN_TEXT = plainTextMessageFormat;
		PLAIN_TEXT_ONLY = new MessageFormat[] { plainTextMessageFormat };

		HTMLMessageFormat htmlMessageFormat = new HTMLMessageFormat();
		formats.put(htmlMessageFormat.getName(), htmlMessageFormat);
		HTML = htmlMessageFormat;

		XHTMLMessageFormat xhtmlMessageFormat = new XHTMLMessageFormat();
		formats.put(xhtmlMessageFormat.getName(), xhtmlMessageFormat);
		XHTML = xhtmlMessageFormat;

		Base64PlainTextFormat base64PlainTextFormat = new Base64PlainTextFormat();
		formats.put(base64PlainTextFormat.getName(), base64PlainTextFormat);
		BASE_64_PLAIN_TEXT = base64PlainTextFormat;

		Base64ImageFormat base64ImageFormat = new Base64ImageFormat();
		formats.put(base64ImageFormat.getName(), base64ImageFormat);
		BASE_64_IMAGE_FORMAT = base64ImageFormat;

		StringEmbeddedImageFormat stringEmbeddedImageFormat = new StringEmbeddedImageFormat();
		formats.put(stringEmbeddedImageFormat.getName(),
				stringEmbeddedImageFormat);
		STRING_EMBEDDED_IMAGE_FORMAT = stringEmbeddedImageFormat;

		FileBackedImageFormat fileBackedImageFormat = new FileBackedImageFormat();
		formats.put(fileBackedImageFormat.getName(), fileBackedImageFormat);
		FILE_BACKED_IMAGE_FORMAT = fileBackedImageFormat;

		formats.forEach((e, v) -> v.registerConversions());
		formats.forEach((e, v) -> v.buildConversionMap());
	}

	public static PlainTextMessageFormat PLAIN_TEXT;
	public static HTMLMessageFormat HTML;
	public static XHTMLMessageFormat XHTML;
	public static Base64PlainTextFormat BASE_64_PLAIN_TEXT;
	public static Base64ImageFormat BASE_64_IMAGE_FORMAT;
	public static StringEmbeddedImageFormat STRING_EMBEDDED_IMAGE_FORMAT;
	public static FileBackedImageFormat FILE_BACKED_IMAGE_FORMAT;

	public static MessageFormat[] PLAIN_TEXT_ONLY;

	public static MessageFormat parseMessageFormat(String messageFormat) {
		if (PLAIN_TEXT.getName().equals(messageFormat)) {
			return PLAIN_TEXT;
		}
		if (HTML.getName().equals(messageFormat)) {
			return HTML;
		}
		if (XHTML.getName().equals(messageFormat)) {
			return XHTML;
		}
		return null;
	}
	
	@Override
	public boolean equals(Object other)
	{
		if(other == null || !(other instanceof MessageFormat))
		{
			return false;
		}
		return ((MessageFormat)other).getName().equals(this.getName());
	}
}
