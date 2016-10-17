package bridgempp.message;

import java.util.Map;
import java.util.logging.Level;

import bridgempp.ShadowManager;

public class MessageBodyRegister {
	
	private static Map<Class<? extends MessageBody>, Map<Class<? extends MessageBody>, MessageBodyConverter<? extends MessageBody, ? extends MessageBody>>> conversions;

	@SuppressWarnings("unchecked")
	public static <I extends MessageBody, O extends MessageBody> O convert(I messageBody, Class<O> targetClass) {
		try {
			if (messageBody == null) {
				return null;
			}
			if(messageBody.getClass().equals(targetClass))
			{
				return (O) messageBody;
			}
			MessageBodyConverter<I, O> converter = (MessageBodyConverter<I, O>) conversions.get(messageBody.getClass()).get(targetClass);
			return converter.apply(messageBody);
		} catch (Exception e) {
			ShadowManager.log(Level.WARNING,
					"Failed to convert Message from Format: " + messageBody.getClass().getSimpleName()	+ "\nMessage: " + messageBody.toString(), e);
		}
		return null;
	}
	
	public static boolean canConvert(MessageBody messageBody, Class<? extends MessageBody> targetClass)
	{
		return conversions.get(messageBody.getClass()).containsKey(targetClass);
	}
	
}
