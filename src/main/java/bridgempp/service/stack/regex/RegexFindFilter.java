package bridgempp.service.stack.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bridgempp.Message;
import bridgempp.service.stack.duplex.SingleMethodDuplexStackElement;
import bridgempp.service.stack.simplex.AscendingStackElement;

public class RegexFindFilter extends SingleMethodDuplexStackElement<String>
{

	private Pattern pattern;
	
	@Override
	protected String processMessage(String message)
	{
		Matcher matcher = pattern.matcher(message);
		while(matcher.find())
		{
			//TODO: Return all finds, not just the first one
			return matcher.group();
		}
		return null;
	}
	
	public RegexFindFilter(String regex, boolean applyUpper, boolean applyLower, boolean passThroughOnNotApplied)
	{
		super(applyUpper, applyLower, passThroughOnNotApplied);
		this.pattern = Pattern.compile(regex);
		
	}

}
