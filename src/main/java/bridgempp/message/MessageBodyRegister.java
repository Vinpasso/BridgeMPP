package bridgempp.message;

import java.io.IOException;
import java.net.URLConnection;
import java.util.AbstractMap;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;

import bridgempp.ShadowManager;
import bridgempp.message.formats.media.ImageMessageBody;
import bridgempp.message.formats.text.Base64EncodedMessageBody;
import bridgempp.message.formats.text.HTMLMessageBody;
import bridgempp.message.formats.text.PlainTextMessageBody;
import bridgempp.message.formats.text.XHTMLMessageBody;
import bridgempp.message.formats.text.XHTMLXMPPMessageBody;

public class MessageBodyRegister
{

	private static Map<Class<? extends MessageBody>, Map<Class<? extends MessageBody>, MessageBodyConverter<? extends MessageBody, ? extends MessageBody>>> conversions;

	@SuppressWarnings("unchecked")
	public static <I extends MessageBody, O extends MessageBody> O convert(I messageBody, Class<O> targetClass)
	{
		try
		{
			if (messageBody == null)
			{
				return null;
			}
			if (messageBody.getClass().equals(targetClass))
			{
				return (O) messageBody;
			}
			MessageBodyConverter<I, O> converter = (MessageBodyConverter<I, O>) conversions.get(messageBody.getClass()).get(targetClass);
			return converter.apply(messageBody);
		} catch (Exception e)
		{
			ShadowManager.log(Level.WARNING, "Failed to convert Message from Format: " + messageBody.getClass().getSimpleName() + "\nMessage: " + messageBody.toString(), e);
		}
		return null;
	}

	public static boolean canConvert(MessageBody messageBody, Class<? extends MessageBody> targetClass)
	{
		return conversions.get(messageBody.getClass()).containsKey(targetClass);
	}

	private static void buildConversionMap()
	{
		ShadowManager.log(Level.INFO, "Building conversion map for all message Formats");
		conversions.forEach((inputClass, convertedMap) -> {
			Queue<Entry<Class<? extends MessageBody>, MessageBodyConverter<?, ?>>> pendingConversionChains = new LinkedList<>();
			pendingConversionChains.addAll(convertedMap.entrySet());
			convertedMap.clear();

			while (!pendingConversionChains.isEmpty())
			{
				Entry<Class<? extends MessageBody>, MessageBodyConverter<?, ?>> entry = pendingConversionChains.poll();
				if (convertedMap.containsKey(entry.getKey()) && convertedMap.get(entry.getKey()).getConversionCost() <= entry.getValue().getConversionCost())
				{
					return;
				}
				convertedMap.put(entry.getKey(), entry.getValue());
				conversions.get(entry.getKey()).forEach((k, v) -> {
					if (convertedMap.containsKey(k) && convertedMap.get(k).getConversionCost() <= v.getConversionCost() + entry.getValue().getConversionCost())
					{
						return;
					}
					if (k.equals(inputClass))
					{
						return;
					}
					if (pendingConversionChains.parallelStream().anyMatch(e -> e.getKey().equals(k)))
					{
						return;
					}
					pendingConversionChains.add(new AbstractMap.SimpleEntry<Class<? extends MessageBody>, MessageBodyConverter<?, ?>>(k, entry.getValue().sequence(v)));
				});
			}

			conversions.forEach((origin, map1) -> {
				final StringBuilder builder = new StringBuilder();
				map1.forEach((e, v) -> builder.append(e.getName() + " (cost: " + v.getConversionCost() + ")\n"));
				ShadowManager.log(Level.INFO, "The conversion map for " + origin.getSimpleName() + " has been built:\n" + builder.toString());
			});
		});

	}
	
	public static <O extends MessageBody, D extends MessageBody> void registerConversion(Class<O> origin, Class<D> destination, int cost, Function<O, D> implementation)
	{
		Map<Class<? extends MessageBody>, MessageBodyConverter<? extends MessageBody, ? extends MessageBody>> map = conversions.get(origin);
		if(map == null)
		{
			map = new HashMap<>();
		}
		map.put(destination, new MessageBodyConverter<>(cost, implementation));
	}
	
	private static final int DEFAULT_COST = 100;
	static
	{
		conversions = new HashMap<>();
		//Default cost 10
		//Base64
		registerConversion(PlainTextMessageBody.class, Base64EncodedMessageBody.class, DEFAULT_COST, t ->	new Base64EncodedMessageBody(t.getText()));
		registerConversion(Base64EncodedMessageBody.class, PlainTextMessageBody.class, DEFAULT_COST, t -> new PlainTextMessageBody(t.getDecodedText()));
		
		//HTML
		registerConversion(HTMLMessageBody.class, PlainTextMessageBody.class, DEFAULT_COST, t -> new PlainTextMessageBody(t.formatToPlainText()));
		
		//XHTML
		registerConversion(HTMLMessageBody.class, XHTMLMessageBody.class, DEFAULT_COST, t -> new XHTMLMessageBody(t.getText()));
		
		//XHTML-IM
		registerConversion(XHTMLMessageBody.class, XHTMLXMPPMessageBody.class, DEFAULT_COST, t -> new XHTMLXMPPMessageBody(t.getText()));
		
		//Image
		registerConversion(ImageMessageBody.class, PlainTextMessageBody.class, DEFAULT_COST, t -> new PlainTextMessageBody(t.getCaption() + ": " + t.getURL().toString()));
//		registerConversion(ImageMessageBody.class, XHTMLXMPPMessageBody.class, DEFAULT_COST * 2, t -> {
//			try
//			{
//				return new XHTMLXMPPMessageBody("<img src=\"data:" + t.getMimeType().toString() + ";base64," + Base64.getEncoder().encodeToString(IOUtils.toByteArray(t.getURL())) + "\"/>");
//			} catch (IOException e)
//			{
//				return new XHTMLXMPPMessageBody("<img src=\""+ t.getURL().toString() + "\"/>");
//			}
//		});
		
		buildConversionMap();
	}

}
