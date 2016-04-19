package bridgempp.services.telegram;

import java.util.logging.Level;

import org.telegram.telegrambots.TelegramApiException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import bridgempp.Message;
import bridgempp.ShadowManager;
import bridgempp.data.DataManager;
import bridgempp.data.Endpoint;
import bridgempp.data.User;
import bridgempp.messageformat.MessageFormat;

public class TelegramBot extends TelegramLongPollingBot
{

	private TelegramService service;

	public TelegramBot(TelegramService service)
	{
		super();
		this.service = service;
	}

	@Override
	public String getBotUsername()
	{
		return "BridgeMPP";
	}

	@Override
	public void onUpdateReceived(Update update)
	{
		org.telegram.telegrambots.api.objects.Message message = update.getMessage();
		if (message != null && message.hasText())
		{
			handleIncomingMessage(message);
		}
	}

	private void handleIncomingMessage(org.telegram.telegrambots.api.objects.Message message)
	{
		Endpoint endpoint = DataManager.getOrNewEndpointForIdentifier(message.getChatId() + "@telegram-groups", service);
		User user = DataManager.getOrNewUserForIdentifier(message.getFrom().getId() + "@telegram-users", endpoint);
		if(!user.hasAlias())
		{
			String username = "";
			username += (message.getFrom().getFirstName()==null)?"":message.getFrom().getFirstName();
			username += (message.getFrom().getLastName()==null)?"":message.getFrom().getLastName();
			username = (username.length()==0)?message.getFrom().getUserName():username;
			user.setName(username);
			ShadowManager.log(Level.INFO, "Automatically extracted Alias from Telegram");
		}
		Message bridgeMessage = new Message(user, endpoint, message.getText(), MessageFormat.PLAIN_TEXT);
		service.receiveMessage(bridgeMessage);
	}
	
	public void sendMessage(Message bridgeMessage)
	{
		SendMessage message = new SendMessage();
		message.setChatId(bridgeMessage.getDestination().getPartOneIdentifier());
		message.setText(bridgeMessage.toSimpleString(service.getSupportedMessageFormats()));
		try
		{
			sendMessage(message);
		} catch (TelegramApiException e)
		{
			ShadowManager.log(Level.SEVERE, "Telegram Api Error", e);
		}
	}

	@Override
	public String getBotToken()
	{
		return service.getToken();
	}

}
