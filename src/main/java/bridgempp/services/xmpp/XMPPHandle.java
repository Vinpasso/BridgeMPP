package bridgempp.services.xmpp;

import bridgempp.data.Endpoint;
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

	public abstract void onLoad();

}
