package bridgempp.message;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import bridgempp.data.Endpoint;

@Entity(name = "DeliveryGoal")
public class DeliveryGoal
{
	@Id()
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "Identifier", nullable = false)
	private int id;
	
	@Column(name = "Target", nullable = false)
	private Endpoint target;

	@Column(name = "Message", nullable = false)
	private Message message;
	
	@Column(name = "DeliveryStatus", nullable = false)
	private DeliveryStatus status;

	
	public DeliveryGoal(Endpoint endpoint)
	{
		target = endpoint;
		status = DeliveryStatus.PENDING;
	}
	
	
	public String toString()
	{
		return target.toString() + " (" + status.toString() + ")"; 
	}


	/**
	 * @return the status
	 */
	public DeliveryStatus getStatus()
	{
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(DeliveryStatus status)
	{
		this.status = status;
	}


	/**
	 * @return the target
	 */
	public Endpoint getTarget()
	{
		return target;
	}


	public void setDelivered()
	{
		setStatus(DeliveryStatus.DELIVERED);
	}
}
