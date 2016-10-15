package bridgempp.message;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.logging.Level;

import bridgempp.ShadowManager;
import bridgempp.messageformat.Converter;
import bridgempp.messageformat.MessageFormat;

public class MessageBodyRegister {
	
	public Map<Class<? extends MessageBody>, Map<Class<? extends MessageBody>, MessageBodyConverter<? extends MessageBody, ? extends MessageBody>>> conversions;

	@SuppressWarnings("unchecked")
	public final <I extends MessageBody, O extends MessageBody> O convert(I messageBody, Class<O> targetClass) {
		try {
			if (messageBody == null) {
				return null;
			}
			if(messageBody.getClass().equals(targetClass))
			{
				return (O) messageBody;
			}
			MessageBodyConverter<I, O> converter = (MessageBodyConverter<I, O>) conversions.get(messageBody.getClass()).get(targetClass);
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
	
}
