package bridgempp.messageformat.text;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.HTMLToPlainText;

public class HTMLMessageFormat extends MessageFormat {

	public HTMLMessageFormat() {
		super();
	}
	
	@Override
	public String getName() {
		return "HTML";
	}

	@Override
	public void registerConversions() {
		addConversion(PLAIN_TEXT, new HTMLToPlainText());	
	}

}
