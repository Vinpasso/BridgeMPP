package bridgempp.message;

import bridgempp.data.Endpoint;

public class DeliveryGoal
{
	private Endpoint target;
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
}
