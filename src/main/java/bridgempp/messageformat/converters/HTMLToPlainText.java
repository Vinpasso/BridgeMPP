package bridgempp.messageformat.converters;

import org.apache.commons.lang.StringEscapeUtils;

import bridgempp.messageformat.Converter;

public class HTMLToPlainText extends Converter
{

	public HTMLToPlainText()
	{
		super(t -> StringEscapeUtils.unescapeHtml(t.replaceAll("<p\\/?>", "\n").replaceAll("<.+?>", "")));
	}

}
