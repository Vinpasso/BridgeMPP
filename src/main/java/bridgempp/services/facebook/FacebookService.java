package bridgempp.services.facebook;

import java.security.InvalidParameterException;
import java.util.Hashtable;

import org.apache.commons.lang.NotImplementedException;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.types.FacebookType;
import com.restfb.types.Post;

import bridgempp.BridgeService;
import bridgempp.Endpoint;
import bridgempp.Message;
import bridgempp.command.CommandInterpreter;
import bridgempp.messageformat.MessageFormat;

public class FacebookService implements BridgeService {

	private FacebookClient facebook;
	private static final MessageFormat[] supportedMessageFormats = MessageFormat.PLAIN_TEXT_ONLY;
	private Hashtable<String, Endpoint> endpoints;
	
	@Override
	public void connect(String args) {
		//ARGS is Access Token
		endpoints = new Hashtable<>();
		facebook = new DefaultFacebookClient(args, Version.VERSION_2_3);
	}

	@Override
	public void disconnect() {
		facebook = null;
	}

	@Override
	public void sendMessage(Message message) {
		if(message.getMessageFormat().equals(MessageFormat.PLAIN_TEXT))
		{
			facebook.publish(message.getTarget().getTarget(), FacebookType.class, Parameter.with("message", message.getPlainTextMessage()));
		}
		else
		{
			throw new NotImplementedException();
		}
	}

	@Override
	public String getName() {
		return "Facebook";
	}

	@Override
	public boolean isPersistent() {
		return true;
	}

	@Override
	public void interpretCommand(Message message) {
		
	}

	@Override
	public void addEndpoint(Endpoint endpoint) {
		endpoints.put(endpoint.getTarget(), endpoint);
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats() {
		return supportedMessageFormats;
	}

	public FacebookClient getFacebook() {
		return facebook;
	}

	public void processPost(String place, Post post) {
		if(!endpoints.containsKey(place))
		{
			endpoints.put(place, new Endpoint(this, place));
		}
		Message message = new Message(endpoints.get(place), post.toString(), MessageFormat.PLAIN_TEXT);
		CommandInterpreter.processMessage(message);
	}

}
