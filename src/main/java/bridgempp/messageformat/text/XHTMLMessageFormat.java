package bridgempp.messageformat.text;

import bridgempp.messageformat.Converter;
import bridgempp.messageformat.MessageFormat;

public class XHTMLMessageFormat extends MessageFormat {

	public XHTMLMessageFormat() {
		super();
	}
	
	@Override
	public String getName() {
		return "XHTML";
	}

	@Override
	public void registerConversions() {
		addConversion(HTML, Converter.identity());	
	}


}
