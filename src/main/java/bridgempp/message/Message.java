package bridgempp.message;

import java.util.List;

import bridgempp.data.Endpoint;
import bridgempp.message.messagebody.MessageBody;

public class Message {	
	
	private Endpoint origin;
	private List<Endpoint> destinations;
	private MessageBody messageBody;
	
}
