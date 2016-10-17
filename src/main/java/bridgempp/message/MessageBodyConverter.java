package bridgempp.message;

import java.util.function.Function;

//@FunctionalInterface
public class MessageBodyConverter<O extends MessageBody, D extends MessageBody>
{
	private int conversionCost;
	private Function<O, D> implementation;
	
	public MessageBodyConverter(int conversionCost, Function<O, D> implementation)
	{
		this.conversionCost = conversionCost;
		this.implementation = implementation;
	}
	
	public int getConversionCost()
	{
		return conversionCost;
	}

	public D apply(O t) throws Exception
	{
		return implementation.apply(t);
	}

	public <T extends MessageBody> MessageBodyConverter<O, T> andThen(MessageBodyConverter<D, T> v)
	{
		return new MessageBodyConverter<O, T>(getConversionCost() + v.getConversionCost(), implementation.andThen(v.implementation));
	}
	
	@SuppressWarnings("unchecked")
	public MessageBodyConverter<?, ?> sequence(MessageBodyConverter<?, ?> v)
	{
		return (MessageBodyConverter<?, ?>) andThen((MessageBodyConverter<D, ?>) v);
	}
}
