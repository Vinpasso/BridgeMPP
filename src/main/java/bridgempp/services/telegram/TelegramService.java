package bridgempp.services.telegram;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.BotSession;

import bridgempp.message.DeliveryGoal;
import bridgempp.message.Message;
import bridgempp.service.BridgeService;
import bridgempp.service.ServiceFilter;
import bridgempp.service.filter.RateLimiter;

@Entity(name = "TELEGRAM_SERVICE_CONFIGURATION")
@DiscriminatorValue("TELEGRAM_SERVICE")
@ServiceFilter(RateLimiter.class)
public class TelegramService extends BridgeService
{
	@Column(name = "TOKEN", nullable = false, length = 255)
	private String token;
	
	private transient TelegramBotsApi api;
	private transient TelegramBot bot;
	private transient BotSession session;


	@Override
	public void connect() throws Exception
	{
		api = new TelegramBotsApi();
		bot = new TelegramBot(this);
		session = api.registerBot(bot);
	}

	@Override
	public void disconnect() throws Exception
	{
		session.close();
		api = null;
		bot = null;
	}

	@Override
	public void sendMessage(Message message, DeliveryGoal deliveryGoal)
	{
		bot.sendMessage(message, deliveryGoal);
	}

	@Override
	public String getName()
	{
		return "Telegram";
	}

	@Override
	public boolean isPersistent()
	{
		return true;
	}

	public String getToken()
	{
		return token;
	}

	public void configure(String token)
	{
		this.token = token;
	}

}
