package bridgempp.services.xmpp;

import java.io.IOException;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


public class BOB extends IQProvider<BOB.BOBIQ> {

	@Override
	public BOBIQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException {
		BOB.BOBIQ bOBIQ = new BOBIQ();
		boolean done = false;
		while (!done) {
			if ("data".equals(parser.getName())) {
				bOBIQ.hash = parser.getAttributeValue("", "cid");
				done = true;
			}
			int eventType = parser.next();

			if (eventType == XmlPullParser.START_TAG && "data".equals(parser.getName())) {
				// Initialize the variables from the parsed XML
				bOBIQ.hash = parser.getAttributeValue("", "cid");
			} else if (eventType == XmlPullParser.END_TAG && "data".equals(parser.getName())) {
				done = true;
			}
		}
		return bOBIQ;
	}

	public static class BOBIQ extends IQ {
		protected BOBIQ()
		{
			super("data", "urn:xmpp:bob");
		}

		String hash;
		String data;
		
		@Override
		protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml)
		{
			xml.attribute("cid", hash).attribute("type", "image/png").rightAngleBracket().append(data);
			return xml;
		}
	}
}