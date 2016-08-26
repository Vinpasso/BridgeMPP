package bridgempp.messageformat.media.image;

import bridgempp.messageformat.MessageFormat;
import bridgempp.messageformat.converters.EmbeddedToHTTPBacked;

public class HTTPBackedImageFormat extends MessageFormat {

	@Override
	public String getName() {
		return "HTTP Backed Image Format";
	}

	@Override
	public void registerConversions() {
		new EmbeddedToHTTPBacked();
	}

}
