package bridgempp.service.stack.handle;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import bridgempp.Message;
import bridgempp.data.Endpoint;

@Entity(name = "MultiBridgeServiceHandle")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "HANDLE_TYPE", discriminatorType=DiscriminatorType.STRING, length = 50)
public abstract class MultiBridgeServiceHandle
{
	@Id
	@Column(name = "HANDLE_IDENTIFIER", nullable = false, length = 255)
	protected String identifier;
	
	@ManyToOne(optional = false)
	protected ServiceHandleStackElement handleStackElement;

	@OneToOne(optional = false)
	protected Endpoint endpoint;
	
	
	public abstract void sendMessage(Message message);

	protected MultiBridgeServiceHandle(Endpoint endpoint, ServiceHandleStackElement handleStackElement)
	{
		this.endpoint = endpoint;
		this.identifier = endpoint.getIdentifier();
		this.handleStackElement = handleStackElement;
	}
	
	/**
	 * JPA Constructor
	 */
	protected MultiBridgeServiceHandle()
	{
	}
	
	protected void removeHandle()
	{
		handleStackElement.removeHandle(this);
	}
	
}
