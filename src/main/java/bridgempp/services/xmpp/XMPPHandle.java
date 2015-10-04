package bridgempp.services.xmpp;

import bridgempp.data.Endpoint;
import bridgempp.service.stack.handle.MultiBridgeServiceHandle;
import bridgempp.service.stack.handle.ServiceHandleStackElement;

public abstract class XMPPHandle extends MultiBridgeServiceHandle
{

	protected XMPPHandle(Endpoint endpoint, ServiceHandleStackElement stackElement)
	{
		super(endpoint, stackElement);
	}
	
	protected XMPPHandle()
	{
		super();
	}

	public abstract void onLoad();

}
