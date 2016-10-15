package bridgempp.message;

import java.util.function.Function;

@FunctionalInterface
public interface MessageBodyConverter<O extends MessageBody, D extends MessageBody> extends Function<O, D>
{
}
