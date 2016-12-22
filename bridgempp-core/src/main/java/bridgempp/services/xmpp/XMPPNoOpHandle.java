package bridgempp.services.xmpp;

import java.util.logging.Level;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.jivesoftware.smack.packet.Message;

import bridgempp.data.Endpoint;
import bridgempp.log.Log;
import bridgempp.message.DeliveryGoal;

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
		Log.log(Level.INFO, "Loaded NO-OP Handle: " + identifier);
	}

	@Override
	public void sendXMPPMessage(Message message, DeliveryGoal deliveryGoal)
	{
		Log.log(Level.FINE, "Rejected message to NO-OP Handle: " + message.toString());
		deliveryGoal.setDelivered();
	}

}
