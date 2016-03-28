package bridgempp.services.facebook;

import java.util.Iterator;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.apache.commons.lang.NotImplementedException;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.types.FacebookType;
import com.restfb.types.Post;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

@Entity(name = "FACEBOOK_SERVICE")
@DiscriminatorValue(value = "FACEBOOK_SERVICE")
public class FacebookService extends BridgeService
{

	transient private FacebookClient facebook;
	transient private static final MessageFormat[] supportedMessageFormats = MessageFormat.PLAIN_TEXT_ONLY;
	transient private FacebookPollService pollService;
	@Column(name = "ACCESS_TOKEN", nullable = false, length = 50)
	private String accessToken;
	@Column(name = "APP_ID", nullable = false, length = 50)
	private String appID;
	@Column(name = "APP_SECRET", nullable = false, length = 50)
	private String appSecret;

	@Override
	public void connect()
	{
		ShadowManager.log(Level.INFO, "Facebook Client starting up");
		facebook = new DefaultFacebookClient(accessToken, appSecret, Version.VERSION_2_3);
		pollService = new FacebookPollService(this);
		Thread thread = new Thread(pollService);
		thread.setName("Facebook Poll Service");
		thread.start();
		Iterator<Endpoint> iterator = endpoints.iterator();
		while (iterator.hasNext())
		{
			addEndpoint(iterator.next());
		}
		ShadowManager.log(Level.INFO, "Facebook Client ready");
	}

	@Override
	public void disconnect()
	{
		updateToken();
		facebook = null;
	}

	@Override
	public void sendMessage(Message message)
	{
		if (message.getMessageFormat().canConvertToFormat(MessageFormat.PLAIN_TEXT))
		{
			facebook.publish(message.getDestination().getIdentifier(), FacebookType.class, Parameter.with("message", message.getPlainTextMessage()));
		} else
		{
			throw new NotImplementedException("Can not currently Post non Plain Text-able Messages");
		}
	}

	@Override
	public String getName()
	{
		return "Facebook";
	}

	@Override
	public boolean isPersistent()
	{
		return true;
	}

	public void addEndpoint(Endpoint endpoint)
	{
		// TODO: UGH
		pollService.addConnection(endpoint.getIdentifier(), endpoint.getUsers().iterator().next().getIdentifier());
	}

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return supportedMessageFormats;
	}

	public FacebookClient getFacebook()
	{
		return facebook;
	}

	public void processPost(String place, Post post)
	{
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(place, this);
		// TODO: POST: GET ID AS TRACKER?
		User user = DataManager.getOrNewUserForIdentifier(post.getId(), endpoint);
		String postString = convertMessageToString(post);
		Message message = new Message(user, endpoint, postString, MessageFormat.PLAIN_TEXT);
		receiveMessage(message);
	}

	private String convertMessageToString(Post post)
	{
		return post.toString();
	}

	public String getAccessToken()
	{
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
		accessToken = exchangeToken();

		ShadowManager.log(Level.INFO, "Got new Access Token: " + getAccessToken());
	}

	private String exchangeToken()
	{
		AccessToken newToken = facebook.obtainExtendedAccessToken(getAppID(), getAppSecret(), getAccessToken());
		return newToken.getAccessToken();
	}

	public void configure(String appID, String appSecret, String accessToken)
	{
		this.appID = appID;
		this.appSecret = appSecret;
		this.accessToken = accessToken;
	}

}
