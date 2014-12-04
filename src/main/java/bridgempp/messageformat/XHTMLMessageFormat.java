package bridgempp.messageformat;


public class XHTMLMessageFormat extends MessageFormat {

	public XHTMLMessageFormat() {
		parentFormat = MessageFormat.HTML;
	}
	
	@Override
	public String getName() {
		return "XHTML";
	}

	@Override
	public String convertToParent(String message) {
		return message;
	}

}
