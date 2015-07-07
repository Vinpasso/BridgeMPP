package bridgempp.services.facebook;

import java.security.InvalidParameterException;
import java.util.Hashtable;
import java.util.logging.Level;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.NotImplementedException;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.types.FacebookType;
import com.restfb.types.Post;

import bridgempp.BridgeService;
import bridgempp.ConfigurationManager;
import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.command.CommandInterpreter;
import bridgempp.data.Endpoint;
import bridgempp.messageformat.MessageFormat;

public class FacebookService implements BridgeService {

	private FacebookClient facebook;
	private static final MessageFormat[] supportedMessageFormats = MessageFormat.PLAIN_TEXT_ONLY;
	private Hashtable<String, Endpoint> endpoints;
	private FacebookPollService pollService;
	private String appSecret;
	private String appID;
	private String accessToken;
	
	@Override
	public void connect(String args) {
		String[] parameters = args.split("; ");
		if(parameters.length != 3)
		{
			throw new InvalidParameterException("Unexpected args (Should be Access Token; App ID; App Secret): " + parameters.toString());
		}
		accessToken = parameters[0];
		appID = parameters[1];
		appSecret = parameters[2];
		ShadowManager.log(Level.INFO, "Facebook Client starting up");
		endpoints = new Hashtable<>();
		facebook = new DefaultFacebookClient(accessToken, appSecret, Version.VERSION_2_3);
		pollService = new FacebookPollService(this);
		Thread thread = new Thread(pollService);
		thread.setName("Facebook Poll Service");
		thread.start();
		ShadowManager.log(Level.INFO, "Facebook Client ready");
	}

	@Override
	public void disconnect() {
		updateToken();
		facebook = null;
	}

	@Override
	public void sendMessage(Message message) {
		if(message.getMessageFormat().canConvertToFormat(MessageFormat.PLAIN_TEXT))
		{
			facebook.publish(message.getTarget().getTarget(), FacebookType.class, Parameter.with("message", message.getPlainTextMessage()));
		}
		else
		{
			throw new NotImplementedException("Can not currently Post non Plain Text-able Messages");
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
		pollService.addConnection(endpoint.getTarget(), endpoint.getExtra());
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
		endpoints.get(place).setExtra(post.getId());
		String postString = convertMessageToString(post);
		Message message = new Message(endpoints.get(place), postString, MessageFormat.PLAIN_TEXT);
		CommandInterpreter.processMessage(message);
	}

	private String convertMessageToString(Post post) {
		return post.toString();
	}

	public String getAccessToken() {
		return accessToken;
	}
	
	protected String getAppID()
	{
		return appID;
	}
	
	protected String getAppSecret()
	{
		return appSecret;
	}
	
	private void updateToken()
	{
		ShadowManager.log(Level.INFO, "Obtaining new Access Token for old Access Token: " + getAccessToken());
		String newToken = exchangeToken();
		ConfigurationManager.serviceConfiguration.setProperty(ConfigurationManager.getServiceConfigurationIdentifier("facebookservice") + ".options", newToken + "; " + getAppID() + "; " + getAppSecret());
		try {
			ConfigurationManager.serviceConfiguration.save();
		} catch (ConfigurationException e) {
			ShadowManager.log(Level.SEVERE, "Failed to save new Configuration with updated Facebook Token", e);
		}
		ShadowManager.log(Level.INFO, "Got new Access Token: " + newToken);
	}

	private String exchangeToken() {
		AccessToken newToken = facebook.obtainExtendedAccessToken(getAppID(), getAppSecret(), getAccessToken());
		return newToken.getAccessToken();
	}

}
