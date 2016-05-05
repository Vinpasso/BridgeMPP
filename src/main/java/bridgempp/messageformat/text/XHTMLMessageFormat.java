package bridgempp.messageformat.text;

import bridgempp.messageformat.Converter;
import bridgempp.messageformat.MessageFormat;

public class XHTMLMessageFormat extends MessageFormat {

	public XHTMLMessageFormat() {
		super();
		addConversion(HTML, Converter.identity());
	}
	
	@Override
	public String getName() {
		return "XHTML";
	}


}
