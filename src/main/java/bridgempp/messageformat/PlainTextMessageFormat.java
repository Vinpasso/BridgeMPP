package bridgempp.messageformat;

public class PlainTextMessageFormat extends MessageFormat {

	@Override
	public String getName() {
		return "Plain Text";
	}

	@Override
	public String convertToParent(String message) {
		return message;
	}

}
