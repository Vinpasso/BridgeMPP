package bridgempp.service.stack.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.Message;
import bridgempp.service.stack.common.AscendingStackElement;

public class AscendingRegexFindFilter extends AscendingStackElement
{

	private Pattern pattern;
	
	@Override
	protected void messageAscending(Message message)
	{
		Matcher matcher = pattern.matcher(message.getMessageRaw());
		while(matcher.find())
		{
			sendToUpper(new Message(message.getSender(), message.getOrigin(), matcher.group(), message.getMessageFormat()));
		}
	}
	
	public AscendingRegexFindFilter(String regex)
	{
		this.pattern = Pattern.compile(regex);
	}

}
