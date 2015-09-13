package bridgempp.services.xmpp;

import bridgempp.data.Endpoint;
import bridgempp.service.MultiBridgeServiceHandle;

public abstract class XMPPHandle extends MultiBridgeServiceHandle<XMPPService, XMPPHandle>
{

	protected XMPPHandle(Endpoint endpoint, XMPPService service)
	{
		super(endpoint, service);
	}

}
