package bridgempp.services.telegram;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.BotSession;

import bridgempp.Message;
import bridgempp.messageformat.MessageFormat;
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

	private static final MessageFormat[] supportedMessageFormats = new MessageFormat[] { MessageFormat.PLAIN_TEXT, MessageFormat.FILE_BACKED_IMAGE_FORMAT};
	
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
	public void sendMessage(Message message)
	{
		bot.sendMessage(message);
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

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return supportedMessageFormats ;
	}

}
