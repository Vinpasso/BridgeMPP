package bridgempp.messageformat.text;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.HTMLToPlainText;

public class HTMLMessageFormat extends MessageFormat {

	public HTMLMessageFormat() {
		super();
		addConversion(PLAIN_TEXT, new HTMLToPlainText());
	}
	
	@Override
	public String getName() {
		return "HTML";
	}

}
