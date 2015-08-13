package bridgempp.state;

import bridgempp.data.Endpoint;

public interface EndpointStateListener {

	public void created(Endpoint endpoint);
	public void connected(Endpoint endpoint);
	public void disconnected(Endpoint endpoint);
	public void removed(Endpoint endpoint);
	
}
