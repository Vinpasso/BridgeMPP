package bridgempp.services.xmpp;

import java.io.IOException;
import org.jivesoftware.smack.iqrequest.IQRequestHandler;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


public class BOB extends IQProvider<BOB.BOBIQ> implements IQRequestHandler {

	private XMPPService service;
	
	public BOB(XMPPService service)
	{
		this.service = service;
	}
	
	@Override
	public BOBIQ parse(XmlPullParser parser, int intialDepth) throws XmlPullParserException, IOException {
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

	@Override
	public IQ handleIQRequest(IQ iqRequest) {
			if(iqRequest instanceof BOBIQ)
			{
				BOB.BOBIQ iq = (BOB.BOBIQ) iqRequest;
				String hash = iq.hash;
				String data = service.cachedObjects.get(hash);
				BOB.BOBIQ reply = new BOB.BOBIQ();
				reply.hash = hash;
				reply.data = data;
				reply.setType(IQ.Type.result);
				reply.setTo(iq.getFrom());
				reply.setStanzaId(iq.getStanzaId());
				reply.setFrom(iq.getTo());
				return reply;
			}
			return IQ.createErrorResponse(iqRequest, XMPPError.from(Condition.feature_not_implemented, "Expected BOB Request"));
	}

	@Override
	public Mode getMode() {
		return Mode.async;
	}

	@Override
	public Type getType() {
		return Type.get;
	}

	@Override
	public String getElement() {
		return "data";
	}

	@Override
	public String getNamespace() {
		return "urn:xmpp:bob";
	}
}