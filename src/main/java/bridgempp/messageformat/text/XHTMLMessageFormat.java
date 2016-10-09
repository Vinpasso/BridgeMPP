package bridgempp.messageformat.text;

import bridgempp.messageformat.Converter;

public class XHTMLMessageFormat extends HTMLMessageFormat {

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
