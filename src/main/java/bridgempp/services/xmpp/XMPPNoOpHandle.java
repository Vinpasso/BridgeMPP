package bridgempp.services.xmpp;

import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.jivesoftware.smack.packet.Message;

import bridgempp.ShadowManager;
import bridgempp.data.Endpoint;

@Entity(name = "XMPPNOOPHANDLE")
@DiscriminatorValue("XMPPNoOpHandle")

public class XMPPNoOpHandle extends XMPPHandle
{
	/**
	 * JPA-ONLY
	 */
	public XMPPNoOpHandle()
	{
		super();
	}
	
	public XMPPNoOpHandle(Endpoint endpoint, XMPPService service)
	{
		super(endpoint, service);
	}

	@Override
	public void onLoad()
	{
		ShadowManager.log(Level.INFO, "Loaded NO-OP Handle: " + identifier);
	}

	@Override
	public void sendXMPPMessage(Message message)
	{
		ShadowManager.log(Level.INFO, "Rejected message to NO-OP Handle: " + message.toString());
	}

}
