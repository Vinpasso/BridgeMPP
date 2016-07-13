package bridgempp.messageformat;

import java.util.Objects;
import java.util.function.Function;

public class Converter implements Comparable<Converter>
{
	private Function<String, String> function;
	private int numConversions = 0;

    public Converter(Function<String, String> function)
	{
    	this.function = function;
	}
        
    public String apply(String input)
    {
    	return function.apply(input);
    }

	/**
     * Daisy chain converters together
     * @param after
     * @return The composition of converters
     */
    public Converter andThen(Converter after) {
        Objects.requireNonNull(after);
        Converter newConverter = new Converter(t -> after.apply(function.apply(t)));
        newConverter.numConversions = numConversions + 1;
        return newConverter;
    }

	public static Converter identity() {
		return new Converter(t -> t);
	}

	/**
	 * @return the function
	 */
	Function<String, String> getFunction()
	{
		return function;
	}

	/**
	 * @return the numConversions
	 */
	int getNumConversions()
	{
		return numConversions;
	}

	@Override
	public int compareTo(Converter o)
	{
		if(o == null)
		{
			return -1;
		}
		return numConversions - o.numConversions;
	};
	
}
