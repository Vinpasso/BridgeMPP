package bridgempp.service;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import bridgempp.Message;
import bridgempp.data.Endpoint;

@Entity(name = "MultiBridgeServiceHandle")
public abstract class MultiBridgeServiceHandle<T extends SingleToMultiBridgeService>
{
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "MULTI_BRIDGE_SERVICE_IDENTIFIER", referencedColumnName = "SERVICE_IDENTIFIER")
	protected T service;

	@OneToOne(optional = false)
	protected Endpoint endpoint;
	
	
	public abstract void sendMessage(Message message);

	protected MultiBridgeServiceHandle(Endpoint endpoint, T service)
	{
		this.endpoint = endpoint;
		this.service = service;
		service.addHandle(this);
	}
	
}
