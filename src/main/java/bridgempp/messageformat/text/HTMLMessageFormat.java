package bridgempp.messageformat.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import bridgempp.messageformat.converters.HTMLToPlainText;

/**
 * 
 * Message Format:
 * 
 * <head> ... </head>
 * <body> ... </body>
 * 
 * @author
 *
 */
public class HTMLMessageFormat extends PlainTextMessageFormat
{

	public HTMLMessageFormat()
	{
		super();
	}

	@Override
	public String getName()
	{
		return "HTML";
	}

	@Override
	public void registerConversions()
	{
		addConversion(PLAIN_TEXT, new HTMLToPlainText());
	}

	private static Pattern openBodyTagFinder = Pattern.compile("(<body[^>]*(?<!\\/)>)");

	@Override
	public String encodeMetaInformationTag(String metaInformation, String message)
	{
		Matcher matcher = openBodyTagFinder.matcher(message);
		if (!matcher.find())
		{
			return message;
		}
		return metaInformation.substring(0, matcher.end()) + StringEscapeUtils.escapeHtml(metaInformation) + metaInformation.substring(matcher.start());
	}

}
