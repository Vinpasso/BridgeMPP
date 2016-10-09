package bridgempp.services.xmpp;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;
import org.jivesoftware.smackx.xhtmlim.XHTMLText;

import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.MultiBridgeServiceHandle;

public abstract class XMPPHandle extends MultiBridgeServiceHandle<XMPPService, XMPPHandle>
{

	/**
	 * Create a handle with endpoint and service
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
	
	public void sendMessage(bridgempp.Message message)
	{
		Message sendMessage = new Message();
		if (message.chooseMessageFormat(XMPPService.supportedMessageFormats).equals(MessageFormat.XHTML))
		{
			String messageContents = message.toSimpleString(XMPPService.supportedMessageFormats);
			messageContents = service.cacheEmbeddedBase64Image(messageContents);
			
			XHTMLText xhtmlText = new XHTMLText("", "en");
			xhtmlText.toXML().append(messageContents);
			XHTMLManager.addBody(sendMessage, xhtmlText);
			
		}
		sendMessage.addBody(null, message.toSimpleString(MessageFormat.PLAIN_TEXT));
		sendXMPPMessage(sendMessage);
	}

	public abstract void onLoad();
	public abstract void sendXMPPMessage(Message message);

}
