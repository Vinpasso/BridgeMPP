package bridgempp.service;

import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import bridgempp.data.Endpoint;
import bridgempp.data.processing.Schedule;
import bridgempp.log.Log;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;

@Entity(name = "MULTIBRIDGESERVICEHANDLE")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "HANDLE_TYPE", discriminatorType=DiscriminatorType.STRING, length = 50)
public abstract class MultiBridgeServiceHandle<S extends SingleToMultiBridgeService<S, H>, H extends MultiBridgeServiceHandle<S, H>>
{
	@Id
	@Column(name = "HANDLE_IDENTIFIER", nullable = false, length = 255)
	protected String identifier;
	
	@ManyToOne(optional = false, targetEntity = SingleToMultiBridgeService.class)
	@JoinColumn(name = "MULTI_BRIDGE_SERVICE_IDENTIFIER", referencedColumnName = "SERVICE_IDENTIFIER")
	protected S service;

	@OneToOne(optional = false)
	protected Endpoint endpoint;
	
	
	public abstract void sendMessage(Message message, DeliveryGoal deliveryGoal);

	@SuppressWarnings("unchecked")
	protected MultiBridgeServiceHandle(Endpoint endpoint, S service)
	{
		this.endpoint = endpoint;
		this.identifier = endpoint.getIdentifier();
		this.service = service;
		service.addHandle((H) this);
	}
	
	/**
	 * JPA Constructor
	 */
	protected MultiBridgeServiceHandle()
	{
	}
	
	
	@SuppressWarnings("unchecked")
	protected void removeHandle()
	{
		service.removeHandle((H) this);
	}
	
	protected void scheduleRemoveHandle()
	{
		Schedule.schedule(() -> {
			Log.log(Level.INFO, "Removing handle previously scheduled for deletion: " + identifier);
			removeHandle();
			Log.log(Level.INFO, "Removed handle previously scheduled for deletion: " + identifier);
			});
	}
	
}
