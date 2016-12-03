package bridgempp.services.xmpp;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;
import org.jivesoftware.smackx.xhtmlim.XHTMLText;

import bridgempp.data.Endpoint;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.formats.media.ImageMessageBody;
import bridgempp.message.formats.text.XHTMLXMPPMessageBody;
import bridgempp.service.MultiBridgeServiceHandle;

public abstract class XMPPHandle extends MultiBridgeServiceHandle<XMPPService, XMPPHandle>
{

	/**
	 * Create a handle with endpoint and service
	 * 
	 * @param endpoint
	 * @param service
	 */
	protected XMPPHandle(Endpoint endpoint, XMPPService service)
	{
		super(endpoint, service);
	}

	/**
	 * JPA-ONLY
	 */
	protected XMPPHandle()
	{
		super();
	}

	public void sendMessage(bridgempp.message.Message message, DeliveryGoal deliveryGoal)
	{
		Message sendMessage = new Message();
		String xhtmlMessageContents = null;
		if (message.hasMessageBody(XHTMLXMPPMessageBody.class))
		{
			xhtmlMessageContents = message.getMessageBody(XHTMLXMPPMessageBody.class).getText();
		}
		if (message.hasMessageBody(ImageMessageBody.class))
		{
			xhtmlMessageContents = service.cacheEmbeddedBase64Image(message.getMessageBody(ImageMessageBody.class));
		}
		if (xhtmlMessageContents != null)
		{
			XHTMLText xhtmlText = new XHTMLText(null, "en");
			xhtmlText.toXML().append(xhtmlMessageContents);
			xhtmlText.appendCloseBodyTag();
			XHTMLManager.addBody(sendMessage, xhtmlText);
		}

		sendMessage.addBody(null, message.getPlainTextMessageBody());
		sendXMPPMessage(sendMessage, deliveryGoal);
	}

	public abstract void onLoad();

	public abstract void sendXMPPMessage(Message message, DeliveryGoal deliveryGoal);

}
