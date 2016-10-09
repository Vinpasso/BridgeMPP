package bridgempp.services.facebook;

import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.types.FacebookType;

import bridgempp.ShadowManager;
import bridgempp.data.Endpoint;
import bridgempp.data.processing.Schedule;
import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;
import bridgempp.service.BridgeService;

@Entity(name = "FACEBOOK_SERVICE")
@DiscriminatorValue(value = "FACEBOOK_SERVICE")
public class FacebookService extends BridgeService
{

	transient private FacebookClient facebook;
	transient private SmartFacebookPollService pollService;
	@Column(name = "ACCESS_TOKEN", nullable = false, length = 50)
	private String accessToken;
	@Column(name = "APP_ID", nullable = false, length = 50)
	private String appID;
	@Column(name = "APP_SECRET", nullable = false, length = 50)
	private String appSecret;
	@Column(name = "LAST_UPDATE", nullable = false)
	private Date lastUpdate;

	@Override
	public void connect()
	{
		ShadowManager.log(Level.INFO, "Facebook Client starting up");
		facebook = new DefaultFacebookClient(accessToken, appSecret, Version.VERSION_2_3);
		pollService = new SmartFacebookPollService(this);
		Schedule.scheduleRepeatWithPeriod(pollService, 0, 15, TimeUnit.MINUTES);
		ShadowManager.log(Level.INFO, "Facebook Client ready");
	}

	@Override
	public void disconnect()
	{
		updateToken();
		facebook = null;
	}

	@Override
	public void sendMessage(Message message, DeliveryGoal deliveryGoal)
	{
		Endpoint destination = deliveryGoal.getTarget();
		facebook.publish(destination.getPartOneIdentifier(), FacebookType.class, Parameter.with("message", message.getPlainTextMessageBody()));
		deliveryGoal.setDelivered();
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

	public FacebookClient getFacebook()
	{
		return facebook;
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

	public Date getLastUpdate()
	{
		return lastUpdate;
	}

	public Collection<Endpoint> getEndpoints()
	{
		return endpoints;
	}

	public void setLastUpdate()
	{
		lastUpdate = Date.from(Instant.now());
	}

}
