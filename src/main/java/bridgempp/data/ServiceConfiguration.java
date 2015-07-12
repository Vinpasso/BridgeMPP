package bridgempp.data;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

import bridgempp.BridgeService;

@Entity(name = "SERVICE_CONFIGURATION")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="SERVICE_TYPE", discriminatorType = DiscriminatorType.STRING, length=50)
public abstract class ServiceConfiguration
{
	@Id
	@Column(name = "IDENTIFIER", nullable = false, length = 50)
	private String serviceIdentifier;
	
	@OneToMany(mappedBy="serviceConfiguration")
	private Collection<Endpoint> endpoints;

	protected BridgeService service;
	
	public BridgeService getService()
	{
		return service;
	}
	
	public BridgeService createService()
	{
		if(service != null)
		{
			throw new IllegalStateException("Service Configuration was already assigned to a Service");
		}
		service = instantiateService();
		return service;
	}

	protected abstract BridgeService instantiateService();
	
}
