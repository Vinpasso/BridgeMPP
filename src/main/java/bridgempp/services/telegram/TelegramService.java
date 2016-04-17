package bridgempp.services.telegram;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import org.telegram.telegrambots.TelegramBotsApi;

import bridgempp.Message;
import bridgempp.messageformat.MessageFormat;
import bridgempp.service.BridgeService;

@Entity(name = "TELEGRAM_SERVICE_CONFIGURATION")
@DiscriminatorValue("TELEGRAM_SERVICE")
public class TelegramService extends BridgeService
{
	@Column(name = "TOKEN", nullable = false, length = 255)
	private String token;
	
	private transient TelegramBotsApi api;
	private transient TelegramBot bot;
	
	@Override
	public void connect() throws Exception
	{
		api = new TelegramBotsApi();
		bot = new TelegramBot();
		api.registerBot(bot);
	}

	@Override
	public void disconnect() throws Exception
	{
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

	@Override
	public MessageFormat[] getSupportedMessageFormats()
	{
		return MessageFormat.PLAIN_TEXT_ONLY;
	}

	public String getToken()
	{
		return token;
	}

}
