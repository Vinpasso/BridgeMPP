package bridgempp.services.socket;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import bridgempp.BridgeService;
import bridgempp.data.ServiceConfiguration;

@Entity(name = "SOCKET_SERVICE_CONFIGURATION")
@DiscriminatorValue("SOCKET_SERVICE")
public class SocketServiceConfiguration extends ServiceConfiguration
{
	@Column(name = "Listen_Address", nullable = false, length = 50)
	private String listenAddress;
	
	@Column(name = "List_Port", nullable = false)
	private int listenPort;

	@Override
	protected BridgeService instantiateService()
	{
		SocketService service = new SocketService();
		service.configure(listenAddress, listenPort);
		this.service = service;
		return service;
	}

}
