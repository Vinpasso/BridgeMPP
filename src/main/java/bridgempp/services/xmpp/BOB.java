package bridgempp.services.xmpp;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;


public class BOB implements IQProvider {

	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
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
		String hash;
		String data;

		public CharSequence getChildElementXML() {
			return "<data xmlns='urn:xmpp:bob' cid='" + hash + "' type='image/jpeg'>" + data + "</data>";
		}
	}
}